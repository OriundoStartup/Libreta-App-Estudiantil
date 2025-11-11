package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.AbsenceJustificationEntity
data class PendingJustificationsUiState(
    val justifications: List<AbsenceJustificationEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)