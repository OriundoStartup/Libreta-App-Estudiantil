package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel // 1. Usamos ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
// Se elimina la importación del obsoleto RepositoryProvider
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository // Importamos la interfaz del Repository
import dagger.hilt.android.lifecycle.HiltViewModel // 2. Importación clave de Hilt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // 3. Importación para Inyección

sealed class MessageUiState {
    object Initial : MessageUiState()
    object Loading : MessageUiState()
    data class Success(val message: MessageEntity) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}

@HiltViewModel // 4. Anotación para que Hilt sepa cómo construir este ViewModel
class MessageViewModel @Inject constructor( // 5. Inyección del Repository
    private val messageRepository: MessageRepository
) : ViewModel() { // 6. Heredamos de ViewModel

    // ELIMINADA: private val messageRepository = RepositoryProvider.provideMessageRepository(application)

    private val _sendState = MutableStateFlow<MessageUiState>(MessageUiState.Initial)
    val sendState: StateFlow<MessageUiState> = _sendState.asStateFlow()

    private val _messagesByUser = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messagesByUser: StateFlow<List<MessageEntity>> = _messagesByUser.asStateFlow()

    private val _conversation = MutableStateFlow<List<MessageEntity>>(emptyList())
    val conversation: StateFlow<List<MessageEntity>> = _conversation.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun sendMessage(
        senderId: Int,
        recipientId: Int,
        studentId: Int?,
        subject: String,
        content: String,
        parentMessageId: Int? = null
    ) {
        viewModelScope.launch {
            _sendState.value = MessageUiState.Loading

            val result = messageRepository.sendMessage(
                senderId = senderId,
                recipientId = recipientId,
                studentId = studentId,
                subject = subject,
                content = content,
                parentMessageId = parentMessageId
            )

            // CORRECCIÓN del bloque 'when': Se hace limpio y exhaustivo
            _sendState.value = when (result) {
                is ApiResult.Success -> MessageUiState.Success(result.data)
                is ApiResult.Error -> MessageUiState.Error(result.message)
                ApiResult.Loading -> MessageUiState.Loading
            }
        }
    }

    fun loadMessagesByUser(userId: Int) {
        viewModelScope.launch {
            messageRepository.getMessagesByUser(userId).collect { messages ->
                _messagesByUser.value = messages
            }
        }
    }

    fun loadConversation(user1: Int, user2: Int) {
        viewModelScope.launch {
            messageRepository.getConversation(user1, user2).collect { messages ->
                _conversation.value = messages
            }
        }
    }

    fun loadUnreadCount(userId: Int) {
        viewModelScope.launch {
            messageRepository.getUnreadCount(userId).collect { count ->
                _unreadCount.value = count
            }
        }
    }

    fun markAsRead(messageId: Int) {
        viewModelScope.launch {
            messageRepository.markAsRead(messageId)
        }
    }

    fun resetSendState() {
        _sendState.value = MessageUiState.Initial
    }
}