package com.oriundo.lbretaappestudiantil.domain.model

import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity

/**
 * Data class para estudiante con su clase
 */
data class StudentWithClass(
    val student: StudentEntity,
    val classEntity: ClassEntity,
    val recentActivity: String? = "Todo al día"
) {
    // AÑADIR ESTAS PROPIEDADES CALCULADAS:
    val primaryParentId: Int?
        get() = student.primaryParentId

    val hasPrimaryParent: Boolean
        get() = student.primaryParentId != null
}