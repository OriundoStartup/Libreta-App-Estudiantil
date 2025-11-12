package com.oriundo.lbretaappestudiantil.data.local.models

/**
 * Estados del flujo de revisión y aprobación de una justificación.
 */
enum class JustificationStatus {
    PENDING,    // Enviada por el apoderado, esperando revisión
    APPROVED,   // Aprobada por el profesor
    REJECTED    // Rechazada por el profesor
}