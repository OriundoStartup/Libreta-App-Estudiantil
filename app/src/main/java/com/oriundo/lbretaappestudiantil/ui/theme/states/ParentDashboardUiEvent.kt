package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.ui.theme.NavigationType

/**
 * Eventos únicos que la UI debe consumir (Navegación, Snackbars, etc.)
 */
sealed interface ParentDashboardUiEvent {
    data class Navigate(val route: String) : ParentDashboardUiEvent
    data class ShowSnackbar(val message: String) : ParentDashboardUiEvent
    data class ShowStudentSelector(val navigationType: NavigationType) : ParentDashboardUiEvent
}
