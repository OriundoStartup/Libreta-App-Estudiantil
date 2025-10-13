package com.oriundo.lbretaappestudiantil.repositories

import com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RequestStatus
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface MaterialRequestRepository {
    suspend fun createRequest(request: MaterialRequestEntity): ApiResult<Long>
    suspend fun updateRequest(request: MaterialRequestEntity): ApiResult<Unit>
    suspend fun deleteRequest(request: MaterialRequestEntity): ApiResult<Unit>
    suspend fun updateRequestStatus(requestId: Int, status: RequestStatus): ApiResult<Unit>
    fun getRequestsByTeacher(teacherId: Int): Flow<List<MaterialRequestEntity>>
    fun getRequestsByClass(classId: Int): Flow<List<MaterialRequestEntity>>
    fun getRequestsByStudent(studentId: Int): Flow<List<MaterialRequestEntity>>
}