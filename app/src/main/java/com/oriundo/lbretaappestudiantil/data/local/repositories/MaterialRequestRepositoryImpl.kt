package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.MaterialRequestDao
import com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RequestStatus
import com.oriundo.lbretaappestudiantil.data.local.models.UrgencyLevel
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.MaterialRequestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialRequestRepositoryImpl @Inject constructor(
    private val materialRequestDao: MaterialRequestDao
) : MaterialRequestRepository {

    override suspend fun createRequest(
        teacherId: Int,
        classId: Int,
        studentId: Int?,
        material: String,
        quantity: Int,
        urgency: UrgencyLevel,
        deadlineDate: Long?
    ): ApiResult<MaterialRequestEntity> {
        return try {
            val request = MaterialRequestEntity(
                teacherId = teacherId,
                classId = classId,
                studentId = studentId,
                material = material,
                quantity = quantity,
                urgency = urgency,
                deadlineDate = deadlineDate,
                // ✅ CORRECCIÓN 1: Asignar el estado inicial PENDING (ya que no viene en los parámetros)
                status = RequestStatus.PENDING
            )

            if (request.material.isBlank()) {
                return ApiResult.Error("El material es requerido")
            }
            if (request.quantity <= 0) {
                return ApiResult.Error("La cantidad debe ser mayor a 0")
            }

            materialRequestDao.insertRequest(request)
            ApiResult.Success(request)
        } catch (e: Exception) {
            ApiResult.Error("Error al crear solicitud: ${e.message}", e)
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

    // ✅ CORRECCIÓN 2: Implementación de deleteRequest usando la nueva función del DAO
    override suspend fun deleteRequest(requestId: Int): ApiResult<Unit> {
        return try {
            materialRequestDao.deleteRequestById(requestId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al eliminar solicitud: ${e.message}", e)
        }
    }

    // ✅ CORRECCIÓN 3: Implementación de los métodos Flow (que faltaban en tu implementación)

    override fun getRequestsByClass(classId: Int): Flow<List<MaterialRequestEntity>> {
        return materialRequestDao.getRequestsByClass(classId)
    }

    override fun getRequestsByStudent(studentId: Int): Flow<List<MaterialRequestEntity>> {
        return materialRequestDao.getRequestsByStudent(studentId)
    }

    override fun getRequestsForParent(parentId: Int): Flow<List<MaterialRequestEntity>> {
        // Usamos parentId como studentId, ya que así lo definimos en el DAO.
        return materialRequestDao.getRequestsForParent(parentId)
    }
}