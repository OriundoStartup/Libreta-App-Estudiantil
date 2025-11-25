package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.ui.theme.Screen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.AboutDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.ChangePasswordDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.ContactSchoolDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.LogoutConfirmationDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.PasswordChangeSuccessDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.SelectDefaultViewDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.SelectLanguageDialog
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.components.SelectStudentSortOrderDialog
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.PasswordChangeState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.userPreferences.collectAsState()
    val passwordChangeState by viewModel.passwordChangeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estados de diálogos
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showPasswordSuccessDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDefaultViewDialog by remember { mutableStateOf(false) }
    var showSortOrderDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Manejar estados de cambio de contraseña
    LaunchedEffect(passwordChangeState) {
        when (val state = passwordChangeState) {
            is PasswordChangeState.Success -> {
                showChangePasswordDialog = false
                showPasswordSuccessDialog = true
                viewModel.resetPasswordChangeState()
            }
            is PasswordChangeState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetPasswordChangeState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        fontWeight = FontWeight.Bold
                    )
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Notificaciones
            TeacherSettingsSection(title = "Notificaciones") {
                TeacherSettingsSwitchItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notificaciones Push",
                    subtitle = "Recibir notificaciones en el dispositivo",
                    checked = preferences.notificationsEnabled,
                    onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsSwitchItem(
                    icon = Icons.Filled.Email,
                    title = "Notificaciones por Email",
                    subtitle = "Recibir resúmenes diarios por correo",
                    checked = preferences.emailNotifications,
                    onCheckedChange = { viewModel.updateEmailNotifications(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsSwitchItem(
                    icon = Icons.Filled.Alarm,
                    title = "Recordatorios de Asistencia",
                    subtitle = "Recibir recordatorios para tomar asistencia",
                    checked = preferences.eventReminders,
                    onCheckedChange = { viewModel.updateEventReminders(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsSwitchItem(
                    icon = Icons.Filled.MarkChatRead,
                    title = "Notificar Anotaciones",
                    subtitle = "Confirmar cuando se crea una anotación",
                    checked = preferences.annotationNotifications,
                    onCheckedChange = { viewModel.updateAnnotationNotifications(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferencias de Curso
            TeacherSettingsSection(title = "Preferencias de Curso") {
                TeacherSettingsItem(
                    icon = Icons.Filled.ViewModule,
                    title = "Vista por Defecto",
                    subtitle = preferences.defaultView.displayName,
                    onClick = { showDefaultViewDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.AutoMirrored.Filled.Sort,
                    title = "Orden de Estudiantes",
                    subtitle = "Alfabético por apellido",
                    onClick = { showSortOrderDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Idioma",
                    subtitle = preferences.language.displayName,
                    onClick = { showLanguageDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cuenta
            TeacherSettingsSection(title = "Cuenta") {
                TeacherSettingsItem(
                    icon = Icons.Filled.Lock,
                    title = "Cambiar Contraseña",
                    subtitle = "Actualizar tu contraseña",
                    onClick = { showChangePasswordDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Privacidad y Seguridad",
                    subtitle = "Gestionar tu privacidad",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Funcionalidad próximamente")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Soporte
            TeacherSettingsSection(title = "Soporte") {
                TeacherSettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Centro de Ayuda",
                    subtitle = "Guías y tutoriales para profesores",
                    onClick = { showHelpDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.Filled.BugReport,
                    title = "Reportar un Problema",
                    subtitle = "Enviar feedback o reportar errores",
                    onClick = { showReportDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Acerca de",
                    subtitle = "Libreta App v1.0.0 - Para Profesores",
                    onClick = { showAboutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cerrar Sesión
            TeacherSettingsSection(title = "Sesión") {
                TeacherSettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Cerrar Sesión",
                    subtitle = "Salir de la aplicación",
                    onClick = { showLogoutDialog = true },
                    isDanger = true
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Diálogos
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { current, new ->
                viewModel.changePassword(current, new)
            },
            isLoading = passwordChangeState is PasswordChangeState.Loading
        )
    }

    if (showPasswordSuccessDialog) {
        PasswordChangeSuccessDialog(
            onDismiss = { showPasswordSuccessDialog = false }
        )
    }

    if (showLanguageDialog) {
        SelectLanguageDialog(
            currentLanguage = preferences.language,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { viewModel.updateLanguage(it) }
        )
    }

    if (showDefaultViewDialog) {
        SelectDefaultViewDialog(
            currentView = preferences.defaultView,
            onDismiss = { showDefaultViewDialog = false },
            onViewSelected = { viewModel.updateDefaultView(it) }
        )
    }

    if (showSortOrderDialog) {
        SelectStudentSortOrderDialog(
            onDismiss = { showSortOrderDialog = false },
            onSortOrderSelected = { sortOrder ->
                scope.launch {
                    snackbarHostState.showSnackbar("Orden cambiado a: $sortOrder")
                }
            }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                scope.launch {
                    when (viewModel.logout()) {
                        is ApiResult.Success -> {
                            navController.navigate(Screen.RoleSelection.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        is ApiResult.Error -> {
                            snackbarHostState.showSnackbar("Error al cerrar sesión")
                        }
                        else -> {}
                    }
                }
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showHelpDialog) {
        TeacherHelpDialog(onDismiss = { showHelpDialog = false })
    }

    if (showContactDialog) {
        ContactSchoolDialog(onDismiss = { showContactDialog = false })
    }

    if (showReportDialog) {
        ReportProblemDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { feedback ->
                scope.launch {
                    snackbarHostState.showSnackbar("Gracias por tu feedback")
                    showReportDialog = false
                }
            }
        )
    }
}

@Composable
private fun TeacherSettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun TeacherSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TeacherSettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun TeacherHelpDialog(
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Centro de Ayuda - Profesores",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Preguntas Frecuentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TeacherHelpItem(
                        question = "¿Cómo tomar asistencia?",
                        answer = "Ve a tu curso, selecciona 'Tomar Asistencia' y marca presente/ausente para cada estudiante."
                    )
                    TeacherHelpItem(
                        question = "¿Cómo crear una anotación?",
                        answer = "Entra al perfil del estudiante y selecciona 'Crear Anotación'. Elige el tipo y escribe el comentario."
                    )
                    TeacherHelpItem(
                        question = "¿Cómo revisar justificaciones?",
                        answer = "Ve a 'Justificaciones Pendientes' desde el dashboard y aprueba o rechaza cada solicitud."
                    )
                    TeacherHelpItem(
                        question = "¿Cómo enviar mensajes?",
                        answer = "Usa el botón de mensajes, selecciona el apoderado y escribe tu mensaje."
                    )
                }

                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun TeacherHelpItem(
    question: String,
    answer: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = answer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReportProblemDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var feedback by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reportar un Problema",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Describe el problema o envía tus sugerencias:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                androidx.compose.material3.OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    placeholder = { Text("Escribe aquí...") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(feedback) },
                enabled = feedback.isNotBlank()
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}