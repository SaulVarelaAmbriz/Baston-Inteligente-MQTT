/*
OBJETIVO:
ViewModel de la vista de tablero interactivo que se encarga de
comunicarse con el bastón mediante mqtt y enviar ordenes

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
import com.example.bastoninteligente.data.network.mqtt.MqttManager
import kotlinx.coroutines.launch

class TableroInteractivoViewModel(private val mqttManager: MqttManager) : ViewModel() {

    init {
        // El bloque init se ejecuta automáticamente en cuanto nace el ViewModel.
        // Aquí encendemos el motor de comunicación en segundo plano.
        mqttManager.conectarYSubscribir()
    }

    /**
     * Función para que la pantalla le ordene al bastón encender el zumbador
     */
    fun encenderZumbador() {
        // Usamos el scope del ViewModel para lanzar la tarea de red de forma segura
        viewModelScope.launch {
            mqttManager.encenderZumbador()
        }
    }

    /**
     * Función para que la pantalla le mande un mensaje a la bocina
     */
    fun enviarMensajeBocina() {
        viewModelScope.launch {
            mqttManager.enviarMensajeBocina()
        }
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