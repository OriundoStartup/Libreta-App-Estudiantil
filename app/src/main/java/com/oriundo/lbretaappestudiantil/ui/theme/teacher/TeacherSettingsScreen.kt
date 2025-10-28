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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSettingsScreen(
    navController: NavController
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(false) }
    var attendanceReminders by remember { mutableStateOf(true) }
    var annotationNotifications by remember { mutableStateOf(true) }

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
        }
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
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsSwitchItem(
                    icon = Icons.Filled.Email,
                    title = "Notificaciones por Email",
                    subtitle = "Recibir resúmenes diarios por correo",
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsSwitchItem(
                    icon = Icons.Filled.Alarm,
                    title = "Recordatorios de Asistencia",
                    subtitle = "Recibir recordatorios para tomar asistencia",
                    checked = attendanceReminders,
                    onCheckedChange = { attendanceReminders = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsSwitchItem(
                    icon = Icons.Filled.MarkChatRead,
                    title = "Notificar Anotaciones",
                    subtitle = "Confirmar cuando se crea una anotación",
                    checked = annotationNotifications,
                    onCheckedChange = { annotationNotifications = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferencias de Curso
            TeacherSettingsSection(title = "Preferencias de Curso") {
                TeacherSettingsItem(
                    icon = Icons.Filled.ViewModule,
                    title = "Vista por Defecto",
                    subtitle = "Lista de estudiantes",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.AutoMirrored.Filled.Sort,
                    title = "Orden de Estudiantes",
                    subtitle = "Alfabético por apellido",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cuenta
            TeacherSettingsSection(title = "Cuenta") {
                TeacherSettingsItem(
                    icon = Icons.Filled.Lock,
                    title = "Cambiar Contraseña",
                    subtitle = "Actualizar tu contraseña",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Privacidad y Seguridad",
                    subtitle = "Gestionar tu privacidad",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Soporte
            TeacherSettingsSection(title = "Soporte") {
                TeacherSettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Centro de Ayuda",
                    subtitle = "Guías y tutoriales para profesores",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.Filled.BugReport,
                    title = "Reportar un Problema",
                    subtitle = "Enviar feedback o reportar errores",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                TeacherSettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Acerca de",
                    subtitle = "Libreta App v1.0.0 - Para Profesores",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
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
    onClick: () -> Unit
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