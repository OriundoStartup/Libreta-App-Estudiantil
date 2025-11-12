package com.oriundo.lbretaappestudiantil.data.local.converters

import androidx.room.TypeConverter
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.data.local.models.EventType
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.data.local.models.SyncStatus
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason

/**
 * TypeConverters para Room Database
 * Convierte enums a String y viceversa
 */
@Suppress("unused") // 💡 SOLUCIÓN: Suprime la advertencia 'is never used'
class Converters {

    // ====================================================================
    // CONVERSORES DE ENUMS EXISTENTES
    // ====================================================================

    // EventType Converters
    @TypeConverter
    fun fromEventType(value: EventType): String {
        return value.name
    }

    @TypeConverter
    fun toEventType(value: String): EventType {
        return try {
            EventType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            EventType.OTHER
        }
    }

    // AnnotationType Converters
    @TypeConverter
    fun fromAnnotationType(value: AnnotationType): String {
        return value.name
    }

    @TypeConverter
    fun toAnnotationType(value: String): AnnotationType {
        return try {
            AnnotationType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            AnnotationType.GENERAL
        }
    }

    // AttendanceStatus Converters
    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String {
        return value.name
    }

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus {
        return try {
            AttendanceStatus.valueOf(value)
        } catch (_: IllegalArgumentException) {
            AttendanceStatus.ABSENT
        }
    }

    // ====================================================================
    // CONVERSORES DE JUSTIFICACIÓN Y SINCRONIZACIÓN
    // ====================================================================

    // JustificationStatus Converters
    @TypeConverter
    fun fromJustificationStatus(value: JustificationStatus): String = value.name

    @TypeConverter
    fun toJustificationStatus(value: String): JustificationStatus {
        return try {
            JustificationStatus.valueOf(value)
        } catch (_: IllegalArgumentException) {
            JustificationStatus.PENDING
        }
    }

    // SyncStatus Converters
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return try {
            SyncStatus.valueOf(value)
        } catch (_: IllegalArgumentException) {
            SyncStatus.ERROR
        }
    }

    // AbsenceReason Converters
    @TypeConverter
    fun fromAbsenceReason(value: AbsenceReason): String = value.name

    @TypeConverter
    fun toAbsenceReason(value: String): AbsenceReason {
        return try {
            AbsenceReason.valueOf(value)
        } catch (_: IllegalArgumentException) {
            AbsenceReason.OTHER
        }
    }
}