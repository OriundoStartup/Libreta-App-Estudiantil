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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.data.local.models.RequestStatus
import com.oriundo.lbretaappestudiantil.data.local.models.UrgencyLevel
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.ui.theme.Screen
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AnnotationViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AttendanceViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MaterialRequestViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.StudentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    userWithProfile: UserWithProfile,
    onLogout: () -> Unit,
    navController: NavController,
    studentViewModel: StudentViewModel = hiltViewModel(),
    annotationViewModel: AnnotationViewModel = hiltViewModel(),
    attendanceViewModel: AttendanceViewModel = hiltViewModel(),
    schoolEventViewModel: SchoolEventViewModel = hiltViewModel(),
    materialRequestViewModel: MaterialRequestViewModel = hiltViewModel()
) {
    var showMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedStudentIndex by remember { mutableIntStateOf(0) }

    // Estados de datos reales
    val studentsByParent by studentViewModel.studentsByParent.collectAsState()
    val unreadAnnotations by annotationViewModel.unreadAnnotations.collectAsState()
    val attendanceStats by attendanceViewModel.attendanceStats.collectAsState()
    val eventsByClass by schoolEventViewModel.eventsByClass.collectAsState()
    val materialRequests by materialRequestViewModel.requestsByStudent.collectAsState()
    val annotationsByStudent by annotationViewModel.annotationsByStudent.collectAsState()

    // Cargar datos del padre
    LaunchedEffect(userWithProfile.profile.id) {
        studentViewModel.loadStudentsByParent(userWithProfile.profile.id)
        annotationViewModel.loadUnreadAnnotationsForParent(userWithProfile.profile.id)
    }

    // Cargar datos del estudiante seleccionado
    LaunchedEffect(selectedStudentIndex, studentsByParent) {
        if (studentsByParent.isNotEmpty() && selectedStudentIndex < studentsByParent.size) {
            val selectedStudent = studentsByParent[selectedStudentIndex]
            attendanceViewModel.loadAttendanceByStudent(selectedStudent.student.id)
            schoolEventViewModel.loadEventsByClass(selectedStudent.classEntity.id)
            materialRequestViewModel.loadRequestsByStudent(selectedStudent.student.id)
            annotationViewModel.loadAnnotationsByStudent(selectedStudent.student.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hola, ${userWithProfile.profile.firstName} 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apoderado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Notificaciones con contador real
                    IconButton(
                        onClick = {
                            navController.navigate(
                                Screen.Notifications.createRoute(parentId = userWithProfile.profile.id)
                            )
                        }
                    ) {
                        if (unreadAnnotations.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text("${unreadAnnotations.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notificaciones"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notificaciones"
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Más opciones"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mi Perfil") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.ParentProfile.route)
                                },
                                leadingIcon = { Icon(Icons.Filled.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.ParentSettings.route)
                                },
                                leadingIcon = { Icon(Icons.Filled.Settings, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (studentsByParent.isEmpty()) {
            // Estado vacío
            EmptyStudentsView(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de estudiante si hay más de uno
                if (studentsByParent.size > 1) {
                    StudentSelector(
                        students = studentsByParent,
                        selectedIndex = selectedStudentIndex,
                        onStudentSelected = { selectedStudentIndex = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "Resumen",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Anotaciones",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                            if (unreadAnnotations.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text("${unreadAnnotations.size}")
                                }
                            }
                        }
                    }
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "Materiales",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contenido según tab seleccionado
                val currentStudent = if (studentsByParent.isNotEmpty() && selectedStudentIndex < studentsByParent.size) {
                    studentsByParent[selectedStudentIndex]
                } else null

                currentStudent?.let { student ->
                    when (selectedTab) {
                        0 -> ResumenContent(
                            student = student,
                            attendanceStats = attendanceStats,
                            upcomingEvents = eventsByClass,
                            pendingAnnotations = unreadAnnotations.size
                        )
                        1 -> AnotacionesContent(
                            annotations = annotationsByStudent
                        )
                        2 -> MaterialesContent(
                            requests = materialRequests
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun EmptyStudentsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChildCare,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No hay estudiantes vinculados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Contacta a la escuela para vincular a tu hijo/a",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun StudentSelector(
    students: List<StudentWithClass>,
    selectedIndex: Int,
    onStudentSelected: (Int) -> Unit
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.padding(horizontal = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        edgePadding = 0.dp
    ) {
        students.forEachIndexed { index, studentWithClass ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onStudentSelected(index) },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = studentWithClass.student.firstName,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = studentWithClass.classEntity.className,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ResumenContent(
    student: StudentWithClass,
    attendanceStats: com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AttendanceStats,
    upcomingEvents: List<com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity>,
    pendingAnnotations: Int
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Información del Estudiante",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta del estudiante con datos reales
        StudentCard(
            studentName = student.student.fullName,
            className = student.classEntity.className,
            schoolName = student.classEntity.schoolName,
            attendance = attendanceStats.attendancePercentage,
            pendingAnnotations = pendingAnnotations
        )

        if (upcomingEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Próximos Eventos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            upcomingEvents.take(5).forEach { event ->
                EventCard(event = event)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun AnotacionesContent(
    annotations: List<com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity>
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = if (annotations.isEmpty()) "Sin Anotaciones" else "Últimas Anotaciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (annotations.isEmpty()) {
            EmptyAnnotationsView()
        } else {
            annotations.forEach { annotation ->
                AnnotationCard(annotation = annotation)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun EmptyAnnotationsView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay anotaciones aún",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun MaterialesContent(
    requests: List<com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity>
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = if (requests.isEmpty()) "Sin Solicitudes" else "Solicitudes de Materiales",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (requests.isEmpty()) {
            EmptyMaterialsView()
        } else {
            requests.forEach { request ->
                MaterialRequestCard(request = request)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun EmptyMaterialsView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Inventory,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay solicitudes de materiales",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// Componentes de UI

@Composable
fun StudentCard(
    studentName: String,
    className: String,
    schoolName: String,
    attendance: Int,
    pendingAnnotations: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
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
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = className,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = schoolName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip(
                    text = "Asistencia $attendance%",
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    textColor = MaterialTheme.colorScheme.tertiary
                )
                if (pendingAnnotations > 0) {
                    Chip(
                        text = "$pendingAnnotations nuevas",
                        color = MaterialTheme.colorScheme.errorContainer,
                        textColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun Chip(
    text: String,
    color: Color,
    textColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AnnotationCard(
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
        shape = RoundedCornerShape(16.dp),
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
                    .clip(RoundedCornerShape(12.dp))
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = annotation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!annotation.isRead) FontWeight.Bold else FontWeight.Normal
                    )
                    if (!annotation.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Text(
                    text = annotation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(Date(annotation.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(Date(event.eventDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = event.eventType.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MaterialRequestCard(
    request: com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
) {
    val urgencyColor = when (request.urgency) {
        UrgencyLevel.HIGH -> MaterialTheme.colorScheme.error
        UrgencyLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
        UrgencyLevel.LOW -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.material,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Cantidad: ${request.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    request.deadlineDate?.let {
                        Text(
                            text = "Para: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip(
                    text = "Urgencia: ${request.urgency.name}",
                    color = urgencyColor.copy(alpha = 0.2f),
                    textColor = urgencyColor
                )
                Chip(
                    text = request.status.name,
                    color = if (request.status == RequestStatus.CONFIRMED)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    textColor = if (request.status == RequestStatus.CONFIRMED)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}