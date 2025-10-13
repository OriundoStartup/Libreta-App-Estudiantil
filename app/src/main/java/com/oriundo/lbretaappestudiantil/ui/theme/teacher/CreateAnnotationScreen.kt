package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AnnotationUiState
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AnnotationViewModel


@Composable
fun CreateAnnotationScreen(
    studentId: Int,
    classId: Int,
    teacherId: Int,
    onAnnotationCreated: () -> Unit,
    viewModel: AnnotationViewModel = viewModel()
) {
    var selectedType by remember { mutableStateOf(AnnotationType.POSITIVE) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val createState by viewModel.createState.collectAsState()

    LaunchedEffect(createState) {
        if (createState is AnnotationUiState.Success) {
            onAnnotationCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Nueva Anotación",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
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
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            maxLines = 8
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de crear
    }
}