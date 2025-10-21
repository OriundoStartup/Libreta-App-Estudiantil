package com.oriundo.lbretaappestudiantil.ui.theme.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.ui.theme.Screen
import com.oriundo.lbretaappestudiantil.ui.theme.states.MessagesListUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MessageViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ProfileViewModel

@Composable
fun ComunicacionesContent(
    parentId: Int,
    navController: NavController,
    messageViewModel: MessageViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val conversationsState by messageViewModel.conversationsListState.collectAsState()
    val teacherProfiles = remember { mutableStateMapOf<Int, ProfileEntity>() }

    // Cargar conversaciones
    LaunchedEffect(parentId) {
        messageViewModel.loadConversationsForParent(parentId)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Comunicaciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (conversationsState) {
                is MessagesListUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is MessagesListUiState.Success -> {
                    val messages = (conversationsState as MessagesListUiState.Success).messages

                    // Agrupar mensajes por profesor
                    val conversationsByTeacher = messages.groupBy { message ->
                        if (message.senderId == parentId) message.recipientId else message.senderId
                    }

                    // Cargar perfiles de profesores
                    LaunchedEffect(conversationsByTeacher.keys) {
                        conversationsByTeacher.keys.forEach { teacherId ->
                            if (!teacherProfiles.containsKey(teacherId)) {
                                when (val result = profileViewModel.loadProfile(teacherId)) {
                                    is ApiResult.Success -> {
                                        teacherProfiles[teacherId] = result.data
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        conversationsByTeacher.forEach { (teacherId, messagesWithTeacher) ->
                            val lastMessage = messagesWithTeacher.maxByOrNull { it.sentDate }
                            val unreadCount = messagesWithTeacher.count {
                                !it.isRead && it.recipientId == parentId
                            }

                            teacherProfiles[teacherId]?.let { teacherProfile ->
                                lastMessage?.let { message ->
                                    ConversationCard(
                                        teacherProfile = teacherProfile,
                                        lastMessage = message,
                                        unreadCount = unreadCount,
                                        onClick = {
                                            navController.navigate(
                                                Screen.ParentConversation.createRoute(
                                                    parentId = parentId,
                                                    teacherId = teacherId,
                                                    studentId = message.studentId
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                is MessagesListUiState.Empty -> {
                    EmptyMessagesView()
                }

                is MessagesListUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (conversationsState as MessagesListUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                MessagesListUiState.Initial -> {}
            }
        }

        // ✅ NUEVO - Botón flotante para nuevo mensaje
        FloatingActionButton(
            onClick = {
                navController.navigate(
                    Screen.ParentSendMessage.createRoute(parentId)
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Nuevo mensaje",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun EmptyMessagesView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Message,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No hay comunicaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Los mensajes de tus profesores aparecerán aquí",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}