package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentWithClassAndParentDto
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: StudentEntity): Long

    @Query("SELECT * FROM students WHERE class_id = :classId AND is_active = 1")
    fun getStudentsByClass(classId: Int): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: Int): StudentEntity?

    @Query("SELECT * FROM students WHERE rut = :rut LIMIT 1")
    suspend fun getStudentByRut(rut: String): StudentEntity?

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("SELECT COUNT(*) FROM students WHERE rut = :rut")
    suspend fun rutExists(rut: String): Int

    @Query("SELECT * FROM students WHERE firestore_id = :firestoreId LIMIT 1")
    suspend fun getStudentByFirestoreId(firestoreId: String): StudentEntity?

    // Obtener estudiantes por apoderado
    @Query("""
        SELECT s.* FROM students s
        INNER JOIN student_parent_relation spr ON s.id = spr.student_id
        WHERE spr.parent_id = :parentId
    """)
    fun getStudentsByParent(parentId: Int): Flow<List<StudentEntity>>

    // ⭐ NUEVO: Actualizar primaryParentId directamente
    @Query("UPDATE students SET primary_parent_id = :parentId WHERE id = :studentId")
    suspend fun updatePrimaryParent(studentId: Int, parentId: Int?)

    // ⭐ MEJORADO: Ahora usa el campo primaryParentId de la tabla students
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT 
            s.id,
            s.class_id,
            s.rut,
            s.first_name,
            s.last_name,
            s.birth_date,
            s.photo_url,
            s.enrollment_date,
            s.is_active,
            s.notes,
            s.primary_parent_id,
            c.id as class_id,
            c.class_name as class_name,
            c.school_name as school_name,
            c.class_code as class_code,
            c.teacher_id as teacher_id,
            s.primary_parent_id as primary_parent_id
        FROM students s
        INNER JOIN classes c ON s.class_id = c.id
        WHERE s.is_active = 1
        ORDER BY c.class_name, s.last_name, s.first_name
    """)
    fun getAllStudentsWithClassAndParent(): Flow<List<StudentWithClassAndParentDto>>

    /**
     * Obtiene estudiantes por clase con su apoderado principal
     */
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT 
            s.id,
            s.class_id,
            s.rut,
            s.first_name,
            s.last_name,
            s.birth_date,
            s.photo_url,
            s.enrollment_date,
            s.is_active,
            s.notes,
            s.primary_parent_id,
            c.class_name,
            c.school_name,
            c.class_code,
            c.teacher_id,
            s.primary_parent_id as primary_parent_id
        FROM students s
        INNER JOIN classes c ON s.class_id = c.id
        WHERE s.class_id = :classId AND s.is_active = 1
        ORDER BY s.last_name, s.first_name
    """)
    fun getStudentsByClassWithParent(classId: Int): Flow<List<StudentWithClassAndParentDto>>
}