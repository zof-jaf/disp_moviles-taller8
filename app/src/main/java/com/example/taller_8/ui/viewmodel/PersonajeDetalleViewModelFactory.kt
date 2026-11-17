package com.example.taller_8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taller_8.data.repository.PersonajesRepository

class PersonajeDetalleViewModelFactory(
    private val personajeId: Int,
    private val repository: PersonajesRepository = PersonajesRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PersonajeDetalleViewModel(personajeId, repository) as T
    }
}
