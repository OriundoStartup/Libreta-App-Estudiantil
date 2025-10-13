package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Solicitudes de materiales del profesor a apoderados
 */
@Entity(
    tableName = "material_requests",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["class_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["teacher_id"]),
        Index(value = ["student_id"]),
        Index(value = ["class_id"]),
        Index(value = ["status"])
    ]
)
data class MaterialRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "teacher_id")
    val teacherId: Int,

    @ColumnInfo(name = "class_id")
    val classId: Int,

    @ColumnInfo(name = "student_id")
    val studentId: Int? = null, // null = para toda la clase

    @ColumnInfo(name = "material")
    val material: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "urgency")
    val urgency: UrgencyLevel,

    @ColumnInfo(name = "status")
    val status: RequestStatus,

    @ColumnInfo(name = "deadline_date")
    val deadlineDate: Long? = null,

    @ColumnInfo(name = "created_date")
    val createdDate: Long = System.currentTimeMillis()
)

enum class UrgencyLevel {
    LOW,      // Baja
    MEDIUM,   // Media
    HIGH      // Alta
}

enum class RequestStatus {
    PENDING,   // Pendiente
    CONFIRMED, // Confirmado por apoderado
    DELIVERED, // Entregado
    CANCELLED  // Cancelado
}
