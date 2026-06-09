package com.example.bastoninteligente.data.local.room

import kotlinx.coroutines.flow.Flow

interface AlertasBocinaRepository {
    suspend fun insertarYLimpiar(alerta: AlertaBocina)

    fun obtenerUltimas5Alertas(): Flow<List<AlertaBocina>>
}