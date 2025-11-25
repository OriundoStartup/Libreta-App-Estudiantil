package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.data.local.repositories.AttendanceRepositoryImpl
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.ui.theme.states.AttendanceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepositoryImpl
) : ViewModel() {

    private val _recordState = MutableStateFlow<AttendanceUiState>(AttendanceUiState.Initial)
    val recordState: StateFlow<AttendanceUiState> = _recordState.asStateFlow()

    private val _attendanceByStudent = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendanceByStudent: StateFlow<List<AttendanceEntity>> = _attendanceByStudent.asStateFlow()

    private val _attendanceByDateRange = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendanceByDateRange: StateFlow<List<AttendanceEntity>> = _attendanceByDateRange.asStateFlow()

    private val _attendanceStats = MutableStateFlow(AttendanceStats())
    val attendanceStats: StateFlow<AttendanceStats> = _attendanceStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    /**
     * Registra la asistencia de un estudiante
     */
    fun recordAttendance(
        studentId: Int,
        teacherId: Int,
        date: Long,
        status: AttendanceStatus,
        note: String? = null
    ) {
        viewModelScope.launch {
            _recordState.value = AttendanceUiState.Loading

            val result = attendanceRepository.recordAttendance(
                studentId = studentId,
                teacherId = teacherId,
                date = date,
                status = status,
                note = note
            )

            _recordState.value = when (result) {
                is ApiResult.Success -> {
                    // Recargar datos después de registrar
                    loadAttendanceByStudent(studentId)
                    AttendanceUiState.Success(result.data)
                }
                is ApiResult.Error -> AttendanceUiState.Error(result.message)
                ApiResult.Loading -> AttendanceUiState.Loading
            }
        }
    }

    /**
     * Carga la asistencia de un estudiante específico
     */
    fun loadAttendanceByStudent(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                attendanceRepository.getAttendanceByStudent(studentId).collect { attendance ->
                    _attendanceByStudent.value = attendance
                    calculateStats(attendance)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                println("❌ Error cargando asistencia: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga asistencia por rango de fechas
     */
    fun loadAttendanceByDateRange(
        studentId: Int,
        startDate: Long,
        endDate: Long
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                attendanceRepository.getAttendanceByDateRange(
                    studentId = studentId,
                    startDate = startDate,
                    endDate = endDate
                ).collect { attendance ->
                    _attendanceByDateRange.value = attendance
                    calculateStats(attendance)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                println("❌ Error cargando asistencia por rango: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un registro de asistencia existente
     */
    fun updateAttendance(attendance: AttendanceEntity) {
        viewModelScope.launch {
            _recordState.value = AttendanceUiState.Loading

            val result = attendanceRepository.updateAttendance(attendance)

            _recordState.value = when (result) {
                is ApiResult.Success -> {
                    // Recargar datos
                    loadAttendanceByStudent(attendance.studentId)
                    AttendanceUiState.Success(attendance)
                }
                is ApiResult.Error -> AttendanceUiState.Error(result.message)
                ApiResult.Loading -> AttendanceUiState.Loading
            }
        }
    }

    /**
     * Sincroniza registros pendientes con Firebase
     */
    fun syncPendingAttendance() {
        viewModelScope.launch {
            _isSyncing.value = true

            try {
                val result = attendanceRepository.syncPendingAttendance()

                when (result) {
                    is ApiResult.Success -> {
                        println("✅ Sincronización completada exitosamente")
                    }
                    is ApiResult.Error -> {
                        println("❌ Error en sincronización: ${result.message}")
                    }
                    ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                println("❌ Error sincronizando: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Fuerza la sincronización desde Firebase
     */
    fun forceSync(studentId: Int) {
        viewModelScope.launch {
            _isSyncing.value = true

            try {
                attendanceRepository.syncAttendanceFromFirestore(studentId)
                loadAttendanceByStudent(studentId)
            } catch (e: Exception) {
                println("❌ Error en sincronización forzada: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Calcula estadísticas de asistencia
     */
    private fun calculateStats(attendanceList: List<AttendanceEntity>) {
        val total = attendanceList.size
        if (total == 0) {
            _attendanceStats.value = AttendanceStats()
            return
        }

        val present = attendanceList.count { it.status == AttendanceStatus.PRESENT }
        val absent = attendanceList.count { it.status == AttendanceStatus.ABSENT }
        val late = attendanceList.count { it.status == AttendanceStatus.LATE }
        val justified = attendanceList.count { it.status == AttendanceStatus.JUSTIFIED }

        // Calcular porcentaje considerando presente + justificado como asistencia válida
        val validAttendance = present + justified
        val percentage = if (total > 0) {
            ((validAttendance.toFloat() / total.toFloat()) * 100).toInt()
        } else {
            0
        }

        _attendanceStats.value = AttendanceStats(
            totalDays = total,
            presentDays = present,
            absentDays = absent,
            lateDays = late,
            justifiedDays = justified,
            attendancePercentage = percentage
        )
    }

    /**
     * Resetea el estado de registro
     */
    fun resetRecordState() {
        _recordState.value = AttendanceUiState.Initial
    }

    /**
     * Limpia los datos cargados
     */
    fun clearData() {
        _attendanceByStudent.value = emptyList()
        _attendanceByDateRange.value = emptyList()
        _attendanceStats.value = AttendanceStats()
    }
}

/**
 * Data class para las estadísticas de asistencia
 */
data class AttendanceStats(
    val totalDays: Int = 0,
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val lateDays: Int = 0,
    val justifiedDays: Int = 0,
    val attendancePercentage: Int = 0
)