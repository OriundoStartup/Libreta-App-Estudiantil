package com.oriundo.lbretaappestudiantil.data.local.repositories


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oriundo.lbretaappestudiantil.domain.model.AppLanguage
import com.oriundo.lbretaappestudiantil.domain.model.DefaultView
import com.oriundo.lbretaappestudiantil.domain.model.UserPreferences
import com.oriundo.lbretaappestudiantil.domain.model.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extensión para crear el DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Implementación del repositorio de preferencias usando DataStore
 * Proporciona persistencia local de las configuraciones del usuario
 */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private val dataStore = context.dataStore

    companion object {
        // Keys para DataStore
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val EMAIL_NOTIFICATIONS = booleanPreferencesKey("email_notifications")
        private val ANNOTATION_NOTIFICATIONS = booleanPreferencesKey("annotation_notifications")
        private val EVENT_REMINDERS = booleanPreferencesKey("event_reminders")
        private val MATERIAL_REQUEST_ALERTS = booleanPreferencesKey("material_request_alerts")
        private val DEFAULT_VIEW = stringPreferencesKey("default_view")
        private val LANGUAGE = stringPreferencesKey("language")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            emailNotifications = preferences[EMAIL_NOTIFICATIONS] ?: false,
            annotationNotifications = preferences[ANNOTATION_NOTIFICATIONS] ?: true,
            eventReminders = preferences[EVENT_REMINDERS] ?: true,
            materialRequestAlerts = preferences[MATERIAL_REQUEST_ALERTS] ?: true,
            defaultView = try {
                DefaultView.valueOf(preferences[DEFAULT_VIEW] ?: DefaultView.STUDENT_SUMMARY.name)
            } catch (e: IllegalArgumentException) {
                DefaultView.STUDENT_SUMMARY // Fallback a default si el valor no es válido
            },
            language = try {
                AppLanguage.valueOf(preferences[LANGUAGE] ?: AppLanguage.SPANISH.name)
            } catch (e: IllegalArgumentException) {
                AppLanguage.SPANISH // Fallback a español si el valor no es válido
            }
        )
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun updateEmailNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[EMAIL_NOTIFICATIONS] = enabled
        }
    }

    override suspend fun updateAnnotationNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ANNOTATION_NOTIFICATIONS] = enabled
        }
    }

    override suspend fun updateEventReminders(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[EVENT_REMINDERS] = enabled
        }
    }

    override suspend fun updateMaterialRequestAlerts(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[MATERIAL_REQUEST_ALERTS] = enabled
        }
    }

    override suspend fun updateDefaultView(view: DefaultView) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_VIEW] = view.name
        }
    }

    override suspend fun updateLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = language.name
        }
    }

    override suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}