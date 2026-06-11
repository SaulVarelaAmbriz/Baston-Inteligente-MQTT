/*
OBJETIVO:
ViewModel de la vista de historial de alertas de la bocina que se conecta a
firestore y muestra los datos en tiempo real.
Además se encarga de mantener los datos en la base de datos Room para persistencia

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bastoninteligente.data.local.room.AlertaBocina
import com.example.bastoninteligente.data.local.room.AlertasBocinaRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistorialAlertasViewModel(private val repository: AlertasBocinaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<List<AlertaBocina>>(emptyList())
    val uiState: StateFlow<List<AlertaBocina>> = _uiState.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private var dbJob: Job? = null

    init {
        conectarHistorial()
    }

    private fun conectarHistorial() {
        dbJob?.cancel()

        // Escuchamos directamente FIREBASE en tiempo real para la UI
        firestore.collection("historial_desniveles")
            .orderBy("fecha_hora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                // CASO 1: Hubo un error crítico o Firebase detectó la desconexión total
                if (error != null) {
                    _isOffline.value = true
                    activarModoOfflineRoom()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // CASO 2:
                    // Si el snapshot viene del caché de Firebase significa que NO hay internet real.
                    if (snapshot.metadata.isFromCache) {
                        _isOffline.value = true
                        activarModoOfflineRoom() // Forzamos a que Room tome el control con sus 5 registros
                        return@addSnapshotListener
                    }

                    // CASO 3: Todo está perfecto y tenemos internet real
                    _isOffline.value = false

                    // Mapeamos los documentos de Firebase directamente a la lista de la UI
                    val listaFirebase = snapshot.documents.map { doc ->
                        AlertaBocina(
                            id = 0,
                            clasificacion = doc.getString("clasificacion") ?: "",
                            evento = doc.getString("evento") ?: "",
                            fecha_hora = doc.getString("fecha_hora") ?: "",
                            lectura = doc.getString("lectura") ?: ""
                        )
                    }

                    // Enviamos las alertas a la pantalla
                    _uiState.value = listaFirebase

                    // RESPALDO SEGURO: Tomamos solo las 5 más nuevas y las mandamos a Room
                    viewModelScope.launch {
                        val las5MasNuevas = listaFirebase.take(5)
                        for (alerta in las5MasNuevas) {
                            repository.insertarYLimpiar(alerta)
                        }
                    }
                }
            }
    }

    private fun activarModoOfflineRoom() {
        dbJob = viewModelScope.launch {
            // Cuando no hay internet, Room entra al rescate con sus únicos 5 registros
            repository.obtenerUltimas5Alertas().collect { listaRoom ->
                if (_isOffline.value) {
                    _uiState.value = listaRoom
                }
            }
        }
    }
}