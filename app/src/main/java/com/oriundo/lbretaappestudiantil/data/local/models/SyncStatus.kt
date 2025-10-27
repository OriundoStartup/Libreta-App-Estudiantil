package com.oriundo.lbretaappestudiantil.data.local.models

/**
 * Estados de sincronización entre Room (local) y Firestore (cloud)
 */
enum class SyncStatus {
    PENDING,    // Esperando sincronización con Firestore
    SYNCED,     // Sincronizado correctamente con Firestore
    ERROR,      // Error al sincronizar (se puede reintentar)
    DELETED     // Marcado para eliminar (soft delete)
}