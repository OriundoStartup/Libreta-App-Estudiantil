package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentParentRelationDao
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentParentRelation
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val studentParentRelationDao: StudentParentRelationDao,
    private val classDao: ClassDao
) : StudentRepository {

    override suspend fun registerStudent(
        classId: Int,
        rut: String,
        firstName: String,
        lastName: String,
        birthDate: Long?
    ): ApiResult<StudentEntity> {
        return try {
            if (rut.isBlank()) return ApiResult.Error("El RUT es requerido")
            if (firstName.isBlank()) return ApiResult.Error("El nombre es requerido")
            if (lastName.isBlank()) return ApiResult.Error("El apellido es requerido")

            if (studentDao.rutExists(rut.trim()) > 0) {
                return ApiResult.Error("El RUT ya está registrado")
            }

            val student = StudentEntity(
                classId = classId,
                rut = rut.trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                birthDate = birthDate
            )

            val studentId = studentDao.insertStudent(student).toInt()
            ApiResult.Success(student.copy(id = studentId))
        } catch (e: Exception) {
            ApiResult.Error("Error al registrar estudiante: ${e.message}", e)
        }
    }

    override suspend fun getStudentById(studentId: Int): ApiResult<StudentEntity> {
        return try {
            val student = studentDao.getStudentById(studentId)
                ?: return ApiResult.Error("Estudiante no encontrado")
            ApiResult.Success(student)
        } catch (e: Exception) {
            ApiResult.Error("Error al obtener estudiante: ${e.message}", e)
        }
    }

    override suspend fun getStudentByRut(rut: String): ApiResult<StudentEntity> {
        return try {
            val student = studentDao.getStudentByRut(rut.trim())
                ?: return ApiResult.Error("Estudiante no encontrado")
            ApiResult.Success(student)
        } catch (e: Exception) {
            ApiResult.Error("Error al obtener estudiante: ${e.message}", e)
        }
    }

    override fun getStudentsByClass(classId: Int): Flow<List<StudentEntity>> {
        return studentDao.getStudentsByClass(classId)
    }

    override fun getStudentsByParent(parentId: Int): Flow<List<StudentWithClass>> {
        return studentParentRelationDao.getStudentsByParent(parentId).map { students ->
            students.mapNotNull { student ->
                val classEntity = classDao.getClassById(student.classId)
                classEntity?.let { StudentWithClass(student, it) }
            }
        }
    }

    override suspend fun updateStudent(student: StudentEntity): ApiResult<Unit> {
        return try {
            studentDao.updateStudent(student)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar estudiante: ${e.message}", e)
        }
    }

    override suspend fun linkParentToStudent(
        studentId: Int,
        parentId: Int,
        relationshipType: RelationshipType,
        isPrimary: Boolean
    ): ApiResult<Unit> {
        return try {
            val relation = StudentParentRelation(
                studentId = studentId,
                parentId = parentId,
                relationshipType = relationshipType,
                isPrimary = isPrimary
            )
            studentParentRelationDao.insertRelation(relation)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al vincular apoderado: ${e.message}", e)
        }
    }

    override fun getParentsByStudent(studentId: Int): Flow<List<ProfileEntity>> {
        return studentParentRelationDao.getParentsByStudent(studentId)
    }
    override fun getAllStudentsWithClass(): Flow<List<StudentWithClass>> {
        return studentDao.getAllStudentsWithClassAndParent().map { dtoList ->
            dtoList.mapNotNull { dto ->
                val classEntity = classDao.getClassById(dto.classId)
                classEntity?.let {
                    StudentWithClass(
                        student = StudentEntity(
                            id = dto.id,
                            classId = dto.classId,
                            rut = dto.rut,
                            firstName = dto.firstName,
                            lastName = dto.lastName,
                            birthDate = dto.birthDate,
                            photoUrl = dto.photoUrl,
                            enrollmentDate = dto.enrollmentDate,
                            isActive = dto.isActive,
                            notes = dto.notes
                        ),
                        classEntity = it,
                        primaryParentId = dto.primaryParentId // ✅ Incluye el parentId
                    )
                }
            }
        }
    }
}