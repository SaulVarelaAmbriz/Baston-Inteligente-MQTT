/*
OBJETIVO:
ViewModel para la vista de GPS que consulta los datos del bastón
mediante mqtt y los convierte a una dirección legible para el usuario

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui.item

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bastoninteligente.data.network.mqtt.BastonUiState
import com.example.bastoninteligente.data.network.mqtt.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GpsCamaraViewModel(
    private val mqttManager: MqttManager,
    private val geocoder: Geocoder
) : ViewModel() {

    // Exponemos el estado del bastón directamente a la UI (pantalla)
    // Compose se suscribirá a este flujo único
    val uiState: StateFlow<BastonUiState> = mqttManager.uiState

    init {
        // El bloque init se ejecuta automáticamente en cuanto nace el ViewModel.
        // Aquí encendemos el motor de comunicación en segundo plano.
        mqttManager.conectarYSubscribir()

        // Escuchamos los cambios de coordenadas automáticamente,
        // se enciende un "escuchador" permanente, es decir,
        // 'direccionTexto' de 'BastonUiState' se actualizará automáticamente cada vez que cambien la latitud y la longitud.
        observarCambiosDeUbicacion()
    }

    /**
     * Cuando la aplicación se cierra o el usuario sale de esta pantalla,
     * el ViewModel se destruye y apaga la conexión MQTT automáticamente para no gastar datos.
     */
    override fun onCleared() {
        super.onCleared()
        mqttManager.desconectar()
    }

    private fun observarCambiosDeUbicacion() {
        viewModelScope.launch {
            uiState
                // Extraemos solo la latitud y longitud en un par
                .map { Pair(it.latitud, it.longitud) }
                // Evitamos procesar si las coordenadas son exactamente las mismas que las anteriores
                .distinctUntilChanged()
                .collect { (latitud, longitud) ->
                    if (latitud != 0.0 && longitud != 0.0) {
                        // Buscamos la dirección en un hilo secundario (IO)
                        val direccion = obtenerDireccionDesdeCoordenadas(latitud, longitud)

                        // Le notificamos al MqttManager que actualice el estado con la nueva dirección
                        mqttManager.actualizarDireccionTexto(direccion)
                    }
                }
        }
    }

    // Función suspendida que no bloquea la interfaz de usuario
    private suspend fun obtenerDireccionDesdeCoordenadas(latitud: Double, longitud: Double): String {
        return withContext(Dispatchers.IO) { // Mueve el trabajo a un hilo de red/sistema
            try {
                val direcciones = geocoder.getFromLocation(latitud, longitud, 1)
                if (!direcciones.isNullOrEmpty()) {
                    direcciones[0].getAddressLine(0) ?: "Dirección no disponible"
                } else {
                    "Sin dirección encontrada"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Error al obtener dirección"
            }
        }
    }

}