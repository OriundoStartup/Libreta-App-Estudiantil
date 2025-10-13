package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
import com.oriundo.lbretaappestudiantil.data.local.models.RequestStatus
import com.oriundo.lbretaappestudiantil.data.local.models.UrgencyLevel
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.repositories.MaterialRequestRepository // 👈 CAMBIO AQUÍ
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MaterialRequestUiState {
    object Initial : MaterialRequestUiState()
    object Loading : MaterialRequestUiState()
    data class Success(val request: MaterialRequestEntity) : MaterialRequestUiState()
    data class Error(val message: String) : MaterialRequestUiState()
}

@HiltViewModel
class MaterialRequestViewModel @Inject constructor(
    private val materialRequestRepository: MaterialRequestRepository
) : ViewModel() {

    private val _createState = MutableStateFlow<MaterialRequestUiState>(MaterialRequestUiState.Initial)
    val createState: StateFlow<MaterialRequestUiState> = _createState.asStateFlow()

    private val _requestsByClass = MutableStateFlow<List<MaterialRequestEntity>>(emptyList())
    val requestsByClass: StateFlow<List<MaterialRequestEntity>> = _requestsByClass.asStateFlow()

    private val _requestsByStudent = MutableStateFlow<List<MaterialRequestEntity>>(emptyList())
    val requestsByStudent: StateFlow<List<MaterialRequestEntity>> = _requestsByStudent.asStateFlow()

    private val _requestsForParent = MutableStateFlow<List<MaterialRequestEntity>>(emptyList())
    val requestsForParent: StateFlow<List<MaterialRequestEntity>> = _requestsForParent.asStateFlow()

    fun createRequest(
        teacherId: Int,
        classId: Int,
        studentId: Int?,
        material: String,
        quantity: Int,
        urgency: UrgencyLevel,
        deadlineDate: Long?
    ) {
        viewModelScope.launch {
            _createState.value = MaterialRequestUiState.Loading

            val request = MaterialRequestEntity(
                teacherId = teacherId,
                classId = classId,
                studentId = studentId,
                material = material,
                quantity = quantity,
                urgency = urgency,
                deadlineDate = deadlineDate,
                status = RequestStatus.PENDING
            )

            val result = materialRequestRepository.createRequest(request)

            _createState.value = when (result) {
                is ApiResult.Success -> MaterialRequestUiState.Success(request)
                is ApiResult.Error -> MaterialRequestUiState.Error(result.message)
                ApiResult.Loading -> MaterialRequestUiState.Loading
            }
        }
    }

    fun loadRequestsByClass(classId: Int) {
        viewModelScope.launch {
            materialRequestRepository.getRequestsByClass(classId).collect { requests ->
                _requestsByClass.value = requests
            }
        }
    }

    fun loadRequestsByStudent(studentId: Int) {
        viewModelScope.launch {
            materialRequestRepository.getRequestsByStudent(studentId).collect { requests ->
                _requestsByStudent.value = requests
            }
        }
    }

    fun loadRequestsForParent(parentId: Int) {
        viewModelScope.launch {
            // Nota: Este método no existe en la interfaz MaterialRequestRepository
            // Necesitarás agregarlo o usar otro método
            // materialRequestRepository.getRequestsForParent(parentId).collect { requests ->
            //     _requestsForParent.value = requests
            // }
        }
    }

    fun updateRequestStatus(requestId: Int, status: RequestStatus) {
        viewModelScope.launch {
            materialRequestRepository.updateRequestStatus(requestId, status)
        }
    }

    fun resetCreateState() {
        _createState.value = MaterialRequestUiState.Initial
    }
}