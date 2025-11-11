package com.oriundo.lbretaappestudiantil.ui.theme.states

import android.net.Uri
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason

data class JustificationUiState(
    val selectedDate: Long? = null,
    val selectedReason: AbsenceReason = AbsenceReason.ILLNESS,
    val description: String = "",
    val selectedFileUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val submissionError: String? = null
)