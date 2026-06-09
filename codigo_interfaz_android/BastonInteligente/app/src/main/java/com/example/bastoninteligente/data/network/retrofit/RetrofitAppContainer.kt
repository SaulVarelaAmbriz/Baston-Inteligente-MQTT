package com.example.bastoninteligente.data.network.retrofit

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface RetrofitAppContainer {
    val climaRepository: ClimaRepository
}

class RetrofitDefaultAppContainer : RetrofitAppContainer {
    // La URL base de Open-Meteo
    private val climaBaseUrl = "https://api.open-meteo.com/"

    // Configuramos el formateador de JSON para que ignore campos nuevos que no usemos
    private val jsonConfig = Json { ignoreUnknownKeys = true }

    val climaServicioRetrofit: ClimaApiService by lazy {
        Retrofit.Builder()
            // Añadimos el convertidor para que transforme el JSON usando Kotlinx Serialization
            .addConverterFactory(jsonConfig.asConverterFactory("application/json".toMediaType()))
            .baseUrl(climaBaseUrl)
            .build()
            .create(ClimaApiService::class.java)
    }

    override val climaRepository: ClimaRepository by lazy {
        NetworkClimaRepository(climaServicioRetrofit)
    }
}