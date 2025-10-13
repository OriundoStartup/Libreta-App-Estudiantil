package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RequestStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialRequestDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRequest(request: MaterialRequestEntity): Long

    @Update
    suspend fun updateRequest(request: MaterialRequestEntity)

    @Delete
    suspend fun deleteRequest(request: MaterialRequestEntity)

    @Query("SELECT * FROM material_requests WHERE id = :id")
    suspend fun getRequestById(id: Int): MaterialRequestEntity?

    @Query("SELECT * FROM material_requests WHERE class_id = :classId ORDER BY created_date DESC")
    fun getRequestsByClass(classId: Int): Flow<List<MaterialRequestEntity>>

    @Query("SELECT * FROM material_requests WHERE student_id = :studentId ORDER BY created_date DESC")
    fun getRequestsByStudent(studentId: Int): Flow<List<MaterialRequestEntity>>

    @Query("SELECT * FROM material_requests WHERE teacher_id = :teacherId ORDER BY created_date DESC")
    fun getRequestsByTeacher(teacherId: Int): Flow<List<MaterialRequestEntity>>

    @Query("SELECT * FROM material_requests WHERE class_id = :classId AND status = :status ORDER BY created_date DESC")
    fun getRequestsByClassAndStatus(classId: Int, status: RequestStatus): Flow<List<MaterialRequestEntity>>

    @Query("UPDATE material_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Int, status: RequestStatus)
}