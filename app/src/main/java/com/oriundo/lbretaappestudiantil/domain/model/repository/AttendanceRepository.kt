package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    suspend fun recordAttendance(
        studentId: Int,
        teacherId: Int,
        date: Long,
        status: AttendanceStatus,
        note: String? = null
    ): ApiResult<AttendanceEntity>
    fun getAttendanceByStudent(studentId: Int): Flow<List<AttendanceEntity>>
    fun getAttendanceByDateRange(
        studentId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<AttendanceEntity>>
    suspend fun updateAttendance(attendance: AttendanceEntity): ApiResult<Unit>
}