package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity

@Dao
interface AbsenceJustificationDao {

    /**
     * Inserta una nueva justificación de ausencia en la base de datos local.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJustification(justification: AbsenceJustificationEntity): Long

    /**
     * Actualiza una justificación de ausencia existente. Se usa para actualizar el syncStatus.
     */
    @Update
    suspend fun updateJustification(justification: AbsenceJustificationEntity)


    /**
     * Obtiene una justificación específica por su ID local.
     */
    @Query("SELECT * FROM absence_justifications WHERE id = :id")
    suspend fun getJustificationById(id: Int): AbsenceJustificationEntity?

    /**
     * Obtiene una justificación específica por su ID remoto (Firestore).
     */
    @Query("SELECT * FROM absence_justifications WHERE remoteId = :remoteId")
    suspend fun getJustificationByRemoteId(remoteId: String): AbsenceJustificationEntity?

    /**
     * Obtiene las justificaciones pendientes que corresponden a las clases del profesor.
     * CORRECCIÓN: Se usa 'classId' en la tabla 'students'.
     */
    @Query("SELECT J.* FROM absence_justifications AS J INNER JOIN students AS S ON J.studentId = S.id INNER JOIN classes AS C ON S.classId = C.id WHERE J.status = 'PENDING' AND C.teacher_id = :teacherId")
    suspend fun getPendingJustificationsForTeacher(teacherId: Int): List<AbsenceJustificationEntity>
}