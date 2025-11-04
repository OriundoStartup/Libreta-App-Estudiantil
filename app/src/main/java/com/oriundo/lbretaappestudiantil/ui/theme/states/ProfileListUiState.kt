package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity

/**
 * Estado para lista de perfiles
 */
sealed class ProfileListUiState {
    object Initial : ProfileListUiState()
    object Loading : ProfileListUiState()
    data class Success(val profiles: List<ProfileEntity>) : ProfileListUiState()
    data class Empty(val message: String = "No hay perfiles disponibles") : ProfileListUiState()
    data class Error(val message: String) : ProfileListUiState()
}