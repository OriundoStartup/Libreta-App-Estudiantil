package com.oriundo.lbretaappestudiantil.ui.theme.teacher


import androidx.compose.foundation.layout.Arrangement
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
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para que el profesor revise una justificación pendiente.
 *
 * NOTA: En una app real, 'justification' se cargaría desde un ViewModel
 * usando el 'justificationId'. Aquí se usa data de ejemplo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewJustificationScreen(
    navController: NavController,
    justificationId: Int,
    teacherId: Int // ID del profesor que está revisando
) {
    // --- ESTADO DE LA VISTA ---
    var reviewNotes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf<JustificationStatus?>(null) }

    // --- TODO: Cargar la justificación ---
    // En una app real, esto vendría de un ViewModel:
    // val justification = viewModel.getJustificationById(justificationId).collectAsState()
    // Por ahora, usamos datos de ejemplo basados en tu entidad:
    val justification = AbsenceJustificationEntity(
        id = justificationId,
        studentId = 101, // Ejemplo
        parentId = 202, // Ejemplo
        absenceDate = System.currentTimeMillis() - 86400000, // Ayer
        reason = AbsenceReason.ILLNESS,
        description = "Mi hijo amaneció con fiebre y dolor de garganta. No podrá asistir a clases.",
        attachmentUrl = "uploads/certificado_123.pdf", // URL de ejemplo
    )

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
                            text = "Estudiante ID: ${justification.studentId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Apoderado ID: ${justification.parentId}",
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
                    .format(Date(justification.absenceDate))
            )
            InfoRow(
                icon = justification.reason.icon, // Usando el ícono de tu enum
                label = "Motivo",
                content = justification.reason.label // Usando el label de tu enum
            )
            InfoRow(
                icon = Icons.AutoMirrored.Filled.Notes,
                label = "Descripción del Apoderado",
                content = justification.description
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
                            text = justification.attachmentUrl.substringAfterLast('/')
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
                value = reviewNotes,
                onValueChange = { reviewNotes = it },
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
                        if (reviewNotes.isBlank()) {
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
                    enabled = !isSubmitting
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
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting && showConfirmDialog == JustificationStatus.APPROVED) {
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

    // --- Diálogo de Confirmación ---
    if (showConfirmDialog != null) {
        val statusToUpdate = showConfirmDialog!!
        val actionText = if (statusToUpdate == JustificationStatus.APPROVED) "aprobar" else "rechazar"

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showConfirmDialog = null },
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
                        isSubmitting = true
                        // --- TODO: Implementar lógica de envío ---
                        // viewModel.submitReview(
                        //     justificationId = justificationId,
                        //     status = statusToUpdate,
                        //     reviewNotes = reviewNotes,
                        //     teacherId = teacherId
                        // )
                        scope.launch {
                            // Simulación de red
                            kotlinx.coroutines.delay(1500)
                            isSubmitting = false
                            showConfirmDialog = null
                            snackbarHostState.showSnackbar("Justificación ${actionText}da correctamente")
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (statusToUpdate == JustificationStatus.APPROVED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = null },
                    enabled = !isSubmitting
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