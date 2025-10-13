package com.oriundo.lbretaappestudiantil.domain.model

data class StudentRegistrationForm(
    val classCode: String,
    val studentRut: String,
    val studentFirstName: String,
    val studentLastName: String,
    val studentBirthDate: Long? = null,
    val relationshipType: com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType,
    val isPrimary: Boolean = true
)