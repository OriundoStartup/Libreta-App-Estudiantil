// StudentAnnotationsScreen.kt
package com.oriundo.lbretaappestudiantil.ui.theme.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.ui.theme.AppShapes
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AnnotationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnnotationsScreen(
    studentId: Int,
    parentId: Int,
    navController: NavController,
    viewModel: AnnotationViewModel = hiltViewModel()
) {
    val annotations by viewModel.annotationsByStudent.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadAnnotationsByStudent(studentId)
    }

    StudentAnnotationsContent(
        annotations = annotations,
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnnotationsContent(
    annotations: List<AnnotationEntity>,
    onBackClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf<AnnotationType?>(null) }

    val filteredAnnotations = if (selectedFilter != null) {
        annotations.filter { it.type == selectedFilter }
    } else {
        annotations
    }

    // Calcular estadísticas
    val stats = remember(annotations) {
        AnnotationStats(
            positiveCount = annotations.count { it.type == AnnotationType.POSITIVE },
            negativeCount = annotations.count { it.type == AnnotationType.NEGATIVE },
            neutralCount = annotations.count { it.type == AnnotationType.NEUTRAL || it.type == AnnotationType.GENERAL }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Anotaciones",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (annotations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sin anotaciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Las anotaciones del estudiante aparecerán aquí.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnnotationStatCard(
                            label = "Positivas",
                            count = stats.positiveCount,
                            color = Color(0xFF4CAF50),
                            icon = Icons.Filled.EmojiEvents,
                            modifier = Modifier.weight(1f)
                        )

                        AnnotationStatCard(
                            label = "Negativas",
                            count = stats.negativeCount,
                            color = Color(0xFFE53935),
                            icon = Icons.Filled.Warning,
                            modifier = Modifier.weight(1f)
                        )

                        AnnotationStatCard(
                            label = "Neutras",
                            count = stats.neutralCount,
                            color = Color(0xFF1976D2),
                            icon = Icons.Filled.Info,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("Todas") }
                        )
                        FilterChip(
                            selected = selectedFilter == AnnotationType.POSITIVE,
                            onClick = {
                                selectedFilter = if (selectedFilter == AnnotationType.POSITIVE) null
                                else AnnotationType.POSITIVE
                            },
                            label = { Text("Positivas") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = selectedFilter == AnnotationType.NEGATIVE,
                            onClick = {
                                selectedFilter = if (selectedFilter == AnnotationType.NEGATIVE) null
                                else AnnotationType.NEGATIVE
                            },
                            label = { Text("Negativas") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = selectedFilter == AnnotationType.NEUTRAL,
                            onClick = {
                                selectedFilter = if (selectedFilter == AnnotationType.NEUTRAL) null
                                else AnnotationType.NEUTRAL
                            },
                            label = { Text("Neutras") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(filteredAnnotations.sortedByDescending { it.date }) { annotation ->
                    AnnotationCard(annotation = annotation)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AnnotationStatCard(
    label: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = AppShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@Composable
private fun AnnotationCard(annotation: AnnotationEntity) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(annotation.date))

    val typeInfo = getAnnotationTypeInfo(annotation.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = AppShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            typeInfo.color.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeInfo.icon,
                        contentDescription = null,
                        tint = typeInfo.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = typeInfo.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = typeInfo.color,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!annotation.isRead) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("Nueva")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = annotation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = annotation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class AnnotationTypeInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun getAnnotationTypeInfo(type: AnnotationType): AnnotationTypeInfo {
    return when (type) {
        AnnotationType.POSITIVE -> AnnotationTypeInfo(
            label = "Anotación Positiva",
            icon = Icons.Filled.EmojiEvents,
            color = Color(0xFF4CAF50)
        )
        AnnotationType.NEGATIVE -> AnnotationTypeInfo(
            label = "Anotación Negativa",
            icon = Icons.Filled.Warning,
            color = Color(0xFFE53935)
        )
        AnnotationType.NEUTRAL, AnnotationType.GENERAL -> AnnotationTypeInfo(
            label = "Anotación Informativa",
            icon = Icons.Filled.Info,
            color = Color(0xFF1976D2)
        )
    }
}

private data class AnnotationStats(
    val positiveCount: Int = 0,
    val negativeCount: Int = 0,
    val neutralCount: Int = 0
)