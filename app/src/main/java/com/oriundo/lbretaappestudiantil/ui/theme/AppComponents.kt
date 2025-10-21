package com.oriundo.lbretaappestudiantil.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// AVATAR REUTILIZABLE
// ============================================================================

enum class AvatarType {
    STUDENT,
    TEACHER,
    PARENT
}

@Composable
fun AppAvatar(
    type: AvatarType,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val gradient = when (type) {
        AvatarType.STUDENT -> AppColors.StudentAvatarGradient
        AvatarType.TEACHER -> AppColors.TeacherAvatarGradient
        AvatarType.PARENT -> AppColors.ParentAvatarGradient
    }

    val icon = when (type) {
        AvatarType.STUDENT -> Icons.Filled.ChildCare
        AvatarType.TEACHER -> Icons.Filled.Person
        AvatarType.PARENT -> Icons.Filled.Person
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

// ============================================================================
// GRADIENT BOX REUTILIZABLE
// ============================================================================

@Composable
fun AppGradientBox(
    gradient: Brush,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(gradient),
        contentAlignment = Alignment.Center,
        content = content
    )
}