package com.oriundo.lbretaappestudiantil.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// GRADIENTES Y COLORES PERSONALIZADOS
// ============================================================================

object AppColors {
    // Gradientes principales
    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF8B5CF6)  // Purple
        )
    )

    val SecondaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF06B6D4), // Cyan
            Color(0xFF3B82F6)  // Blue
        )
    )

    val SuccessGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFF059669)  // Green
        )
    )

    val ErrorGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFEF4444), // Red
            Color(0xFFF59E0B)  // Amber
        )
    )

    // ✅ NUEVOS - Gradientes para avatares (AGREGAR ESTOS)
    val StudentAvatarGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFEC4899), // Pink
            Color(0xFFF59E0B)  // Amber
        )
    )

    val TeacherAvatarGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF8B5CF6)  // Purple
        )
    )

    val ParentAvatarGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF06B6D4), // Cyan
            Color(0xFF3B82F6)  // Blue
        )
    )

    // Colores sólidos
    val Primary = Color(0xFF6366F1)
    val Secondary = Color(0xFF06B6D4)
    val Success = Color(0xFF10B981)
    val Error = Color(0xFFEF4444)
    val Warning = Color(0xFFF59E0B)
}

// ============================================================================
// ESQUEMA DE COLORES LIGHT
// ============================================================================

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),

    secondary = Color(0xFF06B6D4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),

    tertiary = Color(0xFF10B981),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),

    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1F2937),

    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),

    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFF3F4F6)
)

// ============================================================================
// ESQUEMA DE COLORES DARK
// ============================================================================

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),

    secondary = Color(0xFF22D3EE),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),

    tertiary = Color(0xFF34D399),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFD1FAE5),

    error = Color(0xFFF87171),
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),

    background = Color(0xFF111827),
    onBackground = Color(0xFFF9FAFB),

    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color(0xFF9CA3AF),

    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF374151)
)