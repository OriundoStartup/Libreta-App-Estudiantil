package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.AnnotationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnnotationRepositoryImpl @Inject constructor(
    private val annotationDao: AnnotationDao,
    private val profileDao: ProfileDao,
    private val studentDao: StudentDao
) : AnnotationRepository {

    override suspend fun createAnnotation(
        teacherId: Int,
        studentId: Int,
        classId: Int,
        type: AnnotationType,
        subject: String,
        description: String
    ): ApiResult<AnnotationEntity> {
        return try {
            // ✅ VALIDAR que el profesor existe
            profileDao.getProfileById(teacherId)
                ?: return ApiResult.Error("Error: El profesor no se encuentra sincronizado. Cierra sesión y vuelve a iniciar.")

            // ✅ VALIDAR que el estudiante existe
            studentDao.getStudentById(studentId)
                ?: return ApiResult.Error("Error: El estudiante no se encuentra en la base de datos local.")

            val annotation = AnnotationEntity(
                studentId = studentId,
                teacherId = teacherId,
                title = subject,
                description = description,
                type = type,
                date = System.currentTimeMillis(),
                isRead = false
            )

            val id = annotationDao.insertAnnotation(annotation)
            val createdAnnotation = annotation.copy(id = id.toInt())

            ApiResult.Success(createdAnnotation)
        } catch (e: Exception) {
            ApiResult.Error("Error al crear la anotación: ${e.message}")
        }
    }

    override fun getAnnotationsByStudent(studentId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByStudent(studentId)
    }

    override fun getAnnotationsByClass(classId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByClass(classId)
    }

    override fun getAnnotationsByTeacher(teacherId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByTeacher(teacherId)
    }

    override suspend fun markAsRead(annotationId: Int): ApiResult<Unit> {
        return try {
            annotationDao.markAsRead(annotationId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error al marcar como leída")
        }
    }

    override fun getUnreadAnnotationsForParent(parentId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getUnreadAnnotationsForParent(parentId)
    }
}