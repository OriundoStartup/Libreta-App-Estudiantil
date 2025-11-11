package com.oriundo.lbretaappestudiantil.data.local.daos


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity

@Dao
interface AbsenceJustificationDao {

    /**
     * Inserta una nueva justificación de ausencia en la base de datos local.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJustification(justification: AbsenceJustificationEntity): Long

    // Nota: Normalmente aquí irían funciones para obtener, actualizar o eliminar justificaciones,
    // pero para el contexto de envío, solo necesitamos la inserción.
}