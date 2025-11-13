package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity

// 1. ESTADO: La foto exacta de la UI, con toda la lógica pre-calculada
data class TeacherDashboardUiState(
    val teacherName: String = "", // Del perfil
    val classes: List<ClassEntity> = emptyList(), // Cursos del profesor
    val totalStudents: Int = 0, // Total de estudiantes en todos los cursos
    val pendingAnnotations: Int = 0, // Conteo de anotaciones
    val unreadMessagesCount: Int = 0, // Conteo de mensajes sin leer
    val notificationBadgeText: String? = null, // Texto del badge: "9+", "5", o null (calculado en VM)
    val isLoading: Boolean = false,
    val error: String? = null
)