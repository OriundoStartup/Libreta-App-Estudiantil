package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "school_events",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["class_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["class_id"]),
        Index(value = ["teacher_id"]),
        Index(value = ["event_date"])
    ]
)
data class SchoolEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "class_id")
    val classId: Int?,

    @ColumnInfo(name = "teacher_id")
    val teacherId: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "event_date")
    val eventDate: Long,

    @ColumnInfo(name = "event_type")
    val eventType: EventType,

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
enum class EventType {
    TEST,           // Prueba
    ASSIGNMENT,     // Tarea
    PROJECT,        // Proyecto
    FIELD_TRIP,     // Salida pedagógica
    MEETING,        // Reunión
    HOLIDAY,        // Festivo
    OTHER           // Otro
}