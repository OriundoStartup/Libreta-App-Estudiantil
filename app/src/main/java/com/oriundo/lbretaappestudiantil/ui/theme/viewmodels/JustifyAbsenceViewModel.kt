package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels


import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.JustificationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
class JustifyAbsenceViewModel @Inject constructor(
    private val repository: JustificationRepository,
    // ✅ Inyectar SavedStateHandle para obtener argumentos de navegación
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ✅ OBTENER ARGUMENTOS DE NAVEGACIÓN DESDE SavedStateHandle
    private val studentId: Int = savedStateHandle.get<Int>("studentId") ?: throw IllegalArgumentException("studentId es requerido para JustifyAbsenceViewModel")
    private val parentId: Int = savedStateHandle.get<Int>("parentId") ?: throw IllegalArgumentException("parentId es requerido para JustifyAbsenceViewModel")

    // Estado principal que se observa en la pantalla
    private val _uiState = MutableStateFlow(JustificationUiState())
    val uiState: StateFlow<JustificationUiState> = _uiState.asStateFlow()

    // Manejadores de eventos de la UI
    fun onDateSelected(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(selectedDate = dateMillis)
    }

    fun onReasonSelected(reason: AbsenceReason) {
        _uiState.value = _uiState.value.copy(selectedReason = reason)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onFileSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedFileUri = uri)
    }

    private fun isFormValid(): Boolean {
        // Usar la razón seleccionada del estado para la validación
        return _uiState.value.selectedDate != null && _uiState.value.description.isNotBlank()
    }

    /**
     * Envía la justificación a través del Repositorio.
     */
    fun submitJustification() {
        if (!isFormValid() || _uiState.value.isSubmitting) return

        _uiState.value = _uiState.value.copy(
            isSubmitting = true,
            submissionError = null,
            submissionSuccess = false
        )

        // Fragmento de JustifyAbsenceViewModel.kt
        viewModelScope.launch {
            try {
                val state = _uiState.value

                // ✅ Uso de IDs reales extraídos de SavedStateHandle (no hardcodeados)
                repository.submitJustification(
                    studentId = studentId,
                    parentId = parentId,
                    dateMillis = state.selectedDate!!, // Estado de la UI
                    reason = state.selectedReason,     // Estado de la UI
                    description = state.description,   // Estado de la UI
                    attachmentUri = state.selectedFileUri // Estado de la UI
                )
                // ...

                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submissionSuccess = true
                )

            } catch (e: CancellationException) {
                // Se ignora la cancelación de corrutina
                throw e
            } catch (e: Exception) {
                // Manejo de errores generales (red, servidor, etc.)
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submissionError = "Error al enviar: ${e.message ?: "Desconocido"}"
                )
            }
        }
    }
}