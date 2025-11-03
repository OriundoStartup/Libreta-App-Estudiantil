package com.oriundo.lbretaappestudiantil.domain.model.repository

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
    ): ApiResult<Pair<ClassEntity, String>>  // ← Retorna ClassEntity y el código generado

    suspend fun getClassById(classId: Int): ApiResult<ClassEntity>
    suspend fun getClassByCode(classCode: String): ApiResult<ClassEntity>

    suspend fun getAndSyncClassByCodeFromRemote(classCode: String): ApiResult<ClassEntity>
    fun getClassesByTeacher(teacherId: Int): Flow<List<ClassEntity>>
    suspend fun updateClass(classEntity: ClassEntity): ApiResult<Unit>
    /**
     * Busca la clase en la base de datos remota (Firebase) por código,
     * la guarda localmente (Room) y luego la devuelve.
     */

}