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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.EventType
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    teacherId: Int,
    eventViewModel: SchoolEventViewModel = hiltViewModel(),
    classViewModel: ClassViewModel = hiltViewModel()
) {
    // Estados del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf<Int?>(null) }
    var selectedEventType by remember { mutableStateOf(EventType.MEETING) }
    var eventDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showEventTypeDropdown by remember { mutableStateOf(false) }

    // Observar estados
    val createState by eventViewModel.createState.collectAsState()
    val teacherClasses by classViewModel.teacherClasses.collectAsState()

    // Cargar clases del profesor
    LaunchedEffect(teacherId) {
        classViewModel.loadTeacherClasses(teacherId)
    }

    // Manejar éxito
    LaunchedEffect(createState) {
        if (createState is SchoolEventUiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Evento Escolar",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de información básica
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Información del Evento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Campo: Título
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título del evento") },
                        placeholder = { Text("Ej: Reunión de apoderados") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = title.isBlank() && createState is SchoolEventUiState.Error,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Campo: Descripción
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        placeholder = { Text("Detalles del evento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        isError = description.isBlank() && createState is SchoolEventUiState.Error,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Tarjeta de configuración
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Selector de Clase
                    ExposedDropdownMenuBox(
                        expanded = showClassDropdown,
                        onExpandedChange = { showClassDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = teacherClasses.find { it.id == selectedClassId }?.className
                                ?: "Seleccionar clase",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Clase (opcional)") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClassDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = showClassDropdown,
                            onDismissRequest = { showClassDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ninguna (evento general)") },
                                onClick = {
                                    selectedClassId = null
                                    showClassDropdown = false
                                }
                            )
                            teacherClasses.forEach { classEntity ->
                                DropdownMenuItem(
                                    text = { Text(classEntity.className) },
                                    onClick = {
                                        selectedClassId = classEntity.id
                                        showClassDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Selector de Tipo de Evento
                    ExposedDropdownMenuBox(
                        expanded = showEventTypeDropdown,
                        onExpandedChange = { showEventTypeDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = when (selectedEventType) {
                                EventType.TEST -> "📝 Prueba"
                                EventType.ASSIGNMENT -> "📚 Tarea"
                                EventType.PROJECT -> "🎯 Proyecto"
                                EventType.FIELD_TRIP -> "🚌 Salida Pedagógica"
                                EventType.MEETING -> "👥 Reunión"
                                EventType.HOLIDAY -> "🎉 Festivo"
                                EventType.OTHER -> "📌 Otro"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de evento") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEventTypeDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = showEventTypeDropdown,
                            onDismissRequest = { showEventTypeDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📝 Prueba") },
                                onClick = {
                                    selectedEventType = EventType.TEST
                                    showEventTypeDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📚 Tarea") },
                                onClick = {
                                    selectedEventType = EventType.ASSIGNMENT
                                    showEventTypeDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🎯 Proyecto") },
                                onClick = {
                                    selectedEventType = EventType.PROJECT
                                    showEventTypeDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🚌 Salida Pedagógica") },
                                onClick = {
                                    selectedEventType = EventType.FIELD_TRIP
                                    showEventTypeDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("👥 Reunión") },
                                onClick = {
                                    selectedEventType = EventType.MEETING
                                    showEventTypeDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🎉 Festivo") },
                                onClick = {
                                    selectedEventType = EventType.HOLIDAY
                                    showEventTypeDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📌 Otro") },
                                onClick = {
                                    selectedEventType = EventType.OTHER
                                    showEventTypeDropdown = false
                                }
                            )
                        }
                    }

                    // Selector de Fecha
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(eventDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha del evento") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    "Seleccionar fecha",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de crear con gradiente
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        eventViewModel.createEvent(
                            classId = selectedClassId,
                            teacherId = teacherId,
                            title = title,
                            description = description,
                            eventDate = eventDate,
                            eventType = selectedEventType
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = createState !is SchoolEventUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (createState is SchoolEventUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "Crear Evento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mostrar errores
            if (createState is SchoolEventUiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ ",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = (createState as SchoolEventUiState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // DatePicker con Material3
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = eventDate
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            eventDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Seleccionar fecha",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }
}