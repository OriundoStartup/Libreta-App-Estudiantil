package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * DTO para la query que obtiene estudiante + clase + apoderado principal
 */
data class StudentWithClassAndParentDto(
    @Embedded
    val student: StudentEntity,

    // Campos de la clase (usando alias)
    @ColumnInfo(name = "className")
    val className: String,

    @ColumnInfo(name = "schoolName")
    val schoolName: String,

    @ColumnInfo(name = "classCode")
    val classCode: String,

    @ColumnInfo(name = "teacherId")
    val teacherId: Int
)
