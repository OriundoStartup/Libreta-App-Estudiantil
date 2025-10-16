package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface AnnotationRepository {

    /**
     * Crea una nueva anotación.
     *
     * @param teacherId ID del profesor que crea la anotación
     * @param studentId ID del estudiante sobre quien es la anotación
     * @param classId ID de la clase (opcional según tu modelo)
     * @param type Tipo de anotación (POSITIVE, NEGATIVE, NEUTRAL, GENERAL)
     * @param subject Asunto o título de la anotación
     * @param description Descripción detallada
     * @return ApiResult con la anotación creada o un error
     */
    suspend fun createAnnotation(
        teacherId: Int,
        studentId: Int,
        classId: Int,
        type: AnnotationType,
        subject: String,
        description: String
    ): ApiResult<AnnotationEntity>

    /**
     * Obtiene todas las anotaciones de un estudiante.
     */
    fun getAnnotationsByStudent(studentId: Int): Flow<List<AnnotationEntity>>

    /**
     * Obtiene todas las anotaciones de una clase.
     */
    fun getAnnotationsByClass(classId: Int): Flow<List<AnnotationEntity>>

    /**
     * Obtiene todas las anotaciones creadas por un profesor.
     */
    fun getAnnotationsByTeacher(teacherId: Int): Flow<List<AnnotationEntity>>

    /**
     * Marca una anotación como leída.
     */
    suspend fun markAsRead(annotationId: Int): ApiResult<Unit>

    /**
     * Obtiene anotaciones no leídas para un apoderado.
     */
    fun getUnreadAnnotationsForParent(parentId: Int): Flow<List<AnnotationEntity>>
}