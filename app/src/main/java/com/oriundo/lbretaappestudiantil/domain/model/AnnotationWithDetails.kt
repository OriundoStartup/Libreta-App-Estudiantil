package com.oriundo.lbretaappestudiantil.domain.model

/**
 * Data class para anotación con información completa
 */
data class AnnotationWithDetails(
    val annotation: com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity,
    val teacher: com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity,
    val student: com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity,
    val classEntity: com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
)