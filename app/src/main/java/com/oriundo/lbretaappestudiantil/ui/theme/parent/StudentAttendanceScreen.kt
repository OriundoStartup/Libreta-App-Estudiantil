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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceScreen(
    studentId: Int,
    classId: Int,
    navController: NavController,
    viewModel: StudentAttendanceViewModel = hiltViewModel()
) {
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val stats by viewModel.attendanceStats.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadAttendanceByStudent(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Asistencia",
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
        if (attendanceRecords.isEmpty()) {
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
                        imageVector = Icons.Filled.EventAvailable,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sin registros de asistencia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Los registros de asistencia aparecerán aquí.",
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
                        AttendanceStatCard(
                            label = "Presente",
                            count = stats.presentCount,
                            total = stats.totalDays,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )

                        AttendanceStatCard(
                            label = "Ausente",
                            count = stats.absentCount,
                            total = stats.totalDays,
                            color = Color(0xFFE53935),
                            modifier = Modifier.weight(1f)
                        )

                        AttendanceStatCard(
                            label = "Tarde",
                            count = stats.lateCount,
                            total = stats.totalDays,
                            color = Color(0xFFFB8C00),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Historial de Asistencia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(attendanceRecords.sortedByDescending { it.date }) { record ->
                    AttendanceRecordCard(record = record)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun AttendanceStatCard(
    label: String,
    count: Int,
    total: Int,
    color: Color,
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (total > 0) "${(count * 100 / total)}%" else "0%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttendanceRecordCard(record: AttendanceRecord) {
    val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("es", "CL"))
    val formattedDate = dateFormat.format(Date(record.date))

    val statusInfo = getAttendanceStatusInfo(record.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        statusInfo.color.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusInfo.icon,
                    contentDescription = null,
                    tint = statusInfo.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusInfo.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusInfo.color,
                    fontWeight = FontWeight.SemiBold
                )

                if (record.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class AttendanceStatusInfo(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun getAttendanceStatusInfo(status: AttendanceStatus): AttendanceStatusInfo {
    return when (status) {
        AttendanceStatus.PRESENT -> AttendanceStatusInfo(
            label = "Presente",
            icon = Icons.Filled.CheckCircle,
            color = Color(0xFF4CAF50)
        )
        AttendanceStatus.ABSENT -> AttendanceStatusInfo(
            label = "Ausente",
            icon = Icons.Filled.Cancel,
            color = Color(0xFFE53935)
        )
        AttendanceStatus.LATE -> AttendanceStatusInfo(
            label = "Llegó tarde",
            icon = Icons.Filled.AccessTime,
            color = Color(0xFFFB8C00)
        )
        AttendanceStatus.JUSTIFIED -> AttendanceStatusInfo(
            label = "Falta justificada",
            icon = Icons.Filled.VerifiedUser,
            color = Color(0xFF1976D2)
        )
    }
}

data class AttendanceRecord(
    val id: Int = 0,
    val studentId: Int,
    val date: Long,
    val status: AttendanceStatus,
    val notes: String = ""
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    JUSTIFIED
}

data class AttendanceStats(
    val totalDays: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val justifiedCount: Int = 0
)

@HiltViewModel
class StudentAttendanceViewModel @Inject constructor() : ViewModel() {
    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords

    private val _attendanceStats = MutableStateFlow(AttendanceStats())
    val attendanceStats: StateFlow<AttendanceStats> = _attendanceStats

    fun loadAttendanceByStudent(studentId: Int) {
        _attendanceRecords.value = listOf(
            AttendanceRecord(1, studentId, System.currentTimeMillis() - 86400000L, AttendanceStatus.PRESENT),
            AttendanceRecord(2, studentId, System.currentTimeMillis() - 172800000L, AttendanceStatus.ABSENT, "Sin justificación"),
            AttendanceRecord(3, studentId, System.currentTimeMillis() - 259200000L, AttendanceStatus.LATE, "Llegó 15 minutos tarde"),
        )

        _attendanceStats.value = AttendanceStats(
            totalDays = 3,
            presentCount = 1,
            absentCount = 1,
            lateCount = 1,
            justifiedCount = 0
        )
    }
}