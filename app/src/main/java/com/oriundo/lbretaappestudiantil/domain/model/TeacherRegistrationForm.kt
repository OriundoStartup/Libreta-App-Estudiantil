package com.oriundo.lbretaappestudiantil.domain.model

/**
 * Data classes para formularios de registro
 */
data class TeacherRegistrationForm(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val address: String? = null
)