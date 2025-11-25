package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.ConversationThread
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.BulkSendUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.ConversationUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.MessageUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.MessagesListUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.ProfileListUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.SendResult
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

    // ⭐ NUEVO - Estado para envío masivo
    private val _bulkSendState = MutableStateFlow<BulkSendUiState>(BulkSendUiState.Initial)
    val bulkSendState: StateFlow<BulkSendUiState> = _bulkSendState.asStateFlow()

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

    // ========== ⭐ NUEVA FUNCIÓN DE ENVÍO MASIVO ==========

    /**
     * Envía un mensaje a múltiples apoderados de manera simultánea.
     * Reporta progreso y resultados individuales.
     */
    fun sendMessageToMultipleParents(
        teacherId: Int,
        selectedStudents: List<StudentWithClass>,
        subject: String,
        content: String
    ) {
        viewModelScope.launch {
            // Validación inicial
            if (teacherId <= 0) {
                _bulkSendState.value = BulkSendUiState.PartialSuccess(
                    successful = emptyList(),
                    failed = selectedStudents.map {
                        SendResult(
                            studentName = it.student.firstName,
                            studentId = it.student.id,
                            success = false,
                            error = "ID de profesor inválido"
                        )
                    }
                )
                return@launch
            }

            val total = selectedStudents.size
            val results = mutableListOf<SendResult>()
            var sentCount = 0

            _bulkSendState.value = BulkSendUiState.Loading(sent = 0, total = total)

            // Enviar a cada apoderado
            selectedStudents.forEach { studentWithClass ->
                val parentId = studentWithClass.student.primaryParentId
                val studentId = studentWithClass.student.id
                val studentName = studentWithClass.student.firstName

                val result = if (parentId == null || parentId <= 0) {
                    // Sin apoderado válido
                    SendResult(
                        studentName = studentName,
                        studentId = studentId,
                        success = false,
                        error = "Estudiante sin apoderado primario asignado"
                    )
                } else {
                    // Intentar enviar
                    when (val apiResult = messageRepository.sendMessage(
                        senderId = teacherId,
                        recipientId = parentId,
                        studentId = studentId,
                        subject = subject,
                        content = content
                    )) {
                        is ApiResult.Success -> {
                            SendResult(
                                studentName = studentName,
                                studentId = studentId,
                                success = true
                            )
                        }
                        is ApiResult.Error -> {
                            SendResult(
                                studentName = studentName,
                                studentId = studentId,
                                success = false,
                                error = apiResult.message
                            )
                        }
                        ApiResult.Loading -> {
                            SendResult(
                                studentName = studentName,
                                studentId = studentId,
                                success = false,
                                error = "Error inesperado en el envío"
                            )
                        }
                    }
                }

                results.add(result)
                sentCount++

                // Actualizar progreso
                _bulkSendState.value = BulkSendUiState.Loading(sent = sentCount, total = total)
            }

            // Estado final
            val successful = results.filter { it.success }
            val failed = results.filter { !it.success }

            _bulkSendState.value = if (failed.isEmpty()) {
                BulkSendUiState.Success(results = results)
            } else {
                BulkSendUiState.PartialSuccess(
                    successful = successful,
                    failed = failed
                )
            }
        }
    }

    /**
     * Resetear el estado de envío masivo
     */
    fun resetBulkSendState() {
        _bulkSendState.value = BulkSendUiState.Initial
    }

    // ========== FUNCIONES PARA CONVERSACIONES ==========

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

    fun sendReply(
        senderId: Int,
        recipientId: Int,
        studentId: Int,
        content: String,
        parentMessageId: Int? = null
    ) {
        viewModelScope.launch {
            _sendState.value = MessageUiState.Loading

            if (studentId <= 0) {
                _sendState.value = MessageUiState.Error("ID de estudiante no válido. (ID: $studentId)")
                return@launch
            }

            val result = messageRepository.sendMessage(
                senderId = senderId,
                recipientId = recipientId,
                studentId = studentId,
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

    fun markConversationAsRead(parentId: Int, teacherId: Int) {
        viewModelScope.launch {
            _conversationMessages.value
                .filter { it.recipientId == parentId && !it.isRead }
                .forEach { message ->
                    messageRepository.markAsRead(message.id)
                }
        }
    }

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

    fun loadSentMessagesByTeacher(teacherId: Int) {
        viewModelScope.launch {
            try {
                messageRepository.getSentMessagesByTeacher(teacherId).collect { messages ->
                    _sentMessages.value = messages
                }
            } catch (_: Exception) {
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
            if (senderId <= 0 || recipientId <= 0) {
                _sendState.value = MessageUiState.Error("ID de remitente o destinatario no válido. (Remitente: $senderId, Destinatario: $recipientId)")
                return@launch
            }
            if (studentId != null && studentId <= 0) {
                _sendState.value = MessageUiState.Error("ID de estudiante no válido. (ID: $studentId)")
                return@launch
            }

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

    fun sendMessageToParent(
        teacherId: Int,
        parentId: Int,
        studentId: Int,
        subject: String,
        content: String
    ) {
        viewModelScope.launch {
            if (teacherId <= 0 || parentId <= 0 || studentId <= 0) {
                _sendState.value = MessageUiState.Error("Error de ID. El remitente, destinatario o estudiante no es válido.")
                return@launch
            }

            _sendState.value = MessageUiState.Loading

            val result = messageRepository.sendMessage(
                senderId = teacherId,
                recipientId = parentId,
                studentId = studentId,
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
            } catch (_: Exception) {
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