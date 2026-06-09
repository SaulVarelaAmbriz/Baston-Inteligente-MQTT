package com.example.bastoninteligente.data.local.room

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OfflineAlertasBocinaRepository(private val alertaBocinaDao: AlertaBocinaDao) : AlertasBocinaRepository {

    override suspend fun insertarYLimpiar(alerta: AlertaBocina) = alertaBocinaDao.insertarYLimpiar(alerta)

    override fun obtenerUltimas5Alertas(): Flow<List<AlertaBocina>> = alertaBocinaDao.obtenerUltimas5Alertas()
}
