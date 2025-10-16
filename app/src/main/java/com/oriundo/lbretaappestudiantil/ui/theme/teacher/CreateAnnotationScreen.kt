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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AnnotationUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AnnotationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAnnotationScreen(
    studentId: Int,
    classId: Int,
    teacherId: Int,
    navController: NavController,
    onAnnotationCreated: () -> Unit,
    viewModel: AnnotationViewModel = hiltViewModel()
) {
    var selectedType by remember { mutableStateOf(AnnotationType.POSITIVE) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val createState by viewModel.createState.collectAsState()

    LaunchedEffect(createState) {
        if (createState is AnnotationUiState.Success) {
            onAnnotationCreated()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Anotación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Registra una observación sobre el estudiante",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de tipo
            Text(
                text = "Tipo de Anotación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == AnnotationType.POSITIVE,
                    onClick = { selectedType = AnnotationType.POSITIVE },
                    label = { Text("Positiva") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null
                        )
                    }
                )
                FilterChip(
                    selected = selectedType == AnnotationType.NEGATIVE,
                    onClick = { selectedType = AnnotationType.NEGATIVE },
                    label = { Text("Negativa") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null
                        )
                    }
                )
                FilterChip(
                    selected = selectedType == AnnotationType.NEUTRAL,
                    onClick = { selectedType = AnnotationType.NEUTRAL },
                    label = { Text("Neutral") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de asunto
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Asunto") },
                placeholder = { Text("Ej: Excelente comportamiento en clase") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Subject,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                placeholder = { Text("Describe la situación...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de crear
            Button(
                onClick = {
                    viewModel.createAnnotation(
                        studentId = studentId,
                        teacherId = teacherId,
                        title = subject,
                        description = description,
                        type = selectedType,
                        classId = classId  // ← AGREGADO
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = subject.isNotBlank() &&
                        description.isNotBlank() &&
                        createState !is AnnotationUiState.Loading
            ) {
                if (createState is AnnotationUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = if (createState is AnnotationUiState.Loading)
                        "Creando..."
                    else
                        "Crear Anotación"
                )
            }

            // Mostrar error si existe
            (createState as? AnnotationUiState.Error)?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}