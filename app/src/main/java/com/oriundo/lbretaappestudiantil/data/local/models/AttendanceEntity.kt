package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AttendanceStatus { PRESENT, ABSENT, LATE, JUSTIFIED }

@Entity(
    tableName = "attendance",
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
            childColumns = ["teacherId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["studentId", "attendanceDate"], unique = true),
        Index(value = ["teacherId"]),
        Index(value = ["firestoreId"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int,
    val teacherId: Int?,
    val attendanceDate: Long, // Epoch millis
    val status: AttendanceStatus,
    val notes: String? = null,

    // ✅ Campos para sincronización con Firebase
    val firestoreId: String? = null, // ID del documento en Firestore
    val studentFirebaseUid: String? = null, // Firebase UID del estudiante
    val teacherFirebaseUid: String? = null, // Firebase UID del profesor
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)