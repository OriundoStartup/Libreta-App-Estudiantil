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

    @Query("SELECT * FROM students WHERE classId = :classId AND isActive = 1")
    fun getStudentsByClass(classId: Int): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: Int): StudentEntity?

    @Query("SELECT * FROM students WHERE rut = :rut LIMIT 1")
    suspend fun getStudentByRut(rut: String): StudentEntity?

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("SELECT COUNT(*) FROM students WHERE rut = :rut")
    suspend fun rutExists(rut: String): Int

    @Query("SELECT * FROM students WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getStudentByFirestoreId(firestoreId: String): StudentEntity?

    @Query("SELECT students.* FROM students JOIN classes ON students.classId = classes.id WHERE classes.class_code IN (:classCodes)")
    suspend fun getStudentsByClassCodes(classCodes: List<String>): List<StudentEntity>

    // Obtener estudiantes por apoderado
    @Query("""
        SELECT s.* FROM students s
        INNER JOIN student_parent_relation spr ON s.id = spr.student_id
        WHERE spr.parent_id = :parentId
    """)
    fun getStudentsByParent(parentId: Int): Flow<List<StudentEntity>>

    @Query("""
        SELECT s.* FROM students s
        INNER JOIN student_parent_relation spr ON s.id = spr.student_id
        WHERE spr.parent_id = :parentId
    """)
    suspend fun getStudentsByParentId(parentId: Int): List<StudentEntity>

    // ⭐ NUEVO: Actualizar primaryParentId directamente
    @Query("UPDATE students SET primaryParentId = :parentId WHERE id = :studentId")
    suspend fun updatePrimaryParent(studentId: Int, parentId: Int?)

    // ⭐ CORREGIDO Y SIMPLIFICADO
    @Query("""
        SELECT 
            s.*, -- Selecciona todas las columnas de la tabla students para el @Embedded
            c.class_name as className,
            c.school_name as schoolName,
            c.class_code as classCode,
            c.teacher_id as teacherId
        FROM students s
        INNER JOIN classes c ON s.classId = c.id
        WHERE s.isActive = 1
        ORDER BY c.class_name, s.lastName, s.firstName
    """)
    fun getAllStudentsWithClassAndParent(): Flow<List<StudentWithClassAndParentDto>>

    /**
     * Obtiene estudiantes por clase con su apoderado principal
     */
    // ⭐ CORREGIDO Y SIMPLIFICADO
    @Query("""
        SELECT 
            s.*, -- Selecciona todas las columnas de la tabla students para el @Embedded
            c.class_name as className,
            c.school_name as schoolName,
            c.class_code as classCode,
            c.teacher_id as teacherId
        FROM students s
        INNER JOIN classes c ON s.classId = c.id
        WHERE s.classId = :classId AND s.isActive = 1
        ORDER BY s.lastName, s.firstName
    """)
    fun getStudentsByClassWithParent(classId: Int): Flow<List<StudentWithClassAndParentDto>>


}
