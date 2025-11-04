package com.oriundo.lbretaappestudiantil.data.local

import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.SchoolEventDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentParentRelationDao
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
    private val schoolEventDao: SchoolEventDao,
    private val studentParentRelationDao: StudentParentRelationDao
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
                ?: return ApiResult.Error("No se pudo obtener el usuario local después de sincronizar")
            val localProfile = profileDao.getProfileByUserId(localUser.id)
                ?: return ApiResult.Error("No se pudo obtener el perfil local después de sincronizar")

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
                        firestoreId = firebaseUid,
                        syncStatus = SyncStatus.SYNCED,
                        firebaseUid = firebaseUid,
                        lastSyncedAt = System.currentTimeMillis()
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
                    profileDao.updateProfile(
                        localProfile.copy(
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            address = address,
                            photoUrl = photoUrl,
                            isTeacher = isTeacher,
                            isParent = isParent,
                            firestoreId = firebaseUid,
                            syncStatus = SyncStatus.SYNCED,
                            firebaseUid = firebaseUid,
                            lastSyncedAt = System.currentTimeMillis()
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
                // ✅ IMPORTANTE: Normalizar el código a MAYÚSCULAS al sincronizar
                val classCode = (doc.getString("classCode") ?: continue).uppercase()
                val gradeLevel = doc.getString("gradeLevel")
                val academicYear = doc.getString("academicYear") ?: "2025"
                val isActive = doc.getBoolean("isActive") ?: true

                val existingClass = classDao.getClassByCode(classCode)

                if (existingClass == null) {
                    classDao.insertClass(
                        ClassEntity(
                            className = className,
                            schoolName = schoolName,
                            teacherId = localProfileId,
                            classCode = classCode, // Ya está en MAYÚSCULAS
                            gradeLevel = gradeLevel,
                            academicYear = academicYear,
                            isActive = isActive,
                            firestoreId = doc.id,
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncedAt = System.currentTimeMillis()
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
                            firestoreId = doc.id,
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncedAt = System.currentTimeMillis()
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
                val classCode = (doc.getString("classCode") ?: continue).uppercase()
                val birthDate = doc.getLong("birthDate")
                val photoUrl = doc.getString("photoUrl")

                // ✅ CAMPOS DE RELACIÓN
                val relationshipString = doc.getString("relationshipType") ?: "OTHER"
                val relationshipType = try {
                    com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType.valueOf(relationshipString)
                } catch (e: Exception) {
                    com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType.OTHER
                }
                val isPrimary = doc.getBoolean("isPrimary") ?: false

                // ✅ PASO 1: Buscar la clase en Room
                var localClass = classDao.getClassByCode(classCode)

                // ✅ PASO 2: Si no existe, buscarla en Firestore
                if (localClass == null) {
                    val classSnapshot = firestore.collection("classes")
                        .whereEqualTo("code", classCode)
                        .limit(1)
                        .get()
                        .await()

                    val classDoc = classSnapshot.documents.firstOrNull()

                    if (classDoc == null) {
                        println("⚠️ Clase $classCode no encontrada en Firestore")
                        continue
                    }

                    // ✅ PASO 3: Obtener el teacherId de Firebase (ahora es un String - Firebase UID)
                    val teacherFirebaseUid = classDoc.getString("teacherId")

                    if (teacherFirebaseUid == null) {
                        println("⚠️ Clase $classCode no tiene teacherId en Firestore")
                        continue
                    }

                    // ✅ PASO 4: Sincronizar el profesor si no existe en Room
                    var localTeacherProfile = userDao.getUserByFirebaseUid(teacherFirebaseUid)?.let { user ->
                        profileDao.getProfileByUserId(user.id)
                    }

                    if (localTeacherProfile == null) {
                        println("🔄 Sincronizando profesor $teacherFirebaseUid desde Firestore")

                        val teacherDoc = firestore.collection("users")
                            .document(teacherFirebaseUid)
                            .get()
                            .await()

                        if (teacherDoc.exists()) {
                            val teacherEmail = teacherDoc.getString("email") ?: ""
                            val teacherFirstName = teacherDoc.getString("firstName") ?: ""
                            val teacherLastName = teacherDoc.getString("lastName") ?: ""
                            val teacherPhone = teacherDoc.getString("phone")
                            val teacherAddress = teacherDoc.getString("address")
                            val teacherPhotoUrl = teacherDoc.getString("photoUrl")

                            // Crear usuario del profesor
                            val newTeacherUserId = userDao.insertUser(
                                UserEntity(
                                    email = teacherEmail,
                                    passwordHash = "",
                                    firebaseUid = teacherFirebaseUid,
                                    syncStatus = SyncStatus.SYNCED,
                                    lastSyncedAt = System.currentTimeMillis()
                                )
                            ).toInt()

                            // Crear perfil del profesor
                            val newTeacherProfileId = profileDao.insertProfile(
                                ProfileEntity(
                                    userId = newTeacherUserId,
                                    firstName = teacherFirstName,
                                    lastName = teacherLastName,
                                    phone = teacherPhone,
                                    address = teacherAddress,
                                    photoUrl = teacherPhotoUrl,
                                    isTeacher = true,
                                    isParent = false,
                                    firestoreId = teacherFirebaseUid,
                                    syncStatus = SyncStatus.SYNCED,
                                    firebaseUid = teacherFirebaseUid,
                                    lastSyncedAt = System.currentTimeMillis()
                                )
                            ).toInt()

                            localTeacherProfile = ProfileEntity(
                                id = newTeacherProfileId,
                                userId = newTeacherUserId,
                                firstName = teacherFirstName,
                                lastName = teacherLastName,
                                phone = teacherPhone,
                                address = teacherAddress,
                                photoUrl = teacherPhotoUrl,
                                isTeacher = true,
                                isParent = false,
                                firestoreId = teacherFirebaseUid,
                                syncStatus = SyncStatus.SYNCED,
                                firebaseUid = teacherFirebaseUid,
                                lastSyncedAt = System.currentTimeMillis()
                            )

                            println("✅ Profesor sincronizado: $teacherFirstName $teacherLastName")
                        } else {
                            println("⚠️ Profesor $teacherFirebaseUid no encontrado en Firestore")
                            continue
                        }
                    }

                    // ✅ PASO 5: Crear la clase en Room con el teacherId correcto
                    val newClass = ClassEntity(
                        className = classDoc.getString("name") ?: "",
                        schoolName = classDoc.getString("school") ?: "",
                        teacherId = localTeacherProfile!!.id,
                        classCode = classCode,
                        gradeLevel = classDoc.getString("gradeLevel"),
                        academicYear = classDoc.getString("academicYear") ?: "2025",
                        isActive = classDoc.getBoolean("isActive") ?: true,
                        createdAt = classDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                        firestoreId = classDoc.id,
                        syncStatus = SyncStatus.SYNCED,
                        lastSyncedAt = System.currentTimeMillis()
                    )

                    val classId = classDao.insertClass(newClass).toInt()
                    localClass = newClass.copy(id = classId)

                    println("✅ Clase sincronizada: ${newClass.className}")
                }

                // ✅ PASO 6: Guardar el estudiante
                val existingStudent = studentDao.getStudentByRut(rut)
                var localStudentId: Int

                if (existingStudent == null) {
                    localStudentId = studentDao.insertStudent(
                        StudentEntity(
                            classId = localClass.id,
                            rut = rut,
                            firstName = firstName,
                            lastName = lastName,
                            birthDate = birthDate,
                            photoUrl = photoUrl,
                            firestoreId = doc.id, // Guardar ID de Firestore
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                    ).toInt()
                    println("✅ Estudiante creado: $firstName $lastName")
                } else {
                    localStudentId = existingStudent.id
                    studentDao.updateStudent(
                        existingStudent.copy(
                            classId = localClass.id,
                            firstName = firstName,
                            lastName = lastName,
                            birthDate = birthDate,
                            photoUrl = photoUrl,
                            firestoreId = doc.id, // Actualizar ID de Firestore
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                    )
                    println("✅ Estudiante actualizado: $firstName $lastName")
                }

                // ✅ PASO 7: VINCULAR ESTUDIANTE Y APODERADO (LA CLAVE)
                // Usamos tu modelo StudentParentRelation
                try {
                    val relation = com.oriundo.lbretaappestudiantil.data.local.models.StudentParentRelation(
                        studentId = localStudentId,
                        parentId = localProfileId,
                        relationshipType = relationshipType,
                        isPrimary = isPrimary
                    )

                    // Gracias al OnConflictStrategy.REPLACE, esto insertará o actualizará
                    studentParentRelationDao.insertRelation(relation)

                    println("✅ Relación CREADA/ACTUALIZADA para estudiante $localStudentId y apoderado $localProfileId")

                } catch (e: Exception) {
                    println("⚠️ Error al crear/actualizar relación: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println(" Error sincronizando estudiantes: ${e.message}")
            e.printStackTrace()
        }
    }

    // =====================================================
    // SINCRONIZAR DE ROOM → FIRESTORE (AL CREAR/ACTUALIZAR)
    // =====================================================

    /**
     * ✅ MÉTODO ACTUALIZADO PARA SINCRONIZAR CLASE A FIRESTORE
     * Guarda el código en MAYÚSCULAS tanto en la colección del usuario como en la colección global
     */
    suspend fun syncClassToFirestore(
        firebaseUid: String,
        classEntity: ClassEntity
    ): ApiResult<Unit> {
        return try {
            val normalizedCode = classEntity.classCode.uppercase()

            // ✅ OBTENER EL FIREBASE UID DEL PROFESOR
            val teacherProfile = profileDao.getProfileById(classEntity.teacherId)
            val teacherFirebaseUid = teacherProfile?.firebaseUid ?: firebaseUid

            // Datos para la colección del usuario
            val classData = hashMapOf(
                "className" to classEntity.className,
                "schoolName" to classEntity.schoolName,
                "classCode" to normalizedCode,
                "gradeLevel" to classEntity.gradeLevel,
                "academicYear" to classEntity.academicYear,
                "isActive" to classEntity.isActive,
                "createdAt" to classEntity.createdAt,
                "lastSyncedAt" to System.currentTimeMillis()
            )

            // Guardar en la colección del usuario
            val userDocRef = firestore.collection("users")
                .document(firebaseUid)
                .collection("classes")
                .document()

            userDocRef.set(classData).await()

            // ✅ GUARDAR EN COLECCIÓN GLOBAL CON teacherFirebaseUid
            val globalClassData = hashMapOf(
                "name" to classEntity.className,
                "school" to classEntity.schoolName,
                "code" to normalizedCode,
                "gradeLevel" to classEntity.gradeLevel,
                "academicYear" to classEntity.academicYear,
                "teacherId" to teacherFirebaseUid, // ✅ CORREGIDO: Usar Firebase UID
                "isActive" to classEntity.isActive,
                "createdAt" to classEntity.createdAt,
                "lastSyncedAt" to System.currentTimeMillis()
            )

            // Buscar si ya existe una clase con este código
            val existingClass = firestore.collection("classes")
                .whereEqualTo("code", normalizedCode)
                .limit(1)
                .get()
                .await()

            if (existingClass.isEmpty) {
                firestore.collection("classes")
                    .add(globalClassData)
                    .await()
            } else {
                val docId = existingClass.documents.first().id
                firestore.collection("classes")
                    .document(docId)
                    .set(globalClassData)
                    .await()
            }

            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error sincronizando a Firestore: ${e.message}", e)
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
        // ✅ Normalizar el código a MAYÚSCULAS para la búsqueda
        return classDao.getClassByCode(classCode.uppercase())
    }

    suspend fun getStudentByRut(rut: String): StudentEntity? {
        return studentDao.getStudentByRut(rut)
    }
    // ... dentro de la clase LocalDatabaseRepository ...

    /**
     * Busca una clase en la colección principal de Firebase por su código.
     * @return ClassEntity si se encuentra, null si no.
     */
    /**
     * Busca una clase en la colección global "classes" de Firestore por código
     * y la convierte manualmente a ClassEntity porque los nombres de campos difieren
     */
    suspend fun getClassFromFirestoreByCode(code: String): ClassEntity? {
        return try {
            val normalizedCode = code.uppercase()

            val snapshot = firestore.collection("classes")
                .whereEqualTo("code", normalizedCode)  // ✅ Campo correcto
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull() ?: return null

            // ✅ Conversión MANUAL porque los campos tienen nombres diferentes
            ClassEntity(
                id = 0, // Se generará al insertar en Room
                className = doc.getString("name") ?: "",
                schoolName = doc.getString("school") ?: "",
                teacherId = 0, // ⚠️ Se asignará después
                classCode = normalizedCode,
                gradeLevel = doc.getString("gradeLevel"),
                academicYear = doc.getString("academicYear") ?: "2025",
                isActive = doc.getBoolean("isActive") ?: true,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                firestoreId = doc.id,
                syncStatus = SyncStatus.SYNCED,
                lastSyncedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

// ... (continúan tus otras funciones, como syncClassToFirestore) ...

    /**
     * ✅ MÉTODO AUXILIAR: Migrar códigos existentes a MAYÚSCULAS
     * Ejecutar una vez para normalizar datos existentes
     */
    suspend fun migrateClassCodesToUpperCase() {
        try {
            // Migrar en Firestore - colección global "classes"
            val classesSnapshot = firestore.collection("classes").get().await()

            for (doc in classesSnapshot.documents) {
                val currentCode = doc.getString("code") ?: continue
                if (currentCode != currentCode.uppercase()) {
                    doc.reference.update("code", currentCode.uppercase()).await()
                }
            }

            // Migrar en Firestore - colecciones de usuarios
            val usersSnapshot = firestore.collection("users").get().await()

            for (userDoc in usersSnapshot.documents) {
                val userClassesSnapshot = userDoc.reference
                    .collection("classes")
                    .get()
                    .await()

                for (classDoc in userClassesSnapshot.documents) {
                    val currentCode = classDoc.getString("classCode") ?: continue
                    if (currentCode != currentCode.uppercase()) {
                        classDoc.reference.update("classCode", currentCode.uppercase()).await()
                    }
                }
            }

            println("Migración de códigos a MAYÚSCULAS completada")
        } catch (e: Exception) {
            println("Error en migración: ${e.message}")
        }
    }
}