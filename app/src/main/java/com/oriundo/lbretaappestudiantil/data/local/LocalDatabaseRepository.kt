package com.oriundo.lbretaappestudiantil.data.local

import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.SchoolEventDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.daos.UserDao
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.data.local.models.SyncStatus
import com.oriundo.lbretaappestudiantil.data.local.models.UserEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatabaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    private val classDao: ClassDao,
    private val studentDao: StudentDao,
    private val schoolEventDao: SchoolEventDao
) {

    // =====================================================
    // SINCRONIZACIÓN COMPLETA DESPUÉS DEL LOGIN
    // =====================================================

    suspend fun syncAllUserDataFromFirestore(firebaseUid: String): ApiResult<Unit> {
        return try {
            // 1. Sincronizar User y Profile
            syncUserAndProfile(firebaseUid)

            // 2. Obtener el profileId local
            val localUser = userDao.getUserByFirebaseUid(firebaseUid)
            if (localUser == null) {
                return ApiResult.Error("No se pudo obtener el usuario local después de sincronizar")
            }
            val localProfile = profileDao.getProfileByUserId(localUser.id)
            if (localProfile == null) {
                return ApiResult.Error("No se pudo obtener el perfil local después de sincronizar")
            }

            // 3. Si es profesor, sincronizar sus clases
            if (localProfile.isTeacher) {
                syncTeacherClasses(firebaseUid, localProfile.id)
            }

            // 4. Si es apoderado, sincronizar sus estudiantes
            if (localProfile.isParent) {
                syncParentStudents(firebaseUid, localProfile.id)
            }

            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error sincronizando datos: ${e.message}", e)
        }
    }

    // =====================================================
    // SINCRONIZAR USER Y PROFILE
    // =====================================================

    private suspend fun syncUserAndProfile(firebaseUid: String) {
        try {
            // Obtener datos de Firestore
            val firestoreDoc = firestore.collection("users")
                .document(firebaseUid)
                .get()
                .await()

            if (!firestoreDoc.exists()) {
                throw Exception("Usuario no encontrado en Firestore")
            }

            val email = firestoreDoc.getString("email") ?: throw Exception("Email no encontrado")
            val firstName = firestoreDoc.getString("firstName") ?: ""
            val lastName = firestoreDoc.getString("lastName") ?: ""
            val photoUrl = firestoreDoc.getString("photoUrl")
            val isTeacher = firestoreDoc.getBoolean("isTeacher") ?: false
            val isParent = firestoreDoc.getBoolean("isParent") ?: false
            val phone = firestoreDoc.getString("phone")
            val address = firestoreDoc.getString("address")

            // ✅ CORREGIDO: Buscar por firebaseUid
            var localUser = userDao.getUserByFirebaseUid(firebaseUid)

            if (localUser == null) {
                // Crear nuevo usuario local
                val newUserId = userDao.insertUser(
                    UserEntity(
                        email = email,
                        passwordHash = "",
                        firebaseUid = firebaseUid,
                        syncStatus = SyncStatus.SYNCED,
                        lastSyncedAt = System.currentTimeMillis()
                    )
                ).toInt()

                // ✅ CORREGIDO: Incluir TODOS los campos de ProfileEntity
                profileDao.insertProfile(
                    ProfileEntity(
                        userId = newUserId,
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone,
                        address = address,
                        photoUrl = photoUrl,
                        isTeacher = isTeacher,
                        isParent = isParent,
                        firestoreId = firebaseUid, // ✅ AÑADIDO
                        syncStatus = SyncStatus.SYNCED, // ✅ AÑADIDO
                        firebaseUid = firebaseUid, // ✅ AÑADIDO (ESTE ERA EL ERROR)
                        lastSyncedAt = System.currentTimeMillis() // ✅ AÑADIDO
                    )
                )
            } else {
                // Actualizar usuario existente
                userDao.updateUser(
                    localUser.copy(
                        firebaseUid = firebaseUid,
                        syncStatus = SyncStatus.SYNCED,
                        lastSyncedAt = System.currentTimeMillis()
                    )
                )

                val localProfile = profileDao.getProfileByUserId(localUser.id)
                if (localProfile != null) {
                    // ✅ CORREGIDO: Incluir TODOS los campos de ProfileEntity
                    profileDao.updateProfile(
                        localProfile.copy(
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            address = address,
                            photoUrl = photoUrl,
                            isTeacher = isTeacher,
                            isParent = isParent,
                            firestoreId = firebaseUid, // ✅ AÑADIDO
                            syncStatus = SyncStatus.SYNCED, // ✅ AÑADIDO
                            firebaseUid = firebaseUid, // ✅ AÑADIDO (ESTE ERA EL ERROR)
                            lastSyncedAt = System.currentTimeMillis() // ✅ AÑADIDO
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Error sincronizando usuario y perfil: ${e.message}")
        }
    }

    // =====================================================
    // SINCRONIZAR CLASES DEL PROFESOR
    // =====================================================

    private suspend fun syncTeacherClasses(firebaseUid: String, localProfileId: Int) {
        try {
            val classesSnapshot = firestore.collection("users")
                .document(firebaseUid)
                .collection("classes")
                .get()
                .await()

            for (doc in classesSnapshot.documents) {
                val className = doc.getString("className") ?: continue
                val schoolName = doc.getString("schoolName") ?: continue
                val classCode = doc.getString("classCode") ?: continue
                val gradeLevel = doc.getString("gradeLevel")
                val academicYear = doc.getString("academicYear") ?: "2025"
                val isActive = doc.getBoolean("isActive") ?: true

                val existingClass = classDao.getClassByCode(classCode)

                if (existingClass == null) {
                    // ✅ Asegúrate de que ClassEntity tenga los mismos campos de sincronización
                    classDao.insertClass(
                        ClassEntity(
                            className = className,
                            schoolName = schoolName,
                            teacherId = localProfileId,
                            classCode = classCode,
                            gradeLevel = gradeLevel,
                            academicYear = academicYear,
                            isActive = isActive,
                            firestoreId = doc.id, // ✅ AÑADIR SI EXISTE
                            syncStatus = SyncStatus.SYNCED, // ✅ AÑADIR SI EXISTE
                            lastSyncedAt = System.currentTimeMillis() // ✅ AÑADIR SI EXISTE
                        )
                    )
                } else {
                    classDao.updateClass(
                        existingClass.copy(
                            className = className,
                            schoolName = schoolName,
                            gradeLevel = gradeLevel,
                            academicYear = academicYear,
                            isActive = isActive,
                            firestoreId = doc.id, // ✅ AÑADIR SI EXISTE
                            syncStatus = SyncStatus.SYNCED, // ✅ AÑADIR SI EXISTE
                            lastSyncedAt = System.currentTimeMillis() // ✅ AÑADIR SI EXISTE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            println("Error sincronizando clases: ${e.message}")
        }
    }

    // =====================================================
    // SINCRONIZAR ESTUDIANTES DEL APODERADO
    // =====================================================

    private suspend fun syncParentStudents(firebaseUid: String, localProfileId: Int) {
        try {
            val studentsSnapshot = firestore.collection("users")
                .document(firebaseUid)
                .collection("students")
                .get()
                .await()

            for (doc in studentsSnapshot.documents) {
                val rut = doc.getString("rut") ?: continue
                val firstName = doc.getString("firstName") ?: continue
                val lastName = doc.getString("lastName") ?: continue
                val classCode = doc.getString("classCode") ?: continue
                val birthDate = doc.getLong("birthDate")
                val photoUrl = doc.getString("photoUrl")

                // Obtener la clase local usando el código
                val localClass = classDao.getClassByCode(classCode) ?: continue

                // Verificar si el estudiante ya existe
                val existingStudent = studentDao.getStudentByRut(rut)

                if (existingStudent == null) {
                    // ✅ CORREGIDO: Solo campos que existen en StudentEntity
                    studentDao.insertStudent(
                        StudentEntity(
                            classId = localClass.id,
                            rut = rut,
                            firstName = firstName,
                            lastName = lastName,
                            birthDate = birthDate,
                            photoUrl = photoUrl
                            // ❌ QUITADO: firestoreId, syncStatus, lastSyncedAt (no existen)
                        )
                    )
                } else {
                    // ✅ CORREGIDO: Solo campos que existen en StudentEntity
                    studentDao.updateStudent(
                        existingStudent.copy(
                            firstName = firstName,
                            lastName = lastName,
                            birthDate = birthDate,
                            photoUrl = photoUrl
                            // ❌ QUITADO: firestoreId, syncStatus, lastSyncedAt (no existen)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            println("Error sincronizando estudiantes: ${e.message}")
        }
    }

    // =====================================================
    // SINCRONIZAR DE ROOM → FIRESTORE (AL CREAR/ACTUALIZAR)
    // =====================================================

    suspend fun syncClassToFirestore(
        firebaseUid: String,
        classEntity: ClassEntity
    ): ApiResult<Unit> {
        return try {
            val classData = hashMapOf(
                "className" to classEntity.className,
                "schoolName" to classEntity.schoolName,
                "classCode" to classEntity.classCode,
                "gradeLevel" to classEntity.gradeLevel,
                "academicYear" to classEntity.academicYear,
                "isActive" to classEntity.isActive,
                "createdAt" to classEntity.createdAt,
                "lastSyncedAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection("users")
                .document(firebaseUid)
                .collection("classes")
                .document()

            docRef.set(classData).await()

            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error sincronizando a Firestore: ${e.message}", e)
        }
    }

    suspend fun syncStudentToFirestore(
        firebaseUid: String,
        studentEntity: StudentEntity,
        classCode: String
    ): ApiResult<Unit> {
        return try {
            val studentData = hashMapOf(
                "rut" to studentEntity.rut,
                "firstName" to studentEntity.firstName,
                "lastName" to studentEntity.lastName,
                "classCode" to classCode,
                "birthDate" to studentEntity.birthDate,
                "photoUrl" to studentEntity.photoUrl,
                "enrollmentDate" to studentEntity.enrollmentDate,
                "isActive" to studentEntity.isActive,
                "notes" to studentEntity.notes,
                "lastSyncedAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection("users")
                .document(firebaseUid)
                .collection("students")
                .document()

            docRef.set(studentData).await()

            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error sincronizando estudiante: ${e.message}", e)
        }
    }

    // =====================================================
    // VERIFICAR ESTADO DE SINCRONIZACIÓN
    // =====================================================

    suspend fun isUserSyncedLocally(firebaseUid: String): Boolean {
        return try {
            val user = userDao.getUserByFirebaseUid(firebaseUid)
            user != null && user.syncStatus == SyncStatus.SYNCED
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserByFirebaseUid(firebaseUid: String): UserWithProfile? {
        val userEntity = userDao.getUserByFirebaseUid(firebaseUid) ?: return null
        val profileEntity = profileDao.getProfileByUserId(userEntity.id) ?: return null
        return UserWithProfile(userEntity, profileEntity)
    }

    suspend fun getClassByCode(classCode: String): ClassEntity? {
        return classDao.getClassByCode(classCode)
    }

    suspend fun getStudentByRut(rut: String): StudentEntity? {
        return studentDao.getStudentByRut(rut)
    }
}