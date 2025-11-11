package com.oriundo.lbretaappestudiantil.ui.theme.parent

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.ui.theme.AppColors
import com.oriundo.lbretaappestudiantil.ui.theme.Screen
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ParentDashboardViewModel
import kotlinx.coroutines.launch

// ============================================================================
// ENUM PARA TIPOS DE NAVEGACIÓN
// ============================================================================

enum class NavigationType {
    EVENTS,
    ATTENDANCE,
    JUSTIFY,
    ANNOTATIONS
}

// ============================================================================
// PANTALLA PRINCIPAL
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    userWithProfile: UserWithProfile,
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToChildDetail: (studentId: Int, classId: Int, parentId: Int) -> Unit,
    viewModel: ParentDashboardViewModel = hiltViewModel()
) {
    var showMenu by remember { mutableStateOf(false) }

    // ✅ Estados para Modal Bottom Sheet
    var showStudentSelector by remember { mutableStateOf(false) }
    var pendingNavigation by remember { mutableStateOf<NavigationType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ✅ Estados para Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val state by viewModel.dashboardState.collectAsState()

    val studentCount = state.students.size
    val totalAnnotations = state.unreadAnnotations.size
    val unreadMessagesCount = state.unreadMessagesCount
    val totalAbsences = 0

    LaunchedEffect(userWithProfile.profile.id) {
        viewModel.loadDashboard(userWithProfile.profile.id)
        println("🔍 ParentDashboard - Usuario: ${userWithProfile.user.email}")
        println("🔍 ParentDashboard - Perfil ID: ${userWithProfile.profile.id}")
        println("🔍 ParentDashboard - Firebase UID: ${userWithProfile.user.firebaseUid}")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            text = "Padre / Apoderado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(
                            Screen.Notifications.createRoute(
                                userWithProfile.profile.id
                            )
                        )
                    }) {
                        Box {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notificaciones",
                                tint = if (totalAnnotations > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            if (totalAnnotations > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = if (totalAnnotations > 9) "9+" else totalAnnotations.toString(),
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
                                    navController.navigate(Screen.ParentProfile.route)
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Person, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.ParentSettings.route)
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats - DINÁMICO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Hijos",
                    value = studentCount.toString(),
                    icon = Icons.Filled.ChildCare,
                    gradient = AppColors.PrimaryGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
                StatCard(
                    title = "Anotaciones",
                    value = totalAnnotations.toString(),
                    icon = Icons.Filled.Description,
                    gradient = AppColors.SuccessGradient,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        when {
                            state.students.isEmpty() -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "No tienes estudiantes asociados. Contacta a la escuela.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                            state.students.size == 1 -> {
                                navController.navigate(
                                    Screen.StudentAnnotations.createRoute(
                                        studentId = state.students.first().student.id,
                                        parentId = userWithProfile.profile.id
                                    )
                                )
                            }
                            else -> {
                                pendingNavigation = NavigationType.ANNOTATIONS
                                showStudentSelector = true
                            }
                        }
                    }
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
                    title = "Faltas",
                    value = totalAbsences.toString(),
                    icon = Icons.Filled.Warning,
                    gradient = AppColors.ErrorGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
                StatCard(
                    title = "Mensajes",
                    value = unreadMessagesCount.toString(),
                    icon = Icons.AutoMirrored.Filled.Message,
                    gradient = AppColors.SecondaryGradient,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(
                            Screen.ParentMessages.createRoute(
                                userWithProfile.profile.id
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Actions
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Eventos",
                        icon = Icons.Filled.CalendarMonth,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            when {
                                state.students.isEmpty() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No tienes estudiantes asociados. Contacta a la escuela.",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                                state.students.size == 1 -> {
                                    navController.navigate(
                                        Screen.StudentEvents.createRoute(
                                            studentId = state.students.first().student.id,
                                            classId = state.students.first().classEntity.id
                                        )
                                    )
                                }
                                else -> {
                                    pendingNavigation = NavigationType.EVENTS
                                    showStudentSelector = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        title = "Asistencia",
                        icon = Icons.Filled.HowToReg,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            when {
                                state.students.isEmpty() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No tienes estudiantes asociados. Contacta a la escuela.",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                                state.students.size == 1 -> {
                                    navController.navigate(
                                        Screen.StudentAttendance.createRoute(
                                            studentId = state.students.first().student.id,
                                            classId = state.students.first().classEntity.id
                                        )
                                    )
                                }
                                else -> {
                                    pendingNavigation = NavigationType.ATTENDANCE
                                    showStudentSelector = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Justificar",
                        icon = Icons.Filled.EventAvailable,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            when {
                                state.students.isEmpty() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No tienes estudiantes asociados. Contacta a la escuela.",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                                state.students.size == 1 -> {
                                    navController.navigate(
                                        Screen.JustifyAbsence.createRoute(
                                            studentId = state.students.first().student.id,
                                            parentId = userWithProfile.profile.id
                                        )
                                    )
                                }
                                else -> {
                                    pendingNavigation = NavigationType.JUSTIFY
                                    showStudentSelector = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        title = "Anotaciones",
                        icon = Icons.Filled.Description,
                        color = MaterialTheme.colorScheme.error,
                        onClick = {
                            when {
                                state.students.isEmpty() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No tienes estudiantes asociados. Contacta a la escuela.",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                                state.students.size == 1 -> {
                                    navController.navigate(
                                        Screen.StudentAnnotations.createRoute(
                                            studentId = state.students.first().student.id,
                                            parentId = userWithProfile.profile.id
                                        )
                                    )
                                }
                                else -> {
                                    pendingNavigation = NavigationType.ANNOTATIONS
                                    showStudentSelector = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mis Hijos - DINÁMICO
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Hijos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.students.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChildCare,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tienes hijos asociados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Contacta a la escuela para asociar un estudiante.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    state.students.forEach { student ->
                        StudentCard(
                            studentWithClass = student,
                            onClick = {
                                onNavigateToChildDetail(
                                    student.student.id,
                                    student.classEntity.id,
                                    userWithProfile.profile.id
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // ✅ MODAL BOTTOM SHEET
    if (showStudentSelector) {
        ModalBottomSheet(
            onDismissRequest = {
                showStudentSelector = false
                pendingNavigation = null
            },
            sheetState = sheetState
        ) {
            StudentSelectorBottomSheet(
                students = state.students,
                onStudentSelected = { selectedStudent ->
                    when (pendingNavigation) {
                        NavigationType.EVENTS -> {
                            navController.navigate(
                                Screen.StudentEvents.createRoute(
                                    selectedStudent.student.id,
                                    selectedStudent.classEntity.id
                                )
                            )
                        }
                        NavigationType.ATTENDANCE -> {
                            navController.navigate(
                                Screen.StudentAttendance.createRoute(
                                    selectedStudent.student.id,
                                    selectedStudent.classEntity.id
                                )
                            )
                        }
                        NavigationType.JUSTIFY -> {
                            navController.navigate(
                                Screen.JustifyAbsence.createRoute(
                                    selectedStudent.student.id,
                                    userWithProfile.profile.id
                                )
                            )
                        }
                        NavigationType.ANNOTATIONS -> {
                            navController.navigate(
                                Screen.StudentAnnotations.createRoute(
                                    selectedStudent.student.id,
                                    userWithProfile.profile.id
                                )
                            )
                        }
                        null -> {}
                    }
                    showStudentSelector = false
                    pendingNavigation = null
                },
                onDismiss = {
                    showStudentSelector = false
                    pendingNavigation = null
                }
            )
        }
    }
}

// ============================================================================
// COMPONENTES AUXILIARES
// ============================================================================

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
        shape = MaterialTheme.shapes.medium,
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
        shape = MaterialTheme.shapes.medium,
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
fun StudentCard(
    studentWithClass: StudentWithClass,
    onClick: () -> Unit
) {
    val recentActivity = "Actividad reciente no cargada"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
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
                        MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ChildCare,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f).padding(horizontal = 16.dp))

            Column(
                modifier = Modifier.weight(4f)
            ) {
                Text(
                    text = "${studentWithClass.student.firstName} ${studentWithClass.student.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Curso: ${studentWithClass.classEntity.className}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Última actividad: $recentActivity",
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

// ============================================================================
// MODAL BOTTOM SHEET COMPONENTS
// ============================================================================

@Composable
fun StudentSelectorBottomSheet(
    students: List<StudentWithClass>,
    onStudentSelected: (StudentWithClass) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Selecciona un estudiante",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        students.forEach { studentWithClass ->
            StudentSelectorItem(
                studentWithClass = studentWithClass,
                onClick = { onStudentSelected(studentWithClass) }
            )

            if (studentWithClass != students.last()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

@Composable
fun StudentSelectorItem(
    studentWithClass: StudentWithClass,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = "${studentWithClass.student.firstName} ${studentWithClass.student.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                text = "Curso: ${studentWithClass.classEntity.className}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.ChildCare,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}