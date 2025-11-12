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
        Index(value = ["absenceDate"]),
        Index(value = ["remoteId"], unique = true) // Índice para el ID de Firebase
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

    // ====================================================================
    // CAMPOS DE SINCRONIZACIÓN (añadidos en el paso anterior)
    // ====================================================================
    val remoteId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    // ====================================================================

    // Ya no necesita import porque JustificationStatus está en el mismo paquete
    val status: JustificationStatus = JustificationStatus.PENDING,
    val reviewedByTeacherId: Int? = null,
    val reviewNotes: String? = null,
    val reviewedAt: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val submittedAt: Long = System.currentTimeMillis()
)