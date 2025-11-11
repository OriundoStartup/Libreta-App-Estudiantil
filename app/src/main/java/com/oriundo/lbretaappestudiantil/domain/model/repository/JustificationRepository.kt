package com.oriundo.lbretaappestudiantil.domain.model.repository


import android.net.Uri
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
}