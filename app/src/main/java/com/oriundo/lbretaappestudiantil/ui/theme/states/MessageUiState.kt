package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity

sealed class MessageUiState {
    object Initial : MessageUiState()
    object Loading : MessageUiState()
    data class Success(val message: MessageEntity) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}