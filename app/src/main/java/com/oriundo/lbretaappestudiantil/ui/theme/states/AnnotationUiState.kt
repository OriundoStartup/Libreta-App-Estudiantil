package com.oriundo.lbretaappestudiantil.ui.theme.states

sealed class AnnotationUiState {
    object Idle : AnnotationUiState()
    object Loading : AnnotationUiState()
    object Success : AnnotationUiState()
    data class Error(val message: String) : AnnotationUiState()
}