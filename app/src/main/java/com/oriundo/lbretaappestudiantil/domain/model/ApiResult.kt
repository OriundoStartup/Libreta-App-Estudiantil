package com.oriundo.lbretaappestudiantil.domain.model

// Importación de StudentEntity no es necesaria aquí, pero no hace daño.
// import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity

/**
 * Clase sellada para manejar resultados de operaciones
 */

sealed class ApiResult<out T> {
    // 1. Success tiene una propiedad 'data' de tipo T
    data class Success<T>(val data: T) : ApiResult<T>()

    // 2. Error tiene una propiedad 'message' (y no necesita tipo T, usa Nothing)
    data class Error(val message: String, val exception: Throwable? = null) : ApiResult<Nothing>()

    // 3. Loading es un objeto singleton (no tiene datos)
    object Loading : ApiResult<Nothing>()

    // El companion object debe estar vacío o contener solo lógica estática, no propiedades abstractas.
    // companion object {
    //     val data: StudentEntity <-- ¡SE ELIMINA ESTO!
    // }
}