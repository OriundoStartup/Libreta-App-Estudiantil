package com.oriundo.lbretaappestudiantil.ui.theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oriundo.lbretaappestudiantil.ui.theme.auth.LoginScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.ParentRegisterScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.RoleSelectionScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.SetPasswordScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.TeacherRegisterScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ConversationThreadScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.NotificationsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentDashboardScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentProfileScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentSendMessageScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentSettingsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.states.AuthUiState
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.ClassStudentsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateAnnotationScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateClassScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateEventScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.NotificationsScreenTeacher
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.SendMessageScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.StudentDetailScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.StudentHistoryScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TakeAttendanceScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherDashboardScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherProfileScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherSettingsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassViewModel

// ============================================================================
// DEFINICIÓN DE RUTAS DE NAVEGACIÓN
// ============================================================================

/**
 * Sealed class que define todas las rutas de navegación de la aplicación.
 * Cada objeto representa una pantalla con su ruta y parámetros asociados.
 */
sealed class Screen(val route: String) {
    /** ✅ NUEVO: Pantalla para establecer contraseña después de Google Auth */
    object SetPassword : Screen("set_password")

    // ========================================
    // RUTAS DE AUTENTICACIÓN
    // ========================================

    /** Pantalla de selección de rol (Profesor/Apoderado) */
    object RoleSelection : Screen("role_selection")

    /** Pantalla de inicio de sesión */
    object Login : Screen("login")

    /** Pantalla de registro para profesores */
    object TeacherRegister : Screen("teacher_register")

    /** Pantalla de registro para apoderados */
    object ParentRegister : Screen("parent_register")

    // ========================================
    // RUTAS DE PROFESOR
    // ========================================

    /** Dashboard principal del profesor */
    object TeacherDashboard : Screen("teacher_dashboard")

    /** Perfil del profesor */
    object TeacherProfile : Screen("teacher_profile")

    /** Configuración del profesor */
    object TeacherSettings : Screen("teacher_settings")

    /** Crear nueva clase */
    object CreateClass : Screen("create_class")

    /** Ver estudiantes de una clase específica */
    object ClassStudents : Screen("class_students/{classId}") {
        fun createRoute(classId: Int) = "class_students/$classId"
    }

    /** Ver detalles de un estudiante */
    object StudentDetail : Screen("student_detail/{studentId}/{classId}") {
        fun createRoute(studentId: Int, classId: Int) = "student_detail/$studentId/$classId"
    }

    /** Crear anotación para un estudiante */
    object CreateAnnotation : Screen("create_annotation/{studentId}/{classId}/{teacherId}") {
        fun createRoute(studentId: Int, classId: Int, teacherId: Int) =
            "create_annotation/$studentId/$classId/$teacherId"
    }

    /** Crear evento escolar */
    object CreateEvent : Screen("create_event/{teacherId}/{classId}") {
        fun createRoute(teacherId: Int, classId: Int) = "create_event/$teacherId/$classId"
    }

    /** Tomar asistencia de la clase */
    object TakeAttendance : Screen("take_attendance/{classId}/{teacherId}") {
        fun createRoute(classId: Int, teacherId: Int) = "take_attendance/$classId/$teacherId"
    }

    /** Notificaciones del profesor */
    object TeacherNotifications : Screen("teacher_notifications/{teacherId}") {
        fun createRoute(teacherId: Int) = "teacher_notifications/$teacherId"
    }

    /** Bandeja de mensajes del profesor (sin uso actualmente) */
    object TeacherMessages : Screen("teacher_messages/{teacherId}") {
        fun createRoute(teacherId: Int) = "teacher_messages/$teacherId"
    }

    /** Enviar mensaje a apoderados (profesor) */
    object SendMessage : Screen("send_message/{teacherId}") {
        fun createRoute(teacherId: Int) = "send_message/$teacherId"
    }

    /** Ver historial completo del estudiante */
    object StudentHistory : Screen("student_history/{studentId}/{classId}") {
        fun createRoute(studentId: Int, classId: Int) = "student_history/$studentId/$classId"
    }

    // ========================================
    // RUTAS DE APODERADO
    // ========================================

    /** Dashboard principal del apoderado */
    object ParentDashboard : Screen("parent_dashboard")

    /** Perfil del apoderado */
    object ParentProfile : Screen("parent_profile")

    /** Configuración del apoderado */
    object ParentSettings : Screen("parent_settings")

    /** Notificaciones del apoderado */
    object Notifications : Screen("notifications/{parentId}") {
        fun createRoute(parentId: Int) = "notifications/$parentId"
    }

    /** Conversación individual con un profesor */
    object ParentConversation : Screen("parent_conversation/{parentId}/{teacherId}/{studentId}") {
        fun createRoute(parentId: Int, teacherId: Int, studentId: Int?) =
            "parent_conversation/$parentId/$teacherId/${studentId ?: 0}"
    }

    /** Enviar nuevo mensaje a un profesor (apoderado) */
    object ParentSendMessage : Screen("parent_send_message/{parentId}") {
        fun createRoute(parentId: Int) = "parent_send_message/$parentId"
    }
}

// ============================================================================
// CONFIGURACIÓN DE NAVEGACIÓN
// ============================================================================

/**
 * Composable principal que configura toda la navegación de la aplicación.
 *
 * @param navController Controlador de navegación de Jetpack Compose
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {

        // ====================================================================
        // PANTALLAS DE AUTENTICACIÓN
        // ====================================================================

        // Selección de rol
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onNavigateToTeacherRegister = {
                    navController.navigate(Screen.TeacherRegister.route)
                },
                onNavigateToParentRegister = {
                    navController.navigate(Screen.ParentRegister.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Login.route) {
            val loginViewModel: AuthViewModel = hiltViewModel()
            val loginUiState by loginViewModel.uiState.collectAsState()

            // ✅ NUEVO: Manejar redirección a SetPassword desde Login
            LaunchedEffect(loginUiState) {
                when (loginUiState) {
                    is AuthUiState.AwaitingPasswordSetup -> {
                        navController.navigate(Screen.SetPassword.route) {
                            popUpTo(Screen.Login.route) { inclusive = false }
                        }
                    }
                    else -> {}
                }
            }

            LoginScreen(
                onNavigateToRegister = {
                    navController.popBackStack()
                    navController.navigate(Screen.RoleSelection.route)
                },
                onLoginSuccess = { userWithProfile ->
                    navController.navigate(
                        if (userWithProfile.profile.isTeacher) {
                            Screen.TeacherDashboard.route
                        } else {
                            Screen.ParentDashboard.route
                        }
                    ) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                viewModel = loginViewModel
            )
        }

        // Registro de profesor
        composable(Screen.TeacherRegister.route) {
            TeacherRegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { userWithProfile ->
                    navController.navigate(Screen.TeacherDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Registro de apoderado
        composable(Screen.ParentRegister.route) {
            val parentRegisterViewModel: AuthViewModel = hiltViewModel()
            val parentRegisterUiState by parentRegisterViewModel.uiState.collectAsState()

            // ✅ Detectar cuando necesita establecer contraseña
            LaunchedEffect(parentRegisterUiState) {
                if (parentRegisterUiState is AuthUiState.AwaitingPasswordSetup) {
                    navController.navigate(Screen.SetPassword.route) {
                        popUpTo(Screen.ParentRegister.route) { inclusive = true }
                    }
                }
            }

            ParentRegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { userWithProfile ->
                    navController.navigate(Screen.ParentDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                viewModel = parentRegisterViewModel
            )
        }

        // ✅ NUEVO: Pantalla de establecer contraseña
        composable(Screen.SetPassword.route) {
            val setPasswordViewModel: AuthViewModel = hiltViewModel()
            val currentUserForPassword by setPasswordViewModel.currentUser.collectAsState()

            currentUserForPassword?.let { user ->
                SetPasswordScreen(
                    userWithProfile = user,
                    isOptional = false, // Hacer obligatorio
                    onPasswordSet = {
                        // Navegar al Dashboard después de establecer contraseña
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    },
                    onSkip = null, // No permitir saltar si es obligatorio
                    viewModel = setPasswordViewModel
                )
            } ?: run {
                // Si no hay usuario, volver al inicio
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        // ====================================================================
        // PANTALLAS PRINCIPALES - DASHBOARDS
        // ====================================================================

        // Dashboard del profesor
        composable(Screen.TeacherDashboard.route) {
            val classViewModel: ClassViewModel = hiltViewModel()

            currentUser?.let { user ->
                TeacherDashboardScreen(
                    userWithProfile = user,
                    navController = navController,
                    onNavigateToCreateClass = {
                        navController.navigate(Screen.CreateClass.route)
                    },
                    onNavigateToClassDetail = { classId ->
                        navController.navigate(Screen.ClassStudents.createRoute(classId))
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    viewModel = classViewModel
                )
            }
        }

        // Dashboard del apoderado
        composable(Screen.ParentDashboard.route) {
            currentUser?.let { user ->
                ParentDashboardScreen(
                    userWithProfile = user,
                    navController = navController,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    // ✅ CORRECCIÓN 1: El callback ahora recibe ambos IDs (studentId y classId)
                    onNavigateToChildDetail = { studentId, classId ->
                        // Y ambos IDs son usados para crear la ruta del detalle
                        navController.navigate(Screen.StudentDetail.createRoute(studentId, classId))
                    }
                )
            }
        }

        // ====================================================================
        // GESTIÓN DE CLASES Y ESTUDIANTES
        // ====================================================================

        // Crear clase
        composable(Screen.CreateClass.route) {
            val classViewModel: ClassViewModel = hiltViewModel()

            CreateClassScreen(
                teacherId = currentUser?.profile?.id ?: 0,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClassCreated = { code ->
                    navController.popBackStack()
                },
                viewModel = classViewModel
            )
        }

        // Ver estudiantes de una clase
        composable(
            route = Screen.ClassStudents.route,
            arguments = listOf(
                navArgument("classId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val authViewModelInstance: AuthViewModel = hiltViewModel()

            ClassStudentsScreen(
                classId = classId,
                onStudentClick = { student ->
                    navController.navigate(
                        Screen.StudentDetail.createRoute(student.id, classId)
                    )
                },
                navController = navController,
                authViewModel = authViewModelInstance
            )
        }

        // Detalle de estudiante
        composable(
            route = Screen.StudentDetail.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.IntType },
                navArgument("classId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0

            StudentDetailScreen(
                studentId = studentId,
                classId = classId,
                navController = navController
            )
        }
        // Historial del estudiante
        composable(
            route = Screen.StudentHistory.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.IntType },
                navArgument("classId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0

            StudentHistoryScreen(
                studentId = studentId,
                classId = classId,
                navController = navController
            )
        }



        // ====================================================================
        // ANOTACIONES Y EVENTOS
        // ====================================================================

        // Crear anotación
        composable(
            route = Screen.CreateAnnotation.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.IntType },
                navArgument("classId") { type = NavType.IntType },
                navArgument("teacherId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0

            CreateAnnotationScreen(
                studentId = studentId,
                classId = classId,
                teacherId = teacherId,
                navController = navController,
                onAnnotationCreated = {
                    // Opcional: mostrar mensaje de éxito
                }
            )
        }

        // Crear evento
        composable(
            route = Screen.CreateEvent.route,
            arguments = listOf(
                navArgument("teacherId") { type = NavType.IntType },
                navArgument("classId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0

            CreateEventScreen(
                navController = navController,
                teacherId = teacherId
            )
        }

        // ====================================================================
        // ASISTENCIA
        // ====================================================================

        // Tomar asistencia
        composable(
            route = Screen.TakeAttendance.route,
            arguments = listOf(
                navArgument("classId") { type = NavType.IntType },
                navArgument("teacherId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0

            TakeAttendanceScreen(
                classId = classId,
                teacherId = teacherId,
                navController = navController
            )
        }

        // ====================================================================
        // NOTIFICACIONES
        // ====================================================================

        // Notificaciones de apoderado
        composable(
            route = Screen.Notifications.route,
            arguments = listOf(
                navArgument("parentId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val parentId = backStackEntry.arguments?.getInt("parentId") ?: 0

            NotificationsScreen(
                parentId = parentId,
                navController = navController
            )
        }

        // Notificaciones de profesor
        composable(
            route = Screen.TeacherNotifications.route,
            arguments = listOf(
                navArgument("teacherId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0

            NotificationsScreenTeacher(
                teacherId = teacherId,
                navController = navController
            )
        }

        // ====================================================================
        // SISTEMA DE MENSAJERÍA
        // ====================================================================

        // Enviar mensaje (Profesor → Apoderados)
        composable(
            route = Screen.SendMessage.route,
            arguments = listOf(
                navArgument("teacherId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0

            SendMessageScreen(
                teacherId = teacherId,
                navController = navController
            )
        }

        // Conversación individual (Apoderado ↔ Profesor)
        composable(
            route = Screen.ParentConversation.route,
            arguments = listOf(
                navArgument("parentId") { type = NavType.IntType },
                navArgument("teacherId") { type = NavType.IntType },
                navArgument("studentId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val parentId = backStackEntry.arguments?.getInt("parentId") ?: 0
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0

            ConversationThreadScreen(
                parentId = parentId,
                teacherId = teacherId,
                navController = navController
            )
        }

        // Enviar nuevo mensaje (Apoderado → Profesor)
        composable(
            route = Screen.ParentSendMessage.route,
            arguments = listOf(
                navArgument("parentId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val parentId = backStackEntry.arguments?.getInt("parentId") ?: 0

            ParentSendMessageScreen(
                parentId = parentId,
                navController = navController
            )
        }

        // ====================================================================
        // PERFILES Y CONFIGURACIÓN
        // ====================================================================

        // Perfil del profesor
        composable(Screen.TeacherProfile.route) {
            currentUser?.let { user ->
                TeacherProfileScreen(
                    userWithProfile = user,
                    navController = navController
                )
            }
        }

        // Configuración del profesor
        composable(Screen.TeacherSettings.route) {
            TeacherSettingsScreen(
                navController = navController
            )
        }

        // Perfil del apoderado
        composable(Screen.ParentProfile.route) {
            currentUser?.let { user ->
                ParentProfileScreen(
                    userWithProfile = user,
                    navController = navController
                )
            }
        }

        // Configuración del apoderado
        composable(Screen.ParentSettings.route) {
            ParentSettingsScreen(
                navController = navController
            )
        }
    }
}