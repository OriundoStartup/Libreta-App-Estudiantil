package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RequestStatus
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.MaterialRequestRepository
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
    val pendingMaterialRequests: List<MaterialRequestEntity> = emptyList(),
    val upcomingEvents: List<SchoolEventEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val annotationRepository: AnnotationRepository,
    private val materialRequestRepository: MaterialRequestRepository,
    private val schoolEventRepository: SchoolEventRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(ParentDashboardState())
    val dashboardState: StateFlow<ParentDashboardState> = _dashboardState.asStateFlow()

    fun loadDashboard(parentId: Int) {
        _dashboardState.value = _dashboardState.value.copy(isLoading = true)

        // Cargar estudiantes del apoderado
        viewModelScope.launch {
            try {
                studentRepository.getStudentsByParent(parentId).collect { students ->
                    _dashboardState.value = _dashboardState.value.copy(
                        students = students,
                        isLoading = false
                    )

                    // Cargar solicitudes de materiales para cada estudiante
                    students.forEach { student ->
                        loadMaterialRequestsForStudent(student.student.id)
                    }
                }
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }

        // Cargar anotaciones no leídas
        viewModelScope.launch {
            try {
                annotationRepository.getUnreadAnnotationsForParent(parentId).collect { annotations ->
                    _dashboardState.value = _dashboardState.value.copy(
                        unreadAnnotations = annotations
                    )
                }
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    error = e.message
                )
            }
        }

        // Cargar eventos generales
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

    private fun loadMaterialRequestsForStudent(studentId: Int) {
        viewModelScope.launch {
            try {
                materialRequestRepository.getRequestsByStudent(studentId).collect { requests ->
                    val pending = requests.filter { it.status == RequestStatus.PENDING }

                    // Combinar con solicitudes existentes
                    val currentRequests = _dashboardState.value.pendingMaterialRequests.toMutableList()
                    currentRequests.addAll(pending)

                    _dashboardState.value = _dashboardState.value.copy(
                        pendingMaterialRequests = currentRequests.distinctBy { it.id }
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