package com.oriundo.lbretaappestudiantil.ui.theme.teacher


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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ReviewJustificationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para que el profesor revise una justificación pendiente.
 *
 * AHORA CONECTADA AL VIEWMODEL
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewJustificationScreen(
    navController: NavController,
    justificationId: Int, // Recibido por NavHost
    teacherId: Int // Recibido por NavHost
) {
    // --- INYECCIÓN DEL VIEWMODEL ---
    val viewModel: ReviewJustificationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // --- ESTADO DE LA VISTA ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf<JustificationStatus?>(null) }

    // --- DATOS ---
    // El 'justification' ahora viene del uiState
    val justification = uiState.justification

    // --- MANEJO DE EFECTOS (SNACKBARS Y NAVEGACIÓN) ---
    LaunchedEffect(uiState.reviewSuccess) {
        if (uiState.reviewSuccess) {
            snackbarHostState.showSnackbar("Revisión enviada correctamente")
            navController.popBackStack()
        }
    }

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError() // Limpiar el error después de mostrarlo
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Revisar Justificación",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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

        // --- ESTADO DE CARGA ---
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold // Salir temprano si está cargando
        }

        // --- ESTADO DE ERROR (si no se pudo cargar) ---
        if (justification == null && uiState.reviewError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                InfoRow(
                    icon = Icons.Filled.Warning,
                    label = "Error",
                    content = uiState.reviewError ?: "No se pudo cargar la justificación."
                )
            }
            return@Scaffold // Salir temprano
        }

        // --- ESTADO DE ÉXITO (justificación cargada) ---
        if (justification != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // --- Tarjeta de Información ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChildCare,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Estudiante ID: ${justification.studentId}", // USANDO DATOS DEL VM
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Apoderado ID: ${justification.parentId}", // USANDO DATOS DEL VM
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Detalles de la Justificación ---
                Text(
                    text = "Detalles de la Solicitud",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(
                    icon = Icons.Filled.CalendarMonth,
                    label = "Fecha de la Falta",
                    content = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(Date(justification.absenceDate)) // USANDO DATOS DEL VM
                )
                InfoRow(
                    icon = justification.reason.icon, // USANDO DATOS DEL VM
                    label = "Motivo",
                    content = justification.reason.label // USANDO DATOS DEL VM
                )
                InfoRow(
                    icon = Icons.AutoMirrored.Filled.Notes,
                    label = "Descripción del Apoderado",
                    content = justification.description // USANDO DATOS DEL VM
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Archivo Adjunto ---
                Text(
                    text = "Archivo Adjunto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (justification.attachmentUrl != null) {
                    OutlinedCard(
                        onClick = {
                            // TODO: Implementar lógica para abrir/descargar el archivo
                            // (usando la URL 'justification.attachmentUrl')
                            scope.launch {
                                snackbarHostState.showSnackbar("Abriendo adjunto...")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = justification.attachmentUrl.substringAfterLast('/') // USANDO DATOS DEL VM
                                    .takeIf { it.isNotBlank() } ?: "Ver archivo adjunto",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    InfoRow(
                        icon = Icons.Filled.AttachFile,
                        label = "Adjunto",
                        content = "No se adjuntó ningún archivo."
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))



                Text(
                    text = "Notas de Revisión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.reviewNotes, // CONECTADO AL VM
                    onValueChange = { viewModel.onReviewNotesChanged(it) }, // CONECTADO AL VM
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Añadir notas (opcional al aprobar)...") },
                    minLines = 3,
                    maxLines = 5,
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Botones de Acción ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón RECHAZAR
                    Button(
                        onClick = {
                            if (uiState.reviewNotes.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Debes añadir notas para rechazar")
                                }
                            } else {
                                showConfirmDialog = JustificationStatus.REJECTED
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !uiState.isSubmitting // CONECTADO AL VM
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rechazar")
                    }

                    // Botón APROBAR
                    Button(
                        onClick = {
                            showConfirmDialog = JustificationStatus.APPROVED
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary // Color "Success" de tu tema
                        ),
                        enabled = !uiState.isSubmitting // CONECTADO AL VM
                    ) {
                        if (uiState.isSubmitting && showConfirmDialog == JustificationStatus.APPROVED) { // CONECTADO AL VM
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aprobar")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // --- Diálogo de Confirmación ---
    if (showConfirmDialog != null) {
        val statusToUpdate = showConfirmDialog!!
        val actionText = if (statusToUpdate == JustificationStatus.APPROVED) "aprobar" else "rechazar"

        AlertDialog(
            onDismissRequest = { if (!uiState.isSubmitting) showConfirmDialog = null }, // CONECTADO AL VM
            icon = {
                Icon(
                    if (statusToUpdate == JustificationStatus.APPROVED) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null
                )
            },
            title = { Text("Confirmar $actionText") },
            text = { Text("¿Estás seguro de que quieres $actionText esta justificación?") },
            confirmButton = {
                Button(
                    onClick = {
                        // --- LLAMADA AL VIEWMODEL ---
                        viewModel.submitReview(statusToUpdate)
                        // El cierre del diálogo ahora ocurre aquí
                        showConfirmDialog = null
                        // La navegación y el snackbar se manejan vía LaunchedEffect
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (statusToUpdate == JustificationStatus.APPROVED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    ),
                    enabled = !uiState.isSubmitting // Habilitar/deshabilitar el botón
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Confirmar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = null },
                    enabled = !uiState.isSubmitting // CONECTADO AL VM
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Composable auxiliar para mostrar filas de información (Icono, Etiqueta, Contenido).
 */
@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    content: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}