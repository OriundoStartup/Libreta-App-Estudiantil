package com.oriundo.lbretaappestudiantil.data.local.firebase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val localDatabaseRepository: LocalDatabaseRepository
) : AuthRepository {

    private val credentialManager = CredentialManager.create(context)

    // =====================================================
    // LOGIN CON GOOGLE (Sin cambios)
    // =====================================================
    override suspend fun loginWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("362206226811-d6s2dpivfotbtipnpbq1v073ktmc8uog.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = try {
                credentialManager.getCredential(request = request, context = context)
            } catch (e: GetCredentialException) {
                return ApiResult.Error("Error obteniendo credenciales de Google: ${e.message}")
            }

            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = credential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Usuario de Firebase no encontrado")

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!userDoc.exists()) {
                auth.signOut()
                return ApiResult.Error(
                    "Esta cuenta de Google no está registrada. Por favor, regístrate primero."
                )
            }

            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario sincronizado")

            ApiResult.Success(localUser)

        } catch (e: Exception) {
            ApiResult.Error("Error en login con Google: ${e.message}", e)
        }
    }

    // =====================================================
    // REGISTRO CON GOOGLE (PROFESORES) (Sin cambios)
    // =====================================================
    override suspend fun registerWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("362206226811-d6s2dpivfotbtipnpbq1v073ktmc8uog.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = try {
                credentialManager.getCredential(request = request, context = context)
            } catch (e: GetCredentialException) {
                return ApiResult.Error("Error obteniendo credenciales de Google: ${e.message}")
            }

            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = credential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Usuario de Firebase no encontrado")

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                auth.signOut()
                return ApiResult.Error(
                    "Esta cuenta de Google ya está registrada. Puedes iniciar sesión directamente."
                )
            }

            val userData = createBasicUserData(
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString(),
                isTeacher = isTeacher
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario sincronizado")

            ApiResult.Success(localUser)

        } catch (e: Exception) {
            ApiResult.Error("Error en registro con Google: ${e.message}", e)
        }
    }

    // =====================================================
    // ✅ MODIFICADO: REGISTRO DE APODERADO CON GOOGLE (CON TOKEN Y CONTRASEÑA)
    // =====================================================

    /**
     * ✅ MODIFICADO: Registra apoderado con Google usando un token existente
     * y vincula la contraseña proporcionada en el mismo paso.
     * Este método SÍ crea en Firebase (se llama desde el Paso 2).
     */
    suspend fun registerParentWithGoogle(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm,
        googleIdToken: String
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>> {

        // ✅ MODIFICACIÓN 1: Obtener la contraseña del formulario
        val password = parentForm.password

        // Validar que la contraseña venga, ya que el Paso 1 ahora la exige
        if (password == null || password.isBlank()) {
            return ApiResult.Error("La contraseña es requerida para el registro con Google.")
        }

        return try {
            // 1. Autenticar con Firebase usando el token
            val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(googleCredential).await()
            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Usuario de Firebase no encontrado")

            // 2. Verificar si ya existe (por seguridad)
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                auth.signOut()
                return ApiResult.Error("Esta cuenta de Google ya está registrada.")
            }

            // ✅ MODIFICACIÓN 2: Vincular la contraseña INMEDIATAMENTE
            try {
                // Crear credencial de email/password
                val emailCredential = EmailAuthProvider.getCredential(parentForm.email, password)

                // Vincular la credencial a la cuenta de Google recién autenticada
                firebaseUser.linkWithCredential(emailCredential).await()

            } catch (linkException: Exception) {
                // Si el enlace falla (p.ej., contraseña débil, email en uso), deshacer todo
                firebaseUser.delete().await() // Borrar usuario de Auth
                auth.signOut()
                return when (linkException) {
                    is FirebaseAuthUserCollisionException -> ApiResult.Error("Este email ya está registrado con otra cuenta")
                    is FirebaseAuthWeakPasswordException -> ApiResult.Error("La contraseña es demasiado débil. Debe tener al menos 6 caracteres")
                    else -> ApiResult.Error("Error vinculando contraseña: ${linkException.message}")
                }
            }

            // 3. Crear documento completo del apoderado en Firestore
            val userData = buildMap<String, Any> {
                put("email", firebaseUser.email ?: parentForm.email)
                put("firstName", parentForm.firstName)
                put("lastName", parentForm.lastName)
                put("phone", parentForm.phone)
                parentForm.address?.let { put("address", it) }
                put("classCode", studentForm.classCode)
                firebaseUser.photoUrl?.toString()?.let { put("photoUrl", it) }
                put("isTeacher", false)
                put("isParent", true)
                put("createdAt", System.currentTimeMillis())
                // ✅ MODIFICACIÓN 3: Marcar que SÍ tiene contraseña
                put("hasPassword", true)
            }

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            // 4. Crear estudiante en Firestore (sin cambios)
            val studentData = createStudentData(
                rut = studentForm.studentRut,
                firstName = studentForm.studentFirstName,
                lastName = studentForm.studentLastName,
                classCode = studentForm.classCode,
                birthDate = studentForm.studentBirthDate,
                relationshipType = studentForm.relationshipType,
                isPrimary = studentForm.isPrimary
            )

            val studentDocRef = firestore.collection("users")
                .document(firebaseUser.uid)
                .collection("students")
                .document()

            studentDocRef.set(studentData).await()

            // 5. Sincronizar todo a Room (sin cambios)
            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            // 6. Obtener datos sincronizados (sin cambios)
            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario")

            val classEntity = localDatabaseRepository.getClassByCode(studentForm.classCode)
                ?: return ApiResult.Error("Clase no encontrada: ${studentForm.classCode}")

            val studentEntity = localDatabaseRepository.getStudentByRut(studentForm.studentRut)
                ?: return ApiResult.Error("Estudiante no sincronizado")

            ApiResult.Success(Triple(localUser, studentEntity, classEntity))

        } catch (e: Exception) {
            // Captura general por si algo falla (p.ej. signInWithCredential)
            ApiResult.Error("Error en registro con Google: ${e.message}", e)
        }
    }

    // =====================================================
    // REGISTRO DE PROFESOR (Sin cambios)
    // =====================================================
    override suspend fun registerTeacher(form: TeacherRegistrationForm): ApiResult<UserWithProfile> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(form.email, form.password).await()
            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Error creando usuario")

            val userData = createTeacherData(
                email = form.email,
                firstName = form.firstName,
                lastName = form.lastName,
                phone = form.phone,
                address = form.address
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario registrado")

            ApiResult.Success(localUser)
        } catch (e: Exception) {
            ApiResult.Error("Error en registro de profesor: ${e.message}", e)
        }
    }

    // =====================================================
    // REGISTRO DE APODERADO (EMAIL/PASSWORD) (Sin cambios)
    // =====================================================
    override suspend fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>> {
        return try {
            val password = parentForm.password
                ?: return ApiResult.Error("La contraseña es requerida")

            val authResult = auth.createUserWithEmailAndPassword(parentForm.email, password).await()
            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Error creando usuario")

            // 1. Crear documento completo del apoderado en Firestore
            val userData = buildMap<String, Any> {
                put("email", parentForm.email)
                put("firstName", parentForm.firstName)
                put("lastName", parentForm.lastName)
                put("phone", parentForm.phone)
                parentForm.address?.let { put("address", it) }
                put("classCode", studentForm.classCode)
                put("isTeacher", false)
                put("isParent", true)
                put("createdAt", System.currentTimeMillis())
                put("hasPassword", true)
            }

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            // 2. Crear estudiante en Firestore
            val studentData = createStudentData(
                rut = studentForm.studentRut,
                firstName = studentForm.studentFirstName,
                lastName = studentForm.studentLastName,
                classCode = studentForm.classCode,
                birthDate = studentForm.studentBirthDate,
                relationshipType = studentForm.relationshipType,
                isPrimary = studentForm.isPrimary
            )

            val studentDocRef = firestore.collection("users")
                .document(firebaseUser.uid)
                .collection("students")
                .document()

            studentDocRef.set(studentData).await()

            // 3. Sincronizar todo a Room
            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            // 4. Obtener datos sincronizados
            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario")

            val classEntity = localDatabaseRepository.getClassByCode(studentForm.classCode)
                ?: return ApiResult.Error("Clase no encontrada: ${studentForm.classCode}")

            val studentEntity = localDatabaseRepository.getStudentByRut(studentForm.studentRut)
                ?: return ApiResult.Error("Estudiante no sincronizado")

            ApiResult.Success(Triple(localUser, studentEntity, classEntity))

        } catch (e: Exception) {
            ApiResult.Error("Error en registro de apoderado: ${e.message}", e)
        }
    }

    // =====================================================
    // LOGIN MANUAL (Sin cambios)
    // =====================================================
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

    // =====================================================
    // VINCULAR CONTRASEÑA A CUENTA DE GOOGLE (Sin cambios)
    // =implements ===================================
    override suspend fun linkPasswordToGoogleAccount(
        email: String,
        password: String
    ): ApiResult<UserWithProfile> {
        return try {
            val currentUser = auth.currentUser
                ?: return ApiResult.Error("No hay usuario autenticado")

            val hasGoogleProvider = currentUser.providerData.any {
                it.providerId == "google.com"
            }

            if (!hasGoogleProvider) {
                return ApiResult.Error("El usuario no se autenticó con Google")
            }

            val hasEmailProvider = currentUser.providerData.any {
                it.providerId == "password"
            }

            if (hasEmailProvider) {
                val localUser = localDatabaseRepository.getUserByFirebaseUid(currentUser.uid)
                    ?: return ApiResult.Error("Usuario no encontrado localmente")

                return ApiResult.Success(localUser)
            }

            val credential = EmailAuthProvider.getCredential(email, password)
            currentUser.linkWithCredential(credential).await()

            firestore.collection("users")
                .document(currentUser.uid)
                .update("hasPassword", true)
                .await()

            localDatabaseRepository.syncAllUserDataFromFirestore(currentUser.uid)

            val localUser = localDatabaseRepository.getUserByFirebaseUid(currentUser.uid)
                ?: return ApiResult.Error("Error al obtener usuario actualizado")

            ApiResult.Success(localUser)

        } catch (_: FirebaseAuthUserCollisionException) {
            ApiResult.Error("Este email ya está registrado con otra cuenta")
        } catch (_: FirebaseAuthWeakPasswordException) {
            ApiResult.Error("La contraseña es demasiado débil. Debe tener al menos 6 caracteres")
        } catch (e: Exception) {
            ApiResult.Error("Error vinculando contraseña: ${e.message}", e)
        }
    }

    // =====================================================
    // OTROS MÉTODOS (Sin cambios)
    // =====================================================
    override suspend fun hasPasswordLinked(): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            currentUser.providerData.any { it.providerId == "password" }
        } catch (_: Exception) {
            false
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

    // =====================================================
    // FUNCIONES AUXILIARES (Sin cambios)
    // =====================================================
    private fun createBasicUserData(
        email: String,
        displayName: String,
        photoUrl: String?,
        isTeacher: Boolean
    ): Map<String, Any> = buildMap {
        put("email", email)
        val nameParts = displayName.split(" ", limit = 2)
        put("firstName", nameParts.firstOrNull() ?: "")
        put("lastName", nameParts.getOrNull(1) ?: "")
        photoUrl?.let { put("photoUrl", it) }
        put("isTeacher", isTeacher)
        put("isParent", !isTeacher)
        put("createdAt", System.currentTimeMillis())
        put("hasPassword", false)
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
        put("createdAt", System.currentTimeMillis())
        put("hasPassword", true)
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