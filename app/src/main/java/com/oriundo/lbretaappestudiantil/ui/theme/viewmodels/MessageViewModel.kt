package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.ConversationThread
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.ConversationUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.MessageUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.MessagesListUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.ProfileListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // ========== ESTADOS PARA MENSAJES ==========
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

    private val _sentMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val sentMessages: StateFlow<List<MessageEntity>> = _sentMessages.asStateFlow()

    // ========== ESTADOS PARA CONVERSACIONES ==========
    private val _conversationsListState = MutableStateFlow<MessagesListUiState>(MessagesListUiState.Initial)
    val conversationsListState: StateFlow<MessagesListUiState> = _conversationsListState.asStateFlow()

    private val _currentConversationState = MutableStateFlow<ConversationUiState>(ConversationUiState.Initial)
    val currentConversationState: StateFlow<ConversationUiState> = _currentConversationState.asStateFlow()

    private val _conversationMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val conversationMessages: StateFlow<List<MessageEntity>> = _conversationMessages.asStateFlow()

    // ========== ESTADOS PARA PROFESORES (DESTINATARIOS) ==========
    private val _teachersListState = MutableStateFlow<ProfileListUiState>(ProfileListUiState.Initial)
    val teachersListState: StateFlow<ProfileListUiState> = _teachersListState.asStateFlow()

    // ========== FUNCIONES PARA CONVERSACIONES ==========

    /**
     * Cargar todas las conversaciones para el padre
     */
    fun loadConversationsForParent(parentId: Int) {
        viewModelScope.launch {
            _conversationsListState.value = MessagesListUiState.Loading

            try {
                messageRepository.getMessagesByUser(parentId).collect { messages ->
                    if (messages.isEmpty()) {
                        _conversationsListState.value = MessagesListUiState.Empty()
                    } else {
                        _conversationsListState.value = MessagesListUiState.Success(messages)
                    }
                }
            } catch (e: Exception) {
                _conversationsListState.value = MessagesListUiState.Error(
                    e.message ?: "Error al cargar conversaciones"
                )
            }
        }
    }

    /**
     * Cargar conversación específica con información del profesor
     */
    fun loadConversationWithTeacher(parentId: Int, teacherId: Int) {
        viewModelScope.launch {
            _currentConversationState.value = ConversationUiState.Loading

            try {
                messageRepository.getConversation(parentId, teacherId).collect { messages ->
                    _conversationMessages.value = messages

                    if (messages.isEmpty()) {
                        _currentConversationState.value = ConversationUiState.Empty()
                    } else {
                        when (val profileResult = profileRepository.getProfileById(teacherId)) {
                            is ApiResult.Success -> {
                                val conversationThread = ConversationThread(
                                    participant = profileResult.data,
                                    lastMessage = messages.last(),
                                    unreadCount = messages.count { !it.isRead && it.recipientId == parentId },
                                    allMessages = messages
                                )
                                _currentConversationState.value = ConversationUiState.Success(conversationThread)
                            }
                            is ApiResult.Error -> {
                                _currentConversationState.value = ConversationUiState.Error(
                                    "Error al cargar información del profesor"
                                )
                            }
                            ApiResult.Loading -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                _currentConversationState.value = ConversationUiState.Error(
                    e.message ?: "Error al cargar conversación"
                )
            }
        }
    }

    /**
     * Enviar respuesta en conversación
     */
    fun sendReply(
        senderId: Int,
        recipientId: Int,
        content: String,
        parentMessageId: Int? = null
    ) {
        viewModelScope.launch {
            _sendState.value = MessageUiState.Loading

            val result = messageRepository.sendMessage(
                senderId = senderId,
                recipientId = recipientId,
                studentId = null,
                subject = "Re: Conversación",
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

    /**
     * Marcar todos los mensajes de una conversación como leídos
     */
    fun markConversationAsRead(parentId: Int, teacherId: Int) {
        viewModelScope.launch {
            _conversationMessages.value
                .filter { it.recipientId == parentId && !it.isRead }
                .forEach { message ->
                    messageRepository.markAsRead(message.id)
                }
        }
    }

    /**
     * Cargar la lista de profesores para que el apoderado pueda seleccionarlos
     */
    fun loadAllTeachersAsRecipients() {
        viewModelScope.launch {
            _teachersListState.value = ProfileListUiState.Loading

            try {
                profileRepository.getAllTeachers().collect { teachers ->
                    if (teachers.isEmpty()) {
                        _teachersListState.value = ProfileListUiState.Empty()
                    } else {
                        _teachersListState.value = ProfileListUiState.Success(teachers)
                    }
                }
            } catch (e: Exception) {
                _teachersListState.value = ProfileListUiState.Error(
                    e.message ?: "Error al cargar la lista de profesores"
                )
            }
        }
    }

    // ========== FUNCIONES PARA MENSAJES GENERALES ==========

    /**
     * Cargar mensajes enviados por un profesor
     */
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

    /**
     * Enviar mensaje general
     */
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

    /**
     * Enviar mensaje de profesor a padre
     */
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

    /**
     * Cargar mensajes por usuario
     */
    fun loadMessagesByUser(userId: Int) {
        viewModelScope.launch {
            messageRepository.getMessagesByUser(userId).collect { messages ->
                _messagesByUser.value = messages
            }
        }
    }

    /**
     * Cargar conversación entre dos usuarios
     */
    fun loadConversation(user1: Int, user2: Int) {
        viewModelScope.launch {
            messageRepository.getConversation(user1, user2).collect { messages ->
                _conversation.value = messages
            }
        }
    }

    /**
     * Cargar contador de mensajes no leídos
     */
    fun loadUnreadCount(userId: Int) {
        viewModelScope.launch {
            messageRepository.getUnreadCount(userId).collect { count ->
                _unreadCount.value = count
            }
        }
    }

    /**
     * Marcar mensaje como leído
     */
    fun markAsRead(messageId: Int) {
        viewModelScope.launch {
            messageRepository.markAsRead(messageId)
        }
    }

    /**
     * Resetear estado de envío
     */
    fun resetSendState() {
        _sendState.value = MessageUiState.Initial
    }

    /**
     * Cargar mensajes no leídos para un profesor
     */
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

    /**
     * Marcar mensaje como leído (alias para compatibilidad)
     */
    fun markMessageAsRead(messageId: Int) {
        viewModelScope.launch {
            messageRepository.markAsRead(messageId)
        }
    }
}