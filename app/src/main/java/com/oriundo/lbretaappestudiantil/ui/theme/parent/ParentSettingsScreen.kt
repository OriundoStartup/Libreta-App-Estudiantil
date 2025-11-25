package com.oriundo.lbretaappestudiantil.ui.theme.parent

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
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
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
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.HelpCenterDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.LogoutConfirmationDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.PasswordChangeSuccessDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.SelectDefaultViewDialog
import com.oriundo.lbretaappestudiantil.ui.theme.parent.components.SelectLanguageDialog
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.PasswordChangeState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentSettingsScreen(
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
            ParentSettingsSection(title = "Notificaciones") {
                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notificaciones Push",
                    subtitle = "Recibir notificaciones en el dispositivo",
                    checked = preferences.notificationsEnabled,
                    onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Email,
                    title = "Notificaciones por Email",
                    subtitle = "Recibir resúmenes semanales por correo",
                    checked = preferences.emailNotifications,
                    onCheckedChange = { viewModel.updateEmailNotifications(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Description,
                    title = "Alertas de Anotaciones",
                    subtitle = "Notificar cuando haya nuevas anotaciones",
                    checked = preferences.annotationNotifications,
                    onCheckedChange = { viewModel.updateAnnotationNotifications(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Event,
                    title = "Recordatorios de Eventos",
                    subtitle = "Recibir recordatorios de eventos escolares",
                    checked = preferences.eventReminders,
                    onCheckedChange = { viewModel.updateEventReminders(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Inventory,
                    title = "Solicitudes de Materiales",
                    subtitle = "Alertas de nuevas solicitudes de materiales",
                    checked = preferences.materialRequestAlerts,
                    onCheckedChange = { viewModel.updateMaterialRequestAlerts(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferencias
            ParentSettingsSection(title = "Preferencias") {
                ParentSettingsItem(
                    icon = Icons.Filled.ViewModule,
                    title = "Vista por Defecto",
                    subtitle = preferences.defaultView.displayName,
                    onClick = { showDefaultViewDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Idioma",
                    subtitle = preferences.language.displayName,
                    onClick = { showLanguageDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cuenta
            ParentSettingsSection(title = "Cuenta") {
                ParentSettingsItem(
                    icon = Icons.Filled.Lock,
                    title = "Cambiar Contraseña",
                    subtitle = "Actualizar tu contraseña",
                    onClick = { showChangePasswordDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Privacidad y Seguridad",
                    subtitle = "Gestionar tu privacidad",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Funcionalidad próximamente")
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.FamilyRestroom,
                    title = "Gestionar Vínculos",
                    subtitle = "Vincular o desvincular estudiantes",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Funcionalidad próximamente")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Soporte
            ParentSettingsSection(title = "Soporte") {
                ParentSettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Centro de Ayuda",
                    subtitle = "Guías y preguntas frecuentes",
                    onClick = { showHelpDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.AutoMirrored.Filled.ContactSupport,
                    title = "Contactar al Colegio",
                    subtitle = "Información de contacto",
                    onClick = { showContactDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.BugReport,
                    title = "Reportar un Problema",
                    subtitle = "Enviar feedback o reportar errores",
                    onClick = { showReportDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Acerca de",
                    subtitle = "Libreta App v1.0.0 - Para Apoderados",
                    onClick = { showAboutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cerrar Sesión
            ParentSettingsSection(title = "Sesión") {
                ParentSettingsItem(
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
        HelpCenterDialog(onDismiss = { showHelpDialog = false })
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
private fun ParentSettingsSection(
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
private fun ParentSettingsItem(
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
            tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
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
private fun ParentSettingsSwitchItem(
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
            tint = MaterialTheme.colorScheme.tertiary,
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