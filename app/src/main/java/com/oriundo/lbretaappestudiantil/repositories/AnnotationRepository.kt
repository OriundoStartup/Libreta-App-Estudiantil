package com.oriundo.lbretaappestudiantil.repositories

import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface AnnotationRepository {
    suspend fun createAnnotation(annotation: AnnotationEntity): ApiResult<Long>
    suspend fun updateAnnotation(annotation: AnnotationEntity): ApiResult<Unit>
    suspend fun deleteAnnotation(annotation: AnnotationEntity): ApiResult<Unit>
    suspend fun markAsRead(annotationId: Int): ApiResult<Unit>
    fun getAnnotationsByTeacher(teacherId: Int): Flow<List<AnnotationEntity>>
    fun getAnnotationsByStudent(studentId: Int): Flow<List<AnnotationEntity>>
    suspend fun getUnreadCount(studentId: Int): Int
}