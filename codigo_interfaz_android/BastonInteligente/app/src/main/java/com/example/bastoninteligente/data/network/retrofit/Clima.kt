package com.example.bastoninteligente.data.network.retrofit

data class Clima(
    val temperatura: Double,
    val codigoClima: Int,
    val esDeDia: Int,
    val velocidadViento: Double,
    val direccionViento: Int,
    val tiempo: String
)

fun Clima.obtenerDescripcionClima(): String {
    return when (codigoClima) {
        0 -> "Cielo despejado"
        1 -> "Principalmente despejado"
        2 -> "Parcialmente nublado"
        3 -> "Nublado"
        45, 48 -> "Niebla afuera"
        51, 53, 55 -> "Llovizna leve"
        61, 63, 65 -> "Lluvia"
        71, 73, 75 -> "Nevada"
        95, 96, 99 -> "Tormenta eléctrica"
        else -> "Condición desconocida"
    }
}

fun Clima.obtenerEstadoDia(): String {
    return if (esDeDia == 1) "De día" else "De noche"
}

fun Clima.obtenerDireccionViento(): String {
    return when (direccionViento) {
        in 338..360, in 0..22 -> "Norte"
        in 23..67 -> "Noreste"
        in 68..112 -> "Este"
        in 113..157 -> "Sureste"
        in 158..202 -> "Sur"
        in 203..247 -> "Suroeste"
        in 248..292 -> "Oeste"
        in 293..337 -> "Noroeste"
        else -> "Dirección variable"
    }
}