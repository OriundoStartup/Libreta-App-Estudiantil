package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["rut"], unique = true),
        Index(value = ["firestoreId"], unique = true),
        Index(value = ["firebaseUid"], unique = true),
        Index(value = ["classId"])
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    var classId: Int, // Clave foránea para la clase

    val rut: String,
    val firstName: String,
    val lastName: String,
    val birthDate: Long? = null,
    val photoUrl: String? = null,

    // ✅ Campos añadidos que faltaban
    val enrollmentDate: Long? = null, // Fecha de matrícula
    val isActive: Boolean = true,       // Si el estudiante está activo en la clase
    val notes: String? = null,          // Notas adicionales sobre el estudiante

    // ID del documento del estudiante en la subcolección del padre en Firestore
    val firestoreId: String? = null,

    // UID del estudiante si tiene su propia cuenta de usuario (para futuras implementaciones)
    val firebaseUid: String? = null,

    // ID del perfil del apoderado principal (denormalizado para acceso rápido)
    var primaryParentId: Int? = null,

    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncedAt: Long? = null
)
