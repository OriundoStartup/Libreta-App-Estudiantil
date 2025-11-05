package com.oriundo.lbretaappestudiantil.data.local.converters


import androidx.room.TypeConverter
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationType
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceStatus
import com.oriundo.lbretaappestudiantil.data.local.models.EventType

/**
 * TypeConverters para Room Database
 * Convierte enums a String y viceversa
 */
class Converters {

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
            EventType.OTHER // Valor por defecto si hay error
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


}