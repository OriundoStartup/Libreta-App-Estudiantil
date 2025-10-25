package com.oriundo.lbretaappestudiantil.domain.model

data class ParentRegistrationForm(
    val email: String,
    val password: String? = null,
    val confirmPassword: String? = null,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val address: String? = null
)