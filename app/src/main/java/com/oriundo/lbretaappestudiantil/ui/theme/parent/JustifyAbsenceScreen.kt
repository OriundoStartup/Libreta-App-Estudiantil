package com.oriundo.lbretaappestudiantil.ui.theme.parent


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.JustifyAbsenceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JustifyAbsenceScreen(
    studentId: Int,
    parentId: Int,
    navController: NavController,
    // ✅ INYECTAR VIEWMODEL
    viewModel: JustifyAbsenceViewModel = hiltViewModel()
) {

    // ✅ SE OBSERVA EL ESTADO DEL VIEWMODEL
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado local para controlar el diálogo de la fecha
    var showDatePicker by remember { mutableStateOf(false) }

    // ✅ LaunchedEffect para manejar la navegación tras el éxito de Firebase
    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Justificación enviada correctamente.")
                navController.popBackStack()
            }
        }
    }

    // ✅ LaunchedEffect para manejar errores de Firebase/Red
    LaunchedEffect(uiState.submissionError) {
        uiState.submissionError?.let { error ->
            scope.launch {
                // Muestra el error de envío (ej. "Error al enviar: Network failure")
                snackbarHostState.showSnackbar(error)
                // Opcional: Llamar a una función en el ViewModel para limpiar el error después de mostrarlo
            }
        }
    }

    // ✅ File Picker ahora llama a la función del ViewModel
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onFileSelected(uri)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // ... (resto de TopAppBar es el mismo)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ... (Card de Estudiante es el mismo) ...

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Fecha de la falta *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        // ✅ Usa el estado del ViewModel
                        text = if (uiState.selectedDate != null) {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(Date(uiState.selectedDate!!))
                        } else {
                            "Seleccionar fecha"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.selectedDate != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Motivo *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            AbsenceReason.entries.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // ✅ Usa el estado del ViewModel
                        .selectable(
                            selected = uiState.selectedReason == reason,
                            onClick = { viewModel.onReasonSelected(reason) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.selectedReason == reason,
                        onClick = { viewModel.onReasonSelected(reason) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = reason.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reason.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Descripción *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                // ✅ Usa el estado del ViewModel
                value = uiState.description,
                // ✅ Llama al handler del ViewModel
                onValueChange = { viewModel.onDescriptionChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Describe el motivo de la falta...") },
                minLines = 4,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Certificado médico (Opcional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Usa el estado del ViewModel
            if (uiState.selectedFileUri == null) {
                OutlinedCard(
                    onClick = {
                        // Abre el selector de archivos del sistema operativo
                        filePickerLauncher.launch("*/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    // 🛠️ CONTENIDO DE LA FILA PARA ADJUNTAR ARCHIVO 🛠️
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), // Padding interno para espacio
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary // Usamos el color primario
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Adjuntar certificado (Toca para seleccionar)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // ⬇️ El bloque 'else' se mantiene sin cambios
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Archivo adjunto",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                // ✅ Usa el estado del ViewModel
                                text = uiState.selectedFileUri?.lastPathSegment ?: "archivo.pdf",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        // ✅ Llama al handler del ViewModel para eliminar el archivo
                        IconButton(onClick = { viewModel.onFileSelected(null) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // **VALIDACIÓN RÁPIDA DE UI** (La validación real está en el ViewModel)
                    if (uiState.selectedDate == null) {
                        scope.launch { snackbarHostState.showSnackbar("Selecciona una fecha") }
                        return@Button
                    }
                    if (uiState.description.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Escribe una descripción") }
                        return@Button
                    }

                    // ✅ LLAMADA AL MÉTODO DEL VIEWMODEL QUE INICIA LA SINCRONIZACIÓN CON FIREBASE
                    viewModel.submitJustification()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                // ✅ Usa el estado del ViewModel para habilitar/deshabilitar el botón
                enabled = !uiState.isSubmitting && uiState.selectedDate != null && uiState.description.isNotBlank()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Justificación")
                }
            }

            // ... (Resto de la UI es el mismo) ...
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false }, // ✅ Cerrar en Dismiss Request
            confirmButton = {
                TextButton(
                    onClick = {
                        // ✅ Llama al handler del ViewModel
                        datePickerState.selectedDateMillis?.let { viewModel.onDateSelected(it) }
                        showDatePicker = false // ✅ Cerrar el diálogo
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { // ✅ Cerrar el diálogo
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
} 