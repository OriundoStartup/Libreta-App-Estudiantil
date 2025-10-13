package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.EventType
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventUiState
import java.text.SimpleDateFormat
import java.util.*

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
    var eventDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showEventTypeDropdown by remember { mutableStateOf(false) }

    // Observar estados - USAR teacherClasses
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
                title = { Text("Crear Evento Escolar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
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
            // Campo: Título
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título del evento") },
                placeholder = { Text("Ej: Reunión de apoderados") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = title.isBlank() && createState is SchoolEventUiState.Error
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
                isError = description.isBlank() && createState is SchoolEventUiState.Error
            )

            // Selector de Clase
            ExposedDropdownMenuBox(
                expanded = showClassDropdown,
                onExpandedChange = { showClassDropdown = it }
            ) {
                OutlinedTextField(
                    value = teacherClasses.find { it.id == selectedClassId }?.className ?: "Seleccionar clase",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Clase (opcional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClassDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
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
                        EventType.TEST -> "Prueba"
                        EventType.ASSIGNMENT -> "Tarea"
                        EventType.PROJECT -> "Proyecto"
                        EventType.FIELD_TRIP -> "Salida Pedagógica"
                        EventType.MEETING -> "Reunión"
                        EventType.HOLIDAY -> "Festivo"
                        EventType.OTHER -> "Otro"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de evento") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEventTypeDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = showEventTypeDropdown,
                    onDismissRequest = { showEventTypeDropdown = false }
                ) {
                    listOf(
                        EventType.TEST to "Prueba",
                        EventType.ASSIGNMENT to "Tarea",
                        EventType.PROJECT to "Proyecto",
                        EventType.FIELD_TRIP to "Salida Pedagógica",
                        EventType.MEETING to "Reunión",
                        EventType.HOLIDAY to "Festivo",
                        EventType.OTHER to "Otro"
                    ).forEach { (type, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedEventType = type
                                showEventTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Selector de Fecha
            OutlinedTextField(
                value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(eventDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha del evento") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de crear
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
                modifier = Modifier.fillMaxWidth(),
                enabled = createState !is SchoolEventUiState.Loading
            ) {
                if (createState is SchoolEventUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Evento")
                }
            }

            // Mostrar errores
            if (createState is SchoolEventUiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (createState as SchoolEventUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { selectedDate ->
                eventDate = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialDate = eventDate
        )
    }
}

@Composable
fun SimpleDatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    initialDate: Long
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = initialDate

    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar fecha") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Año:")
                    Row {
                        IconButton(onClick = { selectedYear-- }) { Text("-") }
                        Text(
                            text = selectedYear.toString(),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { selectedYear++ }) { Text("+") }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mes:")
                    Row {
                        IconButton(onClick = {
                            selectedMonth = if (selectedMonth == 0) 11 else selectedMonth - 1
                        }) { Text("-") }
                        Text(
                            text = when (selectedMonth) {
                                0 -> "Enero"
                                1 -> "Febrero"
                                2 -> "Marzo"
                                3 -> "Abril"
                                4 -> "Mayo"
                                5 -> "Junio"
                                6 -> "Julio"
                                7 -> "Agosto"
                                8 -> "Septiembre"
                                9 -> "Octubre"
                                10 -> "Noviembre"
                                11 -> "Diciembre"
                                else -> "Mes inválido"
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = {
                            selectedMonth = if (selectedMonth == 11) 0 else selectedMonth + 1
                        }) { Text("+") }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Día:")
                    Row {
                        IconButton(onClick = {
                            if (selectedDay > 1) selectedDay--
                        }) { Text("-") }
                        Text(
                            text = selectedDay.toString(),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = {
                            val maxDay = Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYear)
                                set(Calendar.MONTH, selectedMonth)
                            }.getActualMaximum(Calendar.DAY_OF_MONTH)
                            if (selectedDay < maxDay) selectedDay++
                        }) { Text("+") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newCalendar = Calendar.getInstance()
                newCalendar.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                newCalendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(newCalendar.timeInMillis)
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}