package com.example.taller_8.data.model

/**
 * Respuesta de la lista de personajes de Rick & Morty.
 *
 * El JSON envuelve los resultados en la clave "results", igual que PokéAPI,
 * pero los objetos dentro de results son personajes (id, name, status, species, image)
 * en lugar de objetos de Pokémon.
 */
data class PersonajesResponse(
    val results: List<Personaje>
)
