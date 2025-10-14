package com.oriundo.lbretaappestudiantil.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.ui.theme.auth.LoginScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.ParentRegisterScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.RoleSelectionScreen
import com.oriundo.lbretaappestudiantil.ui.theme.auth.TeacherRegisterScreen
import com.oriundo.lbretaappestudiantil.ui.theme.parent.ParentDashboardScreen
import com.oriundo.lbretaappestudiantil.ui.theme.teacher.Course
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

// Función de mapeo (necesaria para convertir ClassEntity a Course para la UI)
fun ClassEntity.toCourse(studentCount: Int = 0): Course {
    return Course(
        id = this.id,
        name = this.className,
        schoolName = this.schoolName,
        studentCount = studentCount,
        // ✅ CORRECCIÓN aplicada en la respuesta anterior: Usamos 'classCode'
        code = this.classCode,
        recentActivity = "Última actividad: Hoy" // Dato simulado
    )
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
                        // ✅ CORRECCIÓN: Limpiar la pila hasta el inicio (RoleSelection)
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
                        // ✅ CORRECCIÓN: Limpiar la pila hasta el inicio
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
                        // ✅ CORRECCIÓN: Limpiar la pila hasta el inicio
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Dashboard profesor
        composable(Screen.TeacherDashboard.route) {
            val classViewModel: ClassViewModel = hiltViewModel()
            val teacherClasses by classViewModel.teacherClasses.collectAsState()
            val isLoading by classViewModel.isClassesLoading.collectAsState()

            currentUser?.let { user ->
                val teacherId = user.profile.id

                LaunchedEffect(teacherId) {
                    if (teacherId > 0) {
                        classViewModel.loadTeacherClasses(teacherId)
                    }
                }

                val uiCourses = remember(teacherClasses) {
                    teacherClasses.map { it.toCourse(studentCount = 0) }
                }

                TeacherDashboardScreen(
                    userWithProfile = user,
                    courses = uiCourses,
                    isLoading = isLoading,
                    onNavigateToCreateClass = {
                        navController.navigate(Screen.CreateClass.route)
                    },
                    onNavigateToClassDetail = { classId ->
                        // TODO: Implementar navegación a detalles del curso
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.RoleSelection.route) {
                            // ✅ CORRECCIÓN: Limpiar toda la pila al hacer logout
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                )
            } ?: run {
                // Si el usuario es nulo aquí, navegamos al inicio (RoleSelection)
                navController.navigate(Screen.RoleSelection.route) {
                    // ✅ CORRECCIÓN: Limpiar toda la pila si no hay usuario
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
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
                            // ✅ CORRECCIÓN: Limpiar toda la pila al hacer logout
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
                viewModel = classViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClassCreated = { code ->
                    // Tras crear el curso, volvemos al Dashboard
                    navController.popBackStack()
                }
            )
        }
    }
}