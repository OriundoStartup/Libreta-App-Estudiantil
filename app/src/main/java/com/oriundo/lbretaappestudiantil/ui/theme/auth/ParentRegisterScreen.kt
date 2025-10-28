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
import androidx.compose.ui.text.font.FontWeight
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

    // Estado para deshabilitar campos (si se autenticó con Google)
    val isGoogleAuthenticated = uiState is AuthUiState.AwaitingProfileCompletion

    // ============================================================================
// REEMPLAZAR EL LaunchedEffect EXISTENTE EN ParentRegisterScreen.kt
// ============================================================================

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                // ✅ Usuario autenticado completamente → Ir al Dashboard
                onRegisterSuccess(state.userWithProfile)
            }
            is AuthUiState.AwaitingProfileCompletion -> {
                // ✅ Usuario de Google nuevo - Pre-cargar datos
                email = state.tempUser.user.email
                firstName = state.tempUser.profile.firstName
                lastName = state.tempUser.profile.lastName
                phone = state.tempUser.profile.phone ?: ""
                address = state.tempUser.profile.address ?: ""

                // NO avanzar automáticamente - el usuario debe completar manualmente
                // El botón de Google se deshabilitará automáticamente con isGoogleAuthenticated
            }
            is AuthUiState.AwaitingPasswordSetup -> {
                // ✅ NUEVO: Perfil completado, ahora redirigir a SetPasswordScreen
                // Esto lo manejamos en AppNavigation.kt
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
                .padding(24.dp)
                .padding(bottom = 48.dp)
        ) {
            // Header con botón de regresar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "Registro de Apoderado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicador de progreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(2) { index ->
                    val step = index + 1
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step <= currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = step.toString(),
                                color = if (step <= currentStep) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (index < 1) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .background(
                                        if (step < currentStep) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contenido scrolleable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "step_transition"
                ) { step ->
                    when (step) {
                        1 -> {
                            // PASO 1: Datos del Apoderado
                            Column {
                                Text(
                                    "Paso 1: Información del Apoderado",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Ingresa tus datos personales",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Campos de nombre y apellido
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = lastName,
                                    onValueChange = { lastName = it },
                                    label = { Text("Apellido") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text("Teléfono") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = { Text("Dirección (Opcional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Botón Google Sign-In
                                Button(
                                    onClick = {
                                        // ✅ CORREGIDO: Usar registerWithGoogle en lugar de loginWithGoogle
                                        viewModel.registerWithGoogle(isTeacher = false)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = uiState !is AuthUiState.Loading && !isGoogleAuthenticated,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4285F4),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.SupervisedUserCircle, null, modifier = Modifier.size(24.dp))
                                        Text("Registrarse con Google", fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Separador "O"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(1.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    Text(
                                        text = " O usa Email/Contraseña ",
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(1.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Campos de credenciales
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isGoogleAuthenticated
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Contraseña") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isGoogleAuthenticated
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Confirmar Contraseña") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isGoogleAuthenticated
                                )

                                // Validación de contraseña
                                if (password.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        ParentPasswordRequirement("Mínimo 6 caracteres", password.length >= 6)
                                        ParentPasswordRequirement("Las contraseñas coinciden", password == confirmPassword && confirmPassword.isNotBlank())
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Botón para continuar
                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    enabled = (isGoogleAuthenticated || (email.isNotBlank() &&
                                            password.isNotBlank() &&
                                            confirmPassword.isNotBlank() &&
                                            password == confirmPassword &&
                                            password.length >= 6)) &&
                                            firstName.isNotBlank() &&
                                            lastName.isNotBlank() &&
                                            phone.isNotBlank() &&
                                            uiState !is AuthUiState.Loading,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                                        Text("Continuar al Paso 2", fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        2 -> {
                            // PASO 2: Datos del Estudiante
                            Column {
                                Text(
                                    "Paso 2: Información del Estudiante",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Ingresa los datos de tu hijo/a o estudiante",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Código de clase
                                OutlinedTextField(
                                    value = classCode,
                                    onValueChange = { classCode = it.uppercase() },
                                    label = { Text("Código de Clase") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    supportingText = { Text("Se convertirá automáticamente a mayúsculas") }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // RUT del estudiante
                                OutlinedTextField(
                                    value = studentRut,
                                    onValueChange = { studentRut = it },
                                    label = { Text("RUT del Estudiante") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Nombre del estudiante
                                OutlinedTextField(
                                    value = studentFirstName,
                                    onValueChange = { studentFirstName = it },
                                    label = { Text("Nombre del Estudiante") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Apellido del estudiante
                                OutlinedTextField(
                                    value = studentLastName,
                                    onValueChange = { studentLastName = it },
                                    label = { Text("Apellido del Estudiante") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Tipo de relación
                                Text(
                                    "Relación con el estudiante",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
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

                                    // ✅ CORREGIDO: Ahora incluye classCode en ParentRegistrationForm
                                    Button(
                                        onClick = {
                                            val parentForm = ParentRegistrationForm(
                                                email = email,
                                                password = password.ifBlank { null },
                                                confirmPassword = confirmPassword.ifBlank { null },
                                                firstName = firstName,
                                                lastName = lastName,
                                                phone = phone,
                                                classCode = classCode, // ✅ AQUÍ ESTÁ LA CORRECCIÓN
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

                                            viewModel.registerParent(parentForm, studentForm)
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
                                            Text("Finalizar Registro", fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mensaje de error fijo en la parte inferior
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