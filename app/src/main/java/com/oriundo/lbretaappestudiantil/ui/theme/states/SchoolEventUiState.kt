package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity

sealed class SchoolEventUiState {
    object Initial : SchoolEventUiState()
    object Loading : SchoolEventUiState()
    data class Success(val event: SchoolEventEntity) : SchoolEventUiState()
    data class Error(val message: String) : SchoolEventUiState()
}