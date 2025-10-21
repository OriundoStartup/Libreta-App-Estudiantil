package com.oriundo.lbretaappestudiantil.data.local.repositories

import com.oriundo.lbretaappestudiantil.data.local.daos.MessageDao
import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl@Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    override suspend fun sendMessage(
        senderId: Int,
        recipientId: Int,
        studentId: Int?,
        subject: String,
        content: String,
        parentMessageId: Int?
    ): ApiResult<MessageEntity> {
        return try {
            if (subject.isBlank()) return ApiResult.Error("El asunto es requerido")
            if (content.isBlank()) return ApiResult.Error("El contenido es requerido")

            val message = MessageEntity(
                senderId = senderId,
                recipientId = recipientId,
                studentId = studentId,
                subject = subject.trim(),
                content = content.trim(),
                parentMessageId = parentMessageId
            )

            val messageId = messageDao.insertMessage(message).toInt()
            ApiResult.Success(message.copy(id = messageId))
        } catch (e: Exception) {
            ApiResult.Error("Error al enviar mensaje: ${e.message}", e)
        }
    }

    override fun getMessagesByUser(userId: Int): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByUser(userId)
    }

    override fun getConversation(user1: Int, user2: Int): Flow<List<MessageEntity>> {
        return messageDao.getConversation(user1, user2)
    }

    override suspend fun markAsRead(messageId: Int): ApiResult<Unit> {
        return try {
            val message = messageDao.getMessagesByUser(0).first()
                .find { it.id == messageId }
                ?: return ApiResult.Error("Mensaje no encontrado")

            messageDao.updateMessage(message.copy(isRead = true))
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Error al marcar mensaje: ${e.message}", e)
        }
    }

    override fun getUnreadCount(userId: Int): Flow<Int> {
        return messageDao.getUnreadCount(userId)
    }
    override fun getUnreadMessagesForTeacher(teacherId: Int): Flow<List<MessageEntity>> {
        return messageDao.getUnreadMessagesForTeacher(teacherId)
    }
    override fun getSentMessagesByTeacher(teacherId: Int): Flow<List<MessageEntity>> {
        return messageDao.getSentMessagesByTeacher(teacherId)
    }

}