package com.oriundo.lbretaappestudiantil.ui.theme.teacher


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MessageViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.StudentViewModel

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

    LaunchedEffect(teacherId) {
        studentViewModel.loadAllStudents()
    }

    Scaffold(
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
                                isSending = true
                                // Enviar mensaje a cada padre
                                selectedStudents.forEach { studentWithClass ->
                                    messageViewModel.sendMessageToParent(
                                        teacherId = teacherId,
                                        parentId = studentWithClass.primaryParentId ?: 0,
                                        subject = subject,
                                        content = content
                                    )
                                }
                                navController.navigateUp()
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
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp)
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
                    shape = RoundedCornerShape(12.dp),
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
                    selectedStudents = selected
                    showStudentSelector = false
                },
                onDismiss = { showStudentSelector = false }
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
                    shape = RoundedCornerShape(12.dp)
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFEC4899),
                                                    Color(0xFFF59E0B)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ChildCare,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

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
        shape = RoundedCornerShape(24.dp)
    )
}