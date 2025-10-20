package com.oriundo.lbretaappestudiantil.ui.theme

import androidx.compose.runtime.Composable
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
import com.oriundo.lbretaappestudiantil.ui.theme.auth.TeacherRegisterScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.NotificationsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentDashboardScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentProfileScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentSettingsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.ClassStudentsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateAnnotationScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateClassScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateEventScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.NotificationsScreenTeacher
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.StudentDetailScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TakeAttendanceScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherDashboardScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherProfileScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherSettingsScreen
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AuthViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassViewModel

// Rutas de navegación
sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object Login : Screen("login")
    object TeacherRegister : Screen("teacher_register")
    object ParentRegister : Screen("parent_register")
    object TeacherDashboard : Screen("teacher_dashboard")
    object ParentDashboard : Screen("parent_dashboard")
    object CreateClass : Screen("create_class")
    object TeacherProfile : Screen("teacher_profile")
    object TeacherSettings : Screen("teacher_settings")
    object ParentProfile : Screen("parent_profile")
    object ParentSettings : Screen("parent_settings")

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
    object Notifications : Screen("notifications/{parentId}") {
        fun createRoute(parentId: Int) = "notifications/$parentId"
    }

    object TeacherNotifications : Screen("teacher_notifications/{teacherId}") {
        fun createRoute(teacherId: Int) = "teacher_notifications/$teacherId"
    }
}

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
                viewModel = authViewModel
            )
        }

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

        composable(Screen.ParentRegister.route) {
            ParentRegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { userWithProfile ->
                    navController.navigate(Screen.ParentDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.TeacherDashboard.route) {
            val classViewModel: ClassViewModel = hiltViewModel()

            currentUser?.let { user ->
                TeacherDashboardScreen(
                    userWithProfile = user,
                    navController = navController,  // ← ESTE PARÁMETRO DEBE ESTAR
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

        composable(Screen.ParentDashboard.route) {
            currentUser?.let { user ->
                ParentDashboardScreen(
                    userWithProfile = user,
                    navController = navController,  // ✅ AGREGADO
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

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
        // Notifications Screen (Parents)
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

        // Notifications Screen (Teachers)
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
        // Perfil Profesor
        composable(Screen.TeacherProfile.route) {
            currentUser?.let { user ->
                TeacherProfileScreen(
                    userWithProfile = user,
                    navController = navController
                )
            }
        }

// Configuración Profesor
        composable(Screen.TeacherSettings.route) {
            TeacherSettingsScreen(
                navController = navController
            )
        }

// Perfil Padre
        composable(Screen.ParentProfile.route) {
            currentUser?.let { user ->
                ParentProfileScreen(
                    userWithProfile = user,
                    navController = navController
                )
            }
        }

// Configuración Padre
        composable(Screen.ParentSettings.route) {
            ParentSettingsScreen(
                navController = navController
            )
        }

    }
}