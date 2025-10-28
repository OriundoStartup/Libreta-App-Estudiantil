package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentParentRelationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.UserDao
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentParentRelation
import com.oriundo.lbretaappestudiantil.data.local.models.SyncStatus
import com.oriundo.lbretaappestudiantil.data.local.models.UserEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.LoginCredentials
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.TeacherRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    private val classDao: ClassDao,
    private val studentDao: StudentDao,
    private val studentParentRelationDao: StudentParentRelationDao
) : AuthRepository {

    private var currentUserWithProfile: UserWithProfile? = null

    // =====================================================
    // LOGIN MANUAL
    // =====================================================

    override suspend fun login(credentials: LoginCredentials): ApiResult<UserWithProfile> {
        return try {
            if (credentials.email.isBlank() || credentials.password.isBlank()) {
                return ApiResult.Error("Email y contraseña son requeridos")
            }

            val normalizedEmail = credentials.email.trim().lowercase()
            val user = userDao.getUserByEmail(normalizedEmail)
                ?: return ApiResult.Error("Email o contraseña incorrectos")

            val passwordHash = hashPassword(credentials.password)
            if (user.passwordHash != passwordHash) {
                return ApiResult.Error("Email o contraseña incorrectos")
            }

            if (!user.isActive) {
                return ApiResult.Error("Usuario desactivado")
            }

            val profile = profileDao.getProfileByUserId(user.id)
                ?: return ApiResult.Error("Perfil no encontrado")

            val userWithProfile = UserWithProfile(user, profile)
            currentUserWithProfile = userWithProfile

            ApiResult.Success(userWithProfile)
        } catch (e: Exception) {
            ApiResult.Error("Error al iniciar sesión: ${e.message}", e)
        }
    }

    // =====================================================
    // REGISTRO DE PROFESOR
    // =====================================================

    override suspend fun registerTeacher(form: TeacherRegistrationForm): ApiResult<UserWithProfile> {
        return try {
            validateTeacherForm(form)?.let { return it }

            if (isEmailRegistered(form.email)) {
                return ApiResult.Error("El email ya está registrado")
            }

            val user = UserEntity(
                email = form.email.trim().lowercase(),
                passwordHash = hashPassword(form.password),
                firebaseUid = null,
                syncStatus = SyncStatus.PENDING,
                lastSyncedAt = null
            )
            val userId = userDao.insertUser(user).toInt()

            val profile = ProfileEntity(
                userId = userId,
                firstName = form.firstName.trim(),
                lastName = form.lastName.trim(),
                phone = form.phone.trim(),
                address = form.address?.trim(),
                photoUrl = null,
                isTeacher = true,
                isParent = false,
                firestoreId = null,
                syncStatus = SyncStatus.PENDING,
                firebaseUid = null,
                lastSyncedAt = null
            )
            val profileId = profileDao.insertProfile(profile).toInt()

            val createdUser = user.copy(id = userId)
            val createdProfile = profile.copy(id = profileId)
            val userWithProfile = UserWithProfile(createdUser, createdProfile)

            currentUserWithProfile = userWithProfile

            ApiResult.Success(userWithProfile)
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar profesor: ${e.message}", e)
        }
    }

    // =====================================================
    // REGISTRO DE APODERADO
    // =====================================================

    override suspend fun registerParent(
        parentForm: ParentRegistrationForm,
        studentForm: StudentRegistrationForm
    ): ApiResult<Triple<UserWithProfile, StudentEntity, ClassEntity>> {
        return try {
            validateParentForm(parentForm)?.let { return it }
            validateStudentForm(studentForm)?.let { return it }

            if (isEmailRegistered(parentForm.email)) {
                return ApiResult.Error("El email ya está registrado")
            }

            val classEntity = classDao.getClassByCode(studentForm.classCode.trim().uppercase())
                ?: return ApiResult.Error("Código de curso inválido")

            if (studentDao.rutExists(studentForm.studentRut.trim()) > 0) {
                return ApiResult.Error("El RUT del estudiante ya está registrado")
            }

            val password = parentForm.password
                ?: return ApiResult.Error("La contraseña es requerida")

            val user = UserEntity(
                email = parentForm.email.trim().lowercase(),
                passwordHash = hashPassword(password),
                firebaseUid = null,
                syncStatus = SyncStatus.PENDING,
                lastSyncedAt = null
            )
            val userId = userDao.insertUser(user).toInt()

            val profile = ProfileEntity(
                userId = userId,
                firstName = parentForm.firstName.trim(),
                lastName = parentForm.lastName.trim(),
                phone = parentForm.phone.trim(),
                address = parentForm.address?.trim(),
                photoUrl = null,
                isTeacher = false,
                isParent = true,
                firestoreId = null,
                syncStatus = SyncStatus.PENDING,
                firebaseUid = null,
                lastSyncedAt = null
            )
            val profileId = profileDao.insertProfile(profile).toInt()

            val student = StudentEntity(
                classId = classEntity.id,
                rut = studentForm.studentRut.trim(),
                firstName = studentForm.studentFirstName.trim(),
                lastName = studentForm.studentLastName.trim(),
                birthDate = studentForm.studentBirthDate
            )
            val studentId = studentDao.insertStudent(student).toInt()

            val relation = StudentParentRelation(
                studentId = studentId,
                parentId = profileId,
                relationshipType = studentForm.relationshipType,
                isPrimary = studentForm.isPrimary
            )
            studentParentRelationDao.insertRelation(relation)

            val createdUser = user.copy(id = userId)
            val createdProfile = profile.copy(id = profileId)
            val createdStudent = student.copy(id = studentId)
            val userWithProfile = UserWithProfile(createdUser, createdProfile)

            currentUserWithProfile = userWithProfile

            ApiResult.Success(Triple(userWithProfile, createdStudent, classEntity))
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar apoderado: ${e.message}", e)
        }
    }

    // =====================================================
    // GOOGLE SIGN-IN (NO SOPORTADO EN ROOM)
    // =====================================================

    override suspend fun loginWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile> {
        return ApiResult.Error(
            "El inicio de sesión con Google no está disponible con la base de datos local (Room). " +
                    "Esta funcionalidad solo está disponible con Firebase."
        )
    }


    override suspend fun registerWithGoogle(isTeacher: Boolean): ApiResult<UserWithProfile> {
        return ApiResult.Error(
            "El registro con Google no está disponible con la base de datos local (Room). " +
                    "Esta funcionalidad solo está disponible con Firebase."
        )
    }


    // =====================================================
    // ✅ NUEVOS MÉTODOS REQUERIDOS POR LA INTERFAZ
    // =====================================================

    /**
     * Vincular contraseña a cuenta de Google.
     * NO SOPORTADO en implementación local (Room).
     * Este método solo funciona con FirebaseAuthRepository.
     */
    override suspend fun linkPasswordToGoogleAccount(
        email: String,
        password: String
    ): ApiResult<UserWithProfile> {
        return ApiResult.Error(
            "La vinculación de contraseña a cuentas de Google no está disponible con la base de datos local (Room). " +
                    "Esta funcionalidad solo está disponible con Firebase."
        )
    }

    /**
     * Verificar si el usuario tiene contraseña vinculada.
     * En implementación local (Room), todos los usuarios tienen contraseña.
     */
    override suspend fun hasPasswordLinked(): Boolean {
        // En Room, todos los usuarios registrados tienen contraseña
        return currentUserWithProfile != null
    }

    // =====================================================
    // OTROS MÉTODOS
    // =====================================================

    override suspend fun logout() {
        currentUserWithProfile = null
    }

    override suspend fun isEmailRegistered(email: String): Boolean {
        return userDao.emailExists(email.trim().lowercase()) > 0
    }

    override suspend fun getCurrentUser(): UserWithProfile? {
        return currentUserWithProfile
    }

    // =====================================================
    // VALIDACIONES
    // =====================================================

    private fun validateTeacherForm(form: TeacherRegistrationForm): ApiResult.Error? {
        if (form.email.isBlank()) return ApiResult.Error("El email es requerido")
        if (!isValidEmail(form.email)) return ApiResult.Error("Email inválido")
        if (form.password.isBlank()) return ApiResult.Error("La contraseña es requerida")
        if (form.password.length < 6) return ApiResult.Error("La contraseña debe tener al menos 6 caracteres")
        if (form.password != form.confirmPassword) return ApiResult.Error("Las contraseñas no coinciden")
        if (form.firstName.isBlank()) return ApiResult.Error("El nombre es requerido")
        if (form.lastName.isBlank()) return ApiResult.Error("El apellido es requerido")
        if (form.phone.isBlank()) return ApiResult.Error("El teléfono es requerido")
        return null
    }

    private fun validateParentForm(form: ParentRegistrationForm): ApiResult.Error? {
        if (form.email.isBlank()) return ApiResult.Error("El email es requerido")
        if (!isValidEmail(form.email)) return ApiResult.Error("Email inválido")

        form.password?.let { password ->
            if (password.isBlank()) return ApiResult.Error("La contraseña no puede estar vacía")
            if (password.length < 6) return ApiResult.Error("La contraseña debe tener al menos 6 caracteres")
            if (form.confirmPassword == null) return ApiResult.Error("Debes confirmar la contraseña")
            if (password != form.confirmPassword) return ApiResult.Error("Las contraseñas no coinciden")
        }

        if (form.firstName.isBlank()) return ApiResult.Error("El nombre es requerido")
        if (form.lastName.isBlank()) return ApiResult.Error("El apellido es requerido")
        if (form.phone.isBlank()) return ApiResult.Error("El teléfono es requerido")
        return null
    }

    private fun validateStudentForm(form: StudentRegistrationForm): ApiResult.Error? {
        if (form.classCode.isBlank()) return ApiResult.Error("El código de curso es requerido")
        if (form.studentRut.isBlank()) return ApiResult.Error("El RUT del estudiante es requerido")
        if (!isValidRut(form.studentRut)) return ApiResult.Error("RUT inválido")
        if (form.studentFirstName.isBlank()) return ApiResult.Error("El nombre del estudiante es requerido")
        if (form.studentLastName.isBlank()) return ApiResult.Error("El apellido del estudiante es requerido")
        return null
    }

    // =====================================================
    // UTILIDADES
    // =====================================================

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidRut(rut: String): Boolean {
        val cleanRut = rut.replace(".", "").replace("-", "")
        return cleanRut.length >= 2
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

}