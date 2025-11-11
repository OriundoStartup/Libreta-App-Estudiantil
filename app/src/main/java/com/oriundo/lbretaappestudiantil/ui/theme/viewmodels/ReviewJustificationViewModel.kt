package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.ReviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException


/**
 * ViewModel para la pantalla ReviewJustificationScreen (Profesor).
 */
class ReviewJustificationViewModel(
    private val repository: JustificationRepository,
    private val justificationId: Int, // Recibido desde la pantalla
    private val teacherId: Int // Recibido desde la pantalla
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadJustificationDetails()
    }

    /**
     * Carga los detalles de la justificación al iniciar el ViewModel.
     */
    fun loadJustificationDetails() {
        _uiState.value = _uiState.value.copy(isLoading = true, reviewError = null)
        viewModelScope.launch {
            try {
                val details = repository.getJustificationDetails(justificationId)
                _uiState.value = _uiState.value.copy(isLoading = false, justification = details)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reviewError = "Error al cargar detalles: ${e.message}"
                )
            }
        }
    }

    fun onReviewNotesChanged(newNotes: String) {
        _uiState.value = _uiState.value.copy(reviewNotes = newNotes)
    }

    /**
     * Llama al repositorio para aprobar o rechazar la justificación.
     */
    fun submitReview(newStatus: JustificationStatus) {
        if (_uiState.value.isSubmitting) return

        if (newStatus == JustificationStatus.REJECTED && _uiState.value.reviewNotes.isBlank()) {
            _uiState.value = _uiState.value.copy(reviewError = "Debes añadir notas para rechazar")
            return
        }

        _uiState.value = _uiState.value.copy(isSubmitting = true, reviewError = null, reviewSuccess = false)

        viewModelScope.launch {
            try {
                val state = _uiState.value
                repository.updateJustificationStatus(
                    justificationId = justificationId,
                    teacherId = teacherId,
                    newStatus = newStatus,
                    reviewNotes = state.reviewNotes
                )
                _uiState.value = _uiState.value.copy(isSubmitting = false, reviewSuccess = true)

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    reviewError = "Error al ${if (newStatus == JustificationStatus.APPROVED) "aprobar" else "rechazar"}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(reviewError = null)
    }
}