package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.*
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

    // --- MÉTODOS FALTANTES QUE CAUSAN 'Unresolved reference' ---

    // 1. Obtener eventos generales (donde class_id es NULL)
    @Query("SELECT * FROM school_events WHERE class_id IS NULL ORDER BY event_date ASC")
    fun getGeneralEvents(): Flow<List<SchoolEventEntity>> // 👈 ¡CORREGIDO!

    // 2. Obtener eventos generales Y específicos de una clase
    @Query("""
        SELECT * FROM school_events 
        WHERE class_id IS NULL OR class_id = :classId 
        ORDER BY event_date ASC
    """)
    fun getAllEventsForClass(classId: Int): Flow<List<SchoolEventEntity>> // 👈 ¡CORREGIDO!

    // 3. Desactivar un evento (asumiendo que tienes una columna is_active en la entidad)
    // Si no tienes 'is_active', usa DELETE en su lugar, pero esta es la forma más común de 'deactivate'.
    @Query("UPDATE school_events SET is_active = 0 WHERE id = :eventId")
    suspend fun deactivateEvent(eventId: Int): Int // 👈 ¡CORREGIDO!

    // --- MÉTODOS ANTERIORES (COPIADOS DE TU ENVÍO) ---

    @Query("SELECT * FROM school_events WHERE teacher_id = :teacherId ORDER BY event_date ASC")
    fun getEventsByTeacher(teacherId: Int): Flow<List<SchoolEventEntity>>

    @Query("SELECT * FROM school_events WHERE class_id = :classId ORDER BY event_date ASC")
    fun getEventsByClass(classId: Int): Flow<List<SchoolEventEntity>>

    @Query("""
        SELECT * FROM school_events 
        WHERE event_date >= :startDate 
        AND event_date <= :endDate 
        ORDER BY event_date ASC
    """)
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<SchoolEventEntity>>

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
    // Si quieres TODOS los eventos ordenados por fecha
    @Query("SELECT * FROM school_events ORDER BY event_date ASC")
    fun getAllEvents(): Flow<List<SchoolEventEntity>>

    // O si solo quieres eventos futuros
    @Query("SELECT * FROM school_events WHERE event_date >= :currentDate ORDER BY event_date ASC")
    fun getAllEvents(currentDate: Long = System.currentTimeMillis()): Flow<List<SchoolEventEntity>>
}