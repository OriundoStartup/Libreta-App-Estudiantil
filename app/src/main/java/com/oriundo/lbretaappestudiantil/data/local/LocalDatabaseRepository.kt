package com.oriundo.lbretaappestudiantil.data.local

import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.daos.AbsenceJustificationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.AnnotationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.AttendanceDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.SchoolEventDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentParentRelationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.UserDao
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentParentRelation
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
    private val studentParentRelationDao: StudentParentRelationDao,
    private val annotationDao: AnnotationDao,
    private val absenceJustificationDao: AbsenceJustificationDao,
    private val attendanceDao: AttendanceDao
) {

    // =====================================================
    // SINCRONIZACIÓN COMPLETA DESPUÉS DEL LOGIN
    // =====================================================

    /**
     * ✅ ACTUALIZADO: Incluir sincronización de asistencia en syncAllUserDataFromFirestore
     */
    suspend fun syncAllUserDataFromFirestore(firebaseUid: String): ApiResult<Unit> {
        return try {
            println("🔄 Iniciando sincronización completa para usuario: $firebaseUid")

            // 1. Sincronizar User y Profile
            syncUserAndProfile(firebaseUid)

            // 2. Obtener el profileId local
            val localUser = userDao.getUserByFirebaseUid(firebaseUid)
                ?: return ApiResult.Error("No se pudo obtener el usuario local después de sincronizar")
            val localProfile = profileDao.getProfileByUserId(localUser.id)
                ?: return ApiResult.Error("No se pudo obtener el perfil local después de sincronizar")

            // 3. Si es profesor, sincronizar: clases, estudiantes, anotaciones, justificaciones Y ASISTENCIA
            if (localProfile.isTeacher) {
                println("👨‍🏫 Usuario es profesor, sincronizando datos de profesor...")
                syncTeacherClasses(firebaseUid, localProfile.id)
                syncTeacherStudents(localProfile.id)
                syncAnnotations(firebaseUid, localProfile.id)
                syncPendingJustifications(localProfile.id)
                // ✅ NUEVO: Sincronizar asistencia
                syncTeacherAttendance(localProfile.id)
            }

            // 4. Si es apoderado, sincronizar sus estudiantes Y su asistencia
            if (localProfile.isParent) {
                println("👨‍👩‍👧 Usuario es apoderado, sincronizando estudiantes...")
                syncParentStudents(firebaseUid, localProfile.id)

                // ✅ NUEVO: Sincronizar asistencia de cada estudiante
                val parentStudents = studentDao.getStudentsByParentId(localProfile.id)
                parentStudents.forEach { student ->
                    syncStudentAttendance(student.id)
                }
            }

            println("✅ Sincronización completa finalizada exitosamente")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            println("❌ Error en sincronización completa: ${e.message}")
            e.printStackTrace()
            ApiResult.Error("Error sincronizando datos: ${e.message}", e)
        }
    }

    /**
     * ✅ ACTUALIZADO: Fuerza la sincronización del dashboard del profesor
     * Incluye justificaciones
     */
    suspend fun forceTeacherDashboardSync(firebaseUid: String?, localProfileId: Int): ApiResult<Unit> {
        return try {
            if (firebaseUid == null) {
                return ApiResult.Error("Firebase UID no puede ser nulo para forzar la sincronización.")
            }

            println("🔄 Forzando sincronización del dashboard del profesor...")
            syncTeacherClasses(firebaseUid, localProfileId)
            syncTeacherStudents(localProfileId)
            // ✅ CRÍTICO: También sincronizar justificaciones al refrescar
            syncPendingJustifications(localProfileId)

            println("✅ Sincronización forzada completada")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            println("❌ Error forzando sincronización: ${e.message}")
            ApiResult.Error("Error forzando sincronización: ${e.message}", e)
        }
    }

    // =====================================================
    // SINCRONIZAR USER Y PROFILE
    // =====================================================

    private suspend fun syncUserAndProfile(firebaseUid: String) {
        try {
            println("👤 Sincronizando usuario y perfil...")

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

            val localUser = userDao.getUserByFirebaseUid(firebaseUid)

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
                println("✅ Usuario y perfil creados")
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
                println("✅ Usuario y perfil actualizados")
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
            println("📚 Sincronizando clases del profesor...")

            val classesSnapshot = firestore.collection("users")
                .document(firebaseUid)
                .collection("classes")
                .get()
                .await()

            println("📚 Clases encontradas en Firestore: ${classesSnapshot.size()}")

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
                    println("✅ Clase creada: $className ($classCode)")
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
                    println("✅ Clase actualizada: $className ($classCode)")
                }
            }
        } catch (e: Exception) {
            println("❌ Error sincronizando clases: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun syncClassToFirestore(firebaseUid: String, classEntity: ClassEntity): ApiResult<Unit> {
        return try {
            println("🔄 Sincronizando clase a Firestore: ${classEntity.classCode}")

            // Usar el classCode como ID en la colección global para evitar duplicados y facilitar búsquedas
            val classDocRef = firestore.collection("classes").document(classEntity.classCode)

            // Datos para la colección global /classes, que puede ser buscada por apoderados
            val classDataGlobal = hashMapOf(
                "code" to classEntity.classCode.uppercase(),
                "name" to classEntity.className,
                "school" to classEntity.schoolName,
                "teacherId" to firebaseUid, // Usar el UID del profesor para referencia cruzada
                "gradeLevel" to classEntity.gradeLevel,
                "academicYear" to classEntity.academicYear,
                "isActive" to classEntity.isActive,
                "createdAt" to classEntity.createdAt
            )

            // Datos para la subcolección del usuario /users/{uid}/classes
            val classDataUser = hashMapOf(
                "classCode" to classEntity.classCode.uppercase(),
                "className" to classEntity.className,
                "schoolName" to classEntity.schoolName,
                "gradeLevel" to classEntity.gradeLevel,
                "academicYear" to classEntity.academicYear,
                "isActive" to classEntity.isActive,
                "createdAt" to classEntity.createdAt
            )

            // Escribir en un batch para asegurar que ambas operaciones se completen o fallen juntas
            firestore.runBatch { batch ->
                // 1. Escribir/Sobrescribir en la colección global
                batch.set(classDocRef, classDataGlobal)

                // 2. Escribir/Sobrescribir en la subcolección del usuario. Usar el código como ID también.
                val userClassDocRef = firestore.collection("users")
                    .document(firebaseUid)
                    .collection("classes")
                    .document(classEntity.classCode)
                batch.set(userClassDocRef, classDataUser)
            }.await()

            println("✅ Clase ${classEntity.classCode} sincronizada en Firestore (global y usuario)")
            ApiResult.Success(Unit)

        } catch (e: Exception) {
            println("❌ Error sincronizando clase ${classEntity.classCode} a Firestore: ${e.message}")
            e.printStackTrace()
            // Es importante no bloquear al usuario, así que se podría retornar éxito aquí si la lógica de negocio lo permite
            // Pero para ser más estrictos, retornamos el error.
            ApiResult.Error("Error al guardar la clase en la nube: ${e.message}", e)
        }
    }

    // =====================================================
    // SINCRONIZAR ESTUDIANTES DE LAS CLASES DEL PROFESOR
    // =====================================================

    private suspend fun syncTeacherStudents(localProfileId: Int) {
        try {
            println("👥 Sincronizando estudiantes del profesor...")

            // 1. Obtener todas las clases del profesor desde Room
            val teacherClassesList = classDao.getClassesByTeacherList(localProfileId)
            println("📚 Clases del profesor: ${teacherClassesList.size}")

            teacherClassesList.forEach { classEntity ->
                // 2. Para cada clase, buscar estudiantes en Firestore
                val classCode = classEntity.classCode

                // 3. Buscar en la colección global de clases
                val classSnapshot = firestore.collection("classes")
                    .whereEqualTo("code", classCode)
                    .limit(1)
                    .get()
                    .await()

                if (classSnapshot.isEmpty) {
                    println("⚠️ Clase $classCode no encontrada en Firestore")
                    return@forEach
                }

                val classDoc = classSnapshot.documents.first()
                val classId = classDoc.id

                // 4. Obtener estudiantes de esta clase
                val studentsSnapshot = firestore.collection("classes")
                    .document(classId)
                    .collection("students")
                    .get()
                    .await()

                println("📚 Sincronizando ${studentsSnapshot.size()} estudiantes para clase $classCode")

                studentsSnapshot.documents.forEach { studentDoc ->
                    val studentId = studentDoc.getString("studentId") ?: return@forEach
                    val parentId = studentDoc.getString("parentId") ?: return@forEach

                    // 5. Buscar datos del estudiante en la colección del padre
                    val studentDataDoc = firestore.collection("users")
                        .document(parentId)
                        .collection("students")
                        .document(studentId)
                        .get()
                        .await()

                    if (!studentDataDoc.exists()) {
                        println("⚠️ Datos del estudiante $studentId no encontrados")
                        return@forEach
                    }

                    val rut = studentDataDoc.getString("rut") ?: return@forEach
                    val firstName = studentDataDoc.getString("firstName") ?: return@forEach
                    val lastName = studentDataDoc.getString("lastName") ?: return@forEach
                    val birthDate = studentDataDoc.getLong("birthDate")
                    val photoUrl = studentDataDoc.getString("photoUrl")
                    // ✅ CRÍTICO: Obtener el Firebase UID del estudiante
                    val firebaseUid = studentDataDoc.getString("firebaseUid") ?: studentId

                    // 6. Verificar si el estudiante ya existe en Room
                    val existingStudent = studentDao.getStudentByRut(rut)

                    if (existingStudent == null) {
                        // Crear nuevo estudiante
                        studentDao.insertStudent(
                            StudentEntity(
                                classId = classEntity.id,
                                rut = rut,
                                firstName = firstName,
                                lastName = lastName,
                                birthDate = birthDate,
                                photoUrl = photoUrl,
                                firestoreId = studentId,
                                firebaseUid = firebaseUid, // ✅ Guardar Firebase UID
                                syncStatus = SyncStatus.SYNCED,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        )
                        println("✅ Estudiante creado: $firstName $lastName (UID: $firebaseUid)")
                    } else {
                        // Actualizar estudiante existente
                        studentDao.updateStudent(
                            existingStudent.copy(
                                classId = classEntity.id,
                                firstName = firstName,
                                lastName = lastName,
                                birthDate = birthDate,
                                photoUrl = photoUrl,
                                firestoreId = studentId,
                                firebaseUid = firebaseUid, // ✅ Actualizar Firebase UID
                                syncStatus = SyncStatus.SYNCED,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        )
                        println("✅ Estudiante actualizado: $firstName $lastName (UID: $firebaseUid)")
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error sincronizando estudiantes del profesor: ${e.message}")
            e.printStackTrace()
        }
    }

    // =====================================================
    // SINCRONIZAR ESTUDIANTES DEL APODERADO
    // =====================================================

    private suspend fun syncParentStudents(firebaseUid: String, localProfileId: Int) {
        try {
            println("👨‍👩‍👧 Sincronizando estudiantes del apoderado...")

            val studentsSnapshot = firestore.collection("users")
                .document(firebaseUid)
                .collection("students")
                .get()
                .await()

            println("👥 Estudiantes encontrados: ${studentsSnapshot.size()}")

            for (doc in studentsSnapshot.documents) {
                val rut = doc.getString("rut") ?: continue
                val firstName = doc.getString("firstName") ?: continue
                val lastName = doc.getString("lastName") ?: continue
                val classCode = (doc.getString("classCode") ?: continue).uppercase()
                val birthDate = doc.getLong("birthDate")
                val photoUrl = doc.getString("photoUrl")
                // ✅ CRÍTICO: Obtener el Firebase UID del estudiante
                val studentFirebaseUid = doc.getString("firebaseUid") ?: doc.id

                // ✅ CAMPOS DE RELACIÓN
                val relationshipString = doc.getString("relationshipType") ?: "OTHER"
                val relationshipType = try {
                    RelationshipType.valueOf(relationshipString)
                } catch (_: Exception) {
                    RelationshipType.OTHER
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
                        teacherId = localTeacherProfile.id,
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
                            firebaseUid = studentFirebaseUid, // ✅ Guardar Firebase UID
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                    ).toInt()
                    println("✅ Estudiante creado: $firstName $lastName (UID: $studentFirebaseUid)")
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
                            firebaseUid = studentFirebaseUid, // ✅ Actualizar Firebase UID
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                    )
                    println("✅ Estudiante actualizado: $firstName $lastName (UID: $studentFirebaseUid)")
                }

                // ✅ PASO 7: VINCULAR ESTUDIANTE Y APODERADO
                try {
                    val relation = StudentParentRelation(
                        studentId = localStudentId,
                        parentId = localProfileId,
                        relationshipType = relationshipType,
                        isPrimary = isPrimary
                    )

                    studentParentRelationDao.insertRelation(relation)
                    println("✅ Relación creada para estudiante $localStudentId y apoderado $localProfileId")

                } catch (e: Exception) {
                    println("⚠️ Error al crear relación: ${e.message}")
                    e.printStackTrace()
                }

                // ✅ PASO 8: ACTUALIZAR EL PRIMARY_PARENT_ID en StudentEntity
                if (isPrimary) {
                    val studentToUpdate = studentDao.getStudentById(localStudentId)

                    if (studentToUpdate != null && studentToUpdate.primaryParentId != localProfileId) {
                        studentDao.updateStudent(
                            studentToUpdate.copy(
                                primaryParentId = localProfileId
                            )
                        )
                        println("✅ PrimaryParentId actualizado para estudiante $localStudentId")
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error sincronizando estudiantes del apoderado: ${e.message}")
            e.printStackTrace()
        }
    }

    // =====================================================
    // ✅ SINCRONIZAR JUSTIFICACIONES PENDIENTES
    // =====================================================

    /**
     * ✅ Sincroniza las justificaciones pendientes desde Firestore a la base de datos local.
     * Mejorada con mejor manejo de errores y logs detallados
     */
    suspend fun syncPendingJustifications(teacherId: Int) {
        try {
            println("🔄 Iniciando sincronización de justificaciones para profesor ID: $teacherId")

            // 1. Obtener los códigos de clase del profesor
            val teacherClasses = classDao.getClassesForTeacher(teacherId)
            println("📚 Clases del profesor: ${teacherClasses.map { it.classCode }}")

            if (teacherClasses.isEmpty()) {
                println("⚠️ No se encontraron clases para el profesor")
                return
            }

            val classCodes = teacherClasses.map { it.classCode }

            // 2. Obtener estudiantes de esas clases
            val students = studentDao.getStudentsByClassCodes(classCodes)
            println("👥 Estudiantes encontrados: ${students.size}")

            // 3. Obtener los Firebase UIDs de los estudiantes
            val studentFirebaseUids = students.mapNotNull { it.firebaseUid }
            println("🔑 Firebase UIDs de estudiantes: $studentFirebaseUids")

            if (studentFirebaseUids.isEmpty()) {
                println("⚠️ No se encontraron Firebase UIDs para los estudiantes")
                return
            }

            // 4. Consultar Firestore: Justificaciones PENDIENTES
            println("☁️ Consultando justificaciones pendientes en Firestore...")

            // ✅ CORRECCIÓN: Dividir en lotes si hay más de 10 UIDs (límite de whereIn)
            val justificationsFromFirestore = mutableListOf<Pair<String, AbsenceJustificationEntity>>()

            studentFirebaseUids.chunked(10).forEach { uidBatch ->
                val firestoreSnapshot = firestore.collection("justifications")
                    .whereEqualTo("status", JustificationStatus.PENDING.name)
                    .whereIn("studentFirebaseUid", uidBatch)
                    .get()
                    .await()

                println("📥 Justificaciones recibidas en este lote: ${firestoreSnapshot.documents.size}")

                firestoreSnapshot.documents.forEach { doc ->
                    val justification = doc.toObject(AbsenceJustificationEntity::class.java)
                    if (justification != null) {
                        justificationsFromFirestore.add(doc.id to justification)
                        println("✅ Justificación encontrada: ID=${doc.id}, StudentUID=${justification.studentFirebaseUid}")
                    }
                }
            }

            println("📊 Total de justificaciones a sincronizar: ${justificationsFromFirestore.size}")

            // 5. Guardar/Actualizar en Room
            var syncedCount = 0
            justificationsFromFirestore.forEach { (remoteId, remoteJustification) ->
                try {
                    // Buscar si ya existe localmente
                    val localMatch = absenceJustificationDao.getJustificationByRemoteId(remoteId)

                    // Mapear el studentFirebaseUid al studentId local
                    val localStudent = students.find { it.firebaseUid == remoteJustification.studentFirebaseUid }

                    if (localStudent == null) {
                        println("⚠️ No se encontró estudiante local para Firebase UID: ${remoteJustification.studentFirebaseUid}")
                        return@forEach
                    }

                    val justificationToSave = remoteJustification.copy(
                        id = localMatch?.id ?: 0, // Mantener ID local si existe
                        studentId = localStudent.id, // ✅ Usar el ID local del estudiante
                        studentName = "${localStudent.firstName} ${localStudent.lastName}", // ✅ Agregar nombre
                        remoteId = remoteId,
                        syncStatus = SyncStatus.SYNCED
                    )

                    absenceJustificationDao.insertJustification(justificationToSave)
                    syncedCount++
                    println("💾 Justificación guardada: ID local=${justificationToSave.id}, Estudiante=${localStudent.firstName}")

                } catch (e: Exception) {
                    println("❌ Error guardando justificación $remoteId: ${e.message}")
                }
            }

            println("✅ Sincronización completada: $syncedCount/${justificationsFromFirestore.size} justificaciones guardadas")

        } catch (e: Exception) {
            println("❌ Error al sincronizar justificaciones pendientes: ${e.message}")
            e.printStackTrace()
            // No lanzamos la excepción para permitir que el repositorio use la caché local
        }
    }

    // =====================================================
    // SINCRONIZAR ANOTACIONES
    // =====================================================

    private fun syncAnnotations(firebaseUid: String, localProfileId: Int) {
        try {
            println("📝 Sincronizando anotaciones...")
            // TODO: Implementar si es necesario
        } catch (e: Exception) {
            println("❌ Error sincronizando anotaciones: ${e.message}")
        }
    }

    // =====================================================
    // MÉTODOS AUXILIARES Y UTILIDADES
    // =====================================================

    suspend fun getUserByFirebaseUid(firebaseUid: String): UserWithProfile? {
        val userEntity = userDao.getUserByFirebaseUid(firebaseUid) ?: return null
        val profileEntity = profileDao.getProfileByUserId(userEntity.id) ?: return null
        return UserWithProfile(userEntity, profileEntity)
    }

    suspend fun getClassByCode(classCode: String): ClassEntity? {
        return classDao.getClassByCode(classCode.uppercase())
    }

    suspend fun getStudentByRut(rut: String): StudentEntity? {
        return studentDao.getStudentByRut(rut)
    }

    /**
     * Busca una clase en la colección global "classes" de Firestore por código
     */
    suspend fun getClassFromFirestoreByCode(code: String): ClassEntity? {
        return try {
            val normalizedCode = code.uppercase()

            val snapshot = firestore.collection("classes")
                .whereEqualTo("code", normalizedCode)
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull() ?: return null

            ClassEntity(
                id = 0,
                className = doc.getString("name") ?: "",
                schoolName = doc.getString("school") ?: "",
                teacherId = 0,
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
    /**
     * ✅ Sincroniza la asistencia de todos los estudiantes del profesor
     */
    suspend fun syncTeacherAttendance(teacherId: Int) {
        try {
            println("📊 Sincronizando asistencia del profesor ID: $teacherId")

            // 1. Obtener las clases del profesor
            val teacherClasses = classDao.getClassesForTeacher(teacherId)
            println("📚 Clases encontradas: ${teacherClasses.size}")

            if (teacherClasses.isEmpty()) {
                println("⚠️ No se encontraron clases para el profesor")
                return
            }

            // 2. Obtener todos los estudiantes de esas clases
            val classCodes = teacherClasses.map { it.classCode }
            val students = studentDao.getStudentsByClassCodes(classCodes)
            println("👥 Estudiantes encontrados: ${students.size}")

            if (students.isEmpty()) {
                println("⚠️ No se encontraron estudiantes")
                return
            }

            // 3. Obtener los Firebase UIDs de los estudiantes
            val studentFirebaseUids = students.mapNotNull { it.firebaseUid }

            if (studentFirebaseUids.isEmpty()) {
                println("⚠️ No se encontraron Firebase UIDs")
                return
            }

            // 4. Consultar Firestore en lotes (máximo 10 por whereIn)
            val allAttendanceRecords = mutableListOf<Pair<String, Map<String, Any?>>>()

            studentFirebaseUids.chunked(10).forEach { uidBatch ->
                try {
                    val snapshot = firestore.collection("attendance")
                        .whereIn("studentFirebaseUid", uidBatch)
                        .get()
                        .await()

                    snapshot.documents.forEach { doc ->
                        val data = doc.data
                        if (data != null) {
                            allAttendanceRecords.add(doc.id to data)
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Error consultando lote: ${e.message}")
                }
            }

            println("📥 Registros de asistencia encontrados en Firestore: ${allAttendanceRecords.size}")

            // 5. Guardar/Actualizar en Room
            var syncedCount = 0
            allAttendanceRecords.forEach { (docId, data) ->
                try {
                    val studentFirebaseUid = data["studentFirebaseUid"] as? String ?: return@forEach
                    val teacherFirebaseUid = data["teacherFirebaseUid"] as? String
                    val statusString = data["status"] as? String ?: return@forEach

                    val status = try {
                        AttendanceStatus.valueOf(statusString)
                    } catch (e: Exception) {
                        AttendanceStatus.PRESENT
                    }

                    // Buscar el estudiante local
                    val localStudent = students.find { it.firebaseUid == studentFirebaseUid }
                    if (localStudent == null) {
                        println("⚠️ Estudiante no encontrado: $studentFirebaseUid")
                        return@forEach
                    }

                    // Buscar el profesor local (si existe)
                    val localTeacherId = if (teacherFirebaseUid != null) {
                        val teacherUser = userDao.getUserByFirebaseUid(teacherFirebaseUid)
                        teacherUser?.let { user ->
                            profileDao.getProfileByUserId(user.id)?.id
                        }
                    } else {
                        null
                    }

                    val attendanceEntity = AttendanceEntity(
                        studentId = localStudent.id,
                        teacherId = localTeacherId,
                        attendanceDate = data["attendanceDate"] as? Long ?: 0L,
                        status = status,
                        notes = data["notes"] as? String,
                        firestoreId = docId,
                        studentFirebaseUid = studentFirebaseUid,
                        teacherFirebaseUid = teacherFirebaseUid,
                        syncStatus = SyncStatus.SYNCED,
                        createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                        updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis(),
                        lastSyncedAt = System.currentTimeMillis()
                    )

                    // Verificar si ya existe
                    val existing = attendanceDao.getAttendanceByFirestoreId(docId)

                    if (existing == null) {
                        attendanceDao.insertAttendance(attendanceEntity)
                        syncedCount++
                        println("✅ Nueva asistencia sincronizada: ${localStudent.firstName}")
                    } else {
                        // Solo actualizar si el remoto es más reciente
                        if (attendanceEntity.updatedAt > existing.updatedAt) {
                            attendanceDao.updateAttendance(
                                attendanceEntity.copy(id = existing.id)
                            )
                            syncedCount++
                            println("🔄 Asistencia actualizada: ${localStudent.firstName}")
                        }
                    }

                } catch (e: Exception) {
                    println("❌ Error procesando registro: ${e.message}")
                    e.printStackTrace()
                }
            }

            println("✅ Sincronización de asistencia completada: $syncedCount registros")

        } catch (e: Exception) {
            println("❌ Error sincronizando asistencia del profesor: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * ✅ Sincroniza la asistencia de un estudiante específico
     */
    suspend fun syncStudentAttendance(studentId: Int) {
        try {
            val student = studentDao.getStudentById(studentId) ?: return
            val studentFirebaseUid = student.firebaseUid ?: return

            println("📥 Sincronizando asistencia del estudiante: ${student.firstName}")

            val snapshot = firestore.collection("attendance")
                .whereEqualTo("studentFirebaseUid", studentFirebaseUid)
                .get()
                .await()

            println("📊 Registros encontrados: ${snapshot.documents.size}")

            snapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach

                val statusString = data["status"] as? String ?: return@forEach
                val status = try {
                    AttendanceStatus.valueOf(statusString)
                } catch (e: Exception) {
                    AttendanceStatus.PRESENT
                }

                val teacherFirebaseUid = data["teacherFirebaseUid"] as? String
                val localTeacherId = if (teacherFirebaseUid != null) {
                    val teacherUser = userDao.getUserByFirebaseUid(teacherFirebaseUid)
                    teacherUser?.let { user ->
                        profileDao.getProfileByUserId(user.id)?.id
                    }
                } else {
                    null
                }

                val attendanceEntity = AttendanceEntity(
                    studentId = studentId,
                    teacherId = localTeacherId,
                    attendanceDate = data["attendanceDate"] as? Long ?: 0L,
                    status = status,
                    notes = data["notes"] as? String,
                    firestoreId = doc.id,
                    studentFirebaseUid = studentFirebaseUid,
                    teacherFirebaseUid = teacherFirebaseUid,
                    syncStatus = SyncStatus.SYNCED,
                    createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                    updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis(),
                    lastSyncedAt = System.currentTimeMillis()
                )

                val existing = attendanceDao.getAttendanceByFirestoreId(doc.id)

                if (existing == null) {
                    attendanceDao.insertAttendance(attendanceEntity)
                    println("✅ Asistencia sincronizada: ${doc.id}")
                } else if (attendanceEntity.updatedAt > existing.updatedAt) {
                    attendanceDao.updateAttendance(
                        attendanceEntity.copy(id = existing.id)
                    )
                    println("🔄 Asistencia actualizada: ${doc.id}")
                }
            }

        } catch (e: Exception) {
            println("❌ Error sincronizando asistencia del estudiante: ${e.message}")
            e.printStackTrace()
        }
    }



}