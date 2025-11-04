package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
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

    /**
     * ✅ NUEVO: Para depuración y verificación
     */
    @Query("SELECT * FROM profiles")
    suspend fun getAllProfiles(): List<ProfileEntity>

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Int)

    /**
     * ✅ NUEVO: Verificar si existe un perfil para un usuario
     */
    @Query("SELECT COUNT(*) FROM profiles WHERE user_id = :userId")
    suspend fun profileExistsForUser(userId: Int): Int

    /**
     * Insertar o actualizar perfil
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ProfileEntity): Long


    /**
     * Obtener perfil del usuario actual (puedes ajustar la lógica según tu app)
     * Por ahora, obtiene el primer perfil disponible
     */
    @Query("SELECT * FROM profiles LIMIT 1")
    fun getCurrentUserProfile(): Flow<ProfileEntity?>

    // ✅ NUEVO: Buscar perfiles por nombre
    @Query("""
        SELECT * FROM profiles 
        WHERE first_name LIKE '%' || :query || '%' 
        OR last_name LIKE '%' || :query || '%'
        ORDER BY first_name, last_name
    """)
    fun searchProfiles(query: String): Flow<List<ProfileEntity>>

    // ✅ NUEVO: Obtener todos los perfiles como Flow (para observar cambios)
    @Query("SELECT * FROM profiles ORDER BY first_name, last_name")
    fun getAllProfilesFlow(): Flow<List<ProfileEntity>>

    // ✅ NUEVO: Insertar múltiples perfiles
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ProfileEntity>)



    /**
     * Eliminar perfil
     */
    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)



}