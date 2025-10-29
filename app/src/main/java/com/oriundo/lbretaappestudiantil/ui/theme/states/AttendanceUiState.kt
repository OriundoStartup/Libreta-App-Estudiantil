package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity

sealed class AttendanceUiState {
    object Initial : AttendanceUiState()
    object Loading : AttendanceUiState()
    data class Success(val attendance: AttendanceEntity) : AttendanceUiState()
    data class Error(val message: String) : AttendanceUiState()
}