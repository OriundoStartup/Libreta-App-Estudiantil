package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.AppLanguage
import com.oriundo.lbretaappestudiantil.domain.model.DefaultView
import com.oriundo.lbretaappestudiantil.domain.model.UserPreferences
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    // Notificaciones
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun updateEmailNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateEmailNotifications(enabled)
        }
    }

    fun updateAnnotationNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAnnotationNotifications(enabled)
        }
    }

    fun updateEventReminders(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateEventReminders(enabled)
        }
    }

    fun updateMaterialRequestAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMaterialRequestAlerts(enabled)
        }
    }

    // Preferencias
    fun updateDefaultView(view: DefaultView) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultView(view)
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.updateLanguage(language)
        }
    }

    // Cambio de contraseña
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _passwordChangeState.value = PasswordChangeState.Loading

            try {
                val user = firebaseAuth.currentUser
                if (user == null || user.email == null) {
                    _passwordChangeState.value = PasswordChangeState.Error("Usuario no autenticado")
                    return@launch
                }

                // Re-autenticar al usuario
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()

                // Cambiar contraseña
                user.updatePassword(newPassword).await()

                _passwordChangeState.value = PasswordChangeState.Success
            } catch (e: Exception) {
                _passwordChangeState.value = PasswordChangeState.Error(
                    when {
                        e.message?.contains("password") == true -> "Contraseña actual incorrecta"
                        else -> "Error al cambiar contraseña: ${e.message}"
                    }
                )
            }
        }
    }

    fun resetPasswordChangeState() {
        _passwordChangeState.value = PasswordChangeState.Idle
    }

    // Cerrar sesión
    suspend fun logout(): ApiResult<Unit> {
        return try {
            authRepository.logout()
            preferencesRepository.clearPreferences()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al cerrar sesión: ${e.message}")
        }
    }
}