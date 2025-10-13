package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel // Cambiado de AndroidViewModel a ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
// import com.oriundo.lbretaappestudiantil.di.RepositoryProvider // ¡ELIMINADO!
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository // Importa la interfaz del Repository
import dagger.hilt.android.lifecycle.HiltViewModel // Nueva importación de Hilt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // Nueva importación de Inyección

sealed class AnnotationUiState {
    object Initial : AnnotationUiState()
    object Loading : AnnotationUiState()
    data class Success(val annotation: AnnotationEntity) : AnnotationUiState()
    data class Error(val message: String) : AnnotationUiState()
}

// 1. Añadir @HiltViewModel
@HiltViewModel
class AnnotationViewModel @Inject constructor(
    // 2. Usar @Inject y pedir el Repository
    private val annotationRepository: AnnotationRepository // Hilt lo inyecta automáticamente
) : ViewModel() { // 3. Heredar de ViewModel

    // Código interno del ViewModel sin cambios, pero ahora usa el repositorio inyectado
    // private val annotationRepository = RepositoryProvider.provideAnnotationDao(application) // ¡ELIMINADO!

    private val _createState = MutableStateFlow<AnnotationUiState>(AnnotationUiState.Initial)
    val createState: StateFlow<AnnotationUiState> = _createState.asStateFlow()

    // ... (El resto de tus StateFlows)
    private val _annotationsByStudent = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val annotationsByStudent: StateFlow<List<AnnotationEntity>> = _annotationsByStudent.asStateFlow()

    private val _annotationsByClass = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val annotationsByClass: StateFlow<List<AnnotationEntity>> = _annotationsByClass.asStateFlow()

    private val _annotationsByTeacher = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val annotationsByTeacher: StateFlow<List<AnnotationEntity>> = _annotationsByTeacher.asStateFlow()

    private val _unreadAnnotations = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val unreadAnnotations: StateFlow<List<AnnotationEntity>> = _unreadAnnotations.asStateFlow()


    fun createAnnotation(
        teacherId: Int,
        studentId: Int,
        classId: Int,
        type: AnnotationType,
        subject: String,
        description: String
    ) {
        viewModelScope.launch {
            _createState.value = AnnotationUiState.Loading

            val result = annotationRepository.createAnnotation( // Uso del repositorio inyectado
                teacherId = teacherId,
                studentId = studentId,
                classId = classId,
                type = type,
                subject = subject,
                description = description
            )

            // En AnnotationViewModel.kt (fun createAnnotation)

            _createState.value = when (result) {
                is ApiResult.Success<*> -> AnnotationUiState.Success(result.data as AnnotationEntity) // OK
                is ApiResult.Error -> AnnotationUiState.Error(result.message) // OK
                is ApiResult.Loading -> AnnotationUiState.Loading           // Usar 'is' explícitamente
                // Si sigue fallando, la última alternativa es añadir un 'else'
                // else -> AnnotationUiState.Error("Respuesta de API desconocida.")
                else -> {}
            } as AnnotationUiState
        }
    }

    fun loadAnnotationsByStudent(studentId: Int) {
        viewModelScope.launch {
            annotationRepository.getAnnotationsByStudent(studentId).collect { annotations ->
                _annotationsByStudent.value = annotations
            }
        }
    }

    fun loadAnnotationsByClass(classId: Int) {
        viewModelScope.launch {
            annotationRepository.getAnnotationsByClass(classId).collect { annotations ->
                _annotationsByClass.value = annotations
            }
        }
    }

    fun loadAnnotationsByTeacher(teacherId: Int) {
        viewModelScope.launch {
            annotationRepository.getAnnotationsByTeacher(teacherId).collect { annotations ->
                _annotationsByTeacher.value = annotations
            }
        }
    }

    fun loadUnreadAnnotationsForParent(parentId: Int) {
        viewModelScope.launch {
            annotationRepository.getUnreadAnnotationsForParent(parentId).collect { annotations ->
                _unreadAnnotations.value = annotations
            }
        }
    }

    fun markAsRead(annotationId: Int) {
        viewModelScope.launch {
            annotationRepository.markAsRead(annotationId)
        }
    }

    fun resetCreateState() {
        _createState.value = AnnotationUiState.Initial
    }
}