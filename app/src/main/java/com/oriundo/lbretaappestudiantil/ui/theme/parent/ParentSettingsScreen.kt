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
fun ParentSettingsScreen(
    navController: NavController
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(false) }
    var annotationNotifications by remember { mutableStateOf(true) }
    var eventReminders by remember { mutableStateOf(true) }
    var materialRequestAlerts by remember { mutableStateOf(true) }

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
            ParentSettingsSection(title = "Notificaciones") {
                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notificaciones Push",
                    subtitle = "Recibir notificaciones en el dispositivo",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Email,
                    title = "Notificaciones por Email",
                    subtitle = "Recibir resúmenes semanales por correo",
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Description,
                    title = "Alertas de Anotaciones",
                    subtitle = "Notificar cuando haya nuevas anotaciones",
                    checked = annotationNotifications,
                    onCheckedChange = { annotationNotifications = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Event,
                    title = "Recordatorios de Eventos",
                    subtitle = "Recibir recordatorios de eventos escolares",
                    checked = eventReminders,
                    onCheckedChange = { eventReminders = it }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsSwitchItem(
                    icon = Icons.Filled.Inventory,
                    title = "Solicitudes de Materiales",
                    subtitle = "Alertas de nuevas solicitudes de materiales",
                    checked = materialRequestAlerts,
                    onCheckedChange = { materialRequestAlerts = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferencias
            ParentSettingsSection(title = "Preferencias") {
                ParentSettingsItem(
                    icon = Icons.Filled.ViewModule,
                    title = "Vista por Defecto",
                    subtitle = "Resumen del estudiante",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Idioma",
                    subtitle = "Español",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cuenta
            ParentSettingsSection(title = "Cuenta") {
                ParentSettingsItem(
                    icon = Icons.Filled.Lock,
                    title = "Cambiar Contraseña",
                    subtitle = "Actualizar tu contraseña",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Privacidad y Seguridad",
                    subtitle = "Gestionar tu privacidad",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.FamilyRestroom,
                    title = "Gestionar Vínculos",
                    subtitle = "Vincular o desvincular estudiantes",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Soporte
            ParentSettingsSection(title = "Soporte") {
                ParentSettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Centro de Ayuda",
                    subtitle = "Guías y preguntas frecuentes",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.AutoMirrored.Filled.ContactSupport,
                    title = "Contactar al Colegio",
                    subtitle = "Información de contacto",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.BugReport,
                    title = "Reportar un Problema",
                    subtitle = "Enviar feedback o reportar errores",
                    onClick = { /* TODO: Implementar */ }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ParentSettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Acerca de",
                    subtitle = "Libreta App v1.0.0 - Para Apoderados",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
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