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
