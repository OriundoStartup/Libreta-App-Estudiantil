package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override suspend fun getProfileById(profileId: Int): ApiResult<ProfileEntity> {
        return try {
            val profile = profileDao.getProfileById(profileId)

            if (profile != null) {
                ApiResult.Success(profile)
            } else {
                ApiResult.Error("Perfil no encontrado con ID: $profileId")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error al obtener perfil: ${e.message}", e)
        }
    }

    override fun getAllTeachers(): Flow<List<ProfileEntity>> {
        return profileDao.getAllTeachers()
    }

    override suspend fun insertOrUpdateProfile(profile: ProfileEntity): ApiResult<Unit> {
        return try {
            profileDao.insertOrUpdateProfile(profile)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al guardar perfil: ${e.message}", e)
        }
    }

    override suspend fun getCurrentUserProfile(): ApiResult<ProfileEntity> {
        return try {
            val profile = profileDao.getCurrentUserProfile().first()

            if (profile != null) {
                ApiResult.Success(profile)
            } else {
                ApiResult.Error("No se encontró perfil de usuario actual")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error al obtener perfil actual: ${e.message}", e)
        }
    }

    override suspend fun updateProfile(profile: ProfileEntity): ApiResult<Unit> {
        return try {
            profileDao.updateProfile(profile)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al actualizar perfil: ${e.message}", e)
        }
    }

    override suspend fun getProfileByUserId(userId: Int): ApiResult<ProfileEntity> {
        return try {
            val profile = profileDao.getProfileByUserId(userId)
                ?: return ApiResult.Error("Perfil no encontrado")
            ApiResult.Success(profile)
        } catch (e: Exception) {
            ApiResult.Error("Error al obtener perfil: ${e.message}", e)
        }
    }





    override fun getAllParents(): Flow<List<ProfileEntity>> {
        return profileDao.getAllParents()
    }
    /**
     * Insertar o actualizar perfil
     */


    override suspend fun addParentRole(profileId: Int): ApiResult<Unit> {
        return try {
            val profile = profileDao.getProfileById(profileId)
                ?: return ApiResult.Error("Perfil no encontrado")
            profileDao.updateProfile(profile.copy(isParent = true))
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al agregar rol de apoderado: ${e.message}", e)
        }
    }





    override suspend fun addTeacherRole(profileId: Int): ApiResult<Unit> {
        return try {
            val profile = profileDao.getProfileById(profileId)
                ?: return ApiResult.Error("Perfil no encontrado")
            profileDao.updateProfile(profile.copy(isTeacher = true))
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al agregar rol de profesor: ${e.message}", e)
        }
    }

}