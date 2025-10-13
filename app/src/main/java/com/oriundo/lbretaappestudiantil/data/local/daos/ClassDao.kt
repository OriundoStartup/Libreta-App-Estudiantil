package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.*
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

    @Query("SELECT * FROM classes WHERE class_code = :code AND is_active = 1 LIMIT 1")
    suspend fun getClassByCode(code: String): ClassEntity?

    @Query("SELECT * FROM classes WHERE teacher_id = :teacherId AND is_active = 1 ORDER BY created_at DESC")
    fun getClassesByTeacher(teacherId: Int): Flow<List<ClassEntity>>

    @Query("SELECT COUNT(*) FROM classes WHERE class_code = :code")
    suspend fun codeExists(code: String): Int

    @Query("DELETE FROM classes WHERE id = :classId")
    suspend fun deleteClass(classId: Int)
}