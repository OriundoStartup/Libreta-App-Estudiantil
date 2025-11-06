package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.ui.theme.AppAvatar
import com.oriundo.lbretaappestudiantil.ui.theme.AppShapes
import com.oriundo.lbretaappestudiantil.ui.theme.AvatarType
import com.oriundo.lbretaappestudiantil.ui.theme.states.MessageUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MessageViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.StudentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMessageScreen(
    teacherId: Int,
    navController: NavController,
    studentViewModel: StudentViewModel = hiltViewModel(),
    messageViewModel: MessageViewModel = hiltViewModel()
) {
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showStudentSelector by remember { mutableStateOf(false) }
    var selectedStudents by remember { mutableStateOf<List<StudentWithClass>>(emptyList()) }
    var isSending by remember { mutableStateOf(false) }

    val allStudents by studentViewModel.allStudents.collectAsState()

    // 🎯 INSERCIÓN 1: Observación de estado y Coroutine Scope
    val sendState by messageViewModel.sendState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // ----------------------------------------------------

    LaunchedEffect(teacherId) {
        studentViewModel.loadAllStudents()
    }

    // 🎯 INSERCIÓN 2: LaunchedEffect para manejar el resultado del envío
    LaunchedEffect(sendState) {
        when (val state = sendState) {
            is MessageUiState.Success -> {
                // ✅ CORRECCIÓN: Llamar a showSnackbar directamente.
                // Esto pausará el LaunchedEffect hasta que el Snackbar se muestre.
                snackbarHostState.showSnackbar(
                    message = "Mensaje(s) enviado(s) exitosamente.",
                    duration = SnackbarDuration.Short // Se mostrará por un corto tiempo
                )

                isSending = false
                messageViewModel.resetSendState()
                // La navegación ocurre AHORA, después de que el Snackbar apareció.
                navController.navigateUp()
            }
            is MessageUiState.Error -> {
                // Aquí usamos coroutineScope.launch para mostrar el error, ya que NO navegamos.
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error al enviar: ${state.message}")
                }
                isSending = false
                messageViewModel.resetSendState()
            }
            else -> {}
        }
    }
    // ----------------------------------------------------

    Scaffold(
        // 🎯 INSERCIÓN 3: Añadir el SnackbarHost
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // ----------------------------------------------------
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuevo Mensaje",
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
                actions = {
                    IconButton(
                        onClick = {
                            if (subject.isNotBlank() &&
                                content.isNotBlank() &&
                                selectedStudents.isNotEmpty() &&
                                !isSending) {
                                // Enviar mensaje a cada padre
                                selectedStudents.forEach { studentWithClass ->
                                    messageViewModel.sendMessageToParent(
                                        teacherId = teacherId,
                                        parentId = studentWithClass.primaryParentId ?: 0,
                                        studentId = studentWithClass.student.id, // Se necesita el studentId para contexto
                                        subject = subject,
                                        content = content
                                    )
                                }
                                // ❌ IMPORTANTE: Eliminamos navController.navigateUp() de aquí.
                                // La navegación se gestiona ahora en LaunchedEffect.
                            }
                        },
                        enabled = subject.isNotBlank() &&
                                content.isNotBlank() &&
                                selectedStudents.isNotEmpty() &&
                                !isSending
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (subject.isNotBlank() && content.isNotBlank() && selectedStudents.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
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

            // Selector de destinatarios
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Destinatarios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStudentSelector = true },
                    shape = AppShapes.small,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedStudents.isEmpty())
                                    "Seleccionar padres"
                                else
                                    "${selectedStudents.size} padre(s) seleccionado(s)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedStudents.isEmpty())
                                    FontWeight.Normal
                                else
                                    FontWeight.Medium
                            )
                            if (selectedStudents.isNotEmpty()) {
                                Text(
                                    text = selectedStudents.joinToString(", ") {
                                        it.student.firstName
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Asunto
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Asunto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Reunión de apoderados") },
                    singleLine = true,
                    shape = AppShapes.small
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Mensaje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    placeholder = { Text("Escribe tu mensaje aquí...") },
                    shape = AppShapes.small,
                    maxLines = 10
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Modal de selección de estudiantes
        if (showStudentSelector) {
            StudentSelectorDialog(
                students = allStudents,
                selectedStudents = selectedStudents,
                onStudentsSelected = { selected ->
                    // ✅ CORRECCIÓN 1: Actualiza la lista Y luego CIERRA el diálogo.
                    selectedStudents = selected
                    showStudentSelector = false
                },
                onDismiss = {
                    // ✅ CORRECCIÓN 2: Cierra el diálogo al presionar 'Cancelar' o fuera.
                    showStudentSelector = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentSelectorDialog(
    students: List<StudentWithClass>,
    selectedStudents: List<StudentWithClass>,
    onStudentsSelected: (List<StudentWithClass>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedStudents by remember { mutableStateOf(selectedStudents) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = remember(searchQuery, students) {
        if (searchQuery.isBlank()) {
            students
        } else {
            students.filter {
                it.student.fullName.contains(searchQuery, ignoreCase = true) ||
                        it.classEntity.className.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Seleccionar Padres",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${tempSelectedStudents.size} seleccionado(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar estudiante...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, null)
                    },
                    singleLine = true,
                    shape = AppShapes.small
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de estudiantes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredStudents) { studentWithClass ->
                        val isSelected = tempSelectedStudents.any {
                            it.student.id == studentWithClass.student.id
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelectedStudents = if (isSelected) {
                                        tempSelectedStudents.filter {
                                            it.student.id != studentWithClass.student.id
                                        }
                                    } else {
                                        tempSelectedStudents + studentWithClass
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = AppShapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // ✅ NORMALIZADO - Usa AppAvatar
                                AppAvatar(
                                    type = AvatarType.STUDENT,
                                    size = 40.dp,
                                    iconSize = 20.dp
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = studentWithClass.student.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = studentWithClass.classEntity.className,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onStudentsSelected(tempSelectedStudents) }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = AppShapes.large
    )
}
