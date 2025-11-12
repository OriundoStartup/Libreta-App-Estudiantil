package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.PendingJustificationsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



// Nota: Asumiendo que usas Hilt para la inyección de dependencias
@HiltViewModel
class PendingJustificationsViewModel @Inject constructor(
    private val repository: JustificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingJustificationsUiState())
    val uiState: StateFlow<PendingJustificationsUiState> = _uiState.asStateFlow()

    // package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

// ... (resto del código)

    fun loadPendingJustifications(teacherId: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // TODO: En una aplicación real, aquí llamarías a una función
                // como repository.getPendingJustifications(teacherId)

                // --- SIMULACIÓN DE DATOS CORREGIDA ---
                val dummyData = listOf(
                    AbsenceJustificationEntity(
                        id = 101, studentId = 1, parentId = 2,
                        absenceDate = System.currentTimeMillis() - 86400000,
                        // ✅ CORREGIDO: Usando ILLNESS
                        reason = com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason.ILLNESS,
                        description = "Fiebre y dolor de garganta.",
                    ),
                    AbsenceJustificationEntity(
                        id = 102, studentId = 3, parentId = 4,
                        absenceDate = System.currentTimeMillis() - 86400000 * 2,
                        // ✅ CORREGIDO: Usando FAMILY_EMERGENCY
                        reason = com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason.FAMILY_EMERGENCY,
                        description = "Asistencia a boda familiar fuera de la ciudad.",
                    )
                )
                kotlinx.coroutines.delay(1000) // Simular carga
                _uiState.value = _uiState.value.copy(
                    justifications = dummyData,
                    isLoading = false
                )
                // --- FIN SIMULACIÓN ---

            } catch (e: Exception) {
                // ... (manejo de error)
            }
        }
    }

}