package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import com.oriundo.lbretaappestudiantil.ui.theme.states.StudentUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentUiState>(StudentUiState.Initial)
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private val _studentsByClass = MutableStateFlow<List<StudentEntity>>(emptyList())
    val studentsByClass: StateFlow<List<StudentEntity>> = _studentsByClass.asStateFlow()

    private val _studentsByParent = MutableStateFlow<List<StudentWithClass>>(emptyList())
    val studentsByParent: StateFlow<List<StudentWithClass>> = _studentsByParent.asStateFlow()

    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    private val _allStudents = MutableStateFlow<List<StudentWithClass>>(emptyList())
    val allStudents: StateFlow<List<StudentWithClass>> = _allStudents.asStateFlow()

    // ⭐ NUEVO - Lista de estudiantes seleccionables (con apoderado válido)
    private val _selectableStudents = MutableStateFlow<List<StudentWithClass>>(emptyList())
    val selectableStudents: StateFlow<List<StudentWithClass>> = _selectableStudents.asStateFlow()

    // ⭐ NUEVO - Indicador si hay estudiantes sin apoderado
    private val _hasInvalidStudents = MutableStateFlow(false)
    val hasInvalidStudents: StateFlow<Boolean> = _hasInvalidStudents.asStateFlow()

    fun loadAllStudents() {
        viewModelScope.launch {
            studentRepository.getAllStudentsWithClass().collect { students ->
                _allStudents.value = students

                // ⭐ Filtrar estudiantes seleccionables
                val selectable = students.filter { student ->
                    student.student.primaryParentId != null &&
                            student.student.primaryParentId != 0
                }
                _selectableStudents.value = selectable

                // ⭐ Detectar si hay estudiantes inválidos
                _hasInvalidStudents.value = students.size > selectable.size
            }
        }
    }

    /**
     * ⭐ NUEVO - Filtrar estudiantes por búsqueda (solo entre los seleccionables)
     */
    fun getFilteredSelectableStudents(query: String): StateFlow<List<StudentWithClass>> {
        val filteredFlow = MutableStateFlow<List<StudentWithClass>>(emptyList())

        viewModelScope.launch {
            selectableStudents.map { students ->
                if (query.isBlank()) {
                    students
                } else {
                    students.filter { studentWithClass ->
                        studentWithClass.student.fullName.contains(query, ignoreCase = true) ||
                                studentWithClass.classEntity.className.contains(query, ignoreCase = true)
                    }
                }
            }.collect { filtered ->
                filteredFlow.value = filtered
            }
        }

        return filteredFlow.asStateFlow()
    }

    fun loadStudentsByClass(classId: Int) {
        viewModelScope.launch {
            _uiState.value = StudentUiState.Loading
            studentRepository.getStudentsByClass(classId).collect { students ->
                _studentsByClass.value = students
                _uiState.value = StudentUiState.Success(students)
            }
        }
    }

    fun loadStudentsByParent(parentId: Int) {
        viewModelScope.launch {
            studentRepository.getStudentsByParent(parentId).collect { apiResult ->
                when (apiResult) {
                    is ApiResult.Success -> {
                        _studentsByParent.value = apiResult.data
                    }
                    is ApiResult.Loading -> {
                        // Estado de carga si es necesario
                    }
                    is ApiResult.Error -> {
                        // Manejar error si es necesario
                    }
                }
            }
        }
    }

    fun registerStudent(
        classId: Int,
        rut: String,
        firstName: String,
        lastName: String,
        birthDate: Long? = null
    ) {
        viewModelScope.launch {
            _uiState.value = StudentUiState.Loading

            val result = studentRepository.registerStudent(
                classId = classId,
                rut = rut,
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate
            )

            _uiState.value = when (result) {
                is ApiResult.Success -> StudentUiState.StudentCreated(result.data)
                is ApiResult.Error -> StudentUiState.Error(result.message)
                ApiResult.Loading -> StudentUiState.Loading
            }
        }
    }

    fun loadStudentById(studentId: Int) {
        viewModelScope.launch {
            when (val result = studentRepository.getStudentById(studentId)) {
                is ApiResult.Success -> _selectedStudent.value = result.data
                is ApiResult.Error -> _selectedStudent.value = null
                ApiResult.Loading -> {}
            }
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            studentRepository.updateStudent(student)
        }
    }

    fun linkParentToStudent(
        studentId: Int,
        parentId: Int,
        relationshipType: RelationshipType,
        isPrimary: Boolean
    ) {
        viewModelScope.launch {
            studentRepository.linkParentToStudent(
                studentId = studentId,
                parentId = parentId,
                relationshipType = relationshipType,
                isPrimary = isPrimary
            )
        }
    }

    fun resetState() {
        _uiState.value = StudentUiState.Initial
    }
}