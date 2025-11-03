package com.oriundo.lbretaappestudiantil.ui.theme.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType
import com.oriundo.lbretaappestudiantil.domain.model.ParentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.StudentRegistrationForm
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.ui.theme.AppColors
import com.oriundo.lbretaappestudiantil.ui.theme.states.AuthUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthViewModel

// ============================================================================
// FUNCIONES AUXILIARES
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text, fontWeight = FontWeight.Medium) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
        )
    )
}

@Composable
fun ParentPasswordRequirement(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isMet) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            contentDescription = null,
            tint = if (isMet) AppColors.Success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isMet) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ============================================================================
// PANTALLA PRINCIPAL
// ============================================================================

@Composable
fun ParentRegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: (UserWithProfile) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    var currentStep by remember { mutableIntStateOf(1) }

    // Datos del apoderado
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Datos del estudiante
    var classCode by remember { mutableStateOf("") }
    var studentRut by remember { mutableStateOf("") }
    var studentFirstName by remember { mutableStateOf("") }
    var studentLastName by remember { mutableStateOf("") }
    var relationshipType by remember { mutableStateOf(RelationshipType.MOTHER) }

    val uiState by viewModel.uiState.collectAsState()

    // Flag para controlar si se está usando Google
    var isUsingGoogle by remember { mutableStateOf(false) }

    // ✅ CAMBIO CLAVE: Solo autocompleta datos, NO intenta autenticar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.GoogleAuthPending -> {
                // Autocompletar datos del formulario
                email = state.email
                val nameParts = state.displayName.split(" ", limit = 2)
                firstName = nameParts.firstOrNull() ?: ""
                lastName = nameParts.getOrNull(1) ?: ""
                isUsingGoogle = true
            }
            is AuthUiState.Success -> {
                onRegisterSuccess(state.userWithProfile)
            }
            else -> {}
        }
    }

    // Validaciones de contraseña
    val hasMinLength = password.length >= 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasNumber = password.any { it.isDigit() }
    val passwordsMatch = password == confirmPassword && confirmPassword.isNotEmpty()
    val isPasswordValid = hasMinLength && hasUpperCase && hasLowerCase && hasNumber

    // Validar si el Paso 1 está completo (contraseña SIEMPRE requerida)
    val isStep1Valid = email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            phone.isNotBlank() &&
            isPasswordValid &&
            passwordsMatch

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 48.dp)
        ) {
            // Header con botón de atrás
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registro de Apoderado",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicador de pasos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(2) { step ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step + 1 <= currentStep)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${step + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (step + 1 <= currentStep)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (step == 0) "Tus Datos" else "Datos del Estudiante",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (step + 1 <= currentStep)
                                MaterialTheme.colorScheme.onBackground
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contenido animado según el paso
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> {
                        // PASO 1: Datos del Apoderado
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Paso 1: Tus Datos Personales",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Completa tu información personal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // ✅ CAMBIO CLAVE: Usar getGoogleDataForAutocomplete (NO autentica)
                            Button(
                                onClick = {
                                    activity?.let {
                                        viewModel.getGoogleDataForAutocomplete(it)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                enabled = uiState !is AuthUiState.Loading
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (uiState is AuthUiState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                    Text("Autocompletar con Google", fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Divider "O"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                Text(
                                    text = "O",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Email
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                placeholder = { Text("tu@email.com") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isUsingGoogle,
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Contraseñas (SIEMPRE visibles y obligatorias)
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Contraseña") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Requisitos de contraseña
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ParentPasswordRequirement("Mínimo 8 caracteres", hasMinLength)
                                ParentPasswordRequirement("Al menos una mayúscula", hasUpperCase)
                                ParentPasswordRequirement("Al menos una minúscula", hasLowerCase)
                                ParentPasswordRequirement("Al menos un número", hasNumber)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar Contraseña") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                                shape = RoundedCornerShape(16.dp)
                            )

                            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                                Text(
                                    text = "Las contraseñas no coinciden",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nombre
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("Nombre") },
                                placeholder = { Text("Juan") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Apellido
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Apellido") },
                                placeholder = { Text("Pérez") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Teléfono
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Teléfono") },
                                placeholder = { Text("+56 9 1234 5678") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dirección (opcional)
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Dirección (opcional)") },
                                placeholder = { Text("Calle 123, Comuna, Ciudad") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Botón Siguiente
                            Button(
                                onClick = { currentStep = 2 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = isStep1Valid && uiState !is AuthUiState.Loading,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Siguiente", fontWeight = FontWeight.Medium)
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }

                    2 -> {
                        // PASO 2: Datos del Estudiante
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Paso 2: Datos del Estudiante",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Información del estudiante que representas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Código de Clase
                            OutlinedTextField(
                                value = classCode,
                                onValueChange = { classCode = it.uppercase() },
                                label = { Text("Código de Clase") },
                                placeholder = { Text("ABC123") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                supportingText = {
                                    Text(
                                        text = "Solicita este código a tu profesor/a",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // RUT del Estudiante
                            OutlinedTextField(
                                value = studentRut,
                                onValueChange = { studentRut = it },
                                label = { Text("RUT del Estudiante") },
                                placeholder = { Text("12345678-9") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nombre del Estudiante
                            OutlinedTextField(
                                value = studentFirstName,
                                onValueChange = { studentFirstName = it },
                                label = { Text("Nombre del Estudiante") },
                                placeholder = { Text("María") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Apellido del Estudiante
                            OutlinedTextField(
                                value = studentLastName,
                                onValueChange = { studentLastName = it },
                                label = { Text("Apellido del Estudiante") },
                                placeholder = { Text("González") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tipo de relación
                            Text(
                                text = "Tipo de Relación",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RelationshipChip(
                                    text = "Madre",
                                    icon = Icons.Filled.FamilyRestroom,
                                    isSelected = relationshipType == RelationshipType.MOTHER,
                                    onClick = { relationshipType = RelationshipType.MOTHER },
                                    modifier = Modifier.weight(1f)
                                )
                                RelationshipChip(
                                    text = "Padre",
                                    icon = Icons.Filled.FamilyRestroom,
                                    isSelected = relationshipType == RelationshipType.FATHER,
                                    onClick = { relationshipType = RelationshipType.FATHER },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RelationshipChip(
                                    text = "Tutor",
                                    icon = Icons.Filled.SupervisedUserCircle,
                                    isSelected = relationshipType == RelationshipType.GUARDIAN,
                                    onClick = { relationshipType = RelationshipType.GUARDIAN },
                                    modifier = Modifier.weight(1f)
                                )
                                RelationshipChip(
                                    text = "Otro",
                                    icon = Icons.Filled.SupervisedUserCircle,
                                    isSelected = relationshipType == RelationshipType.OTHER,
                                    onClick = { relationshipType = RelationshipType.OTHER },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Botones de navegación
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                                        Text("Atrás", fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // ✅ BOTÓN FINAL: Aquí se crea la cuenta en Firebase
                                Button(
                                    onClick = {
                                        val parentForm = ParentRegistrationForm(
                                            email = email,
                                            password = password,
                                            confirmPassword = confirmPassword,
                                            firstName = firstName,
                                            lastName = lastName,
                                            phone = phone,
                                            classCode = classCode,
                                            address = address.ifBlank { null }
                                        )

                                        val studentForm = StudentRegistrationForm(
                                            classCode = classCode,
                                            studentRut = studentRut,
                                            studentFirstName = studentFirstName,
                                            studentLastName = studentLastName,
                                            studentBirthDate = null,
                                            relationshipType = relationshipType,
                                            isPrimary = true
                                        )

                                        // ✅ Registro completo SOLO cuando se presiona este botón
                                        viewModel.registerParent(
                                            parentForm = parentForm,
                                            studentForm = studentForm,
                                            googleIdToken = if (isUsingGoogle) {
                                                viewModel.getPendingGoogleToken()
                                            } else null
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    enabled = classCode.isNotBlank() &&
                                            studentRut.isNotBlank() &&
                                            studentFirstName.isNotBlank() &&
                                            studentLastName.isNotBlank() &&
                                            uiState !is AuthUiState.Loading,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (uiState is AuthUiState.Loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                        }
                                        Text("Crear Cuenta", fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mensaje de error
        if (uiState is AuthUiState.Error) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
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