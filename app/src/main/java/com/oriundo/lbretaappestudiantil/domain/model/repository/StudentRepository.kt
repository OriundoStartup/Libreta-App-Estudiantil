package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    suspend fun registerStudent(
        classId: Int,
        rut: String,
        firstName: String,
        lastName: String,
        birthDate: Long? = null
    ): ApiResult<StudentEntity>
    suspend fun getStudentById(studentId: Int): ApiResult<StudentEntity>
    suspend fun getStudentByRut(rut: String): ApiResult<StudentEntity>
    fun getStudentsByClass(classId: Int): Flow<List<StudentEntity>>
    fun getStudentsByParent(parentId: Int): Flow<ApiResult<List<StudentWithClass>>>
    suspend fun updateStudent(student: StudentEntity): ApiResult<Unit>
    suspend fun linkParentToStudent(
        studentId: Int,
        parentId: Int,
        relationshipType: RelationshipType,
        isPrimary: Boolean
    ): ApiResult<Unit>
    fun getParentsByStudent(studentId: Int): Flow<List<ProfileEntity>>
    // ✅ NUEVO - Para obtener todos los estudiantes con sus clases (selector de mensajes)
    fun getAllStudentsWithClass(): Flow<List<StudentWithClass>>
}