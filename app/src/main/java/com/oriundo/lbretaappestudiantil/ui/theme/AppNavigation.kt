package com.oriundo.lbretaappestudiantil.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.oriundo.lbretaappestudiantil.ui.theme.auth.TeacherRegisterScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ConversationThreadScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.NotificationsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentDashboardScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentMessagesListScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentProfileScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentSendMessageScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentSettingsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentStudentDetailScreen
import com.oriundo.lbretaappestudiantil.ui.theme.states.AuthUiState
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.ClassStudentsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateAnnotationScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateClassScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateEventScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.NotificationsScreenTeacher
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.SendMessageScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TakeAttendanceScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherDashboardScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherProfileScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherSettingsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassViewModel

sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object Login : Screen("login")
    object TeacherRegister : Screen("teacher_register")
    object ParentRegister : Screen("parent_register")
    object TeacherDashboard : Screen("teacher_dashboard")
    object TeacherProfile : Screen("teacher_profile")
    object TeacherSettings : Screen("teacher_settings")
    object CreateClass : Screen("create_class")


    object ClassStudents : Screen("class_students/{classId}") {
        fun createRoute(classId: Int) = "class_students/$classId"
    }
    object StudentDetail : Screen("student_detail/{studentId}/{classId}") {
        fun createRoute(studentId: Int, classId: Int) = "student_detail/$studentId/$classId"
    }
    object CreateAnnotation : Screen("create_annotation/{studentId}/{classId}/{teacherId}") {
        fun createRoute(studentId: Int, classId: Int, teacherId: Int) =
            "create_annotation/$studentId/$classId/$teacherId"
    }
    object CreateEvent : Screen("create_event/{teacherId}/{classId}") {
        fun createRoute(teacherId: Int, classId: Int) = "create_event/$teacherId/$classId"
    }
    object TakeAttendance : Screen("take_attendance/{classId}/{teacherId}") {
        fun createRoute(classId: Int, teacherId: Int) = "take_attendance/$classId/$teacherId"
    }
    object TeacherNotifications : Screen("teacher_notifications/{teacherId}") {
        fun createRoute(teacherId: Int) = "teacher_notifications/$teacherId"
    }

    object SendMessage : Screen("send_message/{teacherId}") {
        fun createRoute(teacherId: Int) = "send_message/$teacherId"
    }
    object StudentHistory : Screen("student_history/{studentId}/{classId}") {
        fun createRoute(studentId: Int, classId: Int) = "student_history/$studentId/$classId"
    }
    object ParentDashboard : Screen("parent_dashboard")

    object ParentProfile : Screen("parent_profile")

    object ParentSettings : Screen("parent_settings")

    object Notifications : Screen("notifications/{parentId}") {
        fun createRoute(parentId: Int) = "notifications/$parentId"
    }
    object ParentConversation : Screen("parent_conversation/{parentId}/{teacherId}/{studentId}") {
        fun createRoute(parentId: Int, teacherId: Int, studentId: Int?) =
            "parent_conversation/$parentId/$teacherId/${studentId ?: 0}"
    }
    object ParentSendMessage : Screen("parent_send_message/{parentId}?studentId={studentId}") {
        fun createRoute(parentId: Int, studentId: Int? = null) =
            "parent_send_message/$parentId?studentId=${studentId ?: -1}"
    }

    object ParentMessages : Screen("parent_messages/{parentId}") {
        fun createRoute(parentId: Int) = "parent_messages/$parentId"
    }

    object ParentStudentDetail : Screen("parent_student_detail/{studentId}/{classId}/{parentId}") {
        fun createRoute(studentId: Int, classId: Int, parentId: Int) = "parent_student_detail/$studentId/$classId/$parentId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    println("🟢 AppNavigation - currentUser: ${currentUser?.profile?.id}")
    println("🟢 AppNavigation - authUiState: $authUiState")

    // Navegación automática después de login/registro exitoso
    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> {
                val route = if (state.userWithProfile.profile.isTeacher) {
                    Screen.TeacherDashboard.route
                } else {
                    Screen.ParentDashboard.route
                }
                navController.navigate(route) {
                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    )
    {
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
            LoginScreen(
                onNavigateToRegister = {
                    navController.popBackStack()
                    navController.navigate(Screen.RoleSelection.route)
                },
                onLoginSuccess = { }, // Manejado por LaunchedEffect
                viewModel = authViewModel
            )
        }

        composable(Screen.TeacherRegister.route) {
            TeacherRegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = { }, // Manejado por LaunchedEffect
                viewModel = authViewModel
            )
        }

        composable(Screen.ParentRegister.route) {
            ParentRegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = { }, // Manejado por LaunchedEffect
                viewModel = authViewModel
            )
        }

        composable(Screen.TeacherDashboard.route) {
            val classViewModel: ClassViewModel = hiltViewModel()

            // ✅ CAMBIO: Usar currentUser en lugar de authUiState
            currentUser?.let { user ->
                println("🟢 TeacherDashboard - teacherId: ${user.profile.id}")
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
            } ?: run {
                // Si no hay usuario, redirigir al login
                LaunchedEffect(Unit) {
                    println("🔴 TeacherDashboard - No hay usuario, redirigiendo al login")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        // Archivo: AppNavigation.kt

// ... (arriba de esta sección)

        composable(Screen.ParentDashboard.route) {
            currentUser?.let { user ->
                println("🟢 ParentDashboard - parentId: ${user.profile.id}")
                ParentDashboardScreen(
                    userWithProfile = user,
                    navController = navController,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    // CORRECCIÓN: DEBE RECIBIR 3 IDS Y USARLOS AL NAVEGAR
                    onNavigateToChildDetail = { studentId: Int, classId: Int, parentId: Int ->
                        navController.navigate(
                            // Creamos la ruta con los tres IDs
                            Screen.ParentStudentDetail.createRoute(studentId, classId, parentId)
                        )
                    },
                )
            } ?: run {
                LaunchedEffect(Unit) {
                    println("🔴 ParentDashboard - No hay usuario, redirigiendo al login")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

// ... (resto del NavHost)

        composable(Screen.CreateClass.route) {
            val classViewModel: ClassViewModel = hiltViewModel()
            val state = authUiState as? AuthUiState.Success

            CreateClassScreen(
                teacherId = state?.userWithProfile?.profile?.id ?: 0,
                onNavigateBack = { navController.popBackStack() },
                onClassCreated = { navController.popBackStack() },
                viewModel = classViewModel
            )
        }

        composable(
            route = Screen.ClassStudents.route,
            arguments = listOf(navArgument("classId") { type = NavType.IntType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            ClassStudentsScreen(
                classId = classId,
                onStudentClick = { student ->
                    navController.navigate(Screen.StudentDetail.createRoute(student.id, classId))
                },
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ...
        composable(
            route = Screen.ParentStudentDetail.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.IntType },
                navArgument("classId") { type = NavType.IntType },
                navArgument("parentId") { type = NavType.IntType } // ⬅️ Nuevo argumento
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val parentId = backStackEntry.arguments?.getInt("parentId") ?: 0 // ⬅️ Recuperar parentId

            // ASUMIMOS que tu pantalla acepta el parámetro parentId
            ParentStudentDetailScreen(
                studentId = studentId,
                classId = classId,
                parentId = parentId,
                navController = navController
            )
        }
// ...

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

            println("🟢 CreateAnnotation - Params: studentId=$studentId, classId=$classId, teacherId=$teacherId")

            CreateAnnotationScreen(
                studentId = studentId,
                classId = classId,
                teacherId = teacherId,
                navController = navController,
                onAnnotationCreated = {}
            )
        }

        composable(
            route = Screen.CreateEvent.route,
            arguments = listOf(
                navArgument("teacherId") { type = NavType.IntType },
                navArgument("classId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0
            CreateEventScreen(navController = navController, teacherId = teacherId)
        }

        composable(
            route = Screen.TakeAttendance.route,
            arguments = listOf(
                navArgument("classId") { type = NavType.IntType },
                navArgument("teacherId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0
            TakeAttendanceScreen(classId = classId, teacherId = teacherId, navController = navController)
        }

        composable(
            route = Screen.Notifications.route,
            arguments = listOf(navArgument("parentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val parentId = backStackEntry.arguments?.getInt("parentId") ?: 0
            NotificationsScreen(parentId = parentId, navController = navController)
        }

        composable(
            route = Screen.TeacherNotifications.route,
            arguments = listOf(navArgument("teacherId") { type = NavType.IntType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0
            NotificationsScreenTeacher(teacherId = teacherId, navController = navController)
        }

        composable(
            route = Screen.SendMessage.route,
            arguments = listOf(navArgument("teacherId") { type = NavType.IntType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0
            SendMessageScreen(teacherId = teacherId, navController = navController)
        }

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

            // 1. EXTRAER studentId del Bundle
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0 // <-- ¡AÑADIDO!

            // 2. PASAR studentId a la pantalla
            ConversationThreadScreen(
                parentId = parentId,
                teacherId = teacherId,
                studentId = studentId, // <-- ¡AÑADIDO!
                navController = navController
            )
        }


        composable(
            route = Screen.ParentSendMessage.route,
            arguments = listOf(
                navArgument("parentId") { type = NavType.IntType },
                navArgument("studentId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            // 1. Obtener el ID sin valor por defecto 0
            val parentId = backStackEntry.arguments?.getInt("parentId")
            val studentId = backStackEntry.arguments?.getInt("studentId")?.takeIf { it != -1 }

            // 2. Validación de seguridad
            if (parentId != null && parentId > 0) {
                // Solo si el parentId es válido, muestra la pantalla
                ParentSendMessageScreen(
                    parentId = parentId, // <-- Ahora 'parentId' está garantizado a ser > 0
                    preselectedStudentId = studentId,
                    onNavigateBack = { navController.popBackStack() },

                    onNavigateToConversation = { pId: Int, tId: Int, sId: Int ->
                        navController.navigate(
                            Screen.ParentConversation.createRoute(pId, tId, sId)
                        ) {
                            popUpTo(Screen.ParentSendMessage.route) { inclusive = true }
                        }
                    }
                )
            } else {
                // Si el parentId es nulo o 0, muestra un error en lugar de crashear
                println("🔴 ERROR AppNavigation: Se intentó navegar a ParentSendMessageScreen con un parentId inválido: $parentId")

                // Muestra un mensaje de error en la UI
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ID de usuario no encontrado ($parentId). No se puede enviar el mensaje.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        composable(
            route = Screen.ParentStudentDetail.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.IntType },
                navArgument("classId") { type = NavType.IntType },
                navArgument("parentId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val parentId = backStackEntry.arguments?.getInt("parentId") ?: 0
            ParentStudentDetailScreen(
                parentId = parentId, // <-- Lo pasamos a la pantalla
                studentId = studentId,
                classId = classId,
                navController = navController
            )
        }

        composable(
            route = Screen.ParentMessages.route,
            arguments = listOf(navArgument("parentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val parentId = backStackEntry.arguments?.getInt("parentId") ?: 0
            ParentMessagesListScreen(parentId = parentId, navController = navController)
        }

        composable(Screen.TeacherProfile.route) {
            // ✅ CAMBIO: Usar currentUser
            currentUser?.let { user ->
                TeacherProfileScreen(userWithProfile = user, navController = navController)
            }
        }

        composable(Screen.TeacherSettings.route) {
            TeacherSettingsScreen(navController = navController)
        }

        composable(Screen.ParentProfile.route) {
            // ✅ CAMBIO: Usar currentUser
            currentUser?.let { user ->
                ParentProfileScreen(userWithProfile = user, navController = navController)
            }
        }

        composable(Screen.ParentSettings.route) {
            ParentSettingsScreen(navController = navController)
        }

    }


}