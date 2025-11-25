package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalCoroutinesApi::class)
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
        // UBICACIÓN: Reemplazar el bloque de código que inicia en 'viewModelScope.launch { try { classRepository.getClassesByTeacher(teacherId).collect { classes ->'
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            try {
                // 1. Iniciar con el Flow de clases del profesor
                classRepository.getClassesByTeacher(teacherId)
                    // 2. Usar flatMapLatest para cambiar a un Flow que emite el total de estudiantes.
                    .flatMapLatest { classes ->
                        // Actualiza la lista de clases en el estado.
                        _dashboardState.value = _dashboardState.value.copy(
                            classes = classes,
                            isLoading = false
                        )

                        if (classes.isEmpty()) {
                            // Si no hay clases, el total es 0.
                            flowOf(0)
                        } else {
                            // 3. Crear una lista de Flows de estudiantes, uno por cada clase.
                            val studentFlows = classes.map { classEntity ->
                                studentRepository.getStudentsByClass(classEntity.id)
                            }

                            // 4. Combinar todos los Flows de estudiantes en un único Flow.
                            combine(studentFlows) { listsOfStudents ->
                                // Aplanar todas las listas de estudiantes en una sola
                                // y usar distinctBy para asegurar que se cuenta a cada estudiante solo una vez.
                                listsOfStudents
                                    .asList()
                                    .flatten()
                                    .distinctBy { it.id } // Garantiza unicidad por ID de estudiante
                                    .size
                            }
                        }
                    }
                    // 5. Recolectar el Flow final (que ahora solo emite el total de estudiantes).
                    .collect { totalCount ->
                        // 6. Actualizar el estado con el conteo final.
                        _dashboardState.value = _dashboardState.value.copy(
                            totalStudents = totalCount
                        )
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