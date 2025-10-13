package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel // 1. Usamos ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
// Se elimina la importación de RepositoryProvider
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository // Importamos la interfaz
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ProfileUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel // 2. Importación de Hilt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // 3. Importación para Inyección


sealed class ProfileUiState {
    object Initial : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: ProfileEntity) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel // 4. Anotación para que Hilt sepa cómo construir
class ProfileViewModel @Inject constructor( // 5. Inyección del Repository
    private val profileRepository: ProfileRepository
) : ViewModel() { // 6. Heredamos de ViewModel

    // ELIMINADA: private val profileRepository = RepositoryProvider.provideProfileRepository(application)

    private val _uiState = MutableStateFlow<ProfileUiState>(Initial)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _currentProfile = MutableStateFlow<ProfileEntity?>(null)
    val currentProfile: StateFlow<ProfileEntity?> = _currentProfile.asStateFlow()

    fun loadProfile(profileId: Int) {
        viewModelScope.launch {
            _uiState.value = Loading

            when (val result = profileRepository.getProfileById(profileId)) {
                // CORRECCIÓN: Se elimina el <*>, ya que el tipo se infiere correctamente.
                is ApiResult.Success -> {
                    _currentProfile.value = result.data
                    _uiState.value = Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = Error(result.message)
                }
                ApiResult.Loading -> {} // No hacemos nada, el estado ya es Loading
            }
        }
    }

    fun loadProfileByUserId(userId: Int) {
        viewModelScope.launch {
            _uiState.value = Loading

            when (val result = profileRepository.getProfileByUserId(userId)) {
                is ApiResult.Success -> {
                    _currentProfile.value = result.data
                    _uiState.value = Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = Error(result.message)
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            when (val result = profileRepository.updateProfile(profile)) {
                is ApiResult.Success -> {
                    _currentProfile.value = profile
                    _uiState.value = Success(profile)
                }
                is ApiResult.Error -> {
                    _uiState.value = Error(result.message)
                }
                // CORRECCIÓN: Se eliminan las ramas duplicadas e incorrectas.
                ApiResult.Loading -> {}
            }
        }
    }

    fun addParentRole(profileId: Int) {
        viewModelScope.launch {
            profileRepository.addParentRole(profileId)
        }
    }

    fun addTeacherRole(profileId: Int) {
        viewModelScope.launch {
            profileRepository.addTeacherRole(profileId)
        }
    }

    fun resetState() {
        _uiState.value = Initial
    }
}