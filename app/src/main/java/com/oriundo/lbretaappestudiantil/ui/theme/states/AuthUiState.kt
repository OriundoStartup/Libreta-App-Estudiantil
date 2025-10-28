package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile

/**
 * Estados de la interfaz de usuario para el flujo de autenticación
 */
sealed class AuthUiState {
    /** Estado inicial - Sin acción */
    object Initial : AuthUiState()

    /** Cargando - Operación en progreso */
    object Loading : AuthUiState()

    /** Éxito - Usuario autenticado completamente */
    data class Success(val userWithProfile: UserWithProfile) : AuthUiState()

    /** Error - Operación fallida */
    data class Error(val message: String) : AuthUiState()

    /** Esperando completar perfil (Google Auth nuevo) */
    data class AwaitingProfileCompletion(val tempUser: UserWithProfile) : AuthUiState()

    /** ✅ NUEVO: Esperando establecer contraseña después de completar perfil */
    data class AwaitingPasswordSetup(
        val userWithProfile: UserWithProfile,
        val isOptional: Boolean = false // Si es true, el usuario puede saltar este paso
    ) : AuthUiState()
}