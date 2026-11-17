package com.example.taller_8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller_8.data.model.Personaje
import com.example.taller_8.data.repository.PersonajesRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface PersonajeDetalleUiState {
    object Loading : PersonajeDetalleUiState
    data class Success(val personaje: Personaje) : PersonajeDetalleUiState
    data class Error(val message: String) : PersonajeDetalleUiState
}

class PersonajeDetalleViewModel(
    private val personajeId: Int,
    private val repository: PersonajesRepository = PersonajesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PersonajeDetalleUiState>(PersonajeDetalleUiState.Loading)
    val uiState: StateFlow<PersonajeDetalleUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = PersonajeDetalleUiState.Loading
            repository.obtenerPersonaje(personajeId).onSuccess { personaje ->
                _uiState.value = PersonajeDetalleUiState.Success(personaje)
            }.onFailure { _ ->
                _uiState.value = PersonajeDetalleUiState.Error("No se pudo cargar el detalle. Reintenta.")
            }
        }
    }
}
