package com.oriundo.lbretaappestudiantil.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz

enum class AbsenceReason(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ILLNESS("Enfermedad", Icons.Filled.MedicalServices),
    MEDICAL_APPOINTMENT("Consulta médica", Icons.Filled.LocalHospital),
    FAMILY_EMERGENCY("Emergencia familiar", Icons.Filled.FamilyRestroom),
    TRAVEL("Viaje", Icons.Filled.Flight),
    OTHER("Otro motivo", Icons.Filled.MoreHoriz)
}