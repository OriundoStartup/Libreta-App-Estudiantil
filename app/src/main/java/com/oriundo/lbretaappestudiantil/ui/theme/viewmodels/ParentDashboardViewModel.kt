package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentDashboardState(
    val students: List<StudentWithClass> = emptyList(),
    val unreadAnnotations: List<AnnotationEntity> = emptyList(),
    // 🗑️ Eliminado: pendingMaterialRequests. Sustituido por un simple contador (o se asume que un nuevo Repository manejará esto)
    val unreadMessagesCount: Int = 0,
    val upcomingEvents: List<SchoolEventEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val annotationRepository: AnnotationRepository,
    // 🗑️ Eliminado: private val materialRequestRepository: MaterialRequestRepository,
    private val schoolEventRepository: SchoolEventRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(ParentDashboardState())
    val dashboardState: StateFlow<ParentDashboardState> = _dashboardState.asStateFlow()

    // ⚠️ Si el Repositorio de Notificaciones/Mensajes aún no existe:
    // **USO TEMPORAL:** Vamos a asumir que las anotaciones no leídas *son* la notificación por ahora.
    // Una vez que implementes tu 'MessageRepository', sustituye esto.

    fun loadDashboard(parentId: Int) {
        _dashboardState.value = _dashboardState.value.copy(isLoading = true)

        // Cargar estudiantes del apoderado
        viewModelScope.launch {
            try {
                // Sigue cargando estudiantes de forma reactiva
                studentRepository.getStudentsByParent(parentId).collect { students ->
                    _dashboardState.value = _dashboardState.value.copy(
                        students = students,
                        isLoading = false
                    )

                    // Ya no hay necesidad del bucle que cargaba las solicitudes de material
                }
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }

        // Cargar anotaciones no leídas (usadas también para el contador de notificaciones TEMPORALMENTE)
        viewModelScope.launch {
            try {
                annotationRepository.getUnreadAnnotationsForParent(parentId).collect { annotations ->
                    _dashboardState.value = _dashboardState.value.copy(
                        unreadAnnotations = annotations,
                        // Asume que las anotaciones no leídas se cuentan como mensajes/notificaciones.
                        unreadMessagesCount = annotations.size
                    )
                }
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    error = e.message
                )
            }
        }

        // ⚠️ Nota sobre Notificaciones/Mensajes:
        // Cuando implementes un ‘NotificationRepository’ o ‘MessageRepository’,
        // sustituye el bloque superior por algo como:
        /*
        viewModelScope.launch {
            try {
                messageRepository.getUnreadMessagesForParent(parentId).collect { messages ->
                    _dashboardState.value = _dashboardState.value.copy(
                        unreadMessagesCount = messages.size
                    )
                }
            } catch (e: Exception) { ... }
        }
        */

        // Cargar eventos generales (sin cambios)
        viewModelScope.launch {
            try {
                schoolEventRepository.getGeneralEvents().collect { events ->
                    _dashboardState.value = _dashboardState.value.copy(
                        upcomingEvents = events.take(5)
                    )
                }
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _dashboardState.value = _dashboardState.value.copy(error = null)
    }
}