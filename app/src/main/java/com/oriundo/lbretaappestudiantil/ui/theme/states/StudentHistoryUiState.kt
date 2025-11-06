package com.oriundo.lbretaappestudiantil.ui.theme.states


/**
 * Representa el estado de la UI para la pantalla de Historial del Estudiante.
 */
data class StudentHistoryUiState(
    // Indica si la información está cargándose de la base de datos
    val isLoading: Boolean = true,

    // Lista de anotaciones disciplinarias o de comportamiento
    val annotations: List<Any> = emptyList(), // Reemplaza Any por tu modelo Annotation

    // Lista de eventos académicos o extracurriculares
    val events: List<Any> = emptyList(), // Reemplaza Any por tu modelo Event

    // Mensaje de error, si ocurre un fallo al cargar los datos
    val errorMessage: String? = null
)