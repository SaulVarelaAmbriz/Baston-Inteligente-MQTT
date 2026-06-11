/*
OBJETIVO:
Implementación del repositorio de la base de datos Room
que permitirá tener las 5 alertas más recientes de la bocina

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
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
