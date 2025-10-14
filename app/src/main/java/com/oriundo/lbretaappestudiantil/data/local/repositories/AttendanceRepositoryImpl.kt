package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.AttendanceDao
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao
) : AttendanceRepository {

    // ✅ CORREGIDO: Firma adaptada a la Interfaz, construyendo la Entidad aquí.
    override suspend fun recordAttendance(
        studentId: Int,
        teacherId: Int,
        date: Long,
        status: AttendanceStatus,
        note: String?
    ): ApiResult<AttendanceEntity> {
        return try {
            // Construir la Entidad para la inserción en la capa de datos.
            val attendance = AttendanceEntity(
                studentId = studentId,
                teacherId = teacherId,
                attendanceDate = date,
                status = status,
                note = note
            )

            attendanceDao.insertAttendance(attendance)
            // Retornar la entidad para que el ViewModel la use en el estado Success.
            ApiResult.Success(attendance)
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar asistencia: ${e.message}", e)
        }
    }

    // ❌ Este método en tu Repositorio Impl original está mal, pues la Interfaz no lo tiene.
    // Si lo necesitas, agrégalo a la Interfaz primero.
    // Por ahora, lo comento para eliminar el error.
    /*
    override suspend fun recordMultipleAttendance(attendances: List<AttendanceEntity>): ApiResult<Unit> {
        return try {
            attendanceDao.insertAttendances(attendances)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar asistencias: ${e.message}", e)
        }
    }
    */

    // ... El resto de los métodos (updateAttendance, getAttendanceByStudent, etc.) son correctos y se mantienen.

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

    override fun getAttendanceByDateRange(
        studentId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByDateRange(studentId, startDate, endDate)
    }

    // NOTA: Los métodos getAttendanceByClassAndDate y getAttendanceByDate no existen en la interfaz.
    // Si se necesitan en el ViewModel, debes agregarlos a AttendanceRepository.
}