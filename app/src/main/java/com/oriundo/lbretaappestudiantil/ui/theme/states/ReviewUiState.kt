package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity

/**
 * Estado de la UI para la pantalla de revisión del profesor.
 */
data class ReviewUiState(
    val justification: AbsenceJustificationEntity? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val reviewNotes: String = "",
    val reviewError: String? = null,
    val reviewSuccess: Boolean = false
)