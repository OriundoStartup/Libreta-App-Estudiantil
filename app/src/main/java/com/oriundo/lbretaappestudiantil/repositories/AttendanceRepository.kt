package com.oriundo.lbretaappestudiantil.repositories

import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    suspend fun recordAttendance(attendance: AttendanceEntity): ApiResult<Long>
    suspend fun recordMultipleAttendance(attendances: List<AttendanceEntity>): ApiResult<Unit>
    suspend fun updateAttendance(attendance: AttendanceEntity): ApiResult<Unit>
    fun getAttendanceByStudent(studentId: Int): Flow<List<AttendanceEntity>>
    fun getAttendanceByClassAndDate(classId: Int, date: Long): Flow<List<AttendanceEntity>>
    fun getAttendanceByDateRange(studentId: Int, startDate: Long, endDate: Long): Flow<List<AttendanceEntity>>
}