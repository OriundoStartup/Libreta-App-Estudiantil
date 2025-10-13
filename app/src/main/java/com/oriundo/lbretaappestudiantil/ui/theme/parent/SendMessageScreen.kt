package com.oriundo.lbretaappestudiantil.ui.theme.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MessageUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MessageViewModel

@Composable
fun SendMessageScreen(
    senderId: Int,
    recipientId: Int,
    studentId: Int?,
    onMessageSent: () -> Unit,
    viewModel: MessageViewModel = viewModel()
) {
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val sendState by viewModel.sendState.collectAsState()

    LaunchedEffect(sendState) {
        if (sendState is MessageUiState.Success) {
            onMessageSent()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Nuevo Mensaje",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo de asunto
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Asunto") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Subject,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de mensaje
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Mensaje") },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(16.dp),
            maxLines = 12
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de enviar
        Button(
            onClick = {
                viewModel.sendMessage(
                    senderId = senderId,
                    recipientId = recipientId,
                    studentId = studentId,
                    subject = subject,
                    content = content
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = subject.isNotBlank() && content.isNotBlank() && sendState !is MessageUiState.Loading,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (sendState is MessageUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )
                    Text(
                        text = "Enviar Mensaje",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}