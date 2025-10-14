package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oriundo.lbretaappestudiantil.domain.model.UserWithProfile
import com.oriundo.lbretaappestudiantil.ui.theme.AppColors // Asumiendo que AppColors tiene los gradientes

// Modelo de datos para Course
data class Course(
    val id: Int,
    val name: String,
    val schoolName: String,
    val studentCount: Int,
    val code: String,
    val recentActivity: String = "Todo al día"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    userWithProfile: UserWithProfile,
    courses: List<Course>,
    isLoading: Boolean = false,
    onNavigateToCreateClass: () -> Unit,
    onNavigateToClassDetail: (Int) -> Unit,
    onLogout: () -> Unit,
    // TODO: Añadir navegación a Perfil, Configuración, Notificaciones, etc.
) {
    var showMenu by remember { mutableStateOf(false) }

    // ✅ Mantenemos el cálculo simple aquí
    val totalCourses = courses.size
    val totalStudents = courses.sumOf { it.studentCount }

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
                    // Botón de notificaciones (Mejorado el uso de Badge)
                    BadgedBox(
                        badge = {
                            if (/* Lógica para tener notificaciones */ true) {
                                Badge(containerColor = MaterialTheme.colorScheme.error)
                            }
                        }
                    ) {
                        IconButton(onClick = { /* TODO: Notificaciones */ }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
                        }
                    }

                    // Menú de opciones
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // ... Items del menú igual
                            DropdownMenuItem(
                                text = { Text("Mi Perfil") },
                                onClick = { showMenu = false /* TODO */ },
                                leadingIcon = { Icon(Icons.Filled.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = { showMenu = false /* TODO */ },
                                leadingIcon = { Icon(Icons.Filled.Settings, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) },
                                colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
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
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Crear Curso") },
                // ✅ Usar el color primario para el FAB
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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

            // Quick Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Cursos",
                    value = totalCourses.toString(),
                    icon = Icons.Filled.Class,
                    gradient = AppColors.PrimaryGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: Ver todos los cursos */ }
                )
                StatCard(
                    title = "Estudiantes",
                    value = totalStudents.toString(),
                    icon = Icons.Filled.People,
                    gradient = AppColors.SecondaryGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: Ver todos los estudiantes */ }
                )
            }
            // Segunda fila de Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Anotaciones",
                    value = "24",
                    icon = Icons.AutoMirrored.Filled.Note,
                    gradient = AppColors.SuccessGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: Ver anotaciones */ }
                )
                StatCard(
                    title = "Mensajes",
                    value = "5",
                    icon = Icons.AutoMirrored.Filled.Message,
                    gradient = AppColors.ErrorGradient,
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: Ver mensajes */ }
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
                        title = "Asistencia",
                        icon = Icons.Filled.HowToReg,
                        // ✅ Usar colores de contenedor de Material 3
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Anotación",
                        icon = Icons.Filled.Edit,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Materiales",
                        icon = Icons.Filled.Inventory,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Calendario",
                        icon = Icons.Filled.CalendarMonth,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mis Cursos
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

                    TextButton(onClick = { /* TODO: Ver todos */ }) {
                        Text("Ver todos")
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (courses.isEmpty()) {
                    EmptyCoursesMessage()
                } else {
                    courses.forEach { course ->
                        ClassCard(
                            course = course, // ✅ Pasamos el objeto completo para limpieza
                            onClick = { onNavigateToClassDetail(course.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// --- Componentes Reutilizables ---

// Componente para manejar el estado vacío de los cursos
@Composable
fun EmptyCoursesMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Class,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tienes cursos todavía",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crea tu primer curso usando el botón +",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
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
        shape = RoundedCornerShape(16.dp), // Ligeramente más redondeado
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(36.dp) // Icono un poco más grande
                )
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold // Fuente más pesada
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
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        // ✅ Usamos los colores de contenedor/contenido de Material 3 para armonía
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
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
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ClassCard(
    course: Course, // ✅ Simplificamos pasando el objeto completo
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
            // Icono del curso
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

            Spacer(modifier = Modifier.width(16.dp)) // ✅ Spacer simple y claro

            // Información del curso
            Column(
                modifier = Modifier.weight(1f) // Usar weight para que ocupe el espacio restante
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = course.schoolName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Código del curso
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
                            text = course.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Número de estudiantes
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
                            text = "${course.studentCount} estudiantes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actividad reciente
                Text(
                    text = course.recentActivity,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Icono de navegación
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ver detalles",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}