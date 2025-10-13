package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfileById(profileId: Int): ApiResult<ProfileEntity>
    suspend fun getProfileByUserId(userId: Int): ApiResult<ProfileEntity>
    suspend fun updateProfile(profile: ProfileEntity): ApiResult<Unit>
    fun getAllTeachers(): Flow<List<ProfileEntity>>
    fun getAllParents(): Flow<List<ProfileEntity>>
    suspend fun addParentRole(profileId: Int): ApiResult<Unit>
    suspend fun addTeacherRole(profileId: Int): ApiResult<Unit>
}