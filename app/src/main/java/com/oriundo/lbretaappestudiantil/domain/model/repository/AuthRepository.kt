package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.LoginCredentials
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.TeacherRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile

interface AuthRepository {
    // =====================================================
    // AUTENTICACIÓN BÁSICA
    // =====================================================

    /**
     * Login con email y contraseña
     */
    suspend fun login(credentials: LoginCredentials): ApiResult<UserWithProfile>

    /**
     * Registro de profesor con email/password
     */
    suspend fun registerTeacher(form: TeacherRegistrationForm): ApiResult<UserWithProfile>

    /**
     * Registro de apoderado con email/password
     */
    suspend fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>>

    /**
     * Cerrar sesión
     */
    suspend fun logout()

    /**
     * Verificar si un email ya está registrado
     */
    suspend fun isEmailRegistered(email: String): Boolean

    /**
     * Obtener el usuario actual
     */
    suspend fun getCurrentUser(): UserWithProfile?

    // =====================================================
    // AUTENTICACIÓN CON GOOGLE
    // =====================================================

    /**
     * Login/Registro con Google Sign-In
     */
    suspend fun loginWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile>

    suspend fun registerWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile>


    // =====================================================
    // GESTIÓN DE CONTRASEÑA
    // =====================================================

    /**
     * Vincula una contraseña a una cuenta de Google existente.
     * Esto permite login dual: Google Sign-In O Email/Password
     */
    suspend fun linkPasswordToGoogleAccount(
        email: String,
        password: String
    ): ApiResult<UserWithProfile>

    /**
     * Verifica si el usuario actual tiene una contraseña vinculada
     */
    suspend fun hasPasswordLinked(): Boolean
}