package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserWithProfile?>(null)
    val currentUser: StateFlow<UserWithProfile?> = _currentUser.asStateFlow()

    private var pendingGoogleToken: String? = null

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
    // AUTENTICACIÓN CON GOOGLE (PASO 1)
    // =====================================================

    /**
     * Autentica con Google pero NO crea en Firebase.
     * Solo guarda el token temporalmente para usarlo después en el Paso 2.
     */
    fun authenticateWithGoogleOnly(isTeacher: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("362206226811-d6s2dpivfotbtipnpbq1v073ktmc8uog.apps.googleusercontent.com")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)

                val result = try {
                    credentialManager.getCredential(request = request, context = context)
                } catch (e: GetCredentialException) {
                    _uiState.value = AuthUiState.Error("Error obteniendo credenciales de Google: ${e.message}")
                    return@launch
                }

                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val googleIdToken = credential.idToken

                pendingGoogleToken = googleIdToken

                val email = credential.id
                val displayName = credential.displayName ?: ""
                val photoUrl = credential.profilePictureUri?.toString()

                _uiState.value = AuthUiState.GoogleAuthPending(
                    email = email,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    googleIdToken = googleIdToken
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error en autenticación con Google: ${e.message}")
            }
        }
    }

    // =====================================================
    // REGISTRO DE APODERADO CON TODOS LOS DATOS
    // =====================================================

    /**
     * Registra apoderado con Email/Password O con Google (usando token guardado).
     * Este método SÍ crea en Firebase cuando se llama desde el Paso 2.
     */
    fun registerParentWithAllData(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm,
        googleIdToken: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            // 1. Registrar el Apoderado
            val result = if (googleIdToken != null) {
                // Caso 1: Registro con Google
                (authRepository as? FirebaseAuthRepository)?.registerParentWithGoogle(
                    parentForm = parentForm,
                    studentForm = studentForm,
                    googleIdToken = googleIdToken
                ) ?: ApiResult.Error("Repositorio no compatible")
            } else {
                // Caso 2: Registro normal con Email/Password
                authRepository.registerParent(parentForm, studentForm)
            }

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    val (userWithProfile, _, _) = result.data
                    val newParentId = userWithProfile.profile.id

                    // 2. BUSCAR AL ALUMNO POR RUT
                    when (val studentResult = studentRepository.getStudentByRut(studentForm.studentRut)) {
                        is ApiResult.Success -> {
                            val student = studentResult.data

                            // 3. VINCULAR AL APODERADO CON EL ALUMNO
                            val linkResult = studentRepository.linkParentToStudent(
                                studentId = student.id,
                                parentId = newParentId,
                                relationshipType = studentForm.relationshipType,
                                isPrimary = studentForm.isPrimary
                            )

                            if (linkResult is ApiResult.Success) {
                                _currentUser.value = userWithProfile

                                // Lógica de estado final
                                if (googleIdToken != null) {
                                    clearPendingGoogleToken()
                                    // Ir directamente a Success (ya no AwaitingPasswordSetup)
                                    AuthUiState.Success(userWithProfile)
                                } else {
                                    // Flujo normal de email/pass
                                    AuthUiState.Success(userWithProfile)
                                }
                            } else {
                                // Error en la vinculación
                                clearPendingGoogleToken()
                                AuthUiState.Error("Registro OK, pero error al vincular el alumno. Intente nuevamente.")
                            }
                        }
                        is ApiResult.Error -> {
                            // Error: Apoderado registrado, pero alumno no encontrado
                            clearPendingGoogleToken()
                            AuthUiState.Error("Apoderado creado, pero no se encontró un alumno con el RUT ${studentForm.studentRut} para vincular.")
                        }
                        ApiResult.Loading -> AuthUiState.Loading
                    }
                }
                is ApiResult.Error -> {
                    // Manejo de error original de la función de registro
                    clearPendingGoogleToken()
                    AuthUiState.Error(result.message)
                }
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    // =====================================================
    // MÉTODOS AUXILIARES GOOGLE TOKEN
    // =====================================================
    fun getPendingGoogleToken(): String? = pendingGoogleToken

    fun clearPendingGoogleToken() {
        pendingGoogleToken = null
    }

    // =====================================================
    // LOGIN CON GOOGLE (SOLO REGISTRADOS)
    // =====================================================
    fun loginWithGoogle(isTeacher: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.loginWithGoogle(isTeacher)
            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    val userWithProfile = result.data
                    if (isTeacher) {
                        _currentUser.value = userWithProfile
                        AuthUiState.Success(userWithProfile)
                    } else {
                        val hasStudents = checkIfParentHasStudents(userWithProfile)
                        if (hasStudents) {
                            _currentUser.value = userWithProfile
                            AuthUiState.Success(userWithProfile)
                        } else {
                            AuthUiState.AwaitingProfileCompletion(userWithProfile)
                        }
                    }
                }
                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    // =====================================================
    // REGISTRO CON GOOGLE (PROFESORES / APODERADOS SIN PERFIL)
    // =====================================================
    fun registerWithGoogle(isTeacher: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = authRepository.registerWithGoogle(isTeacher)

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    val userWithProfile = result.data

                    if (isTeacher) {
                        _currentUser.value = userWithProfile
                        AuthUiState.Success(userWithProfile)
                    } else {
                        // ✅ ESTA ES LA LÍNEA CORREGIDA
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
        clearPendingGoogleToken()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _uiState.value = AuthUiState.Initial
            clearPendingGoogleToken()
        }
    }
}
