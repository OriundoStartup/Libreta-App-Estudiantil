package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity): Long

    @Update
    suspend fun updateAnnotation(annotation: AnnotationEntity)

    @Delete
    suspend fun deleteAnnotation(annotation: AnnotationEntity)

    @Query("SELECT * FROM annotations WHERE teacher_id = :teacherId ORDER BY date DESC")
    fun getAnnotationsByTeacher(teacherId: Int): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations WHERE student_id = :studentId ORDER BY date DESC")
    fun getAnnotationsByStudent(studentId: Int): Flow<List<AnnotationEntity>>

    @Query("SELECT COUNT(id) FROM annotations WHERE student_id = :studentId AND is_read = 0")
    suspend fun getUnreadCount(studentId: Int): Int

    @Query("UPDATE annotations SET is_read = 1 WHERE id = :annotationId")
    suspend fun markAsRead(annotationId: Int)

    // *** MÉTODOS AÑADIDOS PARA RESOLVER 'Unresolved reference' ***

    /**
     * Obtiene anotaciones por clase (requiere JOIN con la tabla 'students').
     */
    @Query("""
        SELECT a.* FROM annotations a
        INNER JOIN students s ON a.student_id = s.id
        WHERE s.class_id = :classId
        ORDER BY a.date DESC
    """)
    fun getAnnotationsByClass(classId: Int): Flow<List<AnnotationEntity>>

    /**
     * Obtiene anotaciones no leídas para un apoderado
     * (requiere JOIN con la tabla de relación 'student_parent_relation').
     */
    @Query("""
        SELECT a.* FROM annotations a
        INNER JOIN student_parent_relation spr ON a.student_id = spr.student_id
        WHERE spr.parent_id = :parentId AND a.is_read = 0
        ORDER BY a.date DESC
    """)
    fun getUnreadAnnotationsForParent(parentId: Int): Flow<List<AnnotationEntity>>
}