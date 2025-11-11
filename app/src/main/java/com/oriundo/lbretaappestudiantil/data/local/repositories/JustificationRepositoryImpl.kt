package com.oriundo.lbretaappestudiantil.data.local.repositories


import android.net.Uri
import com.oriundo.lbretaappestudiantil.data.local.daos.AbsenceJustificationDao
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import javax.inject.Inject

/**
 * Implementación del repositorio.
 * Coordina la base de datos local (DAO) y la API remota.
 */
class JustificationRepositoryImpl @Inject constructor(
    private val dao: AbsenceJustificationDao,
    // TODO: private val api: MyApiService
) : JustificationRepository {

    override suspend fun submitJustification(
        studentId: Int,
        parentId: Int,
        dateMillis: Long,
        reason: AbsenceReason,
        description: String,
        attachmentUri: Uri?
    ) {
        // Lógica de implementación real:
        // 1. Subir el archivo (attachmentUri) a un servicio (ej. Firebase Storage, S3)
        //    y obtener la URL de descarga.
        //    val fileUrl = uploadFileToStorage(attachmentUri)
        val fileUrl: String? = null // TODO: Implementar subida de archivo

        val justification = AbsenceJustificationEntity(
            studentId = studentId,
            parentId = parentId,
            absenceDate = dateMillis,
            reason = reason,
            description = description,
            attachmentUrl = fileUrl,
            status = JustificationStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // 2. Enviar la entidad a la API
        //    val result = api.submitJustification(justification)

        // 3. Guardar en la base de datos local (puede ser para offline o caché)
        //    En tu caso, parece que el DAO es para insertar localmente.
        dao.insertJustification(justification)

        // Simulación de envío a red
        kotlinx.coroutines.delay(1000)
    }

    // --- ESTA ES UNA DE LAS FUNCIONES QUE TE FALTABA ---
    override suspend fun getJustificationDetails(justificationId: Int): AbsenceJustificationEntity {
        // TODO: En una app real, esto vendría de la API
        // return api.getJustification(justificationId)

        // Simulación de datos (usando la lógica de tu pantalla de profesor)
        kotlinx.coroutines.delay(500)
        return AbsenceJustificationEntity(
            id = justificationId,
            studentId = 101,
            parentId = 202,
            absenceDate = System.currentTimeMillis() - 86400000, // Ayer
            reason = AbsenceReason.ILLNESS,
            description = "Mi hijo amaneció con fiebre y dolor de garganta.",
            attachmentUrl = "uploads/certificado_123.pdf",
            status = JustificationStatus.PENDING
        )
    }

    // --- ESTA ES LA OTRA FUNCIÓN QUE TE FALTABA ---
    override suspend fun updateJustificationStatus(
        justificationId: Int,
        teacherId: Int,
        newStatus: JustificationStatus,
        reviewNotes: String
    ) {

        kotlinx.coroutines.delay(1000)
    }
}