package com.example.taller_8.data.model

import com.google.gson.annotations.SerializedName

/**
 * Personaje de Rick & Morty.
 *
 * Nota: Gson asigna null a los campos ausentes del JSON aunque Kotlin los declare
 * no nulos, por eso no se debe asumir que Gson rellena cadenas vacías.
 * El campo JSON se llama "image"; se conserva ese nombre con @SerializedName.
 */
data class Personaje(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    @SerializedName("image") val image: String
)
