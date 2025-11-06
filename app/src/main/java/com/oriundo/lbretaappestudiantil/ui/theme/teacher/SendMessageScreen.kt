package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.ui.theme.AppColors
import com.oriundo.lbretaappestudiantil.ui.theme.states.MessageUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MessageViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.StudentViewModel
import kotlinx.coroutines.delay
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
    var showSuccessDialog by remember { mutableStateOf(false) }

    val sendState by messageViewModel.sendState.collectAsStateWithLifecycle()
    val allStudents by studentViewModel.allStudents.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Cargar estudiantes al iniciar
    LaunchedEffect(teacherId) {
        studentViewModel.loadAllStudents()
    }

    // Manejar estados de envío
    LaunchedEffect(sendState) {
        when (sendState) {
            is MessageUiState.Success -> {
                showSuccessDialog = true
            }
            is MessageUiState.Error -> {
                // El error se muestra en la UI, no es necesario hacer nada aquí
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nuevo Mensaje",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Contacta a los apoderados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de destinatarios
                item {
                    RecipientSelectorCard(
                        selectedStudents = selectedStudents,
                        onClick = { showStudentSelector = true }
                    )
                }

                // Campo de asunto
                item {
                    SubjectField(
                        value = subject,
                        onValueChange = { subject = it },
                        enabled = selectedStudents.isNotEmpty()
                    )
                }

                // Campo de mensaje
                item {
                    MessageContentField(
                        value = content,
                        onValueChange = { content = it },
                        enabled = selectedStudents.isNotEmpty()
                    )
                }

                // Botón de envío
                item {
                    SendMessageButton(
                        enabled = selectedStudents.isNotEmpty() &&
                                subject.isNotBlank() &&
                                content.isNotBlank(),
                        isLoading = sendState is MessageUiState.Loading,
                        onClick = {
                            focusManager.clearFocus()
                            selectedStudents.forEach { studentWithClass ->
                                studentWithClass.primaryParentId?.let { parentId ->
                                    messageViewModel.sendMessageToParent(
                                        teacherId = teacherId,
                                        parentId = parentId,
                                        studentId = studentWithClass.student.id,
                                        subject = subject,
                                        content = content
                                    )
                                }
                            }
                        }
                    )
                }

                // Mensaje de error si existe
                if (sendState is MessageUiState.Error) {
                    item {
                        ErrorMessageCard(
                            message = (sendState as MessageUiState.Error).message
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Sheet para seleccionar destinatarios
            if (showStudentSelector) {
                ParentSelectorBottomSheet(
                    students = allStudents,
                    selectedStudents = selectedStudents,
                    onDismiss = { showStudentSelector = false },
                    onStudentsSelected = { selected ->
                        selectedStudents = selected
                        showStudentSelector = false
                    }
                )
            }

            // Diálogo de éxito
            if (showSuccessDialog) {
                SuccessDialog(
                    onDismiss = {
                        showSuccessDialog = false
                        messageViewModel.resetSendState()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun RecipientSelectorCard(
    selectedStudents: List<StudentWithClass>,
    onClick: () -> Unit
) {
    val count = selectedStudents.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con contador
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (count > 0) AppColors.ParentAvatarGradient
                        else Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información de selección
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (count > 0) "Apoderados seleccionados" else "Seleccionar apoderados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (count) {
                        0 -> "Toca para seleccionar destinatarios"
                        1 -> selectedStudents.first().student.fullName
                        else -> "${selectedStudents.first().student.fullName} y ${count - 1} más"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (count > 0) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubjectField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Asunto") },
        placeholder = { Text("Ej: Reunión de apoderados") },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Subject,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun MessageContentField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Mensaje") },
        placeholder = { Text("Escribe tu mensaje aquí...") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp),
        enabled = enabled,
        maxLines = 8,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun SendMessageButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Enviar Mensaje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentSelectorBottomSheet(
    students: List<StudentWithClass>,
    selectedStudents: List<StudentWithClass>,
    onDismiss: () -> Unit,
    onStudentsSelected: (List<StudentWithClass>) -> Unit
) {
    val tempSelectedStudents = remember { selectedStudents.toMutableStateList() }
    var searchQuery by remember { mutableStateOf("") }

    // Usar derivedStateOf para optimizar el filtrado
    val filteredStudents by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                students
            } else {
                students.filter {
                    it.student.fullName.contains(searchQuery, ignoreCase = true) ||
                            it.classEntity.className.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Header con título y acciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Seleccionar Apoderados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tempSelectedStudents.size} seleccionado(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    if (tempSelectedStudents.isNotEmpty()) {
                        TextButton(onClick = { tempSelectedStudents.clear() }) {
                            Text("Limpiar")
                        }
                    }
                    TextButton(
                        onClick = {
                            onStudentsSelected(tempSelectedStudents.toList())
                        },
                        enabled = tempSelectedStudents.isNotEmpty()
                    ) {
                        Text("Aceptar")
                    }
                }
            }

            // Campo de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Buscar estudiante...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar"
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de estudiantes o estado vacío
            if (filteredStudents.isEmpty()) {
                EmptyStateMessage(
                    message = if (searchQuery.isBlank())
                        "No tienes estudiantes asignados en este momento."
                    else
                        "No se encontraron estudiantes para \"$searchQuery\".",
                    icon = Icons.Default.School
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(
                        items = filteredStudents,
                        key = { it.student.id }
                    ) { studentWithClass ->
                        val isSelected = tempSelectedStudents.any {
                            it.student.id == studentWithClass.student.id
                        }

                        ParentRecipientListItem(
                            studentWithClass = studentWithClass,
                            isSelected = isSelected,
                            onClick = {
                                val hasParent = studentWithClass.primaryParentId != null &&
                                        studentWithClass.primaryParentId != 0

                                if (!hasParent) return@ParentRecipientListItem

                                if (isSelected) {
                                    tempSelectedStudents.removeAll {
                                        it.student.id == studentWithClass.student.id
                                    }
                                } else {
                                    tempSelectedStudents.add(studentWithClass)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentRecipientListItem(
    studentWithClass: StudentWithClass,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hasParent = studentWithClass.primaryParentId != null &&
            studentWithClass.primaryParentId != 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasParent, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                hasParent -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del estudiante
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.StudentAvatarGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentWithClass.student.firstName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del estudiante
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentWithClass.student.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    Text(
                        text = studentWithClass.classEntity.className,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!hasParent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Sin Apoderado)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Indicador de selección
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (hasParent) 1f else 0.3f
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    message: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessDialog(onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppColors.SuccessGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "¡Mensajes Enviados!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Tus mensajes han sido enviados exitosamente. Los apoderados los recibirán pronto.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        delay(200L)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Entendido")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}