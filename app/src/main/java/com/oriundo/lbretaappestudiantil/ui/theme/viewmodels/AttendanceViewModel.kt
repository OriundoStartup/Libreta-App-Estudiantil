package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
// ✅ IMPORTACIÓN CORREGIDA: Apuntando al paquete de dominio correcto.
import com.oriundo.lbretaappestudiantil.domain.model.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AttendanceUiState {
    object Initial : AttendanceUiState()
    object Loading : AttendanceUiState()
    data class Success(val attendance: AttendanceEntity) : AttendanceUiState()
    data class Error(val message: String) : AttendanceUiState()
}

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _recordState = MutableStateFlow<AttendanceUiState>(AttendanceUiState.Initial)
    val recordState: StateFlow<AttendanceUiState> = _recordState.asStateFlow()

    private val _attendanceByStudent = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendanceByStudent: StateFlow<List<AttendanceEntity>> = _attendanceByStudent.asStateFlow()

    private val _attendanceByDateRange = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendanceByDateRange: StateFlow<List<AttendanceEntity>> = _attendanceByDateRange.asStateFlow()

    private val _attendanceStats = MutableStateFlow<AttendanceStats>(AttendanceStats())
    val attendanceStats: StateFlow<AttendanceStats> = _attendanceStats.asStateFlow()

    fun recordAttendance(
        studentId: Int,
        teacherId: Int,
        date: Long,
        status: AttendanceStatus,
        note: String? = null
    ) {
        viewModelScope.launch {
            _recordState.value = AttendanceUiState.Loading

            // ✅ CORREGIDO: Llamando al repositorio con los parámetros individuales
            // que espera la Interfaz, no pasando una Entidad que rompía la firma.
            val result = attendanceRepository.recordAttendance(
                studentId = studentId,
                teacherId = teacherId,
                date = date,
                status = status,
                note = note
            )

            // ✅ CORREGIDO: Manejando ApiResult.Success<AttendanceEntity>
            _recordState.value = when (result) {
                is ApiResult.Success -> AttendanceUiState.Success(result.data)
                is ApiResult.Error -> AttendanceUiState.Error(result.message)
                ApiResult.Loading -> AttendanceUiState.Loading
            }
        }
    }

    fun loadAttendanceByStudent(studentId: Int) {
        viewModelScope.launch {
            attendanceRepository.getAttendanceByStudent(studentId).collect { attendance ->
                _attendanceByStudent.value = attendance
                calculateStats(attendance)
            }
        }
    }

    fun loadAttendanceByDateRange(
        studentId: Int,
        startDate: Long,
        endDate: Long
    ) {
        viewModelScope.launch {
            attendanceRepository.getAttendanceByDateRange(
                studentId = studentId,
                startDate = startDate,
                endDate = endDate
            ).collect { attendance ->
                _attendanceByDateRange.value = attendance
                calculateStats(attendance)
            }
        }
    }

    fun updateAttendance(attendance: AttendanceEntity) {
        viewModelScope.launch {
            attendanceRepository.updateAttendance(attendance)
        }
    }

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

        val percentage = (present.toFloat() / total.toFloat() * 100).toInt()

        _attendanceStats.value = AttendanceStats(
            totalDays = total,
            presentDays = present,
            absentDays = absent,
            lateDays = late,
            justifiedDays = justified,
            attendancePercentage = percentage
        )
    }

    fun resetRecordState() {
        _recordState.value = AttendanceUiState.Initial
    }
}

data class AttendanceStats(
    val totalDays: Int = 0,
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val lateDays: Int = 0,
    val justifiedDays: Int = 0,
    val attendancePercentage: Int = 0
)