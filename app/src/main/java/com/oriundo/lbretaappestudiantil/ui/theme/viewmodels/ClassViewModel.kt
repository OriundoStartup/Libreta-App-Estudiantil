package com.oriundo.lbretaappestudiantil.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.domain.model.ApiResult
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassUiState.Error
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassUiState.Initial
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassUiState.Loading
import com.oriundo.lbretaappestudiantil.ui.theme.viewmodels.ClassUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClassUiState {
    object Initial : ClassUiState()
    object Loading : ClassUiState()
    data class Success(val classEntity: ClassEntity, val code: String) : ClassUiState()
    data class Error(val message: String) : ClassUiState()
}

@HiltViewModel
class ClassViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _createState = MutableStateFlow<ClassUiState>(Initial)
    val createState: StateFlow<ClassUiState> = _createState.asStateFlow()

    private val _teacherClasses = MutableStateFlow<List<ClassEntity>>(emptyList())
    val teacherClasses: StateFlow<List<ClassEntity>> = _teacherClasses.asStateFlow()

    // ✅ Agregamos un estado de carga para los cursos del profesor
    private val _isClassesLoading = MutableStateFlow(false)
    val isClassesLoading: StateFlow<Boolean> = _isClassesLoading.asStateFlow()

    fun createClass(
        teacherId: Int,
        className: String,
        schoolName: String,
        gradeLevel: String,
        academicYear: String
    ) {
        viewModelScope.launch {
            _createState.value = Loading

            val result = classRepository.createClass(
                className = className,
                schoolName = schoolName,
                teacherId = teacherId,
                gradeLevel = gradeLevel,
                academicYear = academicYear.toIntOrNull() ?: 2025
            )

            when (result) {
                is ApiResult.Success -> {
                    _createState.value = Success(result.data.first, result.data.second)
                    // ✅ ¡IMPORTANTE! Actualizar la lista después de la creación
                    loadTeacherClasses(teacherId)
                }
                is ApiResult.Error -> {
                    _createState.value = Error(result.message)
                }

                // ✅ Eliminamos el TODO()
                ApiResult.Loading -> _createState.value = Loading
            }
        }
    }
    // En ClassViewModel.kt

// ... (después de la función createClass)

    // ✅ NUEVA FUNCIÓN PARA SINCRONIZAR Y CARGAR
    fun syncAndLoadTeacherClasses(teacherId: Int, firebaseUid: String?) {
        viewModelScope.launch {
            _isClassesLoading.value = true

            // El teacherId es el localProfileId (ID de Room)
            val syncResult = classRepository.syncTeacherClassesAndStudents(firebaseUid, teacherId)

            when (syncResult) {
                is ApiResult.Success -> {
                    // Sincronización exitosa, cargar los datos actualizados de Room
                    // Reutilizar la lógica de carga existente:
                    classRepository.getClassesByTeacher(teacherId).collect { classes ->
                        _teacherClasses.value = classes
                        _isClassesLoading.value = false
                    }
                }
                is ApiResult.Error -> {
                    // Sincronización fallida, aun así cargar los datos locales (antiguos)
                    // y mostrar un error de sincronización si es necesario.
                    classRepository.getClassesByTeacher(teacherId).collect { classes ->
                        _teacherClasses.value = classes
                    }
                    _isClassesLoading.value = false
                    // TO-DO: Considerar cómo notificar este error (e.g., Toast) a la UI.
                }
                ApiResult.Loading -> { } // Manejo del estado no relevante aquí
            }
        }
    }

// ... (loadTeacherClasses y resetState existentes)

    fun loadTeacherClasses(teacherId: Int) {
        viewModelScope.launch {
            _isClassesLoading.value = true
            // Si ClassRepository.getClassesByTeacher devuelve un Flow, este es el patrón correcto:
            classRepository.getClassesByTeacher(teacherId).collect { classes ->
                _teacherClasses.value = classes
                _isClassesLoading.value = false
            }
            // NOTA: Si getClassesByTeacher no devuelve un Flow, necesitarás manejar errores aquí.
        }
    }

    fun resetState() {
        _createState.value = Initial
    }
}