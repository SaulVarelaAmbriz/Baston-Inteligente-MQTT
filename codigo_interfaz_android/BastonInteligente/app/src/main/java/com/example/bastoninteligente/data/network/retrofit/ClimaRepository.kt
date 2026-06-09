package com.example.bastoninteligente.data.network.retrofit

interface ClimaRepository {
    suspend fun obtenerClima(latitud: Double, longitud: Double): Clima
}

class NetworkClimaRepository(
    private val climaApiService: ClimaApiService
) : ClimaRepository {
    override suspend fun obtenerClima(latitud: Double, longitud: Double): Clima = climaApiService.obtenerClima(latitud, longitud).comoClima()
}