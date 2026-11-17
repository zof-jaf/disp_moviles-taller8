package com.example.taller_8.data.repository

import com.example.taller_8.data.model.Personaje
import com.example.taller_8.data.model.PersonajesResponse
import com.example.taller_8.data.network.RetrofitInstance
import com.example.taller_8.data.network.RickMortyApi
import kotlin.Result
import kotlinx.coroutines.CancellationException

class PersonajesRepository(private val api: RickMortyApi = RetrofitInstance.api) {

    suspend fun obtenerPersonajes(): Result<PersonajesResponse> =
        try {
            Result.success(api.obtenerPersonajes())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun obtenerPersonaje(id: Int): Result<Personaje> =
        try {
            Result.success(api.obtenerPersonaje(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
