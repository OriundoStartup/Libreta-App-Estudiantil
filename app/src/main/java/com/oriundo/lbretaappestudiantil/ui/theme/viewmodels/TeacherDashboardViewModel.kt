package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
// ✅ CORRECCIÓN 1: ¡Importación faltante!
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherDashboardState(
    val classes: List<ClassEntity> = emptyList(),
    val totalStudents: Int = 0,
    val pendingAnnotations: Int = 0,
    val upcomingEvents: List<SchoolEventEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TeacherDashboardViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val annotationRepository: AnnotationRepository,
    // ✅ CORRECCIÓN 2: ¡Parámetro de inyección faltante!
    private val schoolEventRepository: SchoolEventRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(TeacherDashboardState())
    val dashboardState: StateFlow<TeacherDashboardState> = _dashboardState.asStateFlow()

    fun loadDashboard(teacherId: Int) {
        _dashboardState.value = _dashboardState.value.copy(isLoading = true)

        // Cargar cursos del profesor
        viewModelScope.launch {
            try {
                classRepository.getClassesByTeacher(teacherId).collect { classes ->
                    _dashboardState.value = _dashboardState.value.copy(
                        classes = classes,
                        isLoading = false
                    )

                    var totalStudents = 0
                    classes.forEach { classEntity ->
                        viewModelScope.launch {
                            studentRepository.getStudentsByClass(classEntity.id).collect { students ->
                                totalStudents += students.size
                                _dashboardState.value = _dashboardState.value.copy(
                                    totalStudents = totalStudents
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }

        // Cargar anotaciones recientes
        viewModelScope.launch {
            try {
                annotationRepository.getAnnotationsByTeacher(teacherId).collect { annotations ->
                    _dashboardState.value = _dashboardState.value.copy(
                        pendingAnnotations = annotations.size
                    )
                }
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    error = e.message
                )
            }
        }

        // Cargar eventos generales (schoolEventRepository ahora se resuelve)
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


}