package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity

sealed class ProfileUiState {
    object Initial : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: ProfileEntity) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}