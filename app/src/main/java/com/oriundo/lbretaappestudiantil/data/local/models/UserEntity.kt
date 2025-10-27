package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    // ✅ NUEVOS CAMPOS DE SINCRONIZACIÓN
    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String? = null,  // UID de Firebase Auth

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,

)