package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.EventType
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.data.local.models.SyncStatus
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

    // Estado de la creación/edición del evento (Initial, Loading, Success, Error)
    private val _createState = MutableStateFlow<SchoolEventUiState>(SchoolEventUiState.Initial)
    val createState: StateFlow<SchoolEventUiState> = _createState.asStateFlow()



    private val _eventsByClass = MutableStateFlow<List<SchoolEventEntity>>(emptyList())
    val eventsByClass: StateFlow<List<SchoolEventEntity>> = _eventsByClass.asStateFlow()


    /**
     * Crea un nuevo evento escolar.
     * Incluye validaciones para evitar guardar datos vacíos o fechas erróneas.
     */
    fun createEvent(
        classId: Int?,
        teacherId: Int,
        title: String,
        description: String,
        eventDate: Long,
        eventType: EventType
    ) {
        // 1. Validaciones Previas (Protección)
        if (title.isBlank()) {
            _createState.value = SchoolEventUiState.Error("El título es obligatorio.")
            return
        }
        if (description.isBlank()) {
            _createState.value = SchoolEventUiState.Error("La descripción es obligatoria.")
            return
        }
        if (eventDate <= 0) {
            _createState.value = SchoolEventUiState.Error("La fecha seleccionada no es válida.")
            return
        }

        // 2. Iniciar proceso de guardado
        viewModelScope.launch {
            _createState.value = SchoolEventUiState.Loading

            try {
                val event = SchoolEventEntity(
                    classId = classId,
                    teacherId = teacherId,
                    title = title.trim(), // Quitamos espacios extra
                    description = description.trim(),
                    eventDate = eventDate,
                    eventType = eventType,
                    syncStatus = SyncStatus.PENDING // Se marca para subir a la nube
                )

                val result = eventRepository.createEvent(event)

                _createState.value = when (result) {
                    is ApiResult.Success -> SchoolEventUiState.Success(result.data)
                    is ApiResult.Error -> SchoolEventUiState.Error(result.message)
                    ApiResult.Loading -> SchoolEventUiState.Loading
                }
            } catch (e: Exception) {
                _createState.value = SchoolEventUiState.Error("Error inesperado: ${e.message}")
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


}