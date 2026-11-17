package com.example.taller_8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller_8.data.model.Personaje
import com.example.taller_8.data.model.PersonajesResponse
import com.example.taller_8.data.repository.PersonajesRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface PersonajesUiState {
    object Loading : PersonajesUiState
    data class Success(val items: List<Personaje>) : PersonajesUiState
    data class Error(val message: String) : PersonajesUiState
}

class PersonajesViewModel(
    private val repository: PersonajesRepository = PersonajesRepository()
) : ViewModel() {

    constructor() : this(PersonajesRepository())

    private val _uiState = MutableStateFlow<PersonajesUiState>(PersonajesUiState.Loading)
    val uiState: StateFlow<PersonajesUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = PersonajesUiState.Loading
            repository.obtenerPersonajes().onSuccess { response ->
                _uiState.value = PersonajesUiState.Success(response.results)
            }.onFailure { _ ->
                _uiState.value = PersonajesUiState.Error("No se pudo cargar la lista. Reintenta.")
            }
        }
    }
}
