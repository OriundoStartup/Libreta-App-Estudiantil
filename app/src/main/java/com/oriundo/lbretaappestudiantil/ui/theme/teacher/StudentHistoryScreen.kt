package com.oriundo.lbretaappestudiantil.ui.theme.teacher

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.ui.theme.AppShapes
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AnnotationViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AttendanceViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.StudentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHistoryScreen(
    studentId: Int,
    classId: Int,
    navController: NavController,
    studentViewModel: StudentViewModel = hiltViewModel(),
    annotationViewModel: AnnotationViewModel = hiltViewModel(),
    attendanceViewModel: AttendanceViewModel = hiltViewModel(),
    schoolEventViewModel: SchoolEventViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val student by studentViewModel.selectedStudent.collectAsState()
    val annotations by annotationViewModel.annotationsByStudent.collectAsState()
    val attendanceStats by attendanceViewModel.attendanceStats.collectAsState()
    val attendanceRecords by attendanceViewModel.attendanceByStudent.collectAsState()
    val schoolEvents by schoolEventViewModel.eventsByClass.collectAsState()

    // Cargar datos
    LaunchedEffect(studentId, classId) {
        studentViewModel.loadStudentById(studentId)
        annotationViewModel.loadAnnotationsByStudent(studentId)
        attendanceViewModel.loadAttendanceByStudent(studentId)
        schoolEventViewModel.loadEventsByClass(classId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Historial",
                            fontWeight = FontWeight.Bold
                        )
                        student?.let {
                            Text(
                                text = it.fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
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
        ) {
            // Tabs de navegación
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 24.dp),
                containerColor = Color.Transparent,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.clip(AppShapes.small)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Anotaciones",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.clip(AppShapes.small)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Asistencia",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.clip(AppShapes.small)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Event,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Eventos",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido según tab seleccionado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                when (selectedTab) {
                    0 -> AnnotationsHistoryContent(annotations)
                    1 -> AttendanceHistoryContent(attendanceStats, attendanceRecords)
                    2 -> EventsHistoryContent(schoolEvents)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// ============================================================================
// CONTENIDO: ANOTACIONES
// ============================================================================

@Composable
private fun AnnotationsHistoryContent(
    annotations: List<com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity>
) {
    if (annotations.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Filled.Description,
            title = "Sin anotaciones",
            description = "Este estudiante no tiene anotaciones registradas"
        )
    } else {
        // Estadísticas generales
        val positive = annotations.count { it.type == AnnotationType.POSITIVE }
        val negative = annotations.count { it.type == AnnotationType.NEGATIVE }
        val neutral = annotations.count { it.type == AnnotationType.NEUTRAL }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Resumen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Positivas",
                        value = positive.toString(),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    StatItem(
                        label = "Negativas",
                        value = negative.toString(),
                        color = MaterialTheme.colorScheme.error
                    )
                    StatItem(
                        label = "Neutras",
                        value = neutral.toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Historial (${annotations.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        annotations.forEach { annotation ->
            AnnotationHistoryCard(annotation)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AnnotationHistoryCard(
    annotation: com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
) {
    val (color, icon) = when (annotation.type) {
        AnnotationType.POSITIVE -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Filled.Star
        )
        AnnotationType.NEGATIVE -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Filled.Warning
        )
        AnnotationType.NEUTRAL -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            Icons.Filled.Info
        )
        AnnotationType.GENERAL -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Filled.Description
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(AppShapes.small)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when (annotation.type) {
                        AnnotationType.POSITIVE -> MaterialTheme.colorScheme.tertiary
                        AnnotationType.NEGATIVE -> MaterialTheme.colorScheme.error
                        AnnotationType.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                        AnnotationType.GENERAL -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = annotation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = annotation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(annotation.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// CONTENIDO: ASISTENCIA
// ============================================================================

@Composable
private fun AttendanceHistoryContent(
    stats: com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AttendanceStats,
    records: List<com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity>
) {
    // Tarjeta de estadísticas
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Estadísticas de Asistencia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Porcentaje de asistencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stats.attendancePercentage}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total: ${stats.totalDays} días",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Presente: ${stats.presentDays}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Ausente: ${stats.absentDays}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ CORREGIDO - progress sin lambda
            LinearProgressIndicator(
                progress = { stats.attendancePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(AppShapes.small),
                color = when {
                    stats.attendancePercentage >= 85 -> MaterialTheme.colorScheme.tertiary
                    stats.attendancePercentage >= 70 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (records.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Filled.CheckCircle,
            title = "Sin registros",
            description = "No hay registros de asistencia para este estudiante"
        )
    } else {
        Text(
            text = "Historial de Asistencia (${records.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ CORREGIDO - usa attendanceDate
        records.sortedByDescending { it.attendanceDate }.forEach { record ->
            AttendanceRecordCard(record)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AttendanceRecordCard(
    record: com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
) {
    // ✅ CORREGIDO - usa status en lugar de isPresent
    record.status == AttendanceStatus.PRESENT
    val statusText = when (record.status) {
        AttendanceStatus.PRESENT -> "Presente"
        AttendanceStatus.ABSENT -> "Ausente"
        AttendanceStatus.LATE -> "Atrasado"
        AttendanceStatus.JUSTIFIED -> "Justificado"
    }

    val statusColor = when (record.status) {
        AttendanceStatus.PRESENT -> MaterialTheme.colorScheme.tertiary
        AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
        AttendanceStatus.LATE -> MaterialTheme.colorScheme.secondary
        AttendanceStatus.JUSTIFIED -> MaterialTheme.colorScheme.primary
    }

    val statusIcon = when (record.status) {
        AttendanceStatus.PRESENT -> Icons.Filled.CheckCircle
        AttendanceStatus.ABSENT -> Icons.Filled.Warning
        AttendanceStatus.LATE -> Icons.Filled.Schedule
        AttendanceStatus.JUSTIFIED -> Icons.Filled.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.small,
        colors = CardDefaults.cardColors(
            containerColor = when (record.status) {
                AttendanceStatus.PRESENT -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                AttendanceStatus.LATE -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                AttendanceStatus.JUSTIFIED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    // ✅ CORREGIDO - usa attendanceDate
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(record.attendanceDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Mostrar nota si existe
                    record.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// CONTENIDO: EVENTOS
// ============================================================================

@Composable
private fun EventsHistoryContent(
    events: List<com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity>
) {
    if (events.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Filled.Event,
            title = "Sin eventos",
            description = "No hay eventos escolares registrados para esta clase"
        )
    } else {
        Text(
            text = "Eventos de la Clase (${events.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        events.sortedByDescending { it.eventDate }.forEach { event ->
            EventHistoryCard(event)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EventHistoryCard(
    event: com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(AppShapes.small)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(Date(event.eventDate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = AppShapes.extraSmall
            ) {
                Text(
                    text = event.eventType.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================================
// COMPONENTES AUXILIARES
// ============================================================================

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
