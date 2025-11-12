package com.oriundo.lbretaappestudiantil.ui.theme

/**
 * Define los tipos de navegación que requieren una acción previa
 * (como seleccionar un estudiante en el dashboard).
 */
enum class NavigationType {
    // Usado en las Tarjetas de Estadísticas y Acciones Rápidas
    ANNOTATIONS,
    ATTENDANCE,
    EVENTS,
    JUSTIFY,

    // Otros destinos si es necesario (ej. si necesitas abrir el listado de mensajes/hijos, etc.)
    MESSAGES,
    CHILD_DETAIL,
    // ...
}