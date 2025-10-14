package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.LoginCredentials
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.TeacherRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthUiState.Error
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthUiState.Initial
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthUiState.Loading
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val userWithProfile: UserWithProfile) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserWithProfile?>(null)
    val currentUser: StateFlow<UserWithProfile?> = _currentUser.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _currentUser.value = user
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = Loading

            val result = authRepository.login(LoginCredentials(email, password))

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    _currentUser.value = result.data
                    Success(result.data)
                }
                is ApiResult.Error -> Error(result.message)
                // ✅ ApiResult.Loading es ignorado aquí, pues el estado se puso en Loading al inicio
                ApiResult.Loading -> Loading
            }
        }
    }

    fun registerTeacher(form: TeacherRegistrationForm) {
        viewModelScope.launch {
            _uiState.value = Loading

            val result = authRepository.registerTeacher(form)

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    _currentUser.value = result.data
                    Success(result.data)
                }
                is ApiResult.Error -> Error(result.message)
                // ✅ ApiResult.Loading es ignorado
                ApiResult.Loading -> Loading
            }
        }
    }

    fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ) {
        viewModelScope.launch {
            _uiState.value = Loading

            val result = authRepository.registerParent(parentForm, studentForm)

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    // ✅ CORREGIDO: Desestructuramos el Triple<UserWithProfile, StudentEntity, ClassEntity>
                    // y usamos solo el primer elemento (UserWithProfile).
                    val (userWithProfile, _, _) = result.data

                    _currentUser.value = userWithProfile
                    Success(userWithProfile)
                }

                is ApiResult.Error -> Error(result.message)
                ApiResult.Loading -> Loading
            }
        }
    }

    fun resetState() {
        _uiState.value = Initial
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _uiState.value = Initial
        }
    }
}