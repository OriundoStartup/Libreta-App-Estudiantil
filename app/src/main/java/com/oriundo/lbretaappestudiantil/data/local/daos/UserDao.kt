package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun emailExists(email: String): Int

    /**
     * ✅ NUEVO: Para depuración y migraciones
     */
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)

    // Dentro de la interfaz UserDao { ...
    // ... otros métodos ...

    @Query("SELECT * FROM users WHERE firebase_uid = :uid")
    suspend fun getUserByFirebaseUid(uid: String): UserEntity?


    @Query("UPDATE users SET firebase_uid = :firebaseUid WHERE id = :userId")
    suspend fun updateFirebaseUid(userId: Int, firebaseUid: String)
}
