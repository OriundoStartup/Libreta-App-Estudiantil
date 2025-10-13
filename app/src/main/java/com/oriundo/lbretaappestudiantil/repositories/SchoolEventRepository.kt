package com.oriundo.lbretaappestudiantil.repositories


import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface SchoolEventRepository {
    suspend fun createEvent(event: SchoolEventEntity): ApiResult<Long>
    suspend fun updateEvent(event: SchoolEventEntity): ApiResult<Unit>
    suspend fun deleteEvent(event: SchoolEventEntity): ApiResult<Unit>
    fun getEventsByTeacher(teacherId: Int): Flow<List<SchoolEventEntity>>
    fun getEventsByClass(classId: Int): Flow<List<SchoolEventEntity>>
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<SchoolEventEntity>>
    fun getEventsByTeacherAndDateRange(teacherId: Int, startDate: Long, endDate: Long): Flow<List<SchoolEventEntity>>
    fun getGeneralEvents(): Flow<List<SchoolEventEntity>>
}