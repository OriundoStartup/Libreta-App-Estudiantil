package com.oriundo.lbretaappestudiantil.ui.theme.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.ui.theme.states.AuthUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (UserWithProfile) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    // Manejar estados
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                onLoginSuccess(state.userWithProfile)
            }
            else -> {}
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
                .padding(bottom = if (uiState is AuthUiState.Error) 80.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo o ícono
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Accede a tu cuenta",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ✅ BOTONES DE GOOGLE (SOLO PARA USUARIOS REGISTRADOS)
            Text(
                text = "Iniciar sesión con Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Google para Profesores
            Button(
                onClick = {
                    viewModel.loginWithGoogle(isTeacher = true)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4),
                    contentColor = Color.White
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.School, null, modifier = Modifier.size(24.dp))
                    Text("Google (Profesor)", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Google para Apoderados
            Button(
                onClick = {
                    viewModel.loginWithGoogle(isTeacher = false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF34A853),
                    contentColor = Color.White
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.FamilyRestroom, null, modifier = Modifier.size(24.dp))
                    Text("Google (Apoderado)", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Separador
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                Text(
                    text = "  O con Email  ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de login
            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar Sesión", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link a registro
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "¿No tienes cuenta? Regístrate aquí",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nota informativa
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                        text = "Si no tienes cuenta, primero debes registrarte. Los botones de Google solo funcionan para usuarios ya registrados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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