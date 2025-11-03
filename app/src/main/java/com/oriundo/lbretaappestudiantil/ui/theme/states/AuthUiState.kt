package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile

/**
 * Estados de la interfaz de usuario para el flujo de autenticación
 */
sealed class AuthUiState {

    /** Estado inicial - Sin acción */
    data object Idle : AuthUiState()

    /** Cargando - Operación en progreso */
    data object Loading : AuthUiState()

    /** Éxito - Usuario autenticado completamente */
    data class Success(val userWithProfile: UserWithProfile) : AuthUiState()

    /** Error - Operación fallida */
    data class Error(val message: String) : AuthUiState()

    /**
     * Google autenticado pero esperando completar registro
     * Se usa cuando el usuario selecciona Google Sign-In pero aún no completa el formulario
     * Este estado indica que se tienen los datos de Google pero NO se ha creado la cuenta aún
     */
    data class GoogleAuthPending(
        val email: String,
        val displayName: String,
        val photoUrl: String?
    ) : AuthUiState()

    /**
     * Email de recuperación de contraseña enviado exitosamente
     */
    data object PasswordResetSent : AuthUiState()
}