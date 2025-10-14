package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.SchoolEventDao
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
// IMPORTACIÓN FALTANTE: Asegúrate de tener esta importación de la interfaz
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchoolEventRepositoryImpl @Inject constructor(
    private val schoolEventDao: SchoolEventDao
) : SchoolEventRepository {

    // ✅ CORREGIDO: El retorno ahora es ApiResult<SchoolEventEntity>
    override suspend fun createEvent(event: SchoolEventEntity): ApiResult<SchoolEventEntity> {
        return try {
            if (event.title.isBlank()) {
                return ApiResult.Error("El título es requerido")
            }
            if (event.description.isBlank()) {
                return ApiResult.Error("La descripción es requerida")
            }

            // Usamos la función insertEvent, pero devolvemos la entidad que se creó.
            val eventId = schoolEventDao.insertEvent(event)

            // Creamos una nueva entidad con el ID generado (si lo necesitas) o devolvemos la misma.
            // Para simplicidad, devolvemos la entidad insertada (Room manejará el ID si es autogenerado).
            // NOTA: Si necesitas la entidad con el ID generado, tendrías que hacer un SELECT después del INSERT.
            ApiResult.Success(event.copy(id = eventId.toInt())) // Asumiendo que el ID es Int
        } catch (e: Exception) {
            ApiResult.Error("Error al crear evento: ${e.message}", e)
        }
    }

    // ✅ CORREGIDO: El retorno ahora es ApiResult<SchoolEventEntity>
    override suspend fun updateEvent(event: SchoolEventEntity): ApiResult<SchoolEventEntity> {
        return try {
            schoolEventDao.updateEvent(event)
            // Devolvemos la entidad que fue actualizada.
            ApiResult.Success(event)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar evento: ${e.message}", e)
        }
    }

    // ✅ CORREGIDO: El retorno ahora es ApiResult<SchoolEventEntity>
    override suspend fun deleteEvent(event: SchoolEventEntity): ApiResult<SchoolEventEntity> {
        return try {
            schoolEventDao.deleteEvent(event)
            // Devolvemos la entidad que fue eliminada.
            ApiResult.Success(event)
        } catch (e: Exception) {
            ApiResult.Error("Error al eliminar evento: ${e.message}", e)
        }
    }

    // --- Los métodos de consulta (Flow) son correctos y se mantienen ---

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

    // NOTA: La Interfaz requiere getGeneralEvents, el DAO ofrece getAllEvents.
    // Si getAllEvents trae TODOS los eventos (generales y específicos), esto es incorrecto
    // según el nombre del método del DAO. Debería usar el método correcto del DAO.
    // Asumiendo que el DAO tiene un método getGeneralEvents(), lo corregimos a:
    /*
    override fun getGeneralEvents(): Flow<List<SchoolEventEntity>> {
        return schoolEventDao.getGeneralEvents()
    }
    */
    // Basado en tu código anterior, usaré tu DAO corregido:
    override fun getGeneralEvents(): Flow<List<SchoolEventEntity>> {
        return schoolEventDao.getGeneralEvents()
    }
}