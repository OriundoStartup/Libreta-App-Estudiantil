package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.EventType
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
// ✅ IMPORTACIÓN CORREGIDA
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventUiState.Error
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventUiState.Initial
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventUiState.Loading
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.SchoolEventUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SchoolEventUiState {
    object Initial : SchoolEventUiState()
    object Loading : SchoolEventUiState()
    data class Success(val event: SchoolEventEntity) : SchoolEventUiState()
    data class Error(val message: String) : SchoolEventUiState()
}

@HiltViewModel
class SchoolEventViewModel @Inject constructor(
    private val eventRepository: SchoolEventRepository
) : ViewModel() {

    private val _createState = MutableStateFlow<SchoolEventUiState>(Initial)
    val createState: StateFlow<SchoolEventUiState> = _createState.asStateFlow()

    private val _eventsByTeacher = MutableStateFlow<List<SchoolEventEntity>>(emptyList())
    val eventsByTeacher: StateFlow<List<SchoolEventEntity>> = _eventsByTeacher.asStateFlow()

    private val _eventsByClass = MutableStateFlow<List<SchoolEventEntity>>(emptyList())
    val eventsByClass: StateFlow<List<SchoolEventEntity>> = _eventsByClass.asStateFlow()

    private val _eventsByDateRange = MutableStateFlow<List<SchoolEventEntity>>(emptyList())
    val eventsByDateRange: StateFlow<List<SchoolEventEntity>> = _eventsByDateRange.asStateFlow()

    fun createEvent(
        classId: Int?,
        teacherId: Int,
        title: String,
        description: String,
        eventDate: Long,
        eventType: EventType
    ) {
        viewModelScope.launch {
            _createState.value = Loading

            val event = SchoolEventEntity(
                classId = classId,
                teacherId = teacherId,
                title = title,
                description = description,
                eventDate = eventDate,
                eventType = eventType
            )

            val result = eventRepository.createEvent(event)

            _createState.value = when (result) {
                is ApiResult.Success -> Success(result.data) // Usar result.data
                is ApiResult.Error -> Error(result.message)
                ApiResult.Loading -> Loading
            }
        }
    }

    fun updateEvent(event: SchoolEventEntity) {
        viewModelScope.launch {
            _createState.value = Loading

            val result = eventRepository.updateEvent(event)

            _createState.value = when (result) {
                is ApiResult.Success -> Success(result.data) // Usar result.data
                is ApiResult.Error -> Error(result.message)
                ApiResult.Loading -> Loading
            }
        }
    }

    fun deleteEvent(event: SchoolEventEntity) {
        viewModelScope.launch {
            _createState.value = Loading

            val result = eventRepository.deleteEvent(event)

            _createState.value = when (result) {
                is ApiResult.Success -> Initial
                is ApiResult.Error -> Error(result.message)
                ApiResult.Loading -> Loading
            }
        }
    }

    fun loadEventsByTeacher(teacherId: Int) {
        viewModelScope.launch {
            eventRepository.getEventsByTeacher(teacherId).collect { events ->
                _eventsByTeacher.value = events
            }
        }
    }

    fun loadEventsByClass(classId: Int) {
        viewModelScope.launch {
            eventRepository.getEventsByClass(classId).collect { events ->
                _eventsByClass.value = events
            }
        }
    }

    fun loadEventsByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            eventRepository.getEventsByDateRange(startDate, endDate).collect { events ->
                _eventsByDateRange.value = events
            }
        }
    }

    fun loadEventsByTeacherAndDateRange(teacherId: Int, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            eventRepository.getEventsByTeacherAndDateRange(
                teacherId = teacherId,
                startDate = startDate,
                endDate = endDate
            ).collect { events ->
                _eventsByDateRange.value = events
            }
        }
    }

    fun resetCreateState() {
        _createState.value = Initial
    }

    fun clearEvents() {
        _eventsByTeacher.value = emptyList()
        _eventsByClass.value = emptyList()
        _eventsByDateRange.value = emptyList()
    }
}