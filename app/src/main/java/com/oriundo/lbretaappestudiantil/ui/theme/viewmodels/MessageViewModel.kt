package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MessageUiState {
    object Initial : MessageUiState()
    object Loading : MessageUiState()
    data class Success(val message: MessageEntity) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _sendState = MutableStateFlow<MessageUiState>(MessageUiState.Initial)
    val sendState: StateFlow<MessageUiState> = _sendState.asStateFlow()

    private val _messagesByUser = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messagesByUser: StateFlow<List<MessageEntity>> = _messagesByUser.asStateFlow()

    private val _conversation = MutableStateFlow<List<MessageEntity>>(emptyList())
    val conversation: StateFlow<List<MessageEntity>> = _conversation.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _unreadMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val unreadMessages: StateFlow<List<MessageEntity>> = _unreadMessages.asStateFlow()

    // ✅ Para historial de mensajes enviados
    private val _sentMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val sentMessages: StateFlow<List<MessageEntity>> = _sentMessages.asStateFlow()

    fun loadSentMessagesByTeacher(teacherId: Int) {
        viewModelScope.launch {
            try {
                messageRepository.getSentMessagesByTeacher(teacherId).collect { messages ->
                    _sentMessages.value = messages
                }
            } catch (e: Exception) {
                _sentMessages.value = emptyList()
            }
        }
    }

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

            _sendState.value = when (result) {
                is ApiResult.Success -> MessageUiState.Success(result.data)
                is ApiResult.Error -> MessageUiState.Error(result.message)
                ApiResult.Loading -> MessageUiState.Loading
            }
        }
    }

    // ✅ NUEVO - Método específico para enviar mensaje a padres
    fun sendMessageToParent(
        teacherId: Int,
        parentId: Int,
        subject: String,
        content: String
    ) {
        viewModelScope.launch {
            _sendState.value = MessageUiState.Loading

            val result = messageRepository.sendMessage(
                senderId = teacherId,
                recipientId = parentId,
                studentId = null,
                subject = subject,
                content = content
            )

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

    fun loadUnreadMessagesForTeacher(teacherId: Int) {
        viewModelScope.launch {
            try {
                messageRepository.getUnreadMessagesForTeacher(teacherId).collect { messages ->
                    _unreadMessages.value = messages
                }
            } catch (e: Exception) {
                _unreadMessages.value = emptyList()
            }
        }
    }

    fun markMessageAsRead(messageId: Int) {
        viewModelScope.launch {
            messageRepository.markAsRead(messageId)
        }
    }
}