package com.oriundo.lbretaappestudiantil.data.firebase

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.LoginCredentials
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.TeacherRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ FIREBASE AUTH REPOSITORY - VERSIÓN CORREGIDA
 *
 * Esta versión NO lanza excepciones, sino que retorna ApiResult.Error
 * para que la app no se cierre cuando se presiona el botón de Google Sign-In.
 *
 * TODOS los métodos retornan errores informativos en lugar de crashear.
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    // El Credential Manager se inicializa aquí
    private val credentialManager = CredentialManager.create(context)

    /**
     * ✅ CORREGIDO: Ya no lanza NotImplementedError
     * Ahora retorna un error manejable que se muestra en la UI
     */
    override suspend fun loginWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile> {
        return try {
            // TODO: Implementar la lógica real de Google Sign-In con Credential Manager
            // 1. Configurar GetGoogleIdOption
            // 2. Obtener credentials con credentialManager.getCredential()
            // 3. Extraer el ID Token
            // 4. Autenticar en Firebase con auth.signInWithCredential()
            // 5. Crear/actualizar perfil en Firestore
            // 6. Retornar ApiResult.Success(userWithProfile)

            ApiResult.Error(
                "Google Sign-In no está implementado aún.\n\n" +
                        "Por favor, usa el registro manual con email y contraseña.\n\n" +
                        "Para implementar Google Sign-In necesitas:\n" +
                        "1. Configurar Firebase en tu proyecto\n" +
                        "2. Agregar google-services.json\n" +
                        "3. Implementar Credential Manager API\n" +
                        "4. Completar la lógica en FirebaseAuthRepository"
            )
        } catch (e: Exception) {
            ApiResult.Error("Error al intentar iniciar sesión con Google: ${e.message}")
        }
    }

    /**
     * ✅ CORREGIDO: Ya no lanza NotImplementedError
     */
    override suspend fun login(credentials: LoginCredentials): ApiResult<UserWithProfile> {
        return try {
            // TODO: Implementar con Firebase Authentication
            // auth.signInWithEmailAndPassword(credentials.email, credentials.password)
            // Luego obtener el perfil de Firestore

            ApiResult.Error(
                "El login con Firebase no está implementado aún.\n\n" +
                        "Actualmente solo funciona el registro/login con la base de datos local (Room).\n\n" +
                        "Por favor, asegúrate de que tu módulo de Hilt esté configurado para usar AuthRepositoryImpl en lugar de FirebaseAuthRepository."
            )
        } catch (e: Exception) {
            ApiResult.Error("Error al iniciar sesión: ${e.message}")
        }
    }

    /**
     * ✅ CORREGIDO: Ya no lanza NotImplementedError
     */
    override suspend fun registerTeacher(form: TeacherRegistrationForm): ApiResult<UserWithProfile> {
        return try {
            // TODO: Implementar con Firebase Authentication y Firestore
            // 1. auth.createUserWithEmailAndPassword(form.email, form.password)
            // 2. Crear documento de perfil en Firestore collection "profiles"
            // 3. Retornar ApiResult.Success(userWithProfile)

            ApiResult.Error(
                "El registro de profesores con Firebase no está implementado aún.\n\n" +
                        "Por favor, asegúrate de que tu módulo de Hilt esté configurado para usar AuthRepositoryImpl en lugar de FirebaseAuthRepository."
            )
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar profesor: ${e.message}")
        }
    }

    /**
     * ✅ CORREGIDO: Ya no lanza NotImplementedError
     */
    override suspend fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>> {
        return try {
            // TODO: Implementar con Firebase Authentication y Firestore
            // 1. auth.createUserWithEmailAndPassword() o usar cuenta de Google existente
            // 2. Crear documento de perfil en Firestore
            // 3. Crear documento de estudiante en Firestore
            // 4. Crear relación apoderado-estudiante
            // 5. Retornar ApiResult.Success(triple)

            ApiResult.Error(
                "El registro de apoderados con Firebase no está implementado aún.\n\n" +
                        "Por favor, asegúrate de que tu módulo de Hilt esté configurado para usar AuthRepositoryImpl en lugar de FirebaseAuthRepository."
            )
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar apoderado: ${e.message}")
        }
    }

    /**
     * ✅ CORREGIDO: Ya no lanza NotImplementedError
     */
    override suspend fun logout() {
        try {
            // Esta función SÍ podemos implementarla porque es simple
            auth.signOut()
            // TODO: También limpiar estado de Credential Manager si es necesario
        } catch (e: Exception) {
            // Ignoramos errores en logout, no es crítico
        }
    }

    /**
     * ✅ CORREGIDO: Ya no lanza NotImplementedError
     */
    override suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            // TODO: Verificar en Firestore si existe un usuario con este email
            // Por ahora retornamos false para no bloquear el flujo
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * ✅ CORREGIDO: Ya no lanza NotImplementedError
     */
    override suspend fun getCurrentUser(): UserWithProfile? {
        return try {
            // TODO: Obtener auth.currentUser y su perfil de Firestore
            // val firebaseUser = auth.currentUser ?: return null
            // val profile = firestore.collection("profiles").document(firebaseUser.uid).get()
            // return UserWithProfile(...)

            null // Por ahora retornamos null
        } catch (e: Exception) {
            null
        }
    }
}