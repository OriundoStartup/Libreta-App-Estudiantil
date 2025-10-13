package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Query("SELECT * FROM profiles WHERE user_id = :userId LIMIT 1")
    suspend fun getProfileByUserId(userId: Int): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: Int): ProfileEntity?

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE is_teacher = 1")
    fun getAllTeachers(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE is_parent = 1")
    fun getAllParents(): Flow<List<ProfileEntity>>
}