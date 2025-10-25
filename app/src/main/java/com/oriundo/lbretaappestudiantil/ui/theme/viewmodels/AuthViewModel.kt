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
    data class AwaitingProfileCompletion(val tempUser: UserWithProfile) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
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
            _uiState.value = AuthUiState.Loading

            val result = authRepository.login(LoginCredentials(email, password))

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    _currentUser.value = result.data
                    AuthUiState.Success(result.data)
                }
                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    fun registerTeacher(form: TeacherRegistrationForm) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = authRepository.registerTeacher(form)

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    _currentUser.value = result.data
                    AuthUiState.Success(result.data)
                }
                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = authRepository.registerParent(parentForm, studentForm)

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    // Desestructuramos el Triple<UserWithProfile, StudentEntity, ClassEntity>
                    val (userWithProfile, _, _) = result.data
                    _currentUser.value = userWithProfile
                    AuthUiState.Success(userWithProfile)
                }
                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    /**
     * ✅ FUNCIÓN CORREGIDA: Login con Google
     *
     * Esta función intenta autenticar con Google y maneja dos casos:
     * 1. Si es AuthRepositoryImpl (Room/Local): Retorna error inmediatamente
     * 2. Si es FirebaseAuthRepository: Realiza la autenticación real
     *
     * @param isTeacher: true si el usuario se está registrando como profesor
     */
    fun loginWithGoogle(isTeacher: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = authRepository.loginWithGoogle(isTeacher)

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    val userWithProfile = result.data

                    // ✅ CORREGIDO: Verificamos si es un registro nuevo (sin datos adicionales)
                    // Para profesores: Si tiene todos los datos, está completo
                    // Para apoderados: Siempre necesitan completar con datos del estudiante

                    if (isTeacher) {
                        // Profesor: Login completo - Google proporciona todos los datos necesarios
                        _currentUser.value = userWithProfile
                        AuthUiState.Success(userWithProfile)
                    } else {
                        // Apoderado: Siempre necesita ir al Paso 2 para agregar estudiante
                        AuthUiState.AwaitingProfileCompletion(userWithProfile)
                    }
                }
                is ApiResult.Error -> {
                    // Si el error es porque no hay Firebase configurado, mostramos un mensaje claro
                    AuthUiState.Error(result.message)
                }
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Initial
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _uiState.value = AuthUiState.Initial
        }
    }
}