package com.oriundo.lbretaappestudiantil.ui.theme.parent


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AnnotationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    parentId: Int,
    navController: NavController,
    annotationViewModel: AnnotationViewModel = hiltViewModel()
) {
    val unreadAnnotations by annotationViewModel.unreadAnnotations.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedAnnotation by remember { mutableStateOf<AnnotationEntity?>(null) }

    LaunchedEffect(parentId) {
        annotationViewModel.loadUnreadAnnotationsForParent(parentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadAnnotations.isNotEmpty()) {
                            Text(
                                text = "${unreadAnnotations.size} sin leer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (unreadAnnotations.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                unreadAnnotations.forEach { annotation ->
                                    annotationViewModel.markAnnotationAsRead(annotation.id)
                                }
                            }
                        ) {
                            Text("Marcar todas como leídas")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (unreadAnnotations.isEmpty()) {
            EmptyNotificationsView(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(unreadAnnotations) { annotation ->
                    NotificationItem(
                        annotation = annotation,
                        onClick = {
                            selectedAnnotation = annotation
                            showDialog = true
                            annotationViewModel.markAnnotationAsRead(annotation.id)
                        }
                    )
                }
            }
        }
    }

    if (showDialog && selectedAnnotation != null) {
        NotificationDetailDialog(
            annotation = selectedAnnotation!!,
            onDismiss = {
                showDialog = false
                selectedAnnotation = null
            }
        )
    }
}

@Composable
private fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No hay notificaciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Todas tus notificaciones están al día",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun NotificationItem(
    annotation: AnnotationEntity,
    onClick: () -> Unit
) {
    val (color, icon, iconTint) = when (annotation.type) {
        AnnotationType.POSITIVE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Filled.Star,
            MaterialTheme.colorScheme.tertiary
        )
        AnnotationType.NEGATIVE -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Filled.Warning,
            MaterialTheme.colorScheme.error
        )
        AnnotationType.NEUTRAL -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            Icons.Filled.Info,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnnotationType.GENERAL -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Filled.Description,
            MaterialTheme.colorScheme.primary
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!annotation.isRead)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = annotation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!annotation.isRead) FontWeight.Bold else FontWeight.Normal
                    )
                    if (!annotation.isRead) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Text(
                    text = annotation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimeAgo(annotation.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotificationDetailDialog(
    annotation: AnnotationEntity,
    onDismiss: () -> Unit
) {
    val (color, icon, iconTint) = when (annotation.type) {
        AnnotationType.POSITIVE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Filled.Star,
            MaterialTheme.colorScheme.tertiary
        )
        AnnotationType.NEGATIVE -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Filled.Warning,
            MaterialTheme.colorScheme.error
        )
        AnnotationType.NEUTRAL -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            Icons.Filled.Info,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnnotationType.GENERAL -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Filled.Description,
            MaterialTheme.colorScheme.primary
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = annotation.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = annotation.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault())
                            .format(Date(annotation.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Ahora"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000}d"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}