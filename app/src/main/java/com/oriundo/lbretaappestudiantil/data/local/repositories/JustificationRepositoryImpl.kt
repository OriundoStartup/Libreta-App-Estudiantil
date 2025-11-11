package com.oriundo.lbretaappestudiantil.data.local.repositories

// Archivo: JustificationRepositoryImpl.kt

import android.net.Uri
import com.oriundo.lbretaappestudiantil.data.local.daos.AbsenceJustificationDao
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JustificationRepositoryImpl @Inject constructor(
    private val dao: AbsenceJustificationDao
    // private val networkService: JustificationNetworkService // Se inyectaría el servicio de red real
) : JustificationRepository {

    override suspend fun submitJustification(
        studentId: Int,
        parentId: Int,
        dateMillis: Long,
        reason: AbsenceReason,
        description: String,
        attachmentUri: Uri?
    ) = withContext(Dispatchers.IO) {

        // 1. SIMULACIÓN DE LÓGICA DE RED (Subida de archivo y envío a API)

        // Simular la subida del archivo y obtener una URL de servidor
        val attachmentUrl: String? = if (attachmentUri != null) {
            // Lógica real: uploadFile(attachmentUri)
            delay(1000) // Simular un retraso en la subida de 1 segundo
            "https://server.com/attachments/${attachmentUri.lastPathSegment}.pdf"
        } else {
            null
        }

        // Lógica real: networkService.sendJustification(datos)
        delay(500) // Simular un retraso en la llamada a la API

        // 2. PERSISTENCIA LOCAL (Guardar en Room)

        val newJustification = AbsenceJustificationEntity(
            studentId = studentId,
            parentId = parentId,
            absenceDate = dateMillis,
            reason = reason,
            description = description,
            attachmentUrl = attachmentUrl,
            // Los campos de status y revisión se mantienen con los valores por defecto (PENDING)
        )

        // Insertar en la base de datos local
        val insertedId = dao.insertJustification(newJustification)

        println("Justificación ID $insertedId enviada y guardada localmente.")

        // Nota: En caso de error de red, aquí se podría lanzar una excepción
        // para que el ViewModel la capture y actualice el estado de error.
    }
}