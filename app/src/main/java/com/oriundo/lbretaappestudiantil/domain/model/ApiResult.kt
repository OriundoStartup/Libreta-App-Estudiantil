package com.oriundo.lbretaappestudiantil.domain.model

/**
 * Clase sellada para manejar resultados de operaciones (Success, Error, Loading).
 */

sealed class ApiResult<out T> {
    // 1. Success tiene una propiedad 'data' de tipo T
    data class Success<T>(val data: T) : ApiResult<T>()

    // 2. Error tiene una propiedad 'message' (y no necesita tipo T, usa Nothing)
    data class Error(val message: String, val exception: Throwable? = null) : ApiResult<Nothing>()

    // 3. Loading es un objeto singleton (no tiene datos)
    object Loading : ApiResult<Nothing>()

    // =========================================================================
    // ✅ SOLUCIÓN AL ERROR DE TIPADO: Companion Object
    // Estas funciones genéricas permiten a Kotlin inferir el tipo T
    // correctamente cuando se utiliza Error o Loading en un contexto ApiResult<T>.
    // =========================================================================
    companion object {
        fun <T> error(message: String, exception: Throwable? = null): ApiResult<T> {
            // Internamente retorna ApiResult.Error, pero el tipo de retorno es forzado a ApiResult<T>
            return Error(message, exception)
        }

        fun <T> loading(): ApiResult<T> {
            // Internamente retorna ApiResult.Loading, pero el tipo de retorno es forzado a ApiResult<T>
            return Loading
        }
    }
}