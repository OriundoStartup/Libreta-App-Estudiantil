package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    // MÉTODO CORREGIDO: Se añadió la anotación @Query
    // Usa la tabla de relación 'student_parent_relation'.
    @Query("""
        SELECT s.* FROM students s
        INNER JOIN student_parent_relation spr ON s.id = spr.student_id
        WHERE spr.parent_id = :parentId
    """)
    fun getStudentsByParent(parentId: Int): Flow<List<StudentEntity>>
    // ✅ NUEVO - Para obtener todos los estudiantes con clase y apoderado principal
    @Query("""
    SELECT 
        s.*,
        c.id as class_id,
        c.class_name as class_name,
        c.school_name as school_name,
        c.class_code as class_code,
        c.teacher_id as teacher_id,
        spr.parent_id as primary_parent_id
    FROM students s
    INNER JOIN classes c ON s.class_id = c.id
    LEFT JOIN student_parent_relation spr ON s.id = spr.student_id AND spr.is_primary = 1
    WHERE s.is_active = 1
    ORDER BY c.class_name, s.last_name, s.first_name
""")
    fun getAllStudentsWithClassAndParent(): Flow<List<StudentWithClassAndParentDto>>

    // MÉTODOS ELIMINADOS: linkParentToStudent y registerStudent
    // Estos deben ser implementados en el StudentRepository, no aquí.
}