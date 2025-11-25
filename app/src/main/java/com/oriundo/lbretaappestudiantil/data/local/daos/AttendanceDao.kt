package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<AttendanceEntity>)



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE id = :attendanceId")
    suspend fun getAttendanceById(attendanceId: Int): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY attendanceDate DESC")
    fun getAttendanceByStudent(studentId: Int): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT * FROM attendance 
        WHERE studentId = :studentId 
        AND attendanceDate >= :startDate 
        AND attendanceDate <= :endDate
        ORDER BY attendanceDate DESC
    """)
    fun getAttendanceByDateRange(
        studentId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT * FROM attendance 
        WHERE attendanceDate = :date 
        ORDER BY studentId
    """)
    fun getAttendanceByDate(date: Long): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT a.* FROM attendance a
        INNER JOIN students s ON a.studentId = s.id
        WHERE s.classId = :classId AND a.attendanceDate = :date
        ORDER BY s.firstName, s.lastName
    """)
    fun getAttendanceByClassAndDate(classId: Int, date: Long): Flow<List<AttendanceEntity>>

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND attendanceDate = :date")
    suspend fun deleteAttendanceByStudentAndDate(studentId: Int, date: Long)

    @Query("SELECT * FROM attendance WHERE teacherId = :teacherId")
    fun getAttendanceByTeacher(teacherId: Int): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT * FROM attendance 
        WHERE studentId = :studentId 
        AND attendanceDate = :date
    """)
    suspend fun getAttendanceByStudentAndDate(studentId: Int, date: Long): AttendanceEntity?

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE studentId = :studentId 
        AND status = :status
    """)
    suspend fun countAttendanceByStatus(studentId: Int, status: AttendanceStatus): Int

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteAttendancesByStudent(studentId: Int)

    @Query("""
        SELECT * FROM attendance 
        WHERE firestoreId IS NULL OR syncStatus = 'PENDING'
    """)
    suspend fun getUnsyncedAttendance(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE firestoreId = :firestoreId")
    suspend fun getAttendanceByFirestoreId(firestoreId: String): AttendanceEntity?
}
