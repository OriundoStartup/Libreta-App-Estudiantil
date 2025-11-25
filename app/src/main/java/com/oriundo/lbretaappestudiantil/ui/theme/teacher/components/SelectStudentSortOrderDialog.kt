package com.oriundo.lbretaappestudiantil.ui.theme.teacher.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class StudentSortOrder(val displayName: String) {
    LAST_NAME_ASC("Apellido (A-Z)"),
    LAST_NAME_DESC("Apellido (Z-A)"),
    FIRST_NAME_ASC("Nombre (A-Z)"),
    FIRST_NAME_DESC("Nombre (Z-A)"),
    RECENT_ACTIVITY("Actividad Reciente")
}

@Composable
fun SelectStudentSortOrderDialog(
    currentSortOrder: StudentSortOrder = StudentSortOrder.LAST_NAME_ASC,
    onDismiss: () -> Unit,
    onSortOrderSelected: (StudentSortOrder) -> Unit
) {
    var selectedOrder by remember { mutableStateOf(currentSortOrder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Orden de Estudiantes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Selecciona cómo deseas ordenar la lista de estudiantes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                StudentSortOrder.entries.forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOrder = order }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOrder == order,
                            onClick = { selectedOrder = order }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = order.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSortOrderSelected(selectedOrder)
                onDismiss()
            }) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}