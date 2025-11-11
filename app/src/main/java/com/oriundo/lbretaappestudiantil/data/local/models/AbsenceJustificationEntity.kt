package com.oriundo.lbretaappestudiantil.data.local.models


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason

@Entity(
    tableName = "absence_justifications",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["parentId"]),
        Index(value = ["absenceDate"])
    ]
)
data class AbsenceJustificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val studentId: Int,
    val parentId: Int,
    val absenceDate: Long,
    val reason: AbsenceReason,
    val description: String,
    val attachmentUrl: String? = null,

    val status: JustificationStatus = JustificationStatus.PENDING,
    val reviewedByTeacherId: Int? = null,
    val reviewNotes: String? = null,
    val reviewedAt: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class JustificationStatus {
    PENDING,
    APPROVED,
    REJECTED


}