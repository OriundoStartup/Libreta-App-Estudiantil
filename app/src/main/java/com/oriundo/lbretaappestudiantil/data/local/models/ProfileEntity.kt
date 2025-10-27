package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Perfiles - Información personal de usuarios
 * Un usuario puede ser profesor, apoderado o ambos
 */
@Entity(
    tableName = "profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"], unique = true)]
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String,

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "photo_url")
    val photoUrl: String? = null,

    @ColumnInfo(name = "is_teacher")
    val isTeacher: Boolean = false,

    @ColumnInfo(name = "is_parent")
    val isParent: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    // ✅ NUEVOS CAMPOS DE SINCRONIZACIÓN
    @ColumnInfo(name = "firestore_id")
    val firestoreId: String? = null,  // ID del documento en Firestore

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String?,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
}
