package com.oriundo.lbretaappestudiantil.ui.theme.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.ui.theme.AppAvatar
import com.oriundo.lbretaappestudiantil.ui.theme.AppShapes
import com.oriundo.lbretaappestudiantil.ui.theme.AvatarType
import com.oriundo.lbretaappestudiantil.ui.theme.states.ConversationUiState
import com.oriundo.lbretaappestudiantil.ui.theme.states.MessageUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationThreadScreen(
    parentId: Int,
    teacherId: Int,
    navController: NavController,
    messageViewModel: MessageViewModel = hiltViewModel()
) {
    var messageContent by remember { mutableStateOf("") }
    val conversationState by messageViewModel.currentConversationState.collectAsState()
    val conversationMessages by messageViewModel.conversationMessages.collectAsState()
    val sendState by messageViewModel.sendState.collectAsState()
    val listState = rememberLazyListState()

    // Cargar conversación
    LaunchedEffect(parentId, teacherId) {
        messageViewModel.loadConversationWithTeacher(parentId, teacherId)
        messageViewModel.markConversationAsRead(parentId, teacherId)
    }

    // Auto-scroll al enviar mensaje
    LaunchedEffect(conversationMessages.size) {
        if (conversationMessages.isNotEmpty()) {
            listState.animateScrollToItem(conversationMessages.size - 1)
        }
    }

    // Reset send state y limpiar campo al enviar
    LaunchedEffect(sendState) {
        if (sendState is MessageUiState.Success) {
            messageContent = ""
            messageViewModel.resetSendState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (conversationState) {
                        is ConversationUiState.Success -> {
                            val conversation = (conversationState as ConversationUiState.Success).conversation
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppAvatar(
                                    type = AvatarType.TEACHER,
                                    size = 40.dp,
                                    iconSize = 24.dp
                                )
                                Column {
                                    Text(
                                        text = "${conversation.participant.firstName} ${conversation.participant.lastName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Profesor",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        else -> {
                            Text("Conversación")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Campo de entrada de mensaje
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .imePadding(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageContent,
                    onValueChange = { messageContent = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") },
                    shape = AppShapes.large,
                    maxLines = 5
                )

                IconButton(
                    onClick = {
                        if (messageContent.isNotBlank()) {
                            messageViewModel.sendReply(
                                senderId = parentId,
                                recipientId = teacherId,
                                content = messageContent,
                                parentMessageId = conversationMessages.lastOrNull()?.id
                            )
                        }
                    },
                    enabled = messageContent.isNotBlank() && sendState !is MessageUiState.Loading,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (messageContent.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    if (sendState is MessageUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (messageContent.isNotBlank())
                                Color.White
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (conversationState) {
            is ConversationUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ConversationUiState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(conversationMessages) { message ->
                        MessageBubble(
                            message = message,
                            isOwnMessage = message.senderId == parentId
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            is ConversationUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay mensajes en esta conversación")
                }
            }
            is ConversationUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (conversationState as ConversationUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            ConversationUiState.Initial -> {}
        }
    }
}