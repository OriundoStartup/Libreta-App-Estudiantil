package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface SchoolEventRepository {
    // Operaciones de Modificación
    suspend fun createEvent(event: SchoolEventEntity): ApiResult<SchoolEventEntity>
    suspend fun updateEvent(event: SchoolEventEntity): ApiResult<SchoolEventEntity>
    suspend fun deleteEvent(event: SchoolEventEntity): ApiResult<SchoolEventEntity>

    // Operaciones de Consulta (Flujo)
    fun getEventsByTeacher(teacherId: Int): Flow<List<SchoolEventEntity>>
    fun getEventsByClass(classId: Int): Flow<List<SchoolEventEntity>>
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<SchoolEventEntity>>
    fun getEventsByTeacherAndDateRange(
        teacherId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<SchoolEventEntity>>

    // Método para el Dashboard (Eventos no asociados a una clase específica)
    fun getGeneralEvents(): Flow<List<SchoolEventEntity>>
}