package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.ReviewUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import javax.inject.Inject


/**
 * ViewModel para la pantalla ReviewJustificationScreen (Profesor).
 * AHORA HABILITADO PARA HILT
 */
@HiltViewModel
class ReviewJustificationViewModel @Inject constructor(
    private val repository: JustificationRepository,
    savedStateHandle: SavedStateHandle // Inyectado por Hilt
) : ViewModel() {

    // Obtener IDs desde los argumentos de navegación
    // Asumimos que las claves son "justificationId" y "teacherId"
    private val justificationId: Int = checkNotNull(savedStateHandle["justificationId"])
    private val teacherId: Int = checkNotNull(savedStateHandle["teacherId"])

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
                // El justificationId y teacherId ya son miembros de la clase
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    reviewError = "Error al ${if (newStatus == JustificationStatus.APPROVED) "aprobar" else "rechazar"}: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(reviewError = null)
    }
}