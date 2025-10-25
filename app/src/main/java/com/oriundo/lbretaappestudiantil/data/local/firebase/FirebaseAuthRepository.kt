package com.oriundo.lbretaappestudiantil.data.local.firebase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.data.local.models.UserEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.LoginCredentials
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.TeacherRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ FIREBASE AUTH REPOSITORY - IMPLEMENTACIÓN COMPLETA
 *
 * Firebase Auth + Firestore + Room como caché local
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val credentialManager = CredentialManager.create(context)

    companion object {
        // ⬇️ REEMPLAZA ESTO CON TU WEB CLIENT ID
        private const val WEB_CLIENT_ID = "362206226811-d6s2dpivfotbtipnpbq1v073ktmc8uog.apps.googleusercontent.com"

        // Colecciones de Firestore
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_PROFILES = "profiles"
        private const val COLLECTION_STUDENTS = "students"
        private const val COLLECTION_CLASSES = "classes"
        private const val COLLECTION_PARENT_STUDENT = "parent_student_relationships"
    }

    /**
     * ✅ GOOGLE SIGN-IN IMPLEMENTADO
     */
    override suspend fun loginWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile> {
        return try {
            // 1. Configurar Google ID Option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            // 2. Crear request de credenciales
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 3. Obtener credenciales del usuario
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // 4. Procesar la respuesta
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                try {
                    // 5. Extraer el ID Token
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // 6. Autenticar en Firebase
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val firebaseUser = authResult.user ?: return ApiResult.Error("No se pudo obtener el usuario de Firebase")

                    // 7. Verificar si el usuario ya existe en Firestore
                    val userDoc = firestore.collection(COLLECTION_USERS)
                        .document(firebaseUser.uid)
                        .get()
                        .await()

                    val userWithProfile = if (userDoc.exists()) {
                        // Usuario existente - obtener su perfil
                        getExistingUserProfile(firebaseUser.uid)
                    } else {
                        // Usuario nuevo - crear perfil
                        createNewUserProfile(
                            firebaseUid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName ?: "",
                            photoUrl = firebaseUser.photoUrl?.toString(),
                            isTeacher = isTeacher
                        )
                    }

                    ApiResult.Success(userWithProfile)

                } catch (e: GoogleIdTokenParsingException) {
                    ApiResult.Error("Error al procesar el token de Google: ${e.message}")
                }
            } else {
                ApiResult.Error("Tipo de credencial inesperado")
            }

        } catch (e: Exception) {
            ApiResult.Error("Error al iniciar sesión con Google: ${e.message}")
        }
    }

    /**
     * Obtener perfil de usuario existente desde Firestore
     */
    private suspend fun getExistingUserProfile(firebaseUid: String): UserWithProfile {
        // Obtener datos del usuario desde Firestore
        val userDoc = firestore.collection(COLLECTION_USERS)
            .document(firebaseUid)
            .get()
            .await()

        // Buscar perfil asociado
        val profileQuery = firestore.collection(COLLECTION_PROFILES)
            .whereEqualTo("firebaseUid", firebaseUid)
            .limit(1)
            .get()
            .await()

        val profileDoc = profileQuery.documents.firstOrNull()
            ?: throw Exception("No se encontró el perfil del usuario")

        // Convertir a entidades locales (Room)
        val userEntity = UserEntity(
            id = userDoc.getLong("localUserId")?.toInt() ?: 0,
            email = userDoc.getString("email") ?: "",
            passwordHash = "", // No guardamos password para usuarios de Google
            createdAt = userDoc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
            isActive = userDoc.getBoolean("isActive") ?: true
        )

        val profileEntity = ProfileEntity(
            id = profileDoc.id.hashCode(), // Usamos el hash del ID de Firestore
            userId = userEntity.id,
            firstName = profileDoc.getString("firstName") ?: "",
            lastName = profileDoc.getString("lastName") ?: "",
            phone = profileDoc.getString("phone"),
            address = profileDoc.getString("address"),
            photoUrl = profileDoc.getString("photoUrl"),
            isTeacher = profileDoc.getBoolean("isTeacher") ?: false,
            isParent = profileDoc.getBoolean("isParent") ?: false,
            createdAt = profileDoc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
        )

        return UserWithProfile(userEntity, profileEntity)
    }

    /**
     * Crear nuevo perfil de usuario en Firestore
     */
    private suspend fun createNewUserProfile(
        firebaseUid: String,
        email: String,
        displayName: String,
        photoUrl: String?,
        isTeacher: Boolean
    ): UserWithProfile {
        val localUserId = System.currentTimeMillis().toInt() // ID temporal

        // Separar nombre y apellido
        val nameParts = displayName.split(" ", limit = 2)
        val firstName = nameParts.getOrNull(0) ?: ""
        val lastName = nameParts.getOrNull(1) ?: ""

        // Crear documento de usuario en Firestore
        val userData = hashMapOf(
            "email" to email,
            "localUserId" to localUserId,
            "createdAt" to Timestamp.now(),
            "isActive" to true
        )

        firestore.collection(COLLECTION_USERS)
            .document(firebaseUid)
            .set(userData)
            .await()

        // Crear documento de perfil en Firestore
        val profileData = hashMapOf(
            "firebaseUid" to firebaseUid,
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to null,
            "address" to null,
            "photoUrl" to photoUrl,
            "isTeacher" to isTeacher,
            "isParent" to !isTeacher,
            "createdAt" to Timestamp.now()
        )

        val profileRef = firestore.collection(COLLECTION_PROFILES)
            .add(profileData)
            .await()

        // Convertir a entidades locales
        val userEntity = UserEntity(
            id = localUserId,
            email = email,
            passwordHash = "",
            createdAt = System.currentTimeMillis(),
            isActive = true
        )

        val profileEntity = ProfileEntity(
            id = profileRef.id.hashCode(),
            userId = localUserId,
            firstName = firstName,
            lastName = lastName,
            phone = null,
            address = null,
            photoUrl = photoUrl,
            isTeacher = isTeacher,
            isParent = !isTeacher,
            createdAt = System.currentTimeMillis()
        )

        return UserWithProfile(userEntity, profileEntity)
    }

    /**
     * ✅ LOGIN CON EMAIL Y PASSWORD
     */
    override suspend fun login(credentials: LoginCredentials): ApiResult<UserWithProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(credentials.email, credentials.password).await()
            val firebaseUser = authResult.user ?: return ApiResult.Error("No se pudo obtener el usuario")

            val userWithProfile = getExistingUserProfile(firebaseUser.uid)
            ApiResult.Success(userWithProfile)

        } catch (e: Exception) {
            ApiResult.Error("Error al iniciar sesión: ${e.message}")
        }
    }

    /**
     * ✅ REGISTRAR PROFESOR
     */
    override suspend fun registerTeacher(form: TeacherRegistrationForm): ApiResult<UserWithProfile> {
        return try {
            // 1. Crear usuario en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(form.email, form.password).await()
            val firebaseUser = authResult.user ?: return ApiResult.Error("No se pudo crear el usuario")

            // 2. Crear perfil
            val userWithProfile = createNewUserProfile(
                firebaseUid = firebaseUser.uid,
                email = form.email,
                displayName = "${form.firstName} ${form.lastName}",
                photoUrl = null,
                isTeacher = true
            )

            // 3. Actualizar datos adicionales del profesor
            val profileQuery = firestore.collection(COLLECTION_PROFILES)
                .whereEqualTo("firebaseUid", firebaseUser.uid)
                .limit(1)
                .get()
                .await()

            profileQuery.documents.firstOrNull()?.reference?.update(
                mapOf(
                    "phone" to form.phone,
                    "address" to form.address
                )
            )?.await()

            ApiResult.Success(userWithProfile)

        } catch (e: Exception) {
            ApiResult.Error("Error al registrar profesor: ${e.message}")
        }
    }

    /**
     * ✅ REGISTRAR APODERADO CON ESTUDIANTE
     */
    override suspend fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>> {
        return try {
            // 1. Verificar que la clase existe
            val classQuery = firestore.collection(COLLECTION_CLASSES)
                .whereEqualTo("classCode", studentForm.classCode)
                .limit(1)
                .get()
                .await()

            val classDoc = classQuery.documents.firstOrNull()
                ?: return ApiResult.Error("No se encontró una clase con ese código")

            // 2. Crear usuario en Firebase Auth (si tiene password)
            val firebaseUser = if (parentForm.password != null) {
                val authResult = auth.createUserWithEmailAndPassword(parentForm.email, parentForm.password).await()
                authResult.user ?: return ApiResult.Error("No se pudo crear el usuario")
            } else {
                // Si no tiene password, asumimos que ya está autenticado con Google
                auth.currentUser ?: return ApiResult.Error("Debe iniciar sesión con Google primero")
            }

            // 3. Crear perfil del apoderado
            val userWithProfile = createNewUserProfile(
                firebaseUid = firebaseUser.uid,
                email = parentForm.email,
                displayName = "${parentForm.firstName} ${parentForm.lastName}",
                photoUrl = null,
                isTeacher = false
            )

            // 4. Actualizar datos adicionales del apoderado
            val profileQuery = firestore.collection(COLLECTION_PROFILES)
                .whereEqualTo("firebaseUid", firebaseUser.uid)
                .limit(1)
                .get()
                .await()

            profileQuery.documents.firstOrNull()?.reference?.update(
                mapOf(
                    "phone" to parentForm.phone,
                    "address" to parentForm.address
                )
            )?.await()

            // 5. Crear estudiante en Firestore
            val studentData = hashMapOf(
                "classId" to classDoc.id,
                "rut" to studentForm.studentRut,
                "firstName" to studentForm.studentFirstName,
                "lastName" to studentForm.studentLastName,
                "birthDate" to studentForm.studentBirthDate?.let {
                    Timestamp(Date(it))
                },
                "photoUrl" to null,
                "enrollmentDate" to Timestamp.now(),
                "isActive" to true,
                "notes" to null
            )

            val studentRef = firestore.collection(COLLECTION_STUDENTS)
                .add(studentData)
                .await()

            // 6. Crear relación apoderado-estudiante
            val relationshipData = hashMapOf(
                "parentFirebaseUid" to firebaseUser.uid,
                "studentId" to studentRef.id,
                "relationshipType" to studentForm.relationshipType.name,
                "isPrimary" to studentForm.isPrimary
            )

            firestore.collection(COLLECTION_PARENT_STUDENT)
                .add(relationshipData)
                .await()

            // 7. Convertir a entidades locales
            val studentEntity = StudentEntity(
                id = studentRef.id.hashCode(),
                classId = classDoc.id.hashCode(),
                rut = studentForm.studentRut,
                firstName = studentForm.studentFirstName,
                lastName = studentForm.studentLastName,
                birthDate = studentForm.studentBirthDate,
                photoUrl = null,
                enrollmentDate = System.currentTimeMillis(),
                isActive = true,
                notes = null
            )

            val classEntity = ClassEntity(
                id = classDoc.id.hashCode(),
                className = classDoc.getString("className") ?: "",
                schoolName = classDoc.getString("schoolName") ?: "",
                teacherId = classDoc.getString("teacherFirebaseUid")?.hashCode() ?: 0,
                classCode = classDoc.getString("classCode") ?: "",
                gradeLevel = classDoc.getString("gradeLevel"),
                academicYear = classDoc.getString("academicYear") ?: "",
                isActive = classDoc.getBoolean("isActive") ?: true,
                createdAt = classDoc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
            )

            ApiResult.Success(Triple(userWithProfile, studentEntity, classEntity))

        } catch (e: Exception) {
            ApiResult.Error("Error al registrar apoderado: ${e.message}")
        }
    }

    /**
     * ✅ LOGOUT
     */
    override suspend fun logout() {
        try {
            auth.signOut()
        } catch (_: Exception) {
            // Ignoramos errores en logout
        }
    }

    /**
     * ✅ VERIFICAR SI EMAIL ESTÁ REGISTRADO
     */
    /**
     * ✅ VERIFICAR SI EMAIL ESTÁ REGISTRADO
     * Busca en Firestore en lugar de usar el método deprecado
     */
    override suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            val userQuery = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            userQuery.documents.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * ✅ OBTENER USUARIO ACTUAL
     */
    override suspend fun getCurrentUser(): UserWithProfile? {
        return try {
            val firebaseUser = auth.currentUser ?: return null
            getExistingUserProfile(firebaseUser.uid)
        } catch (_: Exception) {
            null
        }
    }
}