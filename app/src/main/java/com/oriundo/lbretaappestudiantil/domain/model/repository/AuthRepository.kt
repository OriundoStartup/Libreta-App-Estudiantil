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
    // LOGIN
    // =====================================================

    suspend fun login(credentials: LoginCredentials): ApiResult<UserWithProfile>

    // =====================================================
    // REGISTRO
    // =====================================================

    suspend fun registerTeacher(form: TeacherRegistrationForm): ApiResult<UserWithProfile>

    suspend fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>>

    // =====================================================
    // RECUPERACIÓN DE CONTRASEÑA
    // =====================================================

    suspend fun sendPasswordResetEmail(email: String): ApiResult<Unit>

    // =====================================================
    // UTILIDADES
    // =====================================================

    suspend fun logout()

    suspend fun getCurrentUser(): UserWithProfile?

    suspend fun isEmailRegistered(email: String): Boolean
}