package com.oriundo.lbretaappestudiantil.ui.theme.states

import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass

data class ParentDashboardState(
    val students: List<StudentWithClass> = emptyList(),
    val unreadAnnotations: List<AnnotationEntity> = emptyList(),
    val unreadMessagesCount: Int = 0,
    val upcomingEvents: List<SchoolEventEntity> = emptyList(),
    val isLoading: Boolean = false,
)