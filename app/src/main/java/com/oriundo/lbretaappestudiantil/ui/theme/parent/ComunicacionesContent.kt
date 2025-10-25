package com.oriundo.lbretaappestudiantil.ui.theme.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
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

    // 1. Box con fillMaxSize para ocupar toda la pantalla y contener el FloatingActionButton
    Box(modifier = Modifier.fillMaxSize()) { // CAMBIO CLAVE: Usamos fillMaxSize en el Box

        // 2. Column principal para el título y el contenido desplazable.
        Column(
            modifier = Modifier
                .fillMaxSize() // CAMBIO CLAVE: Debe ocupar todo el espacio vertical.
                .padding(horizontal = 24.dp)
        ) {
            // Título (Altura fija)
            Text(
                text = "Comunicaciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contenedor principal para la lista
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

                    // 3. Column interno que debe tener el desplazamiento.
                    // Al usar .weight(1f), le decimos que ocupe el resto del espacio vertical
                    // disponible, lo cual le da una altura FINITA necesaria para .verticalScroll().
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // OCUPA EL ESPACIO RESTANTE
                            .verticalScroll(rememberScrollState()), // APLICAMOS EL DESPLAZAMIENTO AQUÍ
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

                        // NOTA: Quité el Spacer(Modifier.height(100.dp)) dentro del Column desplazable
                        // para evitar problemas con el padding del BottomEnd del FloatingActionButton.
                        // Si necesitas padding extra, añádelo al Modifier.padding(bottom=...) del Column desplazable.
                    }
                }

                is MessagesListUiState.Empty -> {
                    // Nota: Aquí EmptyMessagesView también debe respetar la altura del contenedor
                    EmptyMessagesView(modifier = Modifier.weight(1f))
                }

                is MessagesListUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // 👈 OCUPA EL ESPACIO RESTANTE
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

        // Botón flotante para nuevo mensaje
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
//... (El resto de la función EmptyMessagesView no requiere cambios,
//    pero es bueno añadir un parámetro modifier)
@Composable
private fun EmptyMessagesView(modifier: Modifier = Modifier) { // Añadimos el parámetro Modifier
    Card(
        modifier = modifier.fillMaxWidth(), // Usamos el modifier pasado
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
                imageVector = Icons.AutoMirrored.Filled.Message,
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