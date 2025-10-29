package com.oriundo.lbretaappestudiantil.data.local.daos


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("SELECT * FROM calendar_events WHERE teacher_id = :teacherId ORDER BY event_date ASC")
    fun getEventsByTeacher(teacherId: Int): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE class_id = :classId ORDER BY event_date ASC")
    fun getEventsByClass(classId: Int): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE event_date >= :startDate AND event_date <= :endDate ORDER BY event_date ASC")
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    suspend fun getEventById(id: Int): CalendarEventEntity?
}