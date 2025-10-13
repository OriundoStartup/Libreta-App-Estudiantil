package com.oriundo.lbretaappestudiantil.domain.model

data class ClassCreateForm(
    val teacherId: Int,
    val name: String,
    val schoolName: String,
    val gradeLevel: String,
    val academicYear: String
)