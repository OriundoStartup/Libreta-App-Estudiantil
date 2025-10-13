package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface AnnotationRepository {
    suspend fun createAnnotation(
        teacherId: Int,
        studentId: Int,
        classId: Int,
        type: AnnotationType,
        subject: String,
        description: String
    ): Any
    fun getAnnotationsByStudent(studentId: Int): Flow<List<AnnotationEntity>>
    fun getAnnotationsByClass(classId: Int): Flow<List<AnnotationEntity>>
    fun getAnnotationsByTeacher(teacherId: Int): Flow<List<AnnotationEntity>>
    suspend fun markAsRead(annotationId: Int): ApiResult<Unit>
    fun getUnreadAnnotationsForParent(parentId: Int): Flow<List<AnnotationEntity>>
}