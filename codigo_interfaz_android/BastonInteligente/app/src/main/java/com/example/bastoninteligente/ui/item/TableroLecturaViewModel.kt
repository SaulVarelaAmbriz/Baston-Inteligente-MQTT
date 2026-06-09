package com.example.bastoninteligente.ui.item

import androidx.lifecycle.ViewModel
import com.example.bastoninteligente.data.network.mqtt.BastonUiState
import com.example.bastoninteligente.data.network.mqtt.MqttManager
import kotlinx.coroutines.flow.StateFlow

class TableroLecturaViewModel(private val mqttManager: MqttManager) : ViewModel() {

    // Exponemos el estado del bastón directamente a la UI (pantalla)
    // Compose se suscribirá a este flujo único
    val uiState: StateFlow<BastonUiState> = mqttManager.uiState

    init {
        // El bloque init se ejecuta automáticamente en cuanto nace el ViewModel.
        // Aquí encendemos el motor de comunicación en segundo plano.
        mqttManager.conectarYSubscribir()
    }

    /**
     * Cuando la aplicación se cierra o el usuario sale de esta pantalla,
     * el ViewModel se destruye y apaga la conexión MQTT automáticamente para no gastar datos.
     */
    override fun onCleared() {
        super.onCleared()
        mqttManager.desconectar()
    }
}