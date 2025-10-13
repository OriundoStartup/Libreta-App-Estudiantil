package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

// Se elimina la importación de android.app.Application y LibretAppDatabase
import androidx.lifecycle.ViewModel // 1. Cambiamos a la clase base ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.RelationshipType
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
// Se elimina la importación de RepositoryProvider
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.StudentWithClass
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository // 2. Importamos la interfaz del Repository
import dagger.hilt.android.lifecycle.HiltViewModel // 3. Importación clave de Hilt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // 4. Importación para Inyección


sealed class StudentUiState {
    object Initial : StudentUiState()
    object Loading : StudentUiState()
    data class Success(val students: List<StudentEntity>) : StudentUiState()
    data class StudentCreated(val student: StudentEntity) : StudentUiState()
    data class Error(val message: String) : StudentUiState()
}

@HiltViewModel // 5. Anotación para que Hilt sepa cómo construir
class StudentViewModel @Inject constructor( // 6. Inyección del Repository
    private val studentRepository: StudentRepository // Se inyecta la interfaz
) : ViewModel() { // 7. Heredamos de ViewModel

    // ELIMINADA: private val studentRepository = RepositoryProvider.provideStudentDao(database = LibretAppDatabase.getDatabase(application))

    private val _uiState = MutableStateFlow<StudentUiState>(StudentUiState.Initial)
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private val _studentsByClass = MutableStateFlow<List<StudentEntity>>(emptyList())
    val studentsByClass: StateFlow<List<StudentEntity>> = _studentsByClass.asStateFlow()

    private val _studentsByParent = MutableStateFlow<List<StudentWithClass>>(emptyList())
    val studentsByParent: StateFlow<List<StudentWithClass>> = _studentsByParent.asStateFlow()

    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

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
            // El tipo de retorno ya es Flow<List<StudentWithClass>>, no se requiere casting.
            studentRepository.getStudentsByParent(parentId).collect {
                _studentsByParent.value = it
            }
        }
    }

    // ELIMINADA: private fun Unit.collect(function: Any) {} (Código incorrecto)


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

            // CORRECCIÓN: Se accede a result.data y se elimina el casting innecesario
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
                // CORRECCIÓN: Se accede a result.data y se elimina el casting y el wildcard <*>.
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