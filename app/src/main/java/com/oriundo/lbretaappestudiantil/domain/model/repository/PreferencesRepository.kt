package com.oriundo.lbretaappestudiantil.domain.model.repository

import com.oriundo.lbretaappestudiantil.domain.model.AppLanguage
import com.oriundo.lbretaappestudiantil.domain.model.DefaultView
import com.oriundo.lbretaappestudiantil.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para manejar las preferencias del usuario
 * Usa DataStore para persistencia local
 */
interface PreferencesRepository {

    /**
     * Flow que emite las preferencias actuales del usuario
     * Se actualiza automáticamente cuando cambian las preferencias
     */
    val userPreferencesFlow: Flow<UserPreferences>

    /**
     * Actualiza si las notificaciones push están habilitadas
     */
    suspend fun updateNotificationsEnabled(enabled: Boolean)

    /**
     * Actualiza si las notificaciones por email están habilitadas
     */
    suspend fun updateEmailNotifications(enabled: Boolean)

    /**
     * Actualiza si las notificaciones de anotaciones están habilitadas
     */
    suspend fun updateAnnotationNotifications(enabled: Boolean)

    /**
     * Actualiza si los recordatorios de eventos están habilitados
     */
    suspend fun updateEventReminders(enabled: Boolean)

    /**
     * Actualiza si las alertas de solicitudes de materiales están habilitadas
     */
    suspend fun updateMaterialRequestAlerts(enabled: Boolean)

    /**
     * Actualiza la vista por defecto que se muestra al abrir la app
     */
    suspend fun updateDefaultView(view: DefaultView)

    /**
     * Actualiza el idioma de la aplicación
     */
    suspend fun updateLanguage(language: AppLanguage)

    /**
     * Limpia todas las preferencias
     * Usado típicamente al cerrar sesión
     */
    suspend fun clearPreferences()
}