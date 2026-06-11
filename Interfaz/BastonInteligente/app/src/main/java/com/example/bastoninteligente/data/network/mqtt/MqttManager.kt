/*
OBJETIVO:
Clas que permite:
La conexión al bróker público de EMQX
La suscripción a los tópicos de telemetría
El envío de instrucciones a los actuadores
La desconexión del cliente

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.data.network.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.charset.StandardCharsets
import kotlin.arrayOf

class MqttManager {

    private lateinit var mqttClient: Mqtt3AsyncClient
    private val brokerHost = "broker.emqx.io"

    private val topicosSuscripciones = arrayOf(
        "baston_equipo_7718/sensores/ultrasonico",
        "baston_equipo_7718/sensores/pir",
        "baston_equipo_7718/actuadores/zumbador",
        "baston_equipo_7718/actuadores/vibracion",
        "baston_equipo_7718/actuadores/bocina",
        "baston_equipo_7718/actuadores/camara",
        "baston_equipo_7718/gps/latitud",
        "baston_equipo_7718/gps/longitud"
    )

    private val _uiState = MutableStateFlow(BastonUiState())
    val uiState: StateFlow<BastonUiState> = _uiState.asStateFlow()

    fun conectarYSubscribir() {
        mqttClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier("Android_Baston_App_7718")
            .serverHost(brokerHost)
            .serverPort(1883)
            .buildAsync()

        mqttClient.connectWith()
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    Log.e(
                        "MQTT",
                        "❌ Error al conectar al servidor en la nube: ${throwable.message}"
                    )
                } else {
                    Log.d("MQTT", "✅ Conectado exitosamente al bróker público de EMQX")
                    suscribirseATelemetria()
                }
            }
    }

    private fun suscribirseATelemetria() {
        // Recorremos nuestro arreglo de tópicos uno por uno
        for (topico in topicosSuscripciones) {

            mqttClient.subscribeWith()
                .topicFilter(topico) // Asignamos el tópico actual del bucle
                .callback { publish: Mqtt3Publish -> // Registramos el "oído" para este canal
                    val payload = StandardCharsets.UTF_8.decode(publish.payload.get()).toString()
                    val topicoOrigen = publish.topic.toString()

                    Log.d("MQTT", "📥 Recibido en [$topicoOrigen]: $payload")

                    // Modificamos el estado (data class) usando .copy()
                    _uiState.update { estadoActual ->
                        when (topicoOrigen) {
                            "baston_equipo_7718/sensores/ultrasonico" -> estadoActual.copy(
                                ultrasonico = payload.toIntOrNull() ?: 0
                            )

                            "baston_equipo_7718/sensores/pir" -> estadoActual.copy(
                                pir = payload.toIntOrNull() ?: 0
                            )

                            "baston_equipo_7718/actuadores/zumbador" -> estadoActual.copy(
                                zumbador = payload.toIntOrNull() ?: 0
                            )

                            "baston_equipo_7718/actuadores/vibracion" -> estadoActual.copy(
                                vibracion = payload.toIntOrNull() ?: 0
                            )

                            "baston_equipo_7718/actuadores/bocina" -> estadoActual.copy(bocina = payload)

                            "baston_equipo_7718/actuadores/camara" -> estadoActual.copy(
                                camara = payload.toIntOrNull() ?: 0
                            )

                            "baston_equipo_7718/gps/latitud" -> estadoActual.copy(
                                latitud = payload.toDoubleOrNull() ?: 0.0
                            )

                            "baston_equipo_7718/gps/longitud" -> estadoActual.copy(
                                longitud = payload.toDoubleOrNull() ?: 0.0
                            )

                            else -> estadoActual
                        }
                    }
                }
                .send() // Mandamos la orden al servidor EMQX
        }
    }

    fun encenderZumbador() {
        mqttClient.publishWith()
            .topic("baston_equipo_7718/actuadores/zumbador")
            .payload(StandardCharsets.UTF_8.encode("1"))
            .send()
    }

    fun enviarMensajeBocina() {
        mqttClient.publishWith()
            .topic("baston_equipo_7718/actuadores/bocina")
            .payload(StandardCharsets.UTF_8.encode("1"))
            .send()
    }

    fun actualizarDireccionTexto(direccion: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                direccionTexto = direccion
            )
        }
    }

    fun desconectar() {
        if (::mqttClient.isInitialized) {
            Log.d("MQTT", "✅ Desconectado exitosamente del bróker público de EMQX")
            mqttClient.disconnect()
        }
    }

}

data class BastonUiState(
    val ultrasonico: Int = 0,
    val pir: Int = 0,
    val zumbador: Int = 0,
    val vibracion: Int = 0,
    val bocina: String = "Sin mensaje",
    val camara: Int = 0,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val direccionTexto: String = "Cargando ubicación..."
)