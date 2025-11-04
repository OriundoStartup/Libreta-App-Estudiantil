package com.oriundo.lbretaappestudiantil.domain.model

import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity

/**
 * Representa un hilo de conversación entre dos usuarios
 */
data class ConversationThread(
    val participant: ProfileEntity,
    val lastMessage: MessageEntity,
    val unreadCount: Int,
    val allMessages: List<MessageEntity>
)