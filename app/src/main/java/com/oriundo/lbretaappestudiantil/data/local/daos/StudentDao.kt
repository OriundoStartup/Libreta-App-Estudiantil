package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
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

    // MÉTODOS ELIMINADOS: linkParentToStudent y registerStudent
    // Estos deben ser implementados en el StudentRepository, no aquí.
}