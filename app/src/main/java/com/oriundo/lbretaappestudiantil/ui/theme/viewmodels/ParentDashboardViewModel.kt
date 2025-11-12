package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import com.oriundo.lbretaappestudiantil.ui.theme.NavigationType
import com.oriundo.lbretaappestudiantil.ui.theme.Screen
import com.oriundo.lbretaappestudiantil.ui.theme.states.ParentDashboardState
import com.oriundo.lbretaappestudiantil.ui.theme.states.ParentDashboardUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


// ============================================================================
// ✅ 3. VIEWMODEL (Refactorizado)
// ============================================================================
@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val annotationRepository: AnnotationRepository,
    private val schoolEventRepository: SchoolEventRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(ParentDashboardState())
    val dashboardState: StateFlow<ParentDashboardState> = _dashboardState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ParentDashboardUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var currentParentId: Int? = null

    /**
     * Carga el dashboard (Actualizado: Los errores ahora emiten un evento)
     */
    fun loadDashboard(parentId: Int) {
        this.currentParentId = parentId

        // Activamos el estado de carga general al inicio
        _dashboardState.value = _dashboardState.value.copy(isLoading = true)

        // ====================================================================
        // ✅ Cargar estudiantes del apoderado (AHORA USA ApiResult)
        // ====================================================================
        viewModelScope.launch {
            // Asumiendo que getStudentsByParent ahora retorna Flow<ApiResult<List<StudentWithClass>>>
            studentRepository.getStudentsByParent(parentId).collect { apiResult ->
                when (apiResult) {
                    is ApiResult.Loading -> {
                        // El estado principal de carga ya está en 'true', no hacemos nada.
                        // Esto previene que la lógica de la UI reaccione a un listado vacío inicial.
                    }
                    is ApiResult.Success -> {
                        _dashboardState.value = _dashboardState.value.copy(
                            students = apiResult.data,
                            isLoading = false // ✅ Desactivamos la carga principal con el primer dato exitoso
                        )
                    }
                    is ApiResult.Error -> {
                        _dashboardState.value = _dashboardState.value.copy(isLoading = false)
                        // Emitimos el error como un evento único de Snackbar
                        emitEvent(ParentDashboardUiEvent.ShowSnackbar("Error al cargar estudiantes: ${apiResult.message}"))
                    }
                }
            }
        }

        // ====================================================================
        // Cargar anotaciones no leídas (Sigue usando try/catch)
        // ====================================================================
        viewModelScope.launch {
            try {
                annotationRepository.getUnreadAnnotationsForParent(parentId).collect { annotations ->
                    _dashboardState.value = _dashboardState.value.copy(
                        unreadAnnotations = annotations,
                        unreadMessagesCount = annotations.size
                    )
                }
            } catch (e: Exception) {
                // Manejo de error de la corrutina externa
                emitEvent(ParentDashboardUiEvent.ShowSnackbar("Error al cargar anotaciones: ${e.message ?: "Desconocido"}"))
            }
        }

        // ====================================================================
        // Cargar eventos generales (Sigue usando try/catch)
        // ====================================================================
        viewModelScope.launch {
            try {
                schoolEventRepository.getGeneralEvents().collect { events ->
                    _dashboardState.value = _dashboardState.value.copy(
                        upcomingEvents = events.take(5)
                    )
                }
            } catch (e: Exception) {
                // Manejo de error de la corrutina externa
                emitEvent(ParentDashboardUiEvent.ShowSnackbar("Error al cargar eventos: ${e.message ?: "Desconocido"}"))
            }
        }
    }

    // ============================================================================
    // 4. LÓGICA DE NEGOCIO CENTRALIZADA (Sin cambios en la lógica, solo en la emisión de errores)
    // ============================================================================

    fun onQuickActionClick(actionType: NavigationType) {
        val students = _dashboardState.value.students
        val parentId = currentParentId

        if (parentId == null) {
            // Error en la sesión (emite evento)
            emitEvent(ParentDashboardUiEvent.ShowSnackbar("Error de sesión. Intente nuevamente."))
            return
        }

        when {
            students.isEmpty() -> {
                // Sin estudiantes (emite evento)
                emitEvent(ParentDashboardUiEvent.ShowSnackbar("No tienes estudiantes asociados. Contacta a la escuela."))
            }
            students.size == 1 -> {
                // Navegación directa
                val student = students.first()
                val route = getRouteForAction(
                    actionType = actionType,
                    studentId = student.student.id,
                    classId = student.classEntity.id,
                    parentId = parentId
                )
                emitEvent(ParentDashboardUiEvent.Navigate(route))
            }
            else -> {
                // Pedir selección
                emitEvent(ParentDashboardUiEvent.ShowStudentSelector(actionType))
            }
        }
    }

    fun onStudentSelectedFromSheet(student: StudentWithClass, actionType: NavigationType) {
        val parentId = currentParentId ?: return

        val route = getRouteForAction(
            actionType = actionType,
            studentId = student.student.id,
            classId = student.classEntity.id,
            parentId = parentId
        )
        emitEvent(ParentDashboardUiEvent.Navigate(route))
    }

    // ✅ clearError() ha sido eliminado, ya no es necesario.

    // --- Funciones Helper ---

    private fun getRouteForAction(actionType: NavigationType, studentId: Int, classId: Int, parentId: Int): String {
        return when (actionType) {
            NavigationType.EVENTS -> Screen.StudentEvents.createRoute(studentId, classId)
            NavigationType.ATTENDANCE -> Screen.StudentAttendance.createRoute(studentId, classId)
            NavigationType.JUSTIFY -> Screen.JustifyAbsence.createRoute(studentId, parentId)
            NavigationType.ANNOTATIONS -> Screen.StudentAnnotations.createRoute(studentId, parentId)
            else -> {}
        } as String
    }

    private fun emitEvent(event: ParentDashboardUiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
}