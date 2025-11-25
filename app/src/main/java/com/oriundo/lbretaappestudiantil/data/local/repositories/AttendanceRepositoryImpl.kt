package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.daos.AttendanceDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.data.local.models.SyncStatus
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val studentDao: StudentDao,
    private val profileDao: ProfileDao,
    private val firestore: FirebaseFirestore
) : AttendanceRepository {

    /**
     * Registra la asistencia de un estudiante y la sincroniza con Firebase
     */
    override suspend fun recordAttendance(
        studentId: Int,
        teacherId: Int,
        date: Long,
        status: AttendanceStatus,
        note: String?
    ): ApiResult<AttendanceEntity> {
        return try {
            println("📝 Registrando asistencia: studentId=$studentId, status=$status, date=$date")

            // 1. Obtener Firebase UIDs
            val student = studentDao.getStudentById(studentId)
                ?: return ApiResult.Error("Estudiante no encontrado")

            val teacher = profileDao.getProfileById(teacherId)
                ?: return ApiResult.Error("Profesor no encontrado")

            val studentFirebaseUid = student.firebaseUid
                ?: return ApiResult.Error("Estudiante sin Firebase UID")

            val teacherFirebaseUid = teacher.firebaseUid
                ?: return ApiResult.Error("Profesor sin Firebase UID")

            // 2. Verificar si ya existe un registro para esta fecha
            val existingAttendance = attendanceDao.getAttendanceByStudentAndDate(studentId, date)

            val attendance = existingAttendance?.// Actualizar registro existente
            copy(
                status = status,
                notes = note,
                syncStatus = SyncStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )
                ?: // Crear nuevo registro
                AttendanceEntity(
                    studentId = studentId,
                    teacherId = teacherId,
                    attendanceDate = date,
                    status = status,
                    notes = note,
                    studentFirebaseUid = studentFirebaseUid,
                    teacherFirebaseUid = teacherFirebaseUid,
                    syncStatus = SyncStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

            // 3. Guardar en Room
            val localId = if (existingAttendance != null) {
                attendanceDao.updateAttendance(attendance)
                attendance.id
            } else {
                attendanceDao.insertAttendance(attendance).toInt()
            }

            val savedAttendance = attendance.copy(id = localId)

            // 4. Sincronizar con Firebase
            val syncResult = syncAttendanceToFirestore(savedAttendance)

            if (syncResult is ApiResult.Success) {
                println("✅ Asistencia registrada y sincronizada exitosamente")
                ApiResult.Success(syncResult.data)
            } else {
                println("⚠️ Asistencia guardada localmente pero no sincronizada")
                ApiResult.Success(savedAttendance)
            }

        } catch (e: Exception) {
            println("❌ Error registrando asistencia: ${e.message}")
            e.printStackTrace()
            ApiResult.Error("Error al registrar asistencia: ${e.message}", e)
        }
    }

    /**
     * Sincroniza un registro de asistencia a Firebase
     */
    private suspend fun syncAttendanceToFirestore(attendance: AttendanceEntity): ApiResult<AttendanceEntity> {
        return try {
            println("☁️ Sincronizando asistencia a Firestore...")

            val studentFirebaseUid = attendance.studentFirebaseUid
                ?: return ApiResult.Error("Estudiante sin Firebase UID")

            // Estructura de datos para Firestore
            val attendanceData = hashMapOf(
                "studentFirebaseUid" to studentFirebaseUid,
                "teacherFirebaseUid" to attendance.teacherFirebaseUid,
                "attendanceDate" to attendance.attendanceDate,
                "status" to attendance.status.name,
                "notes" to attendance.notes,
                "createdAt" to attendance.createdAt,
                "updatedAt" to attendance.updatedAt
            )

            // Determinar si es actualización o creación
            val docRef = if (attendance.firestoreId != null) {
                // Actualizar documento existente
                firestore.collection("attendance")
                    .document(attendance.firestoreId)
                    .set(attendanceData)
                    .await()

                firestore.collection("attendance").document(attendance.firestoreId)
            } else {
                // Crear nuevo documento
                val newDocRef = firestore.collection("attendance")
                    .add(attendanceData)
                    .await()

                newDocRef
            }

            // Actualizar registro local con el ID de Firestore
            val syncedAttendance = attendance.copy(
                firestoreId = docRef.id,
                syncStatus = SyncStatus.SYNCED,
                lastSyncedAt = System.currentTimeMillis()
            )

            attendanceDao.updateAttendance(syncedAttendance)

            println("✅ Asistencia sincronizada con ID: ${docRef.id}")
            ApiResult.Success(syncedAttendance)

        } catch (e: Exception) {
            println("❌ Error sincronizando con Firestore: ${e.message}")
            e.printStackTrace()
            ApiResult.Error("Error al sincronizar: ${e.message}", e)
        }
    }

    /**
     * Obtiene la asistencia de un estudiante (con caché local)
     */
    override fun getAttendanceByStudent(studentId: Int): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByStudent(studentId)
            .map { localData ->
                // Intentar sincronizar en segundo plano
                syncAttendanceFromFirestore(studentId)
                localData
            }
            .catch { e ->
                println("❌ Error obteniendo asistencia: ${e.message}")
                emit(emptyList())
            }
    }

    /**
     * Obtiene asistencia por rango de fechas
     */
    override fun getAttendanceByDateRange(
        studentId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByDateRange(studentId, startDate, endDate)
            .catch { e ->
                println("❌ Error obteniendo asistencia por rango: ${e.message}")
                emit(emptyList())
            }
    }

    /**
     * Actualiza un registro de asistencia
     */
    override suspend fun updateAttendance(attendance: AttendanceEntity): ApiResult<Unit> {
        return try {
            val updatedAttendance = attendance.copy(
                syncStatus = SyncStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )

            attendanceDao.updateAttendance(updatedAttendance)

            // Sincronizar con Firebase
            syncAttendanceToFirestore(updatedAttendance)

            ApiResult.Success(Unit)
        } catch (e: Exception) {
            println("❌ Error actualizando asistencia: ${e.message}")
            ApiResult.Error("Error al actualizar: ${e.message}", e)
        }
    }

    /**
     * Sincroniza asistencia desde Firestore a Room
     */
    suspend fun syncAttendanceFromFirestore(studentId: Int) {
        try {
            val student = studentDao.getStudentById(studentId) ?: return
            val studentFirebaseUid = student.firebaseUid ?: return

            println("📥 Sincronizando asistencia desde Firestore para estudiante: $studentFirebaseUid")

            val snapshot = firestore.collection("attendance")
                .whereEqualTo("studentFirebaseUid", studentFirebaseUid)
                .get()
                .await()

            println("📊 Registros encontrados en Firestore: ${snapshot.documents.size}")

            snapshot.documents.forEach { doc ->
                val statusString = doc.getString("status") ?: return@forEach
                val status = try {
                    AttendanceStatus.valueOf(statusString)
                } catch (_: Exception) {
                    AttendanceStatus.PRESENT
                }

                val teacherFirebaseUid = doc.getString("teacherFirebaseUid")

                // Buscar el teacherId local
                val teacherProfile = if (teacherFirebaseUid != null) {
                    val teacherUser = profileDao.getProfileByFirebaseUid(teacherFirebaseUid)
                    teacherUser?.id
                } else {
                    null
                }

                val remoteAttendance = AttendanceEntity(
                    studentId = studentId,
                    teacherId = teacherProfile,
                    attendanceDate = doc.getLong("attendanceDate") ?: 0L,
                    status = status,
                    notes = doc.getString("notes"),
                    firestoreId = doc.id,
                    studentFirebaseUid = studentFirebaseUid,
                    teacherFirebaseUid = teacherFirebaseUid,
                    syncStatus = SyncStatus.SYNCED,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    lastSyncedAt = System.currentTimeMillis()
                )

                // Verificar si ya existe localmente
                val existing = attendanceDao.getAttendanceByFirestoreId(doc.id)

                if (existing == null) {
                    attendanceDao.insertAttendance(remoteAttendance)
                    println("✅ Nueva asistencia sincronizada: ${doc.id}")
                } else {
                    // Solo actualizar si el remoto es más reciente
                    if (remoteAttendance.updatedAt > existing.updatedAt) {
                        attendanceDao.updateAttendance(
                            remoteAttendance.copy(id = existing.id)
                        )
                        println("🔄 Asistencia actualizada: ${doc.id}")
                    }
                }
            }

        } catch (e: Exception) {
            println("❌ Error sincronizando desde Firestore: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Sincroniza registros pendientes con Firebase
     */
    suspend fun syncPendingAttendance(): ApiResult<Unit> {
        return try {
            val unsyncedRecords = attendanceDao.getUnsyncedAttendance()

            println("🔄 Sincronizando ${unsyncedRecords.size} registros pendientes")

            unsyncedRecords.forEach { attendance ->
                syncAttendanceToFirestore(attendance)
            }

            ApiResult.Success(Unit)
        } catch (e: Exception) {
            println("❌ Error sincronizando registros pendientes: ${e.message}")
            ApiResult.Error("Error al sincronizar: ${e.message}", e)
        }
    }
}