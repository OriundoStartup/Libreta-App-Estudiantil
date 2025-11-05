package com.oriundo.lbretaappestudiantil.data.local.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.LocalDatabaseRepository
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.LoginCredentials
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.TeacherRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val localDatabaseRepository: LocalDatabaseRepository
) : AuthRepository {

    override suspend fun login(credentials: LoginCredentials): ApiResult<UserWithProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(
                credentials.email,
                credentials.password
            ).await()

            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Usuario no encontrado")

            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario")

            ApiResult.Success(localUser)

        } catch (e: Exception) {
            ApiResult.Error("Error en login: ${e.message}", e)
        }
    }

    suspend fun authenticateWithGoogleToken(googleIdToken: String): ApiResult<UserWithProfile> {
        return try {
            Log.d("FirebaseAuth", "Iniciando autenticación con Google")

            val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(googleCredential).await()

            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Error al autenticar con Google")

            Log.d("FirebaseAuth", "Usuario autenticado: ${firebaseUser.uid}")

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                Log.d("FirebaseAuth", "Usuario existe en Firestore - Login exitoso")

                // ✅ AGREGAR TRY-CATCH Y LOGS
                try {
                    Log.d("FirebaseAuth", "Iniciando sincronización de datos...")
                    localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)
                    Log.d("FirebaseAuth", "Sincronización completada exitosamente")
                } catch (e: Exception) {
                    Log.e("FirebaseAuth", "ERROR en sincronización: ${e.message}", e)
                    e.printStackTrace()
                    return ApiResult.Error("Error al sincronizar datos: ${e.message}")
                }

                val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                    ?: return ApiResult.Error("No se pudo obtener el usuario después de sincronizar")

                Log.d("FirebaseAuth", "Usuario local obtenido: ID=${localUser.user.id}, ProfileID=${localUser.profile.id}")

                return ApiResult.Success(localUser)
            } else {
                Log.d("FirebaseAuth", "Usuario NO existe en Firestore - Eliminando cuenta temporal")

                try {
                    firebaseUser.delete().await()
                    Log.d("FirebaseAuth", "Cuenta temporal eliminada exitosamente")
                } catch (e: Exception) {
                    Log.e("FirebaseAuth", "Error al eliminar cuenta temporal: ${e.message}")
                }

                Log.d("FirebaseAuth", "Retornando NEEDS_REGISTRATION")
                return ApiResult.Error("NEEDS_REGISTRATION")
            }

        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error en authenticateWithGoogleToken: ${e.message}")
            ApiResult.Error("Error con Google: ${e.message}", e)
        }
    }

    override suspend fun registerTeacher(form: TeacherRegistrationForm): ApiResult<UserWithProfile> {
        return try {
            val trimmedEmail = form.email.trim().lowercase()
            val password = form.password

            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()

            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Error al crear usuario en Firebase")

            val userData = createTeacherData(
                email = trimmedEmail,
                firstName = form.firstName.trim(),
                lastName = form.lastName.trim(),
                phone = form.phone.trim(),
                address = form.address?.trim()
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario sincronizado")

            ApiResult.Success(localUser)

        } catch (_: FirebaseAuthUserCollisionException) {
            ApiResult.Error("Este email ya está registrado")
        } catch (_: FirebaseAuthWeakPasswordException) {
            ApiResult.Error("La contraseña es demasiado débil. Debe tener al menos 6 caracteres")
        } catch (e: Exception) {
            ApiResult.Error("Error en registro: ${e.message}", e)
        }
    }

    suspend fun registerTeacherWithGoogle(
        teacherForm: TeacherRegistrationForm,
        googleIdToken: String
    ): ApiResult<UserWithProfile> {
        return try {
            val trimmedEmail = teacherForm.email.trim().lowercase()
            val password = teacherForm.password

            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()

            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Error al crear usuario")

            try {
                val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                firebaseUser.linkWithCredential(googleCredential).await()
            } catch (_: Exception) {
            }

            val googlePhotoUrl = try {
                firebaseUser.photoUrl?.toString()
            } catch (_: Exception) {
                null
            }

            val userData = buildMap<String, Any> {
                put("email", trimmedEmail)
                put("firstName", teacherForm.firstName.trim())
                put("lastName", teacherForm.lastName.trim())
                put("phone", teacherForm.phone.trim())
                teacherForm.address?.trim()?.let { put("address", it) }
                googlePhotoUrl?.let { put("photoUrl", it) }
                put("isTeacher", true)
                put("isParent", false)
                put("hasPassword", true)
                put("createdAt", System.currentTimeMillis())
            }

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario sincronizado")

            ApiResult.Success(localUser)

        } catch (_: FirebaseAuthUserCollisionException) {
            ApiResult.Error("Este email ya está registrado")
        } catch (_: FirebaseAuthWeakPasswordException) {
            ApiResult.Error("La contraseña es demasiado débil. Debe tener al menos 6 caracteres")
        } catch (e: Exception) {
            ApiResult.Error("Error en registro: ${e.message}", e)
        }
    }

    override suspend fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>> {
        val password = parentForm.password
            ?: return ApiResult.Error("La contraseña es requerida")

        return try {
            val classCode = studentForm.classCode.trim().uppercase()

            // ✅ 1. Verificar que la clase existe ANTES de crear el usuario
            val classSnapshot = firestore.collection("classes")
                .whereEqualTo("code", classCode)  // ✅ Campo correcto
                .limit(1)
                .get()
                .await()

            if (classSnapshot.isEmpty) {
                return ApiResult.Error("El código de clase '$classCode' no existe. Verifica con tu profesor.")
            }

            val classDoc = classSnapshot.documents.first()
            val classId = classDoc.id

            // ✅ 2. Crear usuario en Firebase Auth
            val trimmedEmail = parentForm.email.trim().lowercase()
            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()

            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Error al crear usuario")

            try {
                // ✅ 3. Forzar obtención del token de autenticación
                firebaseUser.getIdToken(true).await()
                Log.d("FirebaseAuth", "Token de autenticación obtenido para: ${firebaseUser.uid}")

                // ✅ 4. Preparar datos del padre
                val parentData = buildMap<String, Any> {
                    put("email", trimmedEmail)
                    put("firstName", parentForm.firstName.trim())
                    put("lastName", parentForm.lastName.trim())
                    put("phone", parentForm.phone.trim())
                    parentForm.address?.trim()?.let { put("address", it) }
                    put("isTeacher", false)
                    put("isParent", true)
                    put("hasPassword", true)
                    put("createdAt", System.currentTimeMillis())
                }

                // ✅ 5. Escribir datos del padre en Firestore
                // El usuario YA está autenticado, así que tiene permisos
                Log.d("FirebaseAuth", "Intentando escribir en Firestore para: ${firebaseUser.uid}")
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(parentData)
                    .await()
                Log.d("FirebaseAuth", "Datos del padre escritos exitosamente")

                // ✅ 5. Crear datos del estudiante
                val studentData = createStudentData(
                    rut = studentForm.studentRut.trim(),
                    firstName = studentForm.studentFirstName.trim(),
                    lastName = studentForm.studentLastName.trim(),
                    classCode = classCode,
                    birthDate = studentForm.studentBirthDate,
                    relationshipType = studentForm.relationshipType,
                    isPrimary = studentForm.isPrimary
                )

                // ✅ 6. Agregar estudiante a la subcolección del padre
                Log.d("FirebaseAuth", "Creando estudiante en subcolección")
                val studentDocRef = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .collection("students")
                    .add(studentData)
                    .await()
                Log.d("FirebaseAuth", "Estudiante creado con ID: ${studentDocRef.id}")

                // ✅ 7. Vincular estudiante a la clase
                Log.d("FirebaseAuth", "Vinculando estudiante a la clase")
                firestore.collection("classes")
                    .document(classId)
                    .collection("students")
                    .document(studentDocRef.id)
                    .set(mapOf(
                        "studentId" to studentDocRef.id,
                        "parentId" to firebaseUser.uid,
                        "joinedAt" to System.currentTimeMillis()
                    ))
                    .await()
                Log.d("FirebaseAuth", "Estudiante vinculado a la clase exitosamente")

                // ✅ 8. Sincronizar con la base de datos local
                Log.d("FirebaseAuth", "Sincronizando datos locales")
                localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

                val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                    ?: return ApiResult.Error("No se pudo obtener el usuario sincronizado")

                val studentEntity = localDatabaseRepository.getStudentByRut(studentForm.studentRut)
                    ?: return ApiResult.Error("No se pudo obtener el estudiante")

                val classEntity = localDatabaseRepository.getClassByCode(classCode)
                    ?: return ApiResult.Error("No se pudo obtener la clase")

                Log.d("FirebaseAuth", "Registro completado exitosamente")
                ApiResult.Success(Triple(localUser, studentEntity, classEntity))

            } catch (e: Exception) {
                // ✅ Si algo falla después de crear el usuario, eliminarlo
                Log.e("FirebaseAuth", "Error después de crear usuario: ${e.message}")
                try {
                    firebaseUser.delete().await()
                    Log.d("FirebaseAuth", "Usuario eliminado después de error")
                } catch (deleteError: Exception) {
                    Log.e("FirebaseAuth", "No se pudo eliminar usuario: ${deleteError.message}")
                }
                throw e
            }

        } catch (_: FirebaseAuthUserCollisionException) {
            ApiResult.Error("Este email ya está registrado")
        } catch (_: FirebaseAuthWeakPasswordException) {
            ApiResult.Error("La contraseña es demasiado débil. Debe tener al menos 6 caracteres")
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error en registerParent: ${e.message}", e)
            ApiResult.Error("Error en registro: ${e.message}", e)
        }
    }

    suspend fun registerParentWithGoogle(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm,
        googleIdToken: String
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>> {
        val password = parentForm.password
            ?: return ApiResult.Error("La contraseña es requerida")

        return try {
            val classCode = studentForm.classCode.trim().uppercase()

            //  1. Verificar que la clase existe ANTES de crear el usuario
            val classSnapshot = firestore.collection("classes")
                .whereEqualTo("code", classCode)  // ✅ Campo correcto
                .limit(1)
                .get()
                .await()

            if (classSnapshot.isEmpty) {
                return ApiResult.Error("El código de clase '$classCode' no existe. Verifica con tu profesor.")
            }

            val classDoc = classSnapshot.documents.first()
            val classId = classDoc.id

            // ✅ 2. Crear usuario en Firebase Auth
            val trimmedEmail = parentForm.email.trim().lowercase()
            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()

            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Error al crear usuario")

            try {
                // ✅ 3. Vincular con Google (opcional, no crítico)
                try {
                    val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    firebaseUser.linkWithCredential(googleCredential).await()
                    Log.d("FirebaseAuth", "Cuenta vinculada con Google exitosamente")
                } catch (linkError: Exception) {
                    Log.w("FirebaseAuth", "No se pudo vincular con Google: ${linkError.message}")
                }

                // ✅ 4. Forzar obtención del token de autenticación
                firebaseUser.getIdToken(true).await()
                Log.d("FirebaseAuth", "Token de autenticación obtenido para: ${firebaseUser.uid}")

                // ✅ 5. Obtener foto de Google si está disponible
                val googlePhotoUrl = try {
                    firebaseUser.photoUrl?.toString()
                } catch (_: Exception) {
                    null
                }

                // ✅ 6. Preparar datos del padre
                val parentData = buildMap<String, Any> {
                    put("email", trimmedEmail)
                    put("firstName", parentForm.firstName.trim())
                    put("lastName", parentForm.lastName.trim())
                    put("phone", parentForm.phone.trim())
                    parentForm.address?.trim()?.let { put("address", it) }
                    googlePhotoUrl?.let { put("photoUrl", it) }
                    put("isTeacher", false)
                    put("isParent", true)
                    put("hasPassword", true)
                    put("createdAt", System.currentTimeMillis())
                }

                // ✅ 7. Escribir datos del padre en Firestore
                // El usuario YA está autenticado, así que tiene permisos
                Log.d("FirebaseAuth", "Intentando escribir en Firestore para: ${firebaseUser.uid}")
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(parentData)
                    .await()
                Log.d("FirebaseAuth", "Datos del padre escritos exitosamente")

                // ✅ 7. Crear datos del estudiante
                val studentData = createStudentData(
                    rut = studentForm.studentRut.trim(),
                    firstName = studentForm.studentFirstName.trim(),
                    lastName = studentForm.studentLastName.trim(),
                    classCode = classCode,
                    birthDate = studentForm.studentBirthDate,
                    relationshipType = studentForm.relationshipType,
                    isPrimary = studentForm.isPrimary
                )

                // ✅ 8. Agregar estudiante a la subcolección del padre
                Log.d("FirebaseAuth", "Creando estudiante en subcolección")
                val studentDocRef = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .collection("students")
                    .add(studentData)
                    .await()
                Log.d("FirebaseAuth", "Estudiante creado con ID: ${studentDocRef.id}")

                // ✅ 9. Vincular estudiante a la clase
                Log.d("FirebaseAuth", "Vinculando estudiante a la clase")
                firestore.collection("classes")
                    .document(classId)
                    .collection("students")
                    .document(studentDocRef.id)
                    .set(mapOf(
                        "studentId" to studentDocRef.id,
                        "parentId" to firebaseUser.uid,
                        "joinedAt" to System.currentTimeMillis()
                    ))
                    .await()
                Log.d("FirebaseAuth", "Estudiante vinculado a la clase exitosamente")

                // ✅ 10. Sincronizar con la base de datos local
                Log.d("FirebaseAuth", "Sincronizando datos locales")
                localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

                val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                    ?: return ApiResult.Error("No se pudo obtener el usuario sincronizado")

                val studentEntity = localDatabaseRepository.getStudentByRut(studentForm.studentRut)
                    ?: return ApiResult.Error("No se pudo obtener el estudiante")

                val classEntity = localDatabaseRepository.getClassByCode(classCode)
                    ?: return ApiResult.Error("No se pudo obtener la clase")

                Log.d("FirebaseAuth", "Registro con Google completado exitosamente")
                ApiResult.Success(Triple(localUser, studentEntity, classEntity))

            } catch (e: Exception) {
                // ✅ Si algo falla después de crear el usuario, eliminarlo
                Log.e("FirebaseAuth", "Error después de crear usuario con Google: ${e.message}")
                try {
                    firebaseUser.delete().await()
                    Log.d("FirebaseAuth", "Usuario eliminado después de error")
                } catch (deleteError: Exception) {
                    Log.e("FirebaseAuth", "No se pudo eliminar usuario: ${deleteError.message}")
                }
                throw e
            }

        } catch (_: FirebaseAuthUserCollisionException) {
            ApiResult.Error("Este email ya está registrado")
        } catch (_: FirebaseAuthWeakPasswordException) {
            ApiResult.Error("La contraseña es demasiado débil. Debe tener al menos 6 caracteres")
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error en registerParentWithGoogle: ${e.message}", e)
            ApiResult.Error("Error en registro: ${e.message}", e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): ApiResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("no user record", ignoreCase = true) == true -> {
                    ApiResult.Error("No existe una cuenta con este email.")
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    ApiResult.Error("Error de conexión. Verifica tu internet.")
                }
                e.message?.contains("invalid-email", ignoreCase = true) == true -> {
                    ApiResult.Error("El formato del email no es válido.")
                }
                else -> {
                    ApiResult.Error("Error al enviar email: ${e.message}")
                }
            }
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email.trim().lowercase())
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getCurrentUser(): UserWithProfile? {
        val firebaseUser = auth.currentUser ?: return null
        return localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
    }

    private fun createTeacherData(
        email: String,
        firstName: String,
        lastName: String,
        phone: String,
        address: String?
    ): Map<String, Any> = buildMap {
        put("email", email)
        put("firstName", firstName)
        put("lastName", lastName)
        put("phone", phone)
        address?.let { put("address", it) }
        put("isTeacher", true)
        put("isParent", false)
        put("hasPassword", true)
        put("createdAt", System.currentTimeMillis())
    }

    private fun createStudentData(
        rut: String,
        firstName: String,
        lastName: String,
        classCode: String,
        birthDate: Long?,
        relationshipType: RelationshipType,
        isPrimary: Boolean
    ): Map<String, Any> = buildMap {
        put("rut", rut)
        put("firstName", firstName)
        put("lastName", lastName)
        put("classCode", classCode)
        birthDate?.let { put("birthDate", it) }
        put("relationshipType", relationshipType.name)
        put("isPrimary", isPrimary)
        put("createdAt", System.currentTimeMillis())
    }
}