package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE student_id = :studentId ORDER BY attendance_date DESC")
    fun getAttendanceByStudent(studentId: Int): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT * FROM attendance 
        WHERE student_id = :studentId 
        AND attendance_date >= :startDate 
        AND attendance_date <= :endDate
        ORDER BY attendance_date DESC
    """)
    fun getAttendanceByDateRange(
        studentId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT * FROM attendance 
        WHERE attendance_date = :date 
        ORDER BY student_id
    """)
    fun getAttendanceByDate(date: Long): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT a.* FROM attendance a
        INNER JOIN students s ON a.student_id = s.id
        WHERE s.class_id = :classId AND a.attendance_date = :date
        ORDER BY s.first_name, s.last_name
    """)
    fun getAttendanceByClassAndDate(classId: Int, date: Long): Flow<List<AttendanceEntity>>

    @Query("DELETE FROM attendance WHERE student_id = :studentId AND attendance_date = :date")
    suspend fun deleteAttendanceByStudentAndDate(studentId: Int, date: Long)
}