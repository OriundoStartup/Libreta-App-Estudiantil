package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.EventType
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.SchoolEventUiState

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SchoolEventViewModel @Inject constructor(
    private val eventRepository: SchoolEventRepository
) : ViewModel() {

    private val _createState = MutableStateFlow<SchoolEventUiState>(SchoolEventUiState.Initial)
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
            _createState.value = SchoolEventUiState.Loading

            val event = SchoolEventEntity(
                classId = classId,
                teacherId = teacherId,
                title = title,
                description = description,
                eventDate = eventDate,
                eventType = eventType
            )

            val result = eventRepository.createEvent(event)

            // ✅ CORRECCIÓN FINAL
            _createState.value = when (result) {
                // Instanciar SchoolEventUiState.Success con los datos.
                is ApiResult.Success -> SchoolEventUiState.Success(
                    result.data // Hacemos la conversión de tipo segura
                )
                is ApiResult.Error -> SchoolEventUiState.Error(result.message)
                ApiResult.Loading -> SchoolEventUiState.Loading
            }
        }
    }

    fun updateEvent(event: SchoolEventEntity) {
        viewModelScope.launch {
            _createState.value = SchoolEventUiState.Loading

            val result = eventRepository.updateEvent(event)

            // ✅ CORRECCIÓN FINAL
            _createState.value = when (result) {
                // Instanciar SchoolEventUiState.Success con los datos.
                is ApiResult.Success -> SchoolEventUiState.Success(
                    result.data // Hacemos la conversión de tipo segura
                )
                is ApiResult.Error -> SchoolEventUiState.Error(result.message)
                ApiResult.Loading -> SchoolEventUiState.Loading
            }
        }
    }

    fun deleteEvent(event: SchoolEventEntity) {
        viewModelScope.launch {
            _createState.value = SchoolEventUiState.Loading

            val result = eventRepository.deleteEvent(event)

            // ✅ CORRECCIÓN FINAL (Error y Loading ya estaban bien)
            _createState.value = when (result) {
                is ApiResult.Success -> SchoolEventUiState.Initial
                is ApiResult.Error -> SchoolEventUiState.Error(result.message)
                ApiResult.Loading -> SchoolEventUiState.Loading
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
        _createState.value = SchoolEventUiState.Initial
    }

    fun clearEvents() {
        _eventsByTeacher.value = emptyList()
        _eventsByClass.value = emptyList()
        _eventsByDateRange.value = emptyList()
    }
}