package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepositoryImpl @Inject constructor(
    private val classDao: ClassDao
) : ClassRepository {

    override suspend fun createClass(
        className: String,
        schoolName: String,
        teacherId: Int,
        gradeLevel: String?,
        academicYear: Int
    ): ApiResult<Pair<ClassEntity, String>> {
        return try {
            // Validaciones
            if (className.isBlank()) return ApiResult.Error("El nombre del curso es requerido")
            if (schoolName.isBlank()) return ApiResult.Error("El nombre de la escuela es requerido")

            // Generar código único (ya está en mayúsculas)
            val generatedCode = generateUniqueCode()

            // Crear entity
            val classEntity = ClassEntity(
                id = 0,
                className = className.trim(),
                schoolName = schoolName.trim(),
                teacherId = teacherId,
                classCode = generatedCode, // Ya está en mayúsculas
                gradeLevel = gradeLevel?.trim(),
                academicYear = academicYear.toString(),
                isActive = true,
                createdAt = System.currentTimeMillis()
            )

            val classId = classDao.insertClass(classEntity).toInt()
            val createdClass = classEntity.copy(id = classId)

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
            // ✅ CORREGIDO: Normalizar a mayúsculas
            val classEntity = classDao.getClassByCode(classCode.trim().uppercase())
            if (classEntity != null) {
                ApiResult.Success(classEntity)
            } else {
                ApiResult.Error("Código de curso inválido")
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
            classDao.updateClass(classEntity)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar curso: ${e.message}", e)
        }
    }

    private suspend fun generateUniqueCode(): String {
        var code: String
        do {
            code = generateCode()
        } while (classDao.codeExists(code) > 0)
        return code
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}