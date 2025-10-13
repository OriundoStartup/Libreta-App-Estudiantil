package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

// Se elimina la importación de android.app.Application
import androidx.lifecycle.ViewModel // 1. Cambiamos a la clase base ViewModel
import androidx.lifecycle.viewModelScope
// Se elimina la importación de com.oriundo.lbretaappestudiantil.di.RepositoryProvider
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.LoginCredentials
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.TeacherRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel // Nueva importación clave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // Nueva importación clave

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val userWithProfile: UserWithProfile) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel // 2. Indicamos a Hilt que inyecte este ViewModel
class AuthViewModel @Inject constructor( // 3. Usamos @Inject para el constructor
    private val authRepository: AuthRepository // 4. Hilt inyecta el repositorio
) : ViewModel() { // 5. Heredamos de ViewModel

    // ELIMINADA: private val authRepository: AuthRepository = RepositoryProvider.provideAuthRepository(application)

    // La inyección ya inicializó authRepository, por lo que el resto del código funciona:

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
                ApiResult.Loading -> AuthUiState.Loading // Uso simplificado del object
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
                    // Se asume que result.data es un Triple<UserWithProfile, StudentEntity, ParentEntity>
                    val (userWithProfile, _, _) = result.data
                    _currentUser.value = userWithProfile
                    AuthUiState.Success(userWithProfile)
                }
                is ApiResult.Error -> AuthUiState.Error(result.message)
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