package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels


import android.net.Uri

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.JustificationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException



class JustifyAbsenceViewModel(
    private val repository: JustificationRepository,
    private val studentId: Int, // Recibido desde la pantalla
    private val parentId: Int // Recibido desde la pantalla
) : ViewModel() {

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

    fun onDescriptionChanged(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun onFileSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedFileUri = uri)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(submissionError = null)
    }

    // Lógica de validación
    private fun isFormValid(): Boolean {
        return _uiState.value.selectedDate != null &&
                _uiState.value.description.isNotBlank()
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

        viewModelScope.launch {
            try {
                // Obtener los datos del estado
                val state = _uiState.value

                repository.submitJustification(
                    studentId = studentId,
                    parentId = parentId,
                    dateMillis = state.selectedDate!!,
                    reason = state.selectedReason,
                    description = state.description,
                    attachmentUri = state.selectedFileUri
                )

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