package com.oriundo.lbretaappestudiantil.repositories

import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface ClassRepository {
    suspend fun createClass(
        className: String,
        schoolName: String,
        teacherId: Int,
        gradeLevel: String?,
        academicYear: Int
    ): ApiResult<ClassEntity>
    suspend fun getClassById(classId: Int): Result<ClassEntity>
    suspend fun getClassByCode(classCode: String): Result<ClassEntity>
    fun getClassesByTeacher(teacherId: Int): Flow<List<ClassEntity>>
    suspend fun updateClass(classEntity: ClassEntity): Result<Unit>
    suspend fun deactivateClass(classId: Int): Result<Unit>
    suspend fun generateUniqueClassCode(): String
}