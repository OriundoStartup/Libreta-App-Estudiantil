package com.oriundo.lbretaappestudiantil.data.local.repositories

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.oriundo.lbretaappestudiantil.data.local.daos.AbsenceJustificationDao
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.data.local.models.SyncStatus
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Asumo que esta función de mapeo existe en alguna extensión o util
// Asumo que esta función de mapeo existe en alguna extensión o util
fun DocumentSnapshot.toAbsenceJustificationEntity(): AbsenceJustificationEntity? {
    // Este mapeo es crucial. Aquí va tu lógica real.
    // Usando el mapeo mínimo para que el código compile y sea funcional:
    return try {
        AbsenceJustificationEntity(
            // Asumo que tu entidad ahora tiene un campo para el ID de Firestore.
            // Si el campo 'id' de la Entity es el ID de Firestore, úsalo. Si no, usa el ID local.
            id = (getLong("id") ?: 0).toInt(),
            studentId = (getLong("studentId") ?: 0).toInt(),
            parentId = (getLong("parentId") ?: 0).toInt(),
            absenceDate = getLong("absenceDate") ?: 0L,
            reason = AbsenceReason.valueOf(getString("reason") ?: "OTHER"),
            description = getString("description") ?: "",
            attachmentUrl = getString("attachmentUrl"),
            status = JustificationStatus.valueOf(getString("status") ?: "PENDING"),

            // ✅ CORRECCIÓN: Añadir 'submittedAt' recuperándolo de Firestore
            submittedAt = getLong("submittedAt") ?: 0L,

            reviewedByTeacherId = (getLong("reviewedByTeacherId") ?: 0).toInt().takeIf { it != 0 },
            reviewNotes = getString("reviewNotes"),
            reviewedAt = getLong("reviewedAt"),
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,

            // ⚠️ NOTA: Si incluiste 'remoteId' y 'syncStatus' en la entidad
            // (que se usa para la justificación local/offline), deberías
            // asignarlos aquí también, al menos con valores por defecto o
            // mapeando el ID de Firebase:
            // remoteId = id, // Si el ID de Firestore es el que se usa en el repo
            // syncStatus = SyncStatus.SYNCED // Ya que viene de Firestore
        )
    } catch (e: Exception) {
        Log.e("Mapper", "Error mapeando justificación desde Firestore: ${e.message}")
        null
    }
}

// Asumo que esta función de mapeo existe en alguna extensión o util
fun AbsenceJustificationEntity.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to this.id,
        "studentId" to this.studentId,
        "parentId" to this.parentId,
        "absenceDate" to this.absenceDate,
        "reason" to this.reason.name,
        "description" to this.description,
        "attachmentUrl" to this.attachmentUrl,
        "status" to this.status.name,
        "createdAt" to this.createdAt,
        "updatedAt" to this.updatedAt,
        "reviewedByTeacherId" to this.reviewedByTeacherId,
        "reviewNotes" to this.reviewNotes,
        "reviewedAt" to this.reviewedAt,
    )
}

/**
 * Implementación del repositorio.
 * Coordina la base de datos local (DAO) y Firebase.
 */
class JustificationRepositoryImpl @Inject constructor(
    private val dao: AbsenceJustificationDao,
    private val firestore: FirebaseFirestore // ✅ Inyección de Firebase
) : JustificationRepository {

    // =========================================================================
    // ✅ submitJustification: Guardar Local (PENDING) -> Subir a Firebase -> Actualizar Local (SYNCED)
    // =========================================================================
    override suspend fun submitJustification(
        studentId: Int,
        parentId: Int,
        dateMillis: Long,
        reason: AbsenceReason,
        description: String,
        attachmentUri: Uri?
    ) {
        val submittedAt = System.currentTimeMillis() // ⬅️ Usamos submittedAt

        // 1. Crear una entidad local inicial con estado PENDING
        val initialJustification = AbsenceJustificationEntity(
            // id se genera automáticamente por Room (es 0 aquí)
            studentId = studentId,
            parentId = parentId,
            absenceDate = dateMillis,
            reason = reason,
            description = description,
            attachmentUrl = attachmentUri?.toString(),

            // ✅ CORRECCIÓN: Pasar el valor a su respectivo parámetro
            submittedAt = submittedAt,

            status = JustificationStatus.PENDING,

            // Estos campos ya deberían tener valores por defecto en tu entidad (createdAt = submittedAt)
            // Pero si no tienen default, también necesitan ser pasados.
            // Lo hacemos por seguridad:
            createdAt = submittedAt,
            updatedAt = submittedAt,

            syncStatus = SyncStatus.PENDING
        )

        // 2. Guardar en Room para obtener el ID local y garantizar la persistencia
        // 🛑 CORRECCIÓN: Usar la instancia 'dao' y el nombre correcto 'insertJustification'
        val localId = dao.insertJustification(initialJustification).toInt()
        val justificationToUpload = initialJustification.copy(id = localId)

        try {
            // 3. Subir a Firestore
            // 🛑 CORRECCIÓN: Usar la función helper 'toMap()' que definiste
            val firestoreData = justificationToUpload.toMap()

            val documentReference = firestore
                .collection("justifications")
                .add(firestoreData)
                .await()

            val remoteId = documentReference.id

            // 4. Éxito: Actualizar el registro en Room con el ID remoto y estado SYNCED
            val syncedJustification = justificationToUpload.copy(
                // Asumo que la Entidad YA TIENE el campo 'remoteId'
                remoteId = remoteId,
                syncStatus = SyncStatus.SYNCED
            )
            // 🛑 CORRECCIÓN: Usar la instancia 'dao' y el nombre correcto 'updateJustification'
            dao.updateJustification(syncedJustification)

        } catch (e: Exception) {
            Log.e("JustificationRepo", "Falla al sincronizar justificación (Local ID: $localId): ${e.message}")
            // 5. Falla de Red: El registro ya está en Room como PENDING.
            throw e // Relanzar el error para que el ViewModel/UI lo maneje.
        }
    }

    // =========================================================================
    // 2. GET PENDING JUSTIFICATIONS (PROFESOR) - Obtiene la lista de Firebase
    // =========================================================================
    override suspend fun getPendingJustifications(teacherId: Int): List<AbsenceJustificationEntity> {
        return try {
            val snapshot = firestore.collection("justifications")
                .whereEqualTo("status", JustificationStatus.PENDING.name)
                // .whereArrayContains("classIds", classId) // Opcional: Filtro por clase del profesor
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val justifications = snapshot.documents.mapNotNull { document ->
                document.toAbsenceJustificationEntity()
            }

            // Opcional: Guardar en caché local
            // dao.insertAllJustifications(justifications)

            justifications
        } catch (e: Exception) {
            Log.e("JustificationRepo", "Error al obtener justificaciones pendientes: ${e.message}")
            // Si Firebase falla, intenta obtener de la base de datos local como fallback
            // return dao.getAllPendingLocal()
            emptyList()
        }
    }


    // =========================================================================
    // 3. GET JUSTIFICATION DETAILS (PROFESOR) - Obtiene el detalle de Firebase
    // =========================================================================
    override suspend fun getJustificationDetails(justificationId: Int): AbsenceJustificationEntity {
        // Asumo que el 'justificationId' recibido es el ID local.
        // La forma más robusta es buscar por el ID de Firebase.
        // Aquí buscaremos por el campo 'id' local en Firebase, asumiendo que está indexado.

        return try {
            // 1. Busco el documento en Firebase
            val docSnapshot = firestore.collection("justifications")
                .whereEqualTo("id", justificationId) // 🛑 REEMPLAZAR con .document(firestoreId).get() si es posible
                .get().await().documents.firstOrNull() ?: throw NoSuchElementException("Justificación ID:$justificationId no encontrada en Firebase.")

            // 2. Mapeo a la entidad
            docSnapshot.toAbsenceJustificationEntity() ?: throw IllegalStateException("Error mapeando justificación de Firebase.")

        } catch (e: Exception) {
            Log.e("JustificationRepo", "Error obteniendo detalle de justificación: ${e.message}")
            // ❌ Eliminado: kotlinx.coroutines.delay(500) y hardcodeo
            throw e
        }
    }

    // =========================================================================
    // 4. UPDATE JUSTIFICATION STATUS (PROFESOR) - Actualiza en Firebase
    // =========================================================================
    override suspend fun updateJustificationStatus(
        justificationId: Int,
        teacherId: Int,
        newStatus: JustificationStatus,
        reviewNotes: String
    ) {
        try {
            // 1. Busco el documento en Firebase (necesito su referencia)
            val docSnapshot = firestore.collection("justifications")
                .whereEqualTo("id", justificationId)
                .get().await().documents.firstOrNull() ?: throw NoSuchElementException("Justificación ID:$justificationId no encontrada para actualizar.")

            // 2. Defino los campos a actualizar
            val updates = mapOf(
                "status" to newStatus.name,
                "reviewedByTeacherId" to teacherId,
                "reviewNotes" to reviewNotes,
                "reviewedAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            // 3. Actualizo el documento
            docSnapshot.reference.update(updates).await()

            // 4. Opcional: Actualizar la BD local
            // dao.updateJustificationStatus(justificationId, newStatus, teacherId, reviewNotes)

        } catch (e: Exception) {
            Log.e("JustificationRepo", "Error actualizando estado en Firebase: ${e.message}")
            // ❌ Eliminado: kotlinx.coroutines.delay(1000)
            throw e
        }
    }
}