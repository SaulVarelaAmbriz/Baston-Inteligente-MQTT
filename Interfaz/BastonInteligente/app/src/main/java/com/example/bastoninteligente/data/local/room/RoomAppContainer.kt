/*
OBJETIVO:
Contenedor de App para inyecciones de dependencias

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.data.local.room

import android.content.Context

/**
 * Contenedor de App para inyecciones de dependencias
 */
interface RoomAppContainer {
    val alertasBocinaRepository: AlertasBocinaRepository
}

class RoomAppDataContainer(private val context: Context) : RoomAppContainer {
    override val alertasBocinaRepository: AlertasBocinaRepository by lazy {
        OfflineAlertasBocinaRepository(BastonInteligenteDatabase.getDatabase(context).alertaBocinaDao())
    }
}
