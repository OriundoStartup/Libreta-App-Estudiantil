package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity

sealed class StudentUiState {
    object Initial : StudentUiState()
    object Loading : StudentUiState()
    data class Success(val students: List<StudentEntity>) : StudentUiState()
    data class StudentCreated(val student: StudentEntity) : StudentUiState()
    data class Error(val message: String) : StudentUiState()
}