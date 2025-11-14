package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.google.firebase.auth.FirebaseAuth
import com.oriundo.lbretaappestudiantil.data.local.LocalDatabaseRepository
import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepositoryImpl @Inject constructor(
    private val classDao: ClassDao,
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val firebaseAuth: FirebaseAuth,
) : ClassRepository {

    override suspend fun createClass(
        className: String,
        schoolName: String,
        teacherId: Int,
        gradeLevel: String?,
        academicYear: Int
    ): ApiResult<Pair<ClassEntity, String>> {
        return try {
            // 1. Validaciones
            if (className.isBlank()) return ApiResult.Error("El nombre del curso es requerido")
            if (schoolName.isBlank()) return ApiResult.Error("El nombre de la escuela es requerido")

            // 2. ✅ CAMBIO CRÍTICO: Generar código único SIEMPRE EN MAYÚSCULAS
            val generatedCode = generateUniqueCode().uppercase()

            // 3. Crear entity en Room
            val classEntity = ClassEntity(
                id = 0,
                className = className.trim(),
                schoolName = schoolName.trim(),
                teacherId = teacherId,
                classCode = generatedCode, // ✅ Código ya está en MAYÚSCULAS
                gradeLevel = gradeLevel?.trim(),
                academicYear = academicYear.toString(),
                isActive = true,
                createdAt = System.currentTimeMillis()
            )

            val classId = classDao.insertClass(classEntity).toInt()
            val createdClass = classEntity.copy(id = classId)

            // 4. SINCRONIZAR A FIRESTORE EN SEGUNDO PLANO
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Intentar sincronizar, pero no bloquear si falla
                localDatabaseRepository.syncClassToFirestore(firebaseUser.uid, createdClass)
            }

            ApiResult.Success(Pair(createdClass, generatedCode))
        } catch (e: Exception) {
            ApiResult.Error("Error al crear curso: ${e.message}", e)
        }
    }

    override suspend fun getClassById(classId: Int): ApiResult<ClassEntity> {
        return try {
            val classEntity = classDao.getClassById(classId)
            if (classEntity != null) {
                ApiResult.Success(classEntity)
            } else {
                ApiResult.Error("Curso no encontrado")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error al obtener curso: ${e.message}", e)
        }
    }

    override suspend fun getClassByCode(classCode: String): ApiResult<ClassEntity> {
        return try {
            // ✅ CAMBIO CRÍTICO: Normalizar el código de búsqueda a MAYÚSCULAS
            val normalizedCode = classCode.trim().uppercase()

            val classEntity = classDao.getClassByCode(normalizedCode)
            if (classEntity != null) {
                ApiResult.Success(classEntity)
            } else {
                ApiResult.Error("Código de curso inválido: $normalizedCode")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error al buscar curso: ${e.message}", e)
        }
    }


    override fun getClassesByTeacher(teacherId: Int): Flow<List<ClassEntity>> {
        return classDao.getClassesByTeacher(teacherId)
    }

    override suspend fun updateClass(classEntity: ClassEntity): ApiResult<Unit> {
        return try {
            // ✅ Asegurar que el código siempre esté en mayúsculas al actualizar
            val updatedEntity = classEntity.copy(
                classCode = classEntity.classCode.uppercase()
            )

            classDao.updateClass(updatedEntity)

            // Sincronizar a Firestore si está disponible
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                localDatabaseRepository.syncClassToFirestore(firebaseUser.uid, updatedEntity)
            }

            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar curso: ${e.message}", e)
        }
    }

    /**
     * Genera un código único verificando que no exista en la base de datos
     * @return Código único de 6 caracteres en MAYÚSCULAS
     */
    private suspend fun generateUniqueCode(): String {
        var code: String
        var attempts = 0
        val maxAttempts = 10

        do {
            code = generateCode().uppercase() // ✅ Asegurar MAYÚSCULAS
            attempts++

            if (attempts >= maxAttempts) {
                // Si después de 10 intentos no se puede generar un código único,
                // agregar un timestamp para garantizar unicidad
                code = generateCodeWithTimestamp().uppercase()
                break
            }
        } while (classDao.codeExists(code) > 0)

        return code
    }
    /**
     * Busca una clase en Firestore por código, la guarda localmente y la retorna
     * Usado cuando un apoderado busca una clase para inscribir a un estudiante
     */
    override suspend fun getAndSyncClassByCodeFromRemote(classCode: String): ApiResult<ClassEntity> {
        return try {
            val normalizedCode = classCode.trim().uppercase()

            // 1. Verificar si ya existe localmente
            val existingClass = classDao.getClassByCode(normalizedCode)
            if (existingClass != null) {
                return ApiResult.Success(existingClass)
            }

            // 2. Buscar en Firestore usando el método corregido
            val remoteClass = localDatabaseRepository.getClassFromFirestoreByCode(normalizedCode)
                ?: return ApiResult.Error("Código de curso no encontrado: $normalizedCode")

            // 3. Obtener el usuario actual para buscar su profileId
            val firebaseUser = firebaseAuth.currentUser
                ?: return ApiResult.Error("Usuario no autenticado")

            val localUser = localDatabaseRepository.getUserByFirebaseUid(firebaseUser.uid)
                ?: return ApiResult.Error("Usuario no encontrado localmente")

            // 4. Asignar el teacherId correcto (usar el profileId del usuario actual temporalmente)
            // Nota: En el futuro podrías querer buscar el profesor real si necesitas esa info
            val classToSave = remoteClass.copy(
                teacherId = localUser.profile.id  // Usar el ID del usuario actual como placeholder
            )

            // 5. Guardar en Room
            val insertedId = classDao.insertClass(classToSave).toInt()
            val savedClass = classToSave.copy(id = insertedId)

            ApiResult.Success(savedClass)

        } catch (e: Exception) {
            ApiResult.Error("Error al sincronizar clase desde remoto: ${e.message}", e)
        }
    }

    /**
     * Genera un código de 6 caracteres con formato: 3 letras + 3 números
     * Ejemplo: ABC123, XYZ789
     * @return Código en formato XXX### (siempre en MAYÚSCULAS)
     */
    private fun generateCode(): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"

        // Generar 3 letras
        val letterPart = (1..3)
            .map { letters.random() }
            .joinToString("")

        // Generar 3 números
        val numberPart = (1..3)
            .map { numbers.random() }
            .joinToString("")

        return (letterPart + numberPart).uppercase() // ✅ SIEMPRE EN MAYÚSCULAS
    }

    /**
     * Genera un código con timestamp para garantizar unicidad
     * Usado como fallback si no se puede generar un código único normal
     */
    private fun generateCodeWithTimestamp(): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val timestamp = System.currentTimeMillis()
        val lastDigits = (timestamp % 1000).toString().padStart(3, '0')

        val letterPart = (1..3)
            .map { letters.random() }
            .joinToString("")

        return (letterPart + lastDigits).uppercase()
    }

    // ✅ IMPLEMENTACIÓN DE LA NUEVA FUNCIÓN DE SINCRONIZACIÓN
    override suspend fun syncTeacherClassesAndStudents(firebaseUid: String?, localProfileId: Int): ApiResult<Unit> {
        return localDatabaseRepository.forceTeacherDashboardSync(firebaseUid, localProfileId)
    }
}