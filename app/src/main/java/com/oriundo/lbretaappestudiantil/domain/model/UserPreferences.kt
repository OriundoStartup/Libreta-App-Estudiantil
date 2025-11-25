package com.oriundo.lbretaappestudiantil.domain.model


data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = false,
    val annotationNotifications: Boolean = true,
    val eventReminders: Boolean = true,
    val materialRequestAlerts: Boolean = true,
    val defaultView: DefaultView = DefaultView.STUDENT_SUMMARY,
    val language: AppLanguage = AppLanguage.SPANISH
)

enum class DefaultView(val displayName: String) {
    STUDENT_SUMMARY("Resumen del estudiante"),
    MESSAGES("Mensajes"),
    ATTENDANCE("Asistencia"),
    ANNOTATIONS("Anotaciones")
}

enum class AppLanguage(val displayName: String, val code: String) {
    SPANISH("Español", "es"),
    ENGLISH("English", "en")
}