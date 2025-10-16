package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnnotationUiState {
    object Idle : AnnotationUiState()
    object Loading : AnnotationUiState()
    object Success : AnnotationUiState()
    data class Error(val message: String) : AnnotationUiState()
}

@HiltViewModel
class AnnotationViewModel @Inject constructor(
    private val annotationRepository: AnnotationRepository
) : ViewModel() {

    private val _createState = MutableStateFlow<AnnotationUiState>(AnnotationUiState.Idle)
    val createState: StateFlow<AnnotationUiState> = _createState.asStateFlow()

    private val _annotationsByStudent = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val annotationsByStudent: StateFlow<List<AnnotationEntity>> = _annotationsByStudent.asStateFlow()

    private val _annotationsByClass = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val annotationsByClass: StateFlow<List<AnnotationEntity>> = _annotationsByClass.asStateFlow()

    private val _annotationsByTeacher = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val annotationsByTeacher: StateFlow<List<AnnotationEntity>> = _annotationsByTeacher.asStateFlow()

    private val _unreadAnnotations = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val unreadAnnotations: StateFlow<List<AnnotationEntity>> = _unreadAnnotations.asStateFlow()

    /**
     * Crea una nueva anotación.
     *
     * @param studentId ID del estudiante
     * @param teacherId ID del profesor
     * @param title Título de la anotación
     * @param description Descripción detallada
     * @param type Tipo de anotación
     * @param classId ID de la clase (opcional, por defecto 0)
     */
    fun createAnnotation(
        studentId: Int,
        teacherId: Int,
        title: String,
        description: String,
        type: AnnotationType,
        classId: Int = 0  // ← Parámetro opcional con valor por defecto
    ) {
        viewModelScope.launch {
            _createState.value = AnnotationUiState.Loading

            val result = annotationRepository.createAnnotation(
                teacherId = teacherId,
                studentId = studentId,
                classId = classId,
                type = type,
                subject = title,        // ← title se mapea a subject
                description = description
            )

            _createState.value = when (result) {
                is ApiResult.Success -> AnnotationUiState.Success
                is ApiResult.Error -> AnnotationUiState.Error(result.message)
                ApiResult.Loading -> AnnotationUiState.Loading
            }
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

    fun resetState() {
        _createState.value = AnnotationUiState.Idle
    }
}