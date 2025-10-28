package com.oriundo.lbretaappestudiantil.ui.theme.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.ui.theme.AppColors
import com.oriundo.lbretaappestudiantil.ui.theme.states.AuthUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthViewModel

// ============================================================================
// COMPONENTES AUXILIARES
// ============================================================================

@Composable
private fun PasswordRequirement(
    text: String,
    isMet: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            contentDescription = null,
            tint = if (isMet) AppColors.Success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMet) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SecurityBenefitCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// PANTALLA PRINCIPAL
// ============================================================================

@Composable
fun SetPasswordScreen(
    userWithProfile: UserWithProfile,
    isOptional: Boolean = false,
    onPasswordSet: () -> Unit,
    onSkip: (() -> Unit)? = null,
    viewModel: AuthViewModel = viewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Validaciones
    val hasMinLength = password.length >= 6
    val hasUppercase = password.any { it.isUpperCase() }
    password.any { it.isLowerCase() }
    val hasNumber = password.any { it.isDigit() }
    val passwordsMatch = password == confirmPassword && confirmPassword.isNotBlank()

    val isPasswordValid = hasMinLength && passwordsMatch

    // Manejar éxito
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onPasswordSet()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = if (uiState is AuthUiState.Error) 80.dp else 0.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Ícono de seguridad
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = if (isOptional) "¿Deseas establecer una contraseña?" else "Protege tu cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = "Establece una contraseña para acceder con tu email (${userWithProfile.user.email}) sin depender de Google.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Beneficios de seguridad
            SecurityBenefitCard(
                icon = Icons.Filled.VpnKey,
                title = "Acceso flexible",
                description = "Inicia sesión con Google o con tu email y contraseña"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecurityBenefitCard(
                icon = Icons.Filled.Shield,
                title = "Mayor seguridad",
                description = "Protege tu cuenta con autenticación de dos factores"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecurityBenefitCard(
                icon = Icons.Filled.PhoneAndroid,
                title = "Acceso desde cualquier dispositivo",
                description = "No necesitas tener Google instalado"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nueva contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de confirmar contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                leadingIcon = {
                    Icon(Icons.Filled.LockOpen, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            // Validaciones visuales
            AnimatedVisibility(
                visible = password.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Requisitos de la contraseña:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    PasswordRequirement(
                        text = "Mínimo 6 caracteres",
                        isMet = hasMinLength
                    )

                    PasswordRequirement(
                        text = "Las contraseñas coinciden",
                        isMet = passwordsMatch
                    )

                    // Recomendaciones adicionales (no obligatorias)
                    if (hasMinLength) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recomendado (opcional):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        PasswordRequirement(
                            text = "Al menos una mayúscula",
                            isMet = hasUppercase,
                            modifier = Modifier.alpha(0.7f)
                        )
                        PasswordRequirement(
                            text = "Al menos un número",
                            isMet = hasNumber,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón principal - Establecer contraseña
            Button(
                onClick = {
                    viewModel.linkPasswordToAccount(
                        email = userWithProfile.user.email,
                        password = password
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isPasswordValid && uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Text(
                            text = "Establecer contraseña",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Botón secundario - Saltar (solo si es opcional)
            if (isOptional && onSkip != null) {
                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        viewModel.skipPasswordSetup()
                        onSkip()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Text(
                        text = "Omitir por ahora",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nota de seguridad
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tu contraseña se almacena de forma segura y encriptada. Podrás cambiarla en cualquier momento desde la configuración.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Mensaje de error fijo en la parte inferior
        if (uiState is AuthUiState.Error) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}