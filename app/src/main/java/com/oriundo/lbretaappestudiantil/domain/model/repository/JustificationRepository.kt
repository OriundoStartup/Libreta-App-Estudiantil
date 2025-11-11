package com.oriundo.lbretaappestudiantil.domain.model.repository


import android.net.Uri
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason

interface JustificationRepository {
    /**
     * Envía una solicitud de justificación de ausencia al sistema (simula lógica de red/persistencia).
     */
    suspend fun submitJustification(
        studentId: Int,
        parentId: Int,
        dateMillis: Long,
        reason: AbsenceReason,
        description: String,
        attachmentUri: Uri?
    )


    /**
     * (Para el Profesor)
     * Obtiene los detalles de una justificación específica.
     * TODO: Probablemente necesite un Flow para escuchar cambios en tiempo real.
     */
    suspend fun getJustificationDetails(justificationId: Int): AbsenceJustificationEntity

    /**
     * (Para el Profesor)
     * Actualiza el estado de una justificación (Aprobada o Rechazada).
     */
    suspend fun updateJustificationStatus(
        justificationId: Int,
        teacherId: Int,
        newStatus: JustificationStatus,
        reviewNotes: String
    )

    suspend fun getPendingJustifications(teacherId: Int): List<AbsenceJustificationEntity>
}