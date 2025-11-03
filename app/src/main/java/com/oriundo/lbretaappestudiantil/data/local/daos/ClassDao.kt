package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Query("SELECT * FROM classes WHERE id = :classId")
    suspend fun getClassById(classId: Int): ClassEntity?

    // ✅ CORREGIDO: Case-insensitive
    @Query("SELECT * FROM classes WHERE UPPER(class_code) = UPPER(:code) AND is_active = 1 LIMIT 1")
    suspend fun getClassByCode(code: String): ClassEntity?

    @Query("SELECT * FROM classes WHERE teacher_id = :teacherId AND is_active = 1 ORDER BY created_at DESC")
    fun getClassesByTeacher(teacherId: Int): Flow<List<ClassEntity>>

    // ✅ CORREGIDO: Case-insensitive también aquí
    @Query("SELECT COUNT(*) FROM classes WHERE UPPER(class_code) = UPPER(:code)")
    suspend fun codeExists(code: String): Int



    @Query("DELETE FROM classes WHERE id = :classId")
    suspend fun deleteClass(classId: Int)

    // --- AGREGA ESTA FUNCIÓN ---
    @Query("SELECT * FROM classes")
    suspend fun getAllClasses(): List<ClassEntity>

}