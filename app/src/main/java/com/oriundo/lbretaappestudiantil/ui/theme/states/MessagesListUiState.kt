package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity

sealed class MessagesListUiState {
    object Initial : MessagesListUiState()
    object Loading : MessagesListUiState()
    data class Success(val messages: List<MessageEntity>) : MessagesListUiState()
    data class Empty(val message: String = "No hay mensajes") : MessagesListUiState()
    data class Error(val message: String) : MessagesListUiState()
}