package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.SchoolEventDao
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.repositories.SchoolEventRepository // 👈 IMPORTACIÓN FALTANTE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchoolEventRepositoryImpl @Inject constructor(
    private val schoolEventDao: SchoolEventDao
) : SchoolEventRepository {

    override suspend fun createEvent(event: SchoolEventEntity): ApiResult<Long> {
        return try {
            if (event.title.isBlank()) {
                return ApiResult.Error("El título es requerido")
            }
            if (event.description.isBlank()) {
                return ApiResult.Error("La descripción es requerida")
            }

            val eventId = schoolEventDao.insertEvent(event)
            ApiResult.Success(eventId)
        } catch (e: Exception) {
            ApiResult.Error("Error al crear evento: ${e.message}", e)
        }
    }

    override suspend fun updateEvent(event: SchoolEventEntity): ApiResult<Unit> {
        return try {
            schoolEventDao.updateEvent(event)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar evento: ${e.message}", e)
        }
    }

    override suspend fun deleteEvent(event: SchoolEventEntity): ApiResult<Unit> {
        return try {
            schoolEventDao.deleteEvent(event)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al eliminar evento: ${e.message}", e)
        }
    }

    override fun getEventsByTeacher(teacherId: Int): Flow<List<SchoolEventEntity>> {
        return schoolEventDao.getEventsByTeacher(teacherId)
    }

    override fun getEventsByClass(classId: Int): Flow<List<SchoolEventEntity>> {
        return schoolEventDao.getEventsByClass(classId)
    }

    override fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<SchoolEventEntity>> {
        return schoolEventDao.getEventsByDateRange(startDate, endDate)
    }

    override fun getEventsByTeacherAndDateRange(
        teacherId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<SchoolEventEntity>> {
        return schoolEventDao.getEventsByTeacherAndDateRange(teacherId, startDate, endDate)
    }

    override fun getGeneralEvents(): Flow<List<SchoolEventEntity>> {
        return schoolEventDao.getAllEvents()
    }
}