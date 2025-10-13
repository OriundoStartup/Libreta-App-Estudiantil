package com.oriundo.lbretaappestudiantil.repositories

import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun sendMessage(
        senderId: Int,
        recipientId: Int,
        studentId: Int?,
        subject: String,
        content: String,
        parentMessageId: Int? = null
    ): ApiResult<MessageEntity>
    fun getMessagesByUser(userId: Int): Flow<List<MessageEntity>>
    fun getConversation(user1: Int, user2: Int): Flow<List<MessageEntity>>
    suspend fun markAsRead(messageId: Int): Result<Unit>
    fun getUnreadCount(userId: Int): Flow<Int>
}