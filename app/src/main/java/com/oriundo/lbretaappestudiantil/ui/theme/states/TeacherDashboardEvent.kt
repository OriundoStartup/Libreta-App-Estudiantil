package com.oriundo.lbretaappestudiantil.ui.theme.states

// 2. EVENTOS: Todas las intenciones de la UI
sealed interface TeacherDashboardEvent {
    data class OnInitialize(val userId: Int, val userName: String) : TeacherDashboardEvent
    data object OnRefresh : TeacherDashboardEvent
    data object OnClickCreateClass : TeacherDashboardEvent
    data class OnClickClassDetail(val classId: Int) : TeacherDashboardEvent
    data object OnClickNotifications : TeacherDashboardEvent
    data object OnClickPendingJustifications : TeacherDashboardEvent
    data object OnClickProfile : TeacherDashboardEvent
    data object OnClickSettings : TeacherDashboardEvent
    data object OnLogout : TeacherDashboardEvent
}