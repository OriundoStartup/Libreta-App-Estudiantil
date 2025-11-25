package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.LocalDatabaseRepository
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI para la pantalla de justificaciones pendientes
 */
data class PendingJustificationsUiState(
    val justifications: List<AbsenceJustificationEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSyncing: Boolean = false
)

/**
 * ViewModel para la pantalla de justificaciones pendientes del profesor
 * ✅ Ahora sincroniza con Firebase antes de cargar
 */
@HiltViewModel
class PendingJustificationsViewModel @Inject constructor(
    private val repository: JustificationRepository,
    private val localRepository: LocalDatabaseRepository // ✅ Para sincronizar
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingJustificationsUiState())
    val uiState: StateFlow<PendingJustificationsUiState> = _uiState.asStateFlow()

    /**
     * ✅ Carga las justificaciones pendientes, primero sincronizando con Firebase
     */
    fun loadPendingJustifications(teacherId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    isSyncing = true,
                    error = null
                )

                // 1. ✅ PRIMERO: Sincronizar con Firebase
                println("🔄 Sincronizando justificaciones desde Firebase...")
                localRepository.syncPendingJustifications(teacherId)

                _uiState.value = _uiState.value.copy(isSyncing = false)

                // 2. Luego cargar desde la base de datos local (ya sincronizada)
                println("📚 Cargando justificaciones desde base de datos local...")
                val justifications = repository.getPendingJustifications(teacherId)

                println("✅ Justificaciones cargadas: ${justifications.size}")

                _uiState.value = _uiState.value.copy(
                    justifications = justifications,
                    isLoading = false,
                    error = null
                )

            } catch (e: Exception) {
                println("❌ Error cargando justificaciones: ${e.message}")
                e.printStackTrace()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSyncing = false,
                    error = "Error al cargar justificaciones: ${e.message}"
                )
            }
        }
    }

    /**
     * ✅ Método para refrescar manualmente
     */
    fun refreshJustifications(teacherId: Int) {
        loadPendingJustifications(teacherId)
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}