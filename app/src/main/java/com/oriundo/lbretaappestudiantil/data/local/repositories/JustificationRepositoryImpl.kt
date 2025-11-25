package com.oriundo.lbretaappestudiantil.data.local.repositories

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.LocalDatabaseRepository
import com.oriundo.lbretaappestudiantil.data.local.daos.AbsenceJustificationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.daos.UserDao
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.data.local.models.SyncStatus
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class JustificationRepositoryImpl @Inject constructor(
    private val dao: AbsenceJustificationDao,
    private val studentDao: StudentDao,
    private val classDao: ClassDao,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val localDatabaseRepository: LocalDatabaseRepository
) : JustificationRepository {

    override suspend fun submitJustification(
        studentId: Int,
        parentId: Int,
        dateMillis: Long,
        reason: AbsenceReason,
        description: String,
        attachmentUri: Uri?
    ) {
        val submittedAt = System.currentTimeMillis()

        // 1. Obtener el estudiante de Room
        val studentEntity = studentDao.getStudentById(studentId)
            ?: throw IllegalStateException("Estudiante ID $studentId no encontrado")

        // 2. Obtener la clase del estudiante
        val classEntity = classDao.getClassById(studentEntity.classId)
            ?: throw IllegalStateException("Clase ID ${studentEntity.classId} no encontrada")

        // 3. Obtener el padre de Room y validar su ID de Firebase
        val parentUser = userDao.getUserById(parentId)
            ?: throw IllegalStateException("Padre ID $parentId no encontrado")

        val parentFirebaseUid = parentUser.firebaseUid
            ?: throw IllegalStateException("El padre no tiene firebaseUid")

        // 4. Verificar autenticación
        val currentAuthUid = auth.currentUser?.uid
        if (currentAuthUid != parentFirebaseUid) {
            throw SecurityException("UID de autenticación no coincide")
        }

        // 5. Obtener IDs de Firestore y clase actual
        val studentFirestoreId = studentEntity.firestoreId
            ?: throw IllegalStateException("El estudiante no tiene firestoreId")

        // Buscar la clase ACTUAL en Firestore por código (siempre la fuente de verdad)
        val classSnapshot = firestore.collection("classes")
            .whereEqualTo("code", classEntity.classCode)
            .limit(1)
            .get()
            .await()

        if (classSnapshot.isEmpty) {
            throw IllegalStateException("Clase ${classEntity.classCode} no encontrada en Firestore")
        }

        val classDoc = classSnapshot.documents.first()
        val classFirestoreId = classDoc.id

        // Actualizar Room si el ID de clase cambió
        if (classEntity.firestoreId != classFirestoreId) {
            try {
                val updatedClass = classEntity.copy(firestoreId = classFirestoreId)
                classDao.updateClass(updatedClass)
            } catch (_: Exception) {
                // Se ignora el error de actualización de Room para no detener el envío de la justificación
            }
        }

        // 6. Verificar que el estudiante existe en esa clase en Firestore y que pertenece al padre
        try {
            val studentDoc = firestore
                .collection("classes")
                .document(classFirestoreId)
                .collection("students")
                .document(studentFirestoreId)
                .get()
                .await()

            if (!studentDoc.exists()) {
                throw IllegalStateException("Estudiante no encontrado en la clase en Firestore")
            }

            val firestoreParentId = studentDoc.getString("parentId")
            if (firestoreParentId != parentFirebaseUid) {
                throw SecurityException("El estudiante no pertenece a este padre")
            }

        } catch (e: Exception) {
            if (e is SecurityException || e is IllegalStateException) {
                throw e
            }
            throw e
        }

        // 7. Preparar el documento para Firestore
        val justificationData = hashMapOf(
            "studentId" to studentFirestoreId,
            "classId" to classFirestoreId,
            "parentId" to parentFirebaseUid,
            "reason" to reason.name,
            "description" to description,
            "date" to dateMillis,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )

        // Agregar attachmentUrl solo si existe
        if (attachmentUri != null) {
            justificationData["attachmentUrl"] = attachmentUri.toString()
        }

        // 8. Guardar localmente primero
        val localJustification = AbsenceJustificationEntity(
            studentId = studentId,
            parentId = parentId,
            absenceDate = dateMillis,
            reason = reason,
            description = description,
            attachmentUrl = attachmentUri?.toString(),
            submittedAt = submittedAt,
            status = JustificationStatus.PENDING,
            createdAt = submittedAt,
            updatedAt = submittedAt,
            syncStatus = SyncStatus.PENDING
        )

        val localId = dao.insertJustification(localJustification).toInt()

        // 9. Enviar a Firestore
        try {
            val docRef = firestore
                .collection("justifications")
                .add(justificationData)
                .await()

            val remoteId = docRef.id

            // 10. Actualizar estado local
            val syncedJustification = localJustification.copy(
                id = localId,
                remoteId = remoteId,
                syncStatus = SyncStatus.SYNCED
            )
            dao.updateJustification(syncedJustification)

        } catch (e: Exception) {
            // Se lanza la excepción para que la capa superior la maneje (ej. revertir UI)
            throw e
        }
    }

    // =========================================================================
    // OTROS MÉTODOS
    // =========================================================================

    override suspend fun getPendingJustifications(teacherId: Int): List<AbsenceJustificationEntity> {
        // 1. Activar la sincronización (Pull) delegando a LocalDatabaseRepository
        localDatabaseRepository.syncPendingJustifications(teacherId)

        // 2. Devolver la lista desde el DAO local
        // Se asume que este método ya fue añadido a AbsenceJustificationDao
        return dao.getPendingJustificationsForTeacher(teacherId)
    }



    override suspend fun getJustificationDetails(justificationId: Int): AbsenceJustificationEntity {
        // Implementación necesaria: Usar el DAO para buscar por ID local
        return dao.getJustificationById(justificationId) ?: throw NoSuchElementException(
            "Justificación con ID $justificationId no encontrada en la base de datos local."
        )
    }


    override suspend fun updateJustificationStatus(
        justificationId: Int,
        teacherId: Int,
        newStatus: JustificationStatus,
        reviewNotes: String
    ) {
        // 1. Obtener la justificación local (debe existir)
        val existingJustification = dao.getJustificationById(justificationId)
            ?: throw IllegalStateException("Justificación ID $justificationId no encontrada para actualizar.")

        // 2. Preparar la entidad actualizada con el nuevo estado y marca de sincronización PENDING
        val localUpdatedJustification = existingJustification.copy(
            status = newStatus,
            reviewedByTeacherId = teacherId,
            reviewNotes = reviewNotes,
            reviewedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING, // Marcar como pendiente de sincronización
            updatedAt = System.currentTimeMillis()
        )

        // 3. Actualizar localmente (para dar feedback inmediato al usuario)
        dao.updateJustification(localUpdatedJustification)

        // 4. Sincronizar con Firestore
        val remoteId = localUpdatedJustification.remoteId
            ?: throw IllegalStateException("Justificación ID $justificationId no tiene remoteId para sincronizar.")

        // Datos que se enviarán a Firestore
        val firestoreUpdateData = mapOf(
            "status" to newStatus.name, // El Enum se guarda como String (PENDING, APPROVED, REJECTED)
            "reviewedByTeacherId" to teacherId,
            "reviewNotes" to reviewNotes,
            "reviewedAt" to localUpdatedJustification.reviewedAt,
            "updatedAt" to FieldValue.serverTimestamp() // Marca de tiempo del servidor
        )

        try {
            // Actualizar el documento en la colección 'justifications' de Firebase
            firestore.collection("justifications")
                .document(remoteId)
                .update(firestoreUpdateData)
                .await()

            // 5. Si la sincronización es exitosa, actualizar el estado local a SYNCED
            val finalJustification = localUpdatedJustification.copy(
                syncStatus = SyncStatus.SYNCED
            )
            dao.updateJustification(finalJustification)

        } catch (e: Exception) {
            // Lanzar la excepción para que el ViewModel la capture y muestre el error en la UI
            throw Exception("Error al sincronizar la revisión de la justificación ID $justificationId con Firestore: ${e.message}", e)
        }
    }
}
