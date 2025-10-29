package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Initial)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _currentProfile = MutableStateFlow<ProfileEntity?>(null)
    val currentProfile: StateFlow<ProfileEntity?> = _currentProfile.asStateFlow()

    // ✅ MODIFICADO - Ahora devuelve ApiResult<ProfileEntity>
    suspend fun loadProfile(profileId: Int): ApiResult<ProfileEntity> {
        _uiState.value = ProfileUiState.Loading

        return when (val result = profileRepository.getProfileById(profileId)) {
            is ApiResult.Success -> {
                _currentProfile.value = result.data
                _uiState.value = ProfileUiState.Success(result.data)
                result
            }
            is ApiResult.Error -> {
                _uiState.value = ProfileUiState.Error(result.message)
                result
            }
            ApiResult.Loading -> ApiResult.Loading
        }
    }

    fun loadProfileByUserId(userId: Int) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            when (val result = profileRepository.getProfileByUserId(userId)) {
                is ApiResult.Success -> {
                    _currentProfile.value = result.data
                    _uiState.value = ProfileUiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = ProfileUiState.Error(result.message)
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
                    _uiState.value = ProfileUiState.Success(profile)
                }
                is ApiResult.Error -> {
                    _uiState.value = ProfileUiState.Error(result.message)
                }
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
        _uiState.value = ProfileUiState.Initial
    }
}