package com.example.taller_8.data.network

import com.example.taller_8.data.model.Personaje
import com.example.taller_8.data.model.PersonajesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface RickMortyApi {

    @GET("character")
    suspend fun obtenerPersonajes(): PersonajesResponse

    @GET("character/{id}")
    suspend fun obtenerPersonaje(@Path("id") id: Int): Personaje
}
