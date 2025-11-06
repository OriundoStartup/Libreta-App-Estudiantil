package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.AnnotationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AnnotationRepositoryImpl @Inject constructor(
    private val annotationDao: AnnotationDao,
    private val profileDao: ProfileDao,
    private val studentDao: StudentDao
) : AnnotationRepository {

    override suspend fun createAnnotation(
        teacherId: Int,
        studentId: Int,
        classId: Int,
        type: AnnotationType,
        subject: String,
        description: String
    ): ApiResult<AnnotationEntity> {
        return try {
            // Validar que el profesor existe
            val teacherProfile = profileDao.getProfileById(teacherId)
                ?: return ApiResult.Error("Error: El profesor no se encuentra sincronizado. Cierra sesión y vuelve a iniciar.")

            // Validar que el estudiante existe
            val student = studentDao.getStudentById(studentId)
                ?: return ApiResult.Error("Error: El estudiante no se encuentra en la base de datos local.")

            // Crear anotación local
            val annotation = AnnotationEntity(
                studentId = studentId,
                teacherId = teacherId,
                title = subject,
                description = description,
                type = type,
                date = System.currentTimeMillis(),
                isRead = false
            )

            val id = annotationDao.insertAnnotation(annotation)
            val createdAnnotation = annotation.copy(id = id.toInt())

            // ✅ GUARDAR EN FIRESTORE
            val teacherFirebaseUid = teacherProfile.firebaseUid
                ?: return ApiResult.Error("Error: El profesor no tiene Firebase UID")

            val studentFirestoreId = student.firestoreId
                ?: return ApiResult.Error("Error: El estudiante no tiene ID de Firestore")

            val annotationData = hashMapOf(
                "teacherId" to teacherFirebaseUid,
                "studentId" to studentFirestoreId,
                "classId" to classId,
                "title" to subject,
                "description" to description,
                "type" to type.name,
                "date" to System.currentTimeMillis(),
                "isRead" to false
            )

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("annotations")
                .add(annotationData)
                .await()

            ApiResult.Success(createdAnnotation)
        } catch (e: Exception) {
            ApiResult.Error("Error al crear la anotación: ${e.message}")
        }
    }

    override fun getAnnotationsByStudent(studentId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByStudent(studentId)
    }

    override fun getAnnotationsByClass(classId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByClass(classId)
    }

    override fun getAnnotationsByTeacher(teacherId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsByTeacher(teacherId)
    }

    override suspend fun markAsRead(annotationId: Int): ApiResult<Unit> {
        return try {
            annotationDao.markAsRead(annotationId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error al marcar como leída")
        }
    }

    override fun getUnreadAnnotationsForParent(parentId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getUnreadAnnotationsForParent(parentId)
    }
}