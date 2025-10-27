package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cursos creados por profesores
 */
@Entity(
    tableName = "classes",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["teacher_id"]),
        Index(value = ["class_code"], unique = true)
    ]
)
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "class_name")
    val className: String,

    @ColumnInfo(name = "school_name")
    val schoolName: String,

    @ColumnInfo(name = "teacher_id")
    val teacherId: Int,

    @ColumnInfo(name = "class_code")
    val classCode: String,

    @ColumnInfo(name = "grade_level")
    val gradeLevel: String? = null,

    @ColumnInfo(name = "academic_year")
    val academicYear: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    // ✅ NUEVOS CAMPOS DE SINCRONIZACIÓN
    @ColumnInfo(name = "firestore_id")
    val firestoreId: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null
)
