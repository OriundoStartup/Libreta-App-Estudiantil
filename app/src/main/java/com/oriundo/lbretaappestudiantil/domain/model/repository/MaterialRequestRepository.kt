package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RequestStatus
import com.oriundo.lbretaappestudiantil.data.local.models.UrgencyLevel
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface MaterialRequestRepository {
    suspend fun createRequest(
        teacherId: Int,
        classId: Int,
        studentId: Int?,
        material: String,
        quantity: Int,
        urgency: UrgencyLevel,
        deadlineDate: Long?
    ): ApiResult<MaterialRequestEntity>
    fun getRequestsByClass(classId: Int): Flow<List<MaterialRequestEntity>>
    fun getRequestsByStudent(studentId: Int): Flow<List<MaterialRequestEntity>>
    fun getRequestsForParent(parentId: Int): Flow<List<MaterialRequestEntity>>
    suspend fun updateRequestStatus(requestId: Int, status: RequestStatus): ApiResult<Unit>
    suspend fun deleteRequest(requestId: Int): ApiResult<Unit>

}