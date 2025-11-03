package com.oriundo.lbretaappestudiantil.ui.theme.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oriundo.lbretaappestudiantil.ui.theme.states.AuthUiState

@Composable
fun ForgotPasswordDialog(
    uiState: AuthUiState,
    onDismiss: () -> Unit,
    onSendEmail: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }

    // Detectar cuando el email fue enviado exitosamente
    if (uiState is AuthUiState.Error &&
        uiState.message.contains("enviado", ignoreCase = true)) {
        emailSent = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recuperar Contraseña",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column {
                when {
                    emailSent -> {
                        // ✅ Estado de éxito
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "¡Email enviado!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Revisa tu bandeja de entrada y sigue las instrucciones para restablecer tu contraseña.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    uiState is AuthUiState.Error && !emailSent -> {
                        // ❌ Mensaje de error real
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = uiState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Mostrar campo de email de nuevo
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            placeholder = { Text("ejemplo@correo.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = null)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    else -> {
                        // 📧 Estado inicial
                        Text(
                            text = "Ingresa tu email y te enviaremos un enlace para restablecer tu contraseña.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            placeholder = { Text("ejemplo@correo.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = null)
                            },
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState !is AuthUiState.Loading
                        )
                    }
                }
            }
        },
        confirmButton = {
            when {
                emailSent -> {
                    // Botón para cerrar después del éxito
                    Button(
                        onClick = {
                            emailSent = false
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Entendido")
                    }
                }
                uiState is AuthUiState.Loading -> {
                    // Botón con loading
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text("Enviando...")
                        }
                    }
                }
                else -> {
                    // Botón normal
                    Button(
                        onClick = {
                            if (email.isNotBlank()) {
                                onSendEmail(email.trim())
                            }
                        },
                        enabled = email.isNotBlank() && uiState !is AuthUiState.Loading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enviar Email")
                    }
                }
            }
        },
        dismissButton = {
            if (!emailSent) {
                TextButton(
                    onClick = onDismiss,
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Text("Cancelar")
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}