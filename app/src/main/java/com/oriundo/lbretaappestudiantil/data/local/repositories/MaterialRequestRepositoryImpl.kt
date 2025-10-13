package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.MaterialRequestDao
import com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RequestStatus
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.repositories.MaterialRequestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialRequestRepositoryImpl @Inject constructor(
    private val materialRequestDao: MaterialRequestDao
) : MaterialRequestRepository {

    override suspend fun createRequest(request: MaterialRequestEntity): ApiResult<Long> {
        return try {
            if (request.material.isBlank()) {
                return ApiResult.Error("El material es requerido")
            }
            if (request.quantity <= 0) {
                return ApiResult.Error("La cantidad debe ser mayor a 0")
            }
            val id = materialRequestDao.insertRequest(request)
            ApiResult.Success(id)
        } catch (e: Exception) {
            ApiResult.Error("Error al crear solicitud: ${e.message}", e)
        }
    }

    override suspend fun updateRequest(request: MaterialRequestEntity): ApiResult<Unit> {
        return try {
            materialRequestDao.updateRequest(request)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar solicitud: ${e.message}", e)
        }
    }

    override suspend fun deleteRequest(request: MaterialRequestEntity): ApiResult<Unit> {
        return try {
            materialRequestDao.deleteRequest(request)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al eliminar solicitud: ${e.message}", e)
        }
    }

    override suspend fun updateRequestStatus(requestId: Int, status: RequestStatus): ApiResult<Unit> {
        return try {
            materialRequestDao.updateRequestStatus(requestId, status)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar estado: ${e.message}", e)
        }
    }

    override fun getRequestsByTeacher(teacherId: Int): Flow<List<MaterialRequestEntity>> {
        return materialRequestDao.getRequestsByTeacher(teacherId)
    }

    override fun getRequestsByClass(classId: Int): Flow<List<MaterialRequestEntity>> {
        return materialRequestDao.getRequestsByClass(classId)
    }

    override fun getRequestsByStudent(studentId: Int): Flow<List<MaterialRequestEntity>> {
        return materialRequestDao.getRequestsByStudent(studentId)
    }
}