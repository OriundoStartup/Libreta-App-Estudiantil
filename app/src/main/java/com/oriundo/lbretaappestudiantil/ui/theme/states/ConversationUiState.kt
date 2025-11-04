package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.domain.model.ConversationThread


/**
 * Estado para una conversación individual
 */
sealed class ConversationUiState {
    object Initial : ConversationUiState()
    object Loading : ConversationUiState()
    data class Success(val conversation: ConversationThread) : ConversationUiState()
    data class Empty(val message: String = "No hay mensajes en esta conversación") : ConversationUiState()
    data class Error(val message: String) : ConversationUiState()
}
