package com.oriundo.lbretaappestudiantil.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oriundo.lbretaappestudiantil.ui.theme.auth.LoginScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.ParentRegisterScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.RoleSelectionScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.TeacherRegisterScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentDashboardScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.CreateClassScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.TeacherDashboardScreen
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
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // ViewModel compartido en toda la navegación
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {
        // Pantalla de selección de rol
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

        // Pantalla de login
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

        // Dashboard profesor
        composable(Screen.TeacherDashboard.route) {
            currentUser?.let { user ->
                TeacherDashboardScreen(
                    userWithProfile = user,
                    onNavigateToCreateClass = {
                        navController.navigate(Screen.CreateClass.route)
                    },
                    onNavigateToClassDetail = { classId ->
                        // TODO: Implementar pantalla de detalle del curso
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Dashboard apoderado
        composable(Screen.ParentDashboard.route) {
            currentUser?.let { user ->
                ParentDashboardScreen(
                    userWithProfile = user,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Creación del curso
        composable(Screen.CreateClass.route) {
            val classViewModel: ClassViewModel = hiltViewModel()

            CreateClassScreen(
                teacherId = currentUser?.profile?.id ?: 0,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClassCreated = { code ->
                    navController.popBackStack()
                }
            )
        }
    }
}