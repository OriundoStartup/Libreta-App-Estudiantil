package com.oriundo.lbretaappestudiantil.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Relación Many-to-Many entre estudiantes y apoderados
 * Un estudiante puede tener varios apoderados (mamá, papá, tutor)
 */
@Entity(
    tableName = "student_parent_relation",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["student_id"]),
        Index(value = ["parent_id"]),
        Index(value = ["student_id", "parent_id"], unique = true)
    ]
)
data class StudentParentRelation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "student_id")
    val studentId: Int,

    @ColumnInfo(name = "parent_id")
    val parentId: Int,

    @ColumnInfo(name = "relationship_type")
    val relationshipType: RelationshipType,

    @ColumnInfo(name = "is_primary")
    val isPrimary: Boolean = false, // Apoderado principal

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

enum class RelationshipType {
    FATHER,   // Padre
    MOTHER,   // Madre
    GUARDIAN, // Tutor legal
    OTHER     // Otro
}