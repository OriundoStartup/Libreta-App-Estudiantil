package com.oriundo.lbretaappestudiantil.ui.theme.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.AttendanceViewModel
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.StudentViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeAttendanceScreen(
    classId: Int,
    teacherId: Int,
    onNavigateBack: () -> Unit = {},
    date: Long = System.currentTimeMillis(),
    studentViewModel: StudentViewModel = viewModel(),
    attendanceViewModel: AttendanceViewModel = viewModel()
) {
    val students by studentViewModel.studentsByClass.collectAsState()
    var attendanceMap by remember { mutableStateOf<Map<Int, AttendanceStatus>>(emptyMap()) }

    LaunchedEffect(classId) {
        studentViewModel.loadStudentsByClass(classId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tomar Asistencia") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        // Guardar asistencia
                        attendanceMap.forEach { (studentId, status) ->
                            attendanceViewModel.recordAttendance(
                                studentId = studentId,
                                teacherId = teacherId,
                                date = date,
                                status = status
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = attendanceMap.size == students.size
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Guardar Asistencia")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(students) { student ->
                AttendanceItem(
                    student = student,
                    selectedStatus = attendanceMap[student.id],
                    onStatusSelected = { status ->
                        attendanceMap = attendanceMap + (student.id to status)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun AttendanceItem(
    student: StudentEntity,
    selectedStatus: AttendanceStatus?,
    onStatusSelected: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = student.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "RUT: ${student.rut}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceStatusChip(
                    status = AttendanceStatus.PRESENT,
                    isSelected = selectedStatus == AttendanceStatus.PRESENT,
                    onClick = { onStatusSelected(AttendanceStatus.PRESENT) }
                )
                AttendanceStatusChip(
                    status = AttendanceStatus.ABSENT,
                    isSelected = selectedStatus == AttendanceStatus.ABSENT,
                    onClick = { onStatusSelected(AttendanceStatus.ABSENT) }
                )
                AttendanceStatusChip(
                    status = AttendanceStatus.LATE,
                    isSelected = selectedStatus == AttendanceStatus.LATE,
                    onClick = { onStatusSelected(AttendanceStatus.LATE) }
                )
            }
        }
    }
}

@Composable
fun AttendanceStatusChip(
    status: AttendanceStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val text: String
    val color: Color

    when (status) {
        AttendanceStatus.PRESENT -> {
            text = "Presente"
            color = MaterialTheme.colorScheme.tertiary
        }
        AttendanceStatus.ABSENT -> {
            text = "Ausente"
            color = MaterialTheme.colorScheme.error
        }
        AttendanceStatus.LATE -> {
            text = "Tarde"
            color = MaterialTheme.colorScheme.secondary
        }
        AttendanceStatus.JUSTIFIED -> {
            text = "Justificado"
            color = MaterialTheme.colorScheme.primary
        }
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color,
            selectedLabelColor = Color.White
        )
    )
}