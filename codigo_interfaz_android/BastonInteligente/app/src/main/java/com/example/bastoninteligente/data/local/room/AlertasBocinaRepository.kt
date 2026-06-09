/*
OBJETIVO:
Contrato del repositorio de la base de datos Room para persistencia

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.data.local.room

import kotlinx.coroutines.flow.Flow

interface AlertasBocinaRepository {
    suspend fun insertarYLimpiar(alerta: AlertaBocina)

    fun obtenerUltimas5Alertas(): Flow<List<AlertaBocina>>
}