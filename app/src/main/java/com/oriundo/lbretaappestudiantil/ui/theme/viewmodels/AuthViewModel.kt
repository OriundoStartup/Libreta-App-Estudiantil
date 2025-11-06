package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.oriundo.lbretaappestudiantil.R
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()


    private val _currentUser = MutableStateFlow<UserWithProfile?>(null)
    val currentUser: StateFlow<UserWithProfile?> = _currentUser.asStateFlow()

    // ✅ NUEVO: Obtener el ID del usuario actual
    val currentUserId: Int?
        get() = _currentUser.value?.user?.id

    val currentProfileId: Int?
        get() = _currentUser.value?.profile?.id
    private var pendingGoogleIdToken: String? = null
    private var pendingGooglePhotoUrl: String? = null

    init {
        // ✅ Cargar usuario actual al iniciar
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                _currentUser.value = user
                println("🟢 Usuario cargado en init: ${user?.profile?.id}")
            } catch (e: Exception) {
                println("🔴 Error cargando usuario en init: ${e.message}")
            }
        }
    }


    @Volatile
    private var isRegistering = false

    // =====================================================
    // LOGIN
    // =====================================================

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = authRepository.login(LoginCredentials(email, password))

            _uiState.value = when (result) {
                is ApiResult.Success -> {
                    _currentUser.value = result.data // ✅ GUARDAR USUARIO
                    println("🟢 Login exitoso, currentUser = ${result.data.profile.id}")
                    AuthUiState.Success(result.data)
                }

                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }

    // =====================================================
    // GOOGLE SIGN-IN (PARA LOGIN CON USUARIOS EXISTENTES)
    // =====================================================

    fun authenticateWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val credentialManager = CredentialManager.create(activity)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(activity.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleIdTokenCredential.idToken

                    pendingGoogleIdToken = googleIdToken
                    pendingGooglePhotoUrl = googleIdTokenCredential.profilePictureUri?.toString()

                    if (authRepository is FirebaseAuthRepository) {
                        val authResult = authRepository.authenticateWithGoogleToken(googleIdToken)

                        when (authResult) {
                            is ApiResult.Success -> {
                                _currentUser.value = authResult.data // ✅ GUARDAR USUARIO
                                pendingGoogleIdToken = null
                                pendingGooglePhotoUrl = null
                                println("🟢 Google login exitoso, currentUser = ${authResult.data.profile.id}")
                                _uiState.value = AuthUiState.Success(authResult.data)
                            }
                            is ApiResult.Error -> {
                                if (authResult.message == "NEEDS_REGISTRATION") {
                                    _uiState.value = AuthUiState.GoogleAuthPending(
                                        email = googleIdTokenCredential.id,
                                        displayName = googleIdTokenCredential.displayName ?: "",
                                        photoUrl = pendingGooglePhotoUrl
                                    )
                                } else {
                                    pendingGoogleIdToken = null
                                    pendingGooglePhotoUrl = null
                                    _uiState.value = AuthUiState.Error(authResult.message)
                                }
                            }
                            ApiResult.Loading -> {
                                _uiState.value = AuthUiState.Loading
                            }
                        }
                    } else {
                        _uiState.value = AuthUiState.GoogleAuthPending(
                            email = googleIdTokenCredential.id,
                            displayName = googleIdTokenCredential.displayName ?: "",
                            photoUrl = pendingGooglePhotoUrl
                        )
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Tipo de credencial no válido")
                }
            } catch (e: GetCredentialException) {
                _uiState.value = AuthUiState.Error("Error de Google Sign-In: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // =====================================================
    // NUEVO: OBTENER DATOS DE GOOGLE SOLO PARA AUTOCOMPLETAR
    // (NO CREA CUENTA EN FIREBASE)
    // =====================================================

    /**
     * Obtiene los datos de Google sin autenticar en Firebase.
     * Solo para autocompletar formularios de registro.
     * Similar a como funciona el registro del profesor.
     */
    fun getGoogleDataForAutocomplete(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val credentialManager = CredentialManager.create(activity)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(activity.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    // Guardar token para uso posterior
                    pendingGoogleIdToken = googleIdTokenCredential.idToken
                    pendingGooglePhotoUrl = googleIdTokenCredential.profilePictureUri?.toString()

                    // NO autenticar en Firebase, solo cambiar estado a GoogleAuthPending
                    _uiState.value = AuthUiState.GoogleAuthPending(
                        email = googleIdTokenCredential.id,
                        displayName = googleIdTokenCredential.displayName ?: "",
                        photoUrl = pendingGooglePhotoUrl
                    )
                } else {
                    _uiState.value = AuthUiState.Error("Tipo de credencial no válido")
                }
            } catch (e: GetCredentialException) {
                _uiState.value = AuthUiState.Error("Error de Google Sign-In: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun getPendingGoogleToken(): String? = pendingGoogleIdToken

    // =====================================================
    // REGISTRO PROFESOR (SIN CAMBIOS)
    // =====================================================

    fun registerTeacher(
        teacherForm: TeacherRegistrationForm,
        googleIdToken: String? = null
    ) {
        synchronized(this) {
            if (isRegistering) {
                return
            }
            isRegistering = true
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val result = if (googleIdToken != null && authRepository is FirebaseAuthRepository) {
                    authRepository.registerTeacherWithGoogle(
                        teacherForm = teacherForm,
                        googleIdToken = googleIdToken
                    )
                } else {
                    authRepository.registerTeacher(teacherForm)
                }

                _uiState.value = when (result) {
                    is ApiResult.Success -> {
                        _currentUser.value = result.data // ✅ GUARDAR USUARIO
                        pendingGoogleIdToken = null
                        pendingGooglePhotoUrl = null
                        println("🟢 Registro profesor exitoso, currentUser = ${result.data.profile.id}")
                        AuthUiState.Success(result.data)
                    }
                    is ApiResult.Error -> AuthUiState.Error(result.message)
                    ApiResult.Loading -> AuthUiState.Loading
                }
            } finally {
                synchronized(this@AuthViewModel) {
                    isRegistering = false
                }
            }
        }
    }

    // =====================================================
    // REGISTRO APODERADO (SIN CAMBIOS)
    // =====================================================

    fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm,
        googleIdToken: String? = null
    ) {
        synchronized(this) {
            if (isRegistering) {
                return
            }
            isRegistering = true
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val result = if (googleIdToken != null && authRepository is FirebaseAuthRepository) {
                    authRepository.registerParentWithGoogle(
                        parentForm = parentForm,
                        studentForm = studentForm,
                        googleIdToken = googleIdToken
                    )
                } else {
                    authRepository.registerParent(parentForm, studentForm)
                }

                _uiState.value = when (result) {
                    is ApiResult.Success -> {
                        val (userWithProfile, student, _) = result.data

                        try {
                            studentRepository.linkParentToStudent(
                                studentId = student.id,
                                parentId = userWithProfile.profile.id,
                                relationshipType = studentForm.relationshipType,
                                isPrimary = true
                            )
                        } catch (_: Exception) {
                        }

                        _currentUser.value = userWithProfile // ✅ GUARDAR USUARIO
                        pendingGoogleIdToken = null
                        pendingGooglePhotoUrl = null
                        println("🟢 Registro apoderado exitoso, currentUser = ${userWithProfile.profile.id}")

                        AuthUiState.Success(userWithProfile)

                        AuthUiState.Success(userWithProfile)
                    }
                    is ApiResult.Error -> AuthUiState.Error(result.message)
                    ApiResult.Loading -> AuthUiState.Loading
                }
            } finally {
                synchronized(this@AuthViewModel) {
                    isRegistering = false
                }
            }
        }
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null // ✅ LIMPIAR USUARIO
            _uiState.value = AuthUiState.Idle
            pendingGoogleIdToken = null
            pendingGooglePhotoUrl = null
            synchronized(this@AuthViewModel) {
                isRegistering = false
            }
        }
    }

    // =====================================================
    // RECUPERACIÓN CONTRASEÑA
    // =====================================================

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = authRepository.sendPasswordResetEmail(email)

            _uiState.value = when (result) {
                is ApiResult.Success -> AuthUiState.Error("Email de recuperación enviado")
                is ApiResult.Error -> AuthUiState.Error(result.message)
                ApiResult.Loading -> AuthUiState.Loading
            }
        }
    }
}