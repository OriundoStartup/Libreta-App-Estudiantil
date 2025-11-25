package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.EventType
import com.oriundo.lbretaappestudiantil.ui.theme.AppColors
import com.oriundo.lbretaappestudiantil.ui.theme.states.SchoolEventUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassViewModel
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

    // Control de diálogos y menús
    var showDatePicker by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showEventTypeDropdown by remember { mutableStateOf(false) }

    // Observar estados
    val createState by eventViewModel.createState.collectAsState()
    val teacherClasses by classViewModel.teacherClasses.collectAsState()

    // Cargar datos
    LaunchedEffect(teacherId) {
        classViewModel.loadTeacherClasses(teacherId)
    }

    // Manejar éxito y navegación
    LaunchedEffect(createState) {
        if (createState is SchoolEventUiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Nuevo Evento",
                        style = MaterialTheme.typography.headlineSmall // [cite: 4]
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp) // Márgenes laterales consistentes
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // SECCIÓN 1: DATOS PRINCIPALES
            Text(
                text = "Información General",
                style = MaterialTheme.typography.titleMedium, // [cite: 5]
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = MaterialTheme.shapes.medium, //  (16.dp)
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Input: Título
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título del evento") },
                        placeholder = { Text("Ej: Reunión de apoderados") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small, //  (12.dp)
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Input: Descripción
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        shape = MaterialTheme.shapes.small, // 
                    )
                }
            }

            // SECCIÓN 2: CONFIGURACIÓN
            Text(
                text = "Detalles",
                style = MaterialTheme.typography.titleMedium, // [cite: 5]
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = MaterialTheme.shapes.medium, // 
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Selector de Clase (Dropdown Estilizado)
                    ExposedDropdownMenuBox(
                        expanded = showClassDropdown,
                        onExpandedChange = { showClassDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = teacherClasses.find { it.id == selectedClassId }?.className
                                ?: "Evento General (Sin clase)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Asignar a Clase") },
                            trailingIcon = {
                                Icon(
                                    if (showClassDropdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = MaterialTheme.shapes.small, // 
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showClassDropdown,
                            onDismissRequest = { showClassDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ninguna (Evento General)") },
                                onClick = { selectedClassId = null; showClassDropdown = false }
                            )
                            teacherClasses.forEach { classEntity ->
                                DropdownMenuItem(
                                    text = { Text(classEntity.className) },
                                    onClick = { selectedClassId = classEntity.id; showClassDropdown = false }
                                )
                            }
                        }
                    }

                    // 2. Selector de Tipo
                    ExposedDropdownMenuBox(
                        expanded = showEventTypeDropdown,
                        onExpandedChange = { showEventTypeDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedEventType.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Evento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEventTypeDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = MaterialTheme.shapes.small
                        )
                        ExposedDropdownMenu(
                            expanded = showEventTypeDropdown,
                            onDismissRequest = { showEventTypeDropdown = false }
                        ) {
                            EventType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = { selectedEventType = type; showEventTypeDropdown = false }
                                )
                            }
                        }
                    }

                    // 3. Selector de Fecha
                    OutlinedTextField(
                        value = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
                            .format(Date(eventDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, "Cambiar fecha", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTÓN DE CREAR (Con Gradiente de la App) 
            Button(
                onClick = {
                    eventViewModel.createEvent(
                        selectedClassId,
                        teacherId,
                        title,
                        description,
                        eventDate,
                        selectedEventType
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium, // 
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent // Transparente para que se vea el gradiente
                ),
                contentPadding = PaddingValues(), // Sin padding para que el Box llene todo
                enabled = createState !is SchoolEventUiState.Loading
            ) {
                // Box con el gradiente definido en AppColors
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.PrimaryGradient), // 
                    contentAlignment = Alignment.Center
                ) {
                    if (createState is SchoolEventUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "Guardar Evento",
                            style = MaterialTheme.typography.titleMedium, // [cite: 5]
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Manejo de Errores
            if (createState is SchoolEventUiState.Error) {
                Text(
                    text = (createState as SchoolEventUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // DatePicker con la corrección del bug
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = eventDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false }, // Corrección: Cerrar
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { eventDate = it }
                    showDatePicker = false // Corrección: Cerrar al confirmar
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } // Corrección: Cerrar al cancelar
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
