package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.PendingJustificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * ✅ Pantalla actualizada con sincronización de Firebase y pull-to-refresh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPendingJustificationsScreen(
    teacherId: Int,
    navController: NavHostController,
    viewModel: PendingJustificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar datos al iniciar la pantalla
    LaunchedEffect(teacherId) {
        viewModel.loadPendingJustifications(teacherId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Justificaciones Pendientes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // ✅ Botón de refresh
                    IconButton(
                        onClick = { viewModel.refreshJustifications(teacherId) },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Actualizar",
                            tint = if (uiState.isSyncing) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ✅ Indicador de sincronización
            if (uiState.isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ✅ Pull-to-refresh
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refreshJustifications(teacherId) },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    // Estado de carga inicial
                    uiState.isLoading && uiState.justifications.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (uiState.isSyncing) {
                                        "Sincronizando con Firebase..."
                                    } else {
                                        "Cargando justificaciones..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Manejo de error
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    text = "Error: ${uiState.error}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Desliza hacia abajo para reintentar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Estado vacío
                    uiState.justifications.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    text = "🎉",
                                    style = MaterialTheme.typography.displayLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No hay justificaciones pendientes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Todas las justificaciones han sido revisadas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Mostrar la lista
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(
                                        text = "Tienes ${uiState.justifications.size} justificación${if (uiState.justifications.size != 1) "es" else ""} pendiente${if (uiState.justifications.size != 1) "s" else ""}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Toca una justificación para revisarla",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                 }
                            }

                            items(uiState.justifications) { justification ->
                                JustificationItem(
                                    justification = justification,
                                    onClick = {
                                        navController.navigate(
                                            com.oriundo.lbretaappestudiantil.ui.theme.Screen.ReviewJustification.createRoute(
                                                justificationId = justification.id,
                                                teacherId = teacherId
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JustificationItem(
    justification: AbsenceJustificationEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // ✅ Mostrar nombre del estudiante si está disponible
                    Text(
                        text = justification.studentName.orEmpty().ifBlank { "Estudiante ID: ${justification.studentId}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Fecha de ausencia
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text(
                        text = "Ausencia: ${dateFormat.format(Date(justification.absenceDate))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Ícono de Pendiente
                Icon(
                    Icons.Filled.Info,
                    contentDescription = "Pendiente",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Razón formateada
            val formattedReason = justification.reason.name
                .replace('_', ' ')
                .lowercase()
                .replaceFirstChar { it.uppercase() }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Razón: $formattedReason",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Fecha de envío
                if (justification.createdAt > 0) {
                    val createdDate = SimpleDateFormat("dd/MM", Locale.getDefault())
                        .format(Date(justification.createdAt))
                    Text(
                        text = "Enviado: $createdDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}