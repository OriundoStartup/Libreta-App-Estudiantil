package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Estudiantes
 */
@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["class_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["class_id"]),
        Index(value = ["rut"], unique = true)
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "class_id")
    val classId: Int,

    @ColumnInfo(name = "rut")
    val rut: String,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String,

    @ColumnInfo(name = "birth_date")
    val birthDate: Long? = null,

    @ColumnInfo(name = "photo_url")
    val photoUrl: String? = null,

    @ColumnInfo(name = "enrollment_date")
    val enrollmentDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    // ✅ NUEVOS CAMPOS DE SINCRONIZACIÓN
    @ColumnInfo(name = "firestore_id")
    val firestoreId: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
}