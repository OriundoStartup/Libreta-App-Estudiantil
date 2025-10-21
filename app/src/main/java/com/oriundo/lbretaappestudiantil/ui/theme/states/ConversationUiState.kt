package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity

data class ConversationThread(
    val participant: ProfileEntity,
    val lastMessage: MessageEntity,
    val unreadCount: Int,
    val allMessages: List<MessageEntity>
)

sealed class ConversationUiState {
    object Initial : ConversationUiState()
    object Loading : ConversationUiState()
    data class Success(val conversation: ConversationThread) : ConversationUiState()
    data class Empty(val message: String = "No hay conversación") : ConversationUiState()
    data class Error(val message: String) : ConversationUiState()
}