/*
OBJETIVO:
Clase que representa la respuesta completa (raíz) que viene de la API Open-Meteo.

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.data.network.retrofit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. Esta clase representa la respuesta completa (raíz) que viene de Open-Meteo.
 */
@Serializable
data class ClimaApiRespuesta(
    @SerialName(value = "latitude")
    val latitud: Double,
    @SerialName(value = "longitude")
    val longitud: Double,
    @SerialName(value = "current_weather")
    val climaActual: ClimaActual // Aquí enlazamos con el objeto de abajo
)

/**
 * 2. Esta clase representa únicamente el objeto interno "current_weather".
 */
@Serializable
data class ClimaActual(
    @SerialName(value = "temperature")
    val temperatura: Double,
    @SerialName(value = "weathercode")
    val codigoClima: Int,
    @SerialName(value = "is_day")
    val esDeDia: Int,
    @SerialName(value = "windspeed")
    val velocidadViento: Double,
    @SerialName(value = "winddirection")
    val direccionViento: Int,
    @SerialName(value = "time")
    val tiempo: String
)

/**
 * Función de extensión que convierte la respuesta de la API
 * en el objeto Clima.
 */
fun ClimaApiRespuesta.comoClima(): Clima {
    return Clima(
        temperatura = this.climaActual.temperatura,
        codigoClima = this.climaActual.codigoClima,
        esDeDia = this.climaActual.esDeDia,
        velocidadViento = this.climaActual.velocidadViento,
        direccionViento = this.climaActual.direccionViento,
        tiempo = this.climaActual.tiempo
    )
}