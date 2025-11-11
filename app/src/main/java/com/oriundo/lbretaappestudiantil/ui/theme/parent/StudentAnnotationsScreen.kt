package com.oriundo.lbretaappestudiantil.ui.theme.parent


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnnotationsScreen(
    studentId: Int,
    parentId: Int,
    navController: NavController,
    viewModel: StudentAnnotationsViewModel = hiltViewModel()
) {
    val annotations by viewModel.annotations.collectAsState()
    val stats by viewModel.annotationStats.collectAsState()
    var selectedFilter by remember { mutableStateOf<AnnotationType?>(null) }

    LaunchedEffect(studentId) {
        viewModel.loadAnnotationsByStudent(studentId)
    }

    val filteredAnnotations = if (selectedFilter != null) {
        annotations.filter { it.type == selectedFilter }
    } else {
        annotations
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
                    IconButton(onClick = { navController.popBackStack() }) {
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
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${filteredAnnotations.size} anotación${if (filteredAnnotations.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

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
fun AnnotationStatCard(
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
        )
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
                style = MaterialTheme.typography.headlineSmall,
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
fun AnnotationCard(annotation: Annotation) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(annotation.date))

    val typeInfo = getAnnotationTypeInfo(annotation.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = annotation.teacherName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class AnnotationTypeInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun getAnnotationTypeInfo(type: AnnotationType): AnnotationTypeInfo {
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
        AnnotationType.NEUTRAL -> AnnotationTypeInfo(
            label = "Anotación Informativa",
            icon = Icons.Filled.Info,
            color = Color(0xFF1976D2)
        )
    }
}

data class Annotation(
    val id: Int = 0,
    val studentId: Int,
    val teacherId: Int,
    val teacherName: String,
    val type: AnnotationType,
    val title: String,
    val description: String,
    val date: Long,
    val isRead: Boolean = false
)

enum class AnnotationType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

data class AnnotationStats(
    val positiveCount: Int = 0,
    val negativeCount: Int = 0,
    val neutralCount: Int = 0
)

@HiltViewModel
class StudentAnnotationsViewModel @Inject constructor() : ViewModel() {
    private val _annotations = MutableStateFlow<List<Annotation>>(emptyList())
    val annotations: StateFlow<List<Annotation>> = _annotations

    private val _annotationStats = MutableStateFlow(AnnotationStats())
    val annotationStats: StateFlow<AnnotationStats> = _annotationStats

    fun loadAnnotationsByStudent(studentId: Int) {
        _annotations.value = listOf(
            Annotation(
                id = 1,
                studentId = studentId,
                teacherId = 1,
                teacherName = "Prof. María González",
                type = AnnotationType.POSITIVE,
                title = "Excelente participación",
                description = "El estudiante demostró gran interés y participación activa durante la clase de matemáticas.",
                date = System.currentTimeMillis() - 86400000L,
                isRead = false
            ),
            Annotation(
                id = 2,
                studentId = studentId,
                teacherId = 2,
                teacherName = "Prof. Juan Pérez",
                type = AnnotationType.NEGATIVE,
                title = "No trajo materiales",
                description = "El estudiante no trajo los materiales solicitados para la clase de arte.",
                date = System.currentTimeMillis() - 172800000L,
                isRead = true
            ),
            Annotation(
                id = 3,
                studentId = studentId,
                teacherId = 1,
                teacherName = "Prof. María González",
                type = AnnotationType.NEUTRAL,
                title = "Recordatorio",
                description = "Próxima prueba de matemáticas el viernes 22 de noviembre.",
                date = System.currentTimeMillis() - 259200000L,
                isRead = true
            )
        )

        _annotationStats.value = AnnotationStats(
            positiveCount = 1,
            negativeCount = 1,
            neutralCount = 1
        )
    }
}