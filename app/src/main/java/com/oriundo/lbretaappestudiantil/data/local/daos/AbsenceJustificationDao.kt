package com.oriundo.lbretaappestudiantil.data.local.daos


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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
    // ⚠️ Necesaria para actualizar el estado de sincronización (SYNCED) y el remoteId.
    suspend fun updateJustification(justification: AbsenceJustificationEntity)

    // Nota: Normalmente aquí irían funciones para obtener, actualizar o eliminar justificaciones,
    // pero para el contexto de envío, solo necesitamos la inserción y actualización.
}
