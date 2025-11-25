package com.oriundo.lbretaappestudiantil.data.local.converters

import androidx.room.TypeConverter
import com.oriundo.lbretaappestudiantil.data.local.models.JustificationStatus
import com.oriundo.lbretaappestudiantil.data.local.models.SyncStatus
import com.oriundo.lbretaappestudiantil.domain.model.AbsenceReason

class Converters {
    @TypeConverter
    fun fromAbsenceReason(value: AbsenceReason?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAbsenceReason(value: String?): AbsenceReason? {
        return value?.let { AbsenceReason.valueOf(it) }
    }

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? {
        return value?.let { SyncStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromJustificationStatus(value: JustificationStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toJustificationStatus(value: String?): JustificationStatus? {
        return value?.let { JustificationStatus.valueOf(it) }
    }
}
