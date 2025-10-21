package com.oriundo.lbretaappestudiantil.data.local.models


import androidx.room.ColumnInfo

/**
 * DTO para la query que obtiene estudiante + clase + apoderado principal
 */
data class StudentWithClassAndParentDto(
    // Campos del estudiante
    val id: Int,
    @ColumnInfo(name = "class_id") val classId: Int,
    val rut: String,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "birth_date") val birthDate: Long?,
    @ColumnInfo(name = "photo_url") val photoUrl: String?,
    @ColumnInfo(name = "enrollment_date") val enrollmentDate: Long,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    val notes: String?,

    // Campos de la clase
    @ColumnInfo(name = "class_name") val className: String,
    @ColumnInfo(name = "school_name") val schoolName: String,
    @ColumnInfo(name = "class_code") val classCode: String,
    @ColumnInfo(name = "teacher_id") val teacherId: Int,

    // Parent ID
    @ColumnInfo(name = "primary_parent_id") val primaryParentId: Int?
)