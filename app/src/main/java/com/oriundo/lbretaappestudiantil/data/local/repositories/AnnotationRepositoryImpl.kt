package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.AnnotationDao
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnotationRepositoryImpl @Inject constructor(
    private val annotationDao: AnnotationDao
) : AnnotationRepository {

    // CORRECCIÓN: El tipo de retorno debe ser el estándar ApiResult<AnnotationEntity>
    override suspend fun createAnnotation(
        teacherId: Int,
        studentId: Int,
        classId: Int,
        type: AnnotationType,
        subject: String,
        description: String
    ): ApiResult<AnnotationEntity> { // 👈 Cambiado de Any a ApiResult<AnnotationEntity>
        return try {
            // 1. Validación de campos
            if (subject.isBlank()) {
                return ApiResult.Error("El título es requerido")
            }
            if (description.isBlank()) {
                return ApiResult.Error("La descripción es requerida")
            }

            // 2. Creación de la entidad
            val newAnnotation = AnnotationEntity(
                teacherId = teacherId,
                studentId = studentId,
                title = subject.trim(),
                description = description.trim(),
                type = type
            )

            // 3. Inserción
            val insertedId = annotationDao.insertAnnotation(newAnnotation).toInt()

            // 4. Retornar la entidad con el ID generado
            ApiResult.Success(newAnnotation.copy(id = insertedId))
        } catch (e: Exception) {
            ApiResult.Error("Error al crear anotación: ${e.message}", e)
        }
    }

    // Estos métodos ya estaban correctos y usan override
    override fun getAnnotationsByClass(classId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByClass(classId)
    }

    override fun getUnreadAnnotationsForParent(parentId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getUnreadAnnotationsForParent(parentId)
    }

    override suspend fun markAsRead(annotationId: Int): ApiResult<Unit> {
        return try {
            annotationDao.markAsRead(annotationId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al marcar como leída: ${e.message}", e)
        }
    }

    override fun getAnnotationsByTeacher(teacherId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByTeacher(teacherId)
    }

    override fun getAnnotationsByStudent(studentId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByStudent(studentId)
    }

    // Asumimos que estos métodos NO están en la interfaz, por eso no usan 'override'.
    suspend fun updateAnnotation(annotation: AnnotationEntity): ApiResult<Unit> {
        return try {
            annotationDao.updateAnnotation(annotation)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar anotación: ${e.message}", e)
        }
    }

    suspend fun deleteAnnotation(annotation: AnnotationEntity): ApiResult<Unit> {
        return try {
            annotationDao.deleteAnnotation(annotation)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al eliminar anotación: ${e.message}", e)
        }
    }

    suspend fun getUnreadCount(studentId: Int): Int {
        return annotationDao.getUnreadCount(studentId)
    }
}