package com.oriundo.lbretaappestudiantil.domain.model

/**
 * Data class para estudiante con su clase
 */
data class StudentWithClass(
    val student: com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity,
    val classEntity: com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity,
    val primaryParentId: Int? = null

)