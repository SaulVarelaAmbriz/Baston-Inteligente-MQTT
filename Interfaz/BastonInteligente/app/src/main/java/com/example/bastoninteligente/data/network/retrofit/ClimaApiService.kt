/*
OBJETIVO:
Interfaz con los Endpoints de la API

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.data.network.retrofit

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Definición de la interfaz con los Endpoints de la API
 */
interface ClimaApiService {

    // Le decimos que haga una petición GET al sub-enlace "v1/forecast"
    @GET("v1/forecast")
    suspend fun obtenerClima(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        // Dejamos este parámetro fijo en true porque siempre queremos el clima actual
        @Query("current_weather") climaActual: Boolean = true
    ): ClimaApiRespuesta // Aquí usamos tu clase puente de red
}