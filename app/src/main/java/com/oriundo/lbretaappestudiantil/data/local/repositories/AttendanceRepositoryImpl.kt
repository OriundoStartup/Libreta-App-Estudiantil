package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.AttendanceDao
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.repositories.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao
) : AttendanceRepository {

    override suspend fun recordAttendance(attendance: AttendanceEntity): ApiResult<Long> {
        return try {
            val id = attendanceDao.insertAttendance(attendance)
            ApiResult.Success(id)
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar asistencia: ${e.message}", e)
        }
    }

    override suspend fun recordMultipleAttendance(attendances: List<AttendanceEntity>): ApiResult<Unit> {
        return try {
            attendanceDao.insertAttendances(attendances)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar asistencias: ${e.message}", e)
        }
    }

    override suspend fun updateAttendance(attendance: AttendanceEntity): ApiResult<Unit> {
        return try {
            attendanceDao.updateAttendance(attendance)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar asistencia: ${e.message}", e)
        }
    }

    override fun getAttendanceByStudent(studentId: Int): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByStudent(studentId)
    }

    override fun getAttendanceByClassAndDate(classId: Int, date: Long): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByClassAndDate(classId, date)
    }

    override fun getAttendanceByDateRange(
        studentId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByDateRange(studentId, startDate, endDate)
    }
}