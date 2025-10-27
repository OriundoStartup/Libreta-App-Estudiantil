package com.oriundo.lbretaappestudiantil.data.local.firebase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.LocalDatabaseRepository
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
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
) : AuthRepository { // 👈 IMPLEMENTA LA INTERFAZ COMPLETA

    private val credentialManager = CredentialManager.create(context)

    // =====================================================
    // LOGIN CON GOOGLE (IMPLEMENTACIÓN ÚNICA DE FIREBASE)
    // =====================================================

    override suspend fun loginWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile> {
        return try {
            // 1. Configurar Google Sign-In
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("362206226811-d6s2dpivfotbtipnpbq1v073ktmc8uog.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 2. Obtener credenciales de Google (Manejo de UI)
            val result = try {
                credentialManager.getCredential(request = request, context = context)
            } catch (e: GetCredentialException) {
                return ApiResult.Error("Error obteniendo credenciales de Google: ${e.message}")
            }

            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = credential.idToken

            // 3. Autenticar con Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user ?: return ApiResult.Error("Usuario de Firebase no encontrado")

            // 4. Verificar/Crear en Firestore (Si es nuevo)
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!userDoc.exists()) {
                // Asumir que si usa Google, NO es registro de estudiante/padre
                val isTeacherUser = isTeacher // Se define desde la UI

                val userData = hashMapOf(
                    "email" to firebaseUser.email,
                    "firstName" to (firebaseUser.displayName?.split(" ")?.firstOrNull() ?: ""),
                    "lastName" to (firebaseUser.displayName?.split(" ")?.lastOrNull() ?: ""),
                    "photoUrl" to firebaseUser.photoUrl?.toString(),
                    "isTeacher" to isTeacherUser,
                    "isParent" to !isTeacherUser,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(userData)
                    .await()
            }

            // 5. SINCRONIZAR A ROOM
            val syncResult = localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)
            if (syncResult is ApiResult.Error) {
                // Advertencia: Puede que falten datos, pero el usuario está logueado
                println("⚠️ Advertencia: No se pudo sincronizar completamente los datos: ${syncResult.message}")
            }

            // 6. Obtener datos sincronizados de Room
            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario sincronizado")

            ApiResult.Success(localUser)

        } catch (e: Exception) {
            ApiResult.Error("Error en login con Google: ${e.message}", e)
        }
    }

    // =====================================================
    // REGISTRO DE PROFESOR (Implementación de AuthRepository)
    // =====================================================

    override suspend fun registerTeacher(form: TeacherRegistrationForm): ApiResult<UserWithProfile> {
        return try {
            // 1. Crear usuario en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(form.email, form.password).await()
            val firebaseUser = authResult.user ?: return ApiResult.Error("Error creando usuario")

            // 2. Crear en Firestore
            val userData = hashMapOf(
                "email" to form.email,
                "firstName" to form.firstName,
                "lastName" to form.lastName,
                "phone" to form.phone,
                "address" to form.address,
                "isTeacher" to true,
                "isParent" to false,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            // 3. SINCRONIZAR AUTOMÁTICAMENTE A ROOM
            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            // 4. Obtener datos sincronizados
            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario registrado")

            ApiResult.Success(localUser)
        } catch (e: Exception) {
            ApiResult.Error("Error en registro de profesor (Firebase): ${e.message}", e)
        }
    }

    // ===================================================================================
    // REGISTRO DE APODERADO CON ESTUDIANTE (Implementación de AuthRepository)
    // ===================================================================================

    // ⚠️ NOTA: El retorno DEBE coincidir con el de la interfaz,
    //            que ahora es ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>>

    override suspend fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>> {
        return try {
            // 1. Crear usuario en Firebase Auth
            val password = parentForm.password
                ?: return ApiResult.Error("La contraseña es requerida para el registro manual con Firebase.")

            val authResult = auth.createUserWithEmailAndPassword(parentForm.email, password).await()
            val firebaseUser = authResult.user ?: return ApiResult.Error("Error creando usuario")

            // 2. Crear Perfil de Apoderado en Firestore
            val apoderadoData = hashMapOf(
                "email" to parentForm.email,
                "firstName" to parentForm.firstName,
                "lastName" to parentForm.lastName,
                "phone" to parentForm.phone,
                "address" to parentForm.address,
                "isTeacher" to false,
                "isParent" to true,
                "classCode" to studentForm.classCode, // Código de clase del estudiante
                "createdAt" to System.currentTimeMillis()
            )

            // Guardar Apoderado
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(apoderadoData)
                .await()

            // 3. Crear Estudiante asociado al Apoderado en Firestore (como subcolección)
            val studentData = hashMapOf(
                "rut" to studentForm.studentRut,
                "firstName" to studentForm.studentFirstName,
                "lastName" to studentForm.studentLastName,
                "classCode" to studentForm.classCode,
                "birthDate" to studentForm.studentBirthDate,
                // Puedes añadir la relación aquí si es necesario
                "relationshipType" to studentForm.relationshipType,
                "isPrimary" to studentForm.isPrimary
            )

            val studentDocRef = firestore.collection("users")
                .document(firebaseUser.uid)
                .collection("students")
                .document() // Firestore ID autogenerado para el estudiante

            studentDocRef.set(studentData).await()


            // 4. SINCRONIZAR A ROOM
            val syncResult = localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            if (syncResult is ApiResult.Error) {
                println("⚠️ Advertencia: No se pudo sincronizar: ${syncResult.message}")
            }

            // 5. Obtener datos sincronizados (Requiere que LocalDatabaseRepository devuelva el Triple)
            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario registrado")

            // ⚠️ FALTA: Necesitas que LocalDatabaseRepository tenga un método para
            //            recuperar el estudiante y la clase recién creados.
            //            Por ahora, usaremos los datos locales después de la sincronización.

            val classEntity = localDatabaseRepository.getClassByCode(studentForm.classCode)
                ?: return ApiResult.Error("Clase no sincronizada después del registro")

            val studentEntity = localDatabaseRepository.getStudentByRut(studentForm.studentRut)
                ?: return ApiResult.Error("Estudiante no sincronizado después del registro")

            ApiResult.Success(Triple(localUser, studentEntity, classEntity))
        } catch (e: Exception) {
            ApiResult.Error("Error en registro de apoderado (Firebase): ${e.message}", e)
        }
    }

    // =====================================================
    // LOGIN MANUAL (Implementación de AuthRepository)
    // =====================================================

    override suspend fun login(credentials: LoginCredentials): ApiResult<UserWithProfile> {
        return try {
            // 1. Autenticar con Firebase
            val authResult = auth.signInWithEmailAndPassword(credentials.email, credentials.password).await()
            val firebaseUser = authResult.user ?: return ApiResult.Error("Usuario no encontrado")

            // 2. SINCRONIZAR AUTOMÁTICAMENTE
            localDatabaseRepository.syncAllUserDataFromFirestore(firebaseUser.uid)

            // 3. Obtener datos sincronizados
            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("No se pudo obtener el usuario")

            ApiResult.Success(localUser)

        } catch (e: Exception) {
            ApiResult.Error("Error en login (Firebase): ${e.message}", e)
        }
    }

    // =====================================================
    // OTROS MÉTODOS REQUERIDOS POR AuthRepository
    // =====================================================

    override suspend fun logout() {
        auth.signOut()
    }

    // Dentro de FirebaseAuthRepository.kt

    override suspend fun isEmailRegistered(email: String): Boolean {
        // Intenta buscar el usuario en Firestore directamente por el email
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email.trim().lowercase()) // Asume que guardas el email
                .limit(1)
                .get()
                .await()

            // El email está registrado si Firestore devuelve al menos un documento
            !snapshot.isEmpty
        } catch (_: Exception) {
            // En caso de error de conexión/permisos, asumimos que no está registrado para prevenir
            false
        }
    }

    override suspend fun getCurrentUser(): UserWithProfile? {
        val firebaseUser = auth.currentUser ?: return null

        // Intenta obtenerlo localmente primero
        return localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
    }
}