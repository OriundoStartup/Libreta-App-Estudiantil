package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mensajes directos entre profesor y apoderado
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["sender_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipient_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sender_id"]),
        Index(value = ["recipient_id"]),
        Index(value = ["student_id"]),
        Index(value = ["sent_date"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "sender_id")
    val senderId: Int,

    @ColumnInfo(name = "recipient_id")
    val recipientId: Int,

    @ColumnInfo(name = "student_id")
    val studentId: Int? = null, // Contexto: sobre qué estudiante es el mensaje

    @ColumnInfo(name = "subject")
    val subject: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "sent_date")
    val sentDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "parent_message_id")
    val parentMessageId: Int? = null // Para hilos de conversación
)
