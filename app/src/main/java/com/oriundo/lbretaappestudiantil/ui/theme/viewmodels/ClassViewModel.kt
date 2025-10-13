package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClassUiState {
    object Initial : ClassUiState()
    object Loading : ClassUiState()
    data class Success(val classEntity: ClassEntity, val code: String) : ClassUiState()
    data class Error(val message: String) : ClassUiState()
}

@HiltViewModel
class ClassViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _createState = MutableStateFlow<ClassUiState>(Initial)
    val createState: StateFlow<ClassUiState> = _createState.asStateFlow()

    private val _teacherClasses = MutableStateFlow<List<ClassEntity>>(emptyList())
    val teacherClasses: StateFlow<List<ClassEntity>> = _teacherClasses.asStateFlow()

    fun createClass(
        teacherId: Int,
        className: String,
        schoolName: String,
        gradeLevel: String,
        academicYear: String
    ) {
        viewModelScope.launch {
            _createState.value = Loading

            val result = classRepository.createClass(
                className = className,
                schoolName = schoolName,
                teacherId = teacherId,
                gradeLevel = gradeLevel,
                academicYear = academicYear.toIntOrNull() ?: 2025
            )

            when (result) {
                is ApiResult.Success -> {
                    _createState.value = Success(result.data.first, result.data.second)
                    loadTeacherClasses(teacherId)
                }
                is ApiResult.Error -> {
                    _createState.value = Error(result.message)
                }

                ApiResult.Loading -> TODO()
            }
        }
    }

    fun loadTeacherClasses(teacherId: Int) {
        viewModelScope.launch {
            classRepository.getClassesByTeacher(teacherId).collect { classes ->
                _teacherClasses.value = classes
            }
        }
    }

    fun resetState() {
        _createState.value = Initial
    }
}