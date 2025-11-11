package com.oriundo.lbretaappestudiantil.ui.theme.parent


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.EventType
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.EventTypeInfo
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEventsScreen(
    studentId: Int,
    classId: Int,
    navController: NavController,
    viewModel: SchoolEventViewModel = hiltViewModel()
) {
    // ✅ Conectar con el ViewModel existente
    val eventsByClass by viewModel.eventsByClass.collectAsState()

    // ✅ Cargar eventos del curso
    LaunchedEffect(classId) {
        viewModel.loadEventsByClass(classId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Eventos Escolares",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (eventsByClass.isEmpty()) {
            // ✅ Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay eventos programados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cuando el profesor cree eventos, aparecerán aquí.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // ✅ Lista de eventos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${eventsByClass.size} evento${if (eventsByClass.size != 1) "s" else ""} próximo${if (eventsByClass.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(eventsByClass.sortedBy { it.eventDate }) { event ->
                    EventCard(event = event)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun EventCard(event: SchoolEventEntity) {
    val eventTypeInfo = getEventTypeInfo(event.eventType)
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "CL"))
    val formattedDate = dateFormat.format(Date(event.eventDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con tipo de evento
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            eventTypeInfo.color.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = eventTypeInfo.icon,
                        contentDescription = null,
                        tint = eventTypeInfo.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = eventTypeInfo.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = eventTypeInfo.color,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Título del evento
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Descripción
            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// ============================================================================
// FUNCIÓN PARA OBTENER INFORMACIÓN DE CADA TIPO DE EVENTO
// ============================================================================

@Composable
fun getEventTypeInfo(eventType: EventType): EventTypeInfo {
    return when (eventType) {
        EventType.GENERAL -> EventTypeInfo(
            label = "Evento General",
            icon = Icons.Filled.Event,
            color = MaterialTheme.colorScheme.primary
        )

        EventType.TEST -> EventTypeInfo(
            label = "Prueba",
            icon = Icons.Filled.Quiz,
            color = Color(0xFFD32F2F) // Rojo
        )

        EventType.ASSIGNMENT -> EventTypeInfo(
            label = "Tarea",
            icon = Icons.AutoMirrored.Filled.Assignment,
            color = Color(0xFFF57C00) // Naranja
        )

        EventType.PROJECT -> EventTypeInfo(
            label = "Proyecto",
            icon = Icons.Filled.AccountTree,
            color = Color(0xFF1976D2) // Azul
        )

        EventType.FIELD_TRIP -> EventTypeInfo(
            label = "Salida Pedagógica",
            icon = Icons.Filled.DirectionsBus,
            color = Color(0xFF388E3C) // Verde
        )

        EventType.MEETING -> EventTypeInfo(
            label = "Reunión",
            icon = Icons.Filled.Groups,
            color = Color(0xFF5E35B1) // Púrpura
        )

        EventType.HOLIDAY -> EventTypeInfo(
            label = "Feriado",
            icon = Icons.Filled.BeachAccess,
            color = Color(0xFF00897B) // Teal
        )

        EventType.OTHER -> EventTypeInfo(
            label = "Otro",
            icon = Icons.Filled.MoreHoriz,
            color = Color(0xFF757575) // Gris
        )
    }
}