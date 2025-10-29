package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolEventDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(event: SchoolEventEntity): Long

    @Update
    suspend fun updateEvent(event: SchoolEventEntity)

    @Delete
    suspend fun deleteEvent(event: SchoolEventEntity)

    @Query("SELECT * FROM school_events WHERE id = :id")
    suspend fun getEventById(id: Int): SchoolEventEntity?

    // Eventos generales (sin clase específica)
    @Query("SELECT * FROM school_events WHERE class_id IS NULL ORDER BY event_date ASC")
    fun getGeneralEvents(): Flow<List<SchoolEventEntity>>

    // Eventos por profesor
    @Query("SELECT * FROM school_events WHERE teacher_id = :teacherId ORDER BY event_date ASC")
    fun getEventsByTeacher(teacherId: Int): Flow<List<SchoolEventEntity>>

    // Eventos por clase
    @Query("SELECT * FROM school_events WHERE class_id = :classId ORDER BY event_date ASC")
    fun getEventsByClass(classId: Int): Flow<List<SchoolEventEntity>>

    // Eventos en rango de fechas
    @Query("""
        SELECT * FROM school_events 
        WHERE event_date >= :startDate 
        AND event_date <= :endDate 
        ORDER BY event_date ASC
    """)
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<SchoolEventEntity>>

    // Eventos por profesor y rango de fechas
    @Query("""
        SELECT * FROM school_events 
        WHERE teacher_id = :teacherId 
        AND event_date >= :startDate 
        AND event_date <= :endDate 
        ORDER BY event_date ASC
    """)
    fun getEventsByTeacherAndDateRange(
        teacherId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<SchoolEventEntity>>

    // ELIMINADO: Los métodos getAllEvents() duplicados
}