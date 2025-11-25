package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.ui.theme.AppColors
import com.oriundo.lbretaappestudiantil.ui.theme.Screen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.components.ClassSelectorBottomSheet
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.components.StudentSelectorBottomSheet
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.MessageViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.StudentViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.TeacherDashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    userWithProfile: UserWithProfile,
    navController: NavHostController,
    onNavigateToCreateClass: () -> Unit,
    onNavigateToClassDetail: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    // ✅ Scope para cerrar los bottom sheets con animación
    val scope = rememberCoroutineScope()

    // ✅ Inicialización del TeacherDashboardViewModel
    val dashboardViewModel: TeacherDashboardViewModel = hiltViewModel()
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()

    // ViewModel para notificaciones (usando MessageViewModel)
    val messageViewModel: MessageViewModel = hiltViewModel()
    val unreadMessages by messageViewModel.unreadMessages.collectAsState()

    // ✅ Estados para controlar la visibilidad de los Bottom Sheets
    var showClassSelectorForAttendance by remember { mutableStateOf(false) }
    var showClassSelectorForAnnotation by remember { mutableStateOf(false) }
    var showClassSelectorForCalendar by remember { mutableStateOf(false) }
    var showStudentSelectorForAnnotation by remember { mutableStateOf(false) }
    var selectedClassForAction by remember { mutableStateOf<ClassEntity?>(null) }

    // ✅ Sheet states para las animaciones de los modales
    val attendanceSheetState = rememberModalBottomSheetState()
    val annotationClassSheetState = rememberModalBottomSheetState()
    val annotationStudentSheetState = rememberModalBottomSheetState()
    val calendarSheetState = rememberModalBottomSheetState()

    // ✅ ViewModel adicional para cargar estudiantes por curso
    val studentViewModel: StudentViewModel = hiltViewModel()
    val studentsByClass by studentViewModel.studentsByClass.collectAsState()
    // ✅ Instanciar ClassViewModel para la carga/sincronización
    val classViewModel: ClassViewModel = hiltViewModel()
    // 🔄 Cargar datos del dashboard y mensajes
    LaunchedEffect(userWithProfile.profile.id) {
        // La ID del perfil local es el teacherId
        val teacherId = userWithProfile.profile.id
        // Asumimos que el UID de Firebase está en el perfil
        val firebaseUid = userWithProfile.profile.firebaseUid

        // 1. ✅ LLAMADA CRÍTICA: Forzar la sincronización antes de cargar.
        // Esto resuelve el problema de inconsistencia de datos.
        classViewModel.syncAndLoadTeacherClasses(teacherId, firebaseUid)

        // 2. Mantener la carga de stats y mensajes
        dashboardViewModel.loadDashboard(teacherId)
        messageViewModel.loadUnreadMessagesForTeacher(teacherId)
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
                            text = "Profesor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Botón de notificaciones con badge conectado al MessageViewModel
                    IconButton(onClick = {
                        navController.navigate(
                            Screen.TeacherNotifications.createRoute(
                                userWithProfile.profile.id
                            )
                        )
                    }) {
                        Box {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notificaciones",
                                tint = if (unreadMessages.isNotEmpty())
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            if (unreadMessages.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = if (unreadMessages.size > 9) "9+" else unreadMessages.size.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
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
                                    navController.navigate(
                                        Screen.TeacherProfile.route
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Person, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(
                                        Screen.TeacherSettings.route
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Settings, null)
                                }
                            )
                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateClass,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
                },
                text = { Text("Crear Curso") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
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

            // Quick Stats - DINÁMICO con TeacherDashboardState
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Cursos",
                    value = dashboardState.classes.size.toString(),
                    icon = Icons.Filled.Class,
                    gradient = AppColors.PrimaryGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
                StatCard(
                    title = "Estudiantes",
                    value = dashboardState.totalStudents.toString(),
                    icon = Icons.Filled.People,
                    gradient = AppColors.SecondaryGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Anotaciones",
                    value = dashboardState.pendingAnnotations.toString(),
                    icon = Icons.AutoMirrored.Filled.Note,
                    gradient = AppColors.SuccessGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
                StatCard(
                    title = "Mensajes",
                    value = unreadMessages.size.toString(),
                    icon = Icons.AutoMirrored.Filled.Message,
                    gradient = AppColors.ErrorGradient,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(
                            Screen.TeacherMessages.createRoute(
                                userWithProfile.profile.id
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Actions - ACCIONES RÁPIDAS
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primera fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Asistencia",
                        icon = Icons.Filled.HowToReg,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            showClassSelectorForAttendance = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Anotación",
                        icon = Icons.Filled.Edit,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            showClassSelectorForAnnotation = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Segunda fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Justificaciones",
                        icon = Icons.AutoMirrored.Filled.FactCheck,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            navController.navigate(
                                Screen.TeacherPendingJustifications.createRoute(userWithProfile.profile.id)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Calendario",
                        icon = Icons.Filled.CalendarMonth,
                        color = MaterialTheme.colorScheme.error,
                        onClick = {
                            showClassSelectorForCalendar = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mis Cursos - DINÁMICO con TeacherDashboardState
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Cursos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (dashboardState.classes.isNotEmpty()) {
                        TextButton(onClick = { /* TODO: Ver todos */ }) {
                            Text("Ver todos")
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (dashboardState.classes.isEmpty()) {
                    // Estado vacío
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Class,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tienes cursos creados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Crea tu primer curso usando el botón de abajo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Lista de cursos reales
                    dashboardState.classes.forEach { classEntity ->
                        ClassCard(
                            className = classEntity.className,
                            schoolName = classEntity.schoolName,
                            studentCount = 0, // Nota: Requiere lógica adicional para ser preciso
                            code = classEntity.classCode,
                            recentActivity = "Todo al día",
                            onClick = { onNavigateToClassDetail(classEntity.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Próximos Eventos
            if (dashboardState.upcomingEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                UpcomingEventsSection(dashboardState.upcomingEvents)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 🎯 BOTTOM SHEETS - ESTOS DEBEN ESTAR AQUÍ, AL FINAL DE LA FUNCIÓN
    // ═══════════════════════════════════════════════════════════════════════

    // 🎯 Bottom Sheet: Selector de Curso para ASISTENCIA
    if (showClassSelectorForAttendance) {
        ClassSelectorBottomSheet(
            sheetState = attendanceSheetState,
            classes = dashboardState.classes,
            onClassSelected = { selectedClass ->
                scope.launch {
                    attendanceSheetState.hide()
                    showClassSelectorForAttendance = false
                    navController.navigate(
                        Screen.TakeAttendance.createRoute(
                            classId = selectedClass.id,
                            teacherId = userWithProfile.profile.id
                        )
                    )
                }
            },
            onDismiss = {
                showClassSelectorForAttendance = false
            },
            title = "Selecciona un curso para tomar asistencia"
        )
    }

    // 🎯 Bottom Sheet: Selector de Curso para ANOTACIÓN (Paso 1/2)
    if (showClassSelectorForAnnotation) {
        ClassSelectorBottomSheet(
            sheetState = annotationClassSheetState,
            classes = dashboardState.classes,
            onClassSelected = { selectedClass ->
                scope.launch {
                    annotationClassSheetState.hide()
                    showClassSelectorForAnnotation = false
                    selectedClassForAction = selectedClass
                    studentViewModel.loadStudentsByClass(selectedClass.id)
                    showStudentSelectorForAnnotation = true
                }
            },
            onDismiss = {
                showClassSelectorForAnnotation = false
            },
            title = "Selecciona un curso"
        )
    }

    // 🎯 Bottom Sheet: Selector de Estudiante para ANOTACIÓN (Paso 2/2)
    if (showStudentSelectorForAnnotation && selectedClassForAction != null) {
        StudentSelectorBottomSheet(
            sheetState = annotationStudentSheetState,
            students = studentsByClass,
            selectedClassName = selectedClassForAction!!.className,
            onStudentSelected = { selectedStudent ->
                scope.launch {
                    annotationStudentSheetState.hide()
                    showStudentSelectorForAnnotation = false
                    navController.navigate(
                        Screen.CreateAnnotation.createRoute(
                            studentId = selectedStudent.id,
                            classId = selectedClassForAction!!.id,
                            teacherId = userWithProfile.profile.id
                        )
                    )
                    selectedClassForAction = null
                }
            },
            onDismiss = {
                showStudentSelectorForAnnotation = false
                selectedClassForAction = null
            },
            onBack = {
                scope.launch {
                    annotationStudentSheetState.hide()
                    showStudentSelectorForAnnotation = false
                    showClassSelectorForAnnotation = true
                }
            }
        )
    }

    // 🎯 Bottom Sheet: Selector de Curso para CALENDARIO
    if (showClassSelectorForCalendar) {
        ClassSelectorBottomSheet(
            sheetState = calendarSheetState,
            classes = dashboardState.classes,
            onClassSelected = { selectedClass ->
                scope.launch {
                    calendarSheetState.hide()
                    showClassSelectorForCalendar = false
                    navController.navigate(
                        Screen.CreateEvent.createRoute(
                            teacherId = userWithProfile.profile.id,
                            classId = selectedClass.id
                        )
                    )
                }
            },
            onDismiss = {
                showClassSelectorForCalendar = false
            },
            title = "Selecciona un curso para crear evento"
        )
    }
}

// ============================================================================
// COMPONENTES AUXILIARES Y UTILIDADES
// ============================================================================

fun formatTimestampToDateString(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Composable
fun UpcomingEventsSection(events: List<SchoolEventEntity>) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Próximos Eventos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        events.forEach { event ->
            EventItem(event.title, formatTimestampToDateString(event.eventDate))
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun EventItem(title: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ClassCard(
    className: String,
    schoolName: String,
    studentCount: Int,
    code: String,
    recentActivity: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Class,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f).padding(horizontal = 16.dp))

            Column(
                modifier = Modifier.weight(3f)
            ) {
                Text(
                    text = className,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = schoolName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$studentCount estudiantes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = recentActivity,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ver detalles",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}