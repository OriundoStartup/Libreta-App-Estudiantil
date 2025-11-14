package com.oriundo.lbretaappestudiantil.ui.theme.states



data class SendResult(
    val studentName: String,
    val studentId: Int,
    val success: Boolean,
    val error: String? = null
)

sealed class BulkSendUiState {
    object Initial : BulkSendUiState()
    data class Loading(val sent: Int, val total: Int) : BulkSendUiState()
    data class Success(val results: List<SendResult>) : BulkSendUiState()
    data class PartialSuccess(
        val successful: List<SendResult>,
        val failed: List<SendResult>
    ) : BulkSendUiState()
}