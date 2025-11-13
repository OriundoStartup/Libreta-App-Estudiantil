package com.oriundo.lbretaappestudiantil.ui.theme.teacher


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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.PendingJustificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPendingJustificationsScreen(
    teacherId: Int,
    navController: NavHostController,
    viewModel: PendingJustificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar datos al iniciar la pantalla
    LaunchedEffect(teacherId) {
        viewModel.loadPendingJustifications(teacherId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Justificaciones Pendientes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                // Manejo de error
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.justifications.isEmpty()) {
                // Estado vacío
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "🎉 No hay justificaciones pendientes para revisar.",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                // Mostrar la lista
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text(
                            text = "Tienes ${uiState.justifications.size} justificaciones esperando tu revisión.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(uiState.justifications) { justification ->
                        JustificationItem(
                            justification = justification,
                            onClick = {
                                // Navegación al detalle
                                navController.navigate(
                                    com.oriundo.lbretaappestudiantil.ui.theme.Screen.ReviewJustification.createRoute(
                                        justificationId = justification.id,
                                        teacherId = teacherId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JustificationItem(justification: AbsenceJustificationEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Asumo que tienes una forma de obtener el nombre del estudiante (aquí usamos el ID por ahora)
                Text(
                    text = "Estudiante ID: ${justification.studentId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Ícono de Pendiente
                Icon(
                    Icons.Filled.Info,
                    contentDescription = "Pendiente",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Fecha de ausencia
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            Text(
                text = "Fecha de ausencia: ${dateFormat.format(Date(justification.absenceDate))}",
                style = MaterialTheme.typography.bodyMedium
            )
            // Razón
            Text(
                text = "Razón: ${justification.reason.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}