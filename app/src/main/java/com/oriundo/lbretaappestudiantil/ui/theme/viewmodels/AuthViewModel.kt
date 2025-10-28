package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.firebase.FirebaseAuthRepository
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.LoginCredentials
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.TeacherRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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

    // =====================================================
    // LOGIN MANUAL
    // =====================================================

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

    // =====================================================
    // REGISTRO DE PROFESOR
    // =====================================================

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

    // =====================================================
    // REGISTRO DE APODERADO
    // =====================================================

    /**
     * ✅ MEJORADO: Registrar apoderado con Email/Password O completar perfil de Google
     */
    fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            // Verificar si el usuario ya está autenticado con Google
            val firebaseUid = firebaseAuth.currentUser?.uid

            val result = if (firebaseUid != null && parentForm.password == null) {
                // Caso 1: Usuario de Google - Completar perfil
                (authRepository as? FirebaseAuthRepository)?.completeParentProfile(
                    firebaseUid = firebaseUid,
                    parentForm = parentForm,
                    studentForm = studentForm
                ) ?: ApiResult.Error("Repositorio no compatible")
            } else {
                // Caso 2: Registro normal con Email/Password
                authRepository.registerParent(parentForm, studentForm)
            }

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    val (userWithProfile, _, _) = result.data
                    _currentUser.value = userWithProfile

                    // ✅ NUEVO: Si es usuario de Google, pedir establecer contraseña
                    if (firebaseUid != null && parentForm.password == null) {
                        AuthUiState.AwaitingPasswordSetup(
                            userWithProfile = userWithProfile,
                            isOptional = false // Contraseña obligatoria
                        )
                    } else {
                        AuthUiState.Success(userWithProfile)
                    }
                }
                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    // =====================================================
    // LOGIN CON GOOGLE (SOLO USUARIOS REGISTRADOS)
    // =====================================================

    /**
     * ✅ Login con Google - Solo permite usuarios YA REGISTRADOS
     * Si el usuario no existe, muestra error y debe ir a registrarse
     */
    fun loginWithGoogle(isTeacher: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = authRepository.loginWithGoogle(isTeacher)

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    val userWithProfile = result.data

                    if (isTeacher) {
                        // ✅ Profesor: Login completo directo
                        _currentUser.value = userWithProfile
                        AuthUiState.Success(userWithProfile)
                    } else {
                        // ✅ Apoderado: Verificar si tiene estudiantes registrados
                        val hasStudents = checkIfParentHasStudents(userWithProfile)

                        if (hasStudents) {
                            // Ya completó su perfil → Login exitoso
                            _currentUser.value = userWithProfile
                            AuthUiState.Success(userWithProfile)
                        } else {
                            // Necesita completar perfil → Mostrar formulario
                            AuthUiState.AwaitingProfileCompletion(userWithProfile)
                        }
                    }
                }
                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }
    fun registerWithGoogle(isTeacher: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            // Llama al método del repositorio para manejar la lógica de negocio/datos
            val result = authRepository.registerWithGoogle(isTeacher)

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    val userWithProfile = result.data

                    if (isTeacher) {
                        // Profesor: Perfil completo
                        _currentUser.value = userWithProfile
                        AuthUiState.Success(userWithProfile)
                    } else {
                        // Apoderado: Necesita completar perfil
                        AuthUiState.AwaitingProfileCompletion(userWithProfile)
                    }
                }
                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    // =====================================================
    // VINCULAR CONTRASEÑA
    // =====================================================

    /**
     * ✅ NUEVO: Vincular contraseña a cuenta de Google
     */
    fun linkPasswordToAccount(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val repository = authRepository as? FirebaseAuthRepository
            if (repository == null) {
                _uiState.value = AuthUiState.Error("Repositorio no compatible")
                return@launch
            }

            val result = repository.linkPasswordToGoogleAccount(email, password)

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

    /**
     * ✅ NUEVO: Saltar configuración de contraseña (solo si isOptional = true)
     */
    fun skipPasswordSetup() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                _uiState.value = AuthUiState.Success(user)
            } else {
                _uiState.value = AuthUiState.Error("No hay usuario autenticado")
            }
        }
    }

    // =====================================================
    // FUNCIONES AUXILIARES PRIVADAS
    // =====================================================

    /**
     * Verifica si el apoderado tiene estudiantes registrados
     */
    private suspend fun checkIfParentHasStudents(userWithProfile: UserWithProfile): Boolean {
        return try {
            val firebaseUid = userWithProfile.user.firebaseUid ?: return false

            val snapshot = firestore.collection("users")
                .document(firebaseUid)
                .collection("students")
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (_: Exception) {
            false
        }
    }

    // =====================================================
    // CONTROL DE ESTADOS
    // =====================================================

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