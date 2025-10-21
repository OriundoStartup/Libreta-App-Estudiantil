package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("""
        SELECT * FROM messages 
        WHERE (sender_id = :userId OR recipient_id = :userId)
        ORDER BY sent_date DESC
    """)
    fun getMessagesByUser(userId: Int): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE (sender_id = :user1 AND recipient_id = :user2) 
           OR (sender_id = :user2 AND recipient_id = :user1)
        ORDER BY sent_date ASC
    """)
    fun getConversation(user1: Int, user2: Int): Flow<List<MessageEntity>>

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("SELECT COUNT(*) FROM messages WHERE recipient_id = :userId AND is_read = 0")
    fun getUnreadCount(userId: Int): Flow<Int>

    @Query("""
        SELECT * FROM messages 
        WHERE recipient_id = :teacherId 
        AND is_read = 0 
        ORDER BY sent_date DESC
    """)

    fun getUnreadMessagesForTeacher(teacherId: Int): Flow<List<MessageEntity>>
    // ✅ NUEVO - Para obtener mensajes enviados por el profesor
    @Query("""
    SELECT * FROM messages 
    WHERE sender_id = :teacherId 
    ORDER BY sent_date DESC
""")
    fun getSentMessagesByTeacher(teacherId: Int): Flow<List<MessageEntity>>

}