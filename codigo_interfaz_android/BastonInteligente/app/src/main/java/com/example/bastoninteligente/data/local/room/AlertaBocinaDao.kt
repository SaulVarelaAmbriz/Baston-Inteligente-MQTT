package com.example.bastoninteligente.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertaBocinaDao {

    @Insert
    suspend fun insertarAlertaSimple(alerta: AlertaBocina)

    // Query clave: Borra las alertas cuyo ID NO pertenezca a los 5 IDs más grandes (más nuevos)
    @Query("""
        DELETE FROM alertas_bocina 
        WHERE id NOT IN (
            SELECT id FROM alertas_bocina 
            ORDER BY id DESC 
            LIMIT 5
        )
    """)
    suspend fun eliminarAlertasExcedentes()

    // Une la inserción y la limpieza en un solo paso seguro
    @Transaction
    suspend fun insertarYLimpiar(alerta: AlertaBocina) {
        insertarAlertaSimple(alerta)
        eliminarAlertasExcedentes()
    }

    // Consulta para que la pantalla pinte las últimas 5 alertas en tiempo real
    @Query("SELECT * FROM alertas_bocina ORDER BY id DESC LIMIT 5")
    fun obtenerUltimas5Alertas(): Flow<List<AlertaBocina>>
}