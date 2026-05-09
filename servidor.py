
#OBJETIVO:
#Implementar un servidor MQTT en Python encargado
#de recibir la telemetría enviada por la ESP32 del
#proyecto Bastón Inteligente, mostrando información
#con timestamps y permitiendo el envío de comandos
#remotos hacia los actuadores del sistema.

#INTEGRANTES:
#Ramirez Abundiz Berenice 22240234
#Rivera Ponce David Eduardo 22240226
#Varela Ambriz Saul 22240256

#PROYECTO:
#Bastón Inteligente


# =========================================================
# IMPORTACIÓN DE LIBRERÍAS
# =========================================================

import paho.mqtt.client as mqtt
from datetime import datetime
import time

# =========================================================
# CONFIGURACIÓN MQTT
# =========================================================

# IMPORTANTE:
# Cambiar esta IP por la IP de la computadora
# donde se encuentra instalado Mosquitto.
BROKER = "localhost"

PUERTO = 1883

# =========================================================
# TÓPICOS MQTT
# =========================================================

# Sensores
TOPICO_ULTRASONICO = "baston/sensores/ultrasonico"
TOPICO_PIR = "baston/sensores/pir"
TOPICO_CONTROL = "baston/sensores/control"

# Actuadores
TOPICO_ZUMBADOR = "baston/actuadores/zumbador"
TOPICO_VIBRACION = "baston/actuadores/vibracion"
TOPICO_BOCINA = "baston/actuadores/bocina"

# =========================================================
# CALLBACK CONEXIÓN MQTT
# =========================================================

def al_conectar(cliente, userdata, flags, rc):
    """
    Callback ejecutado automáticamente cuando
    el cliente logra conectarse al broker MQTT.
    """

    print("====================================")
    print("CONECTADO AL BROKER MQTT")
    print("Código de conexión:", rc)
    print("====================================")

    # ================================================
    # SUSCRIPCIONES
    # ================================================

    cliente.subscribe(TOPICO_ULTRASONICO)
    cliente.subscribe(TOPICO_PIR)
    cliente.subscribe(TOPICO_CONTROL)

    print("Suscripciones completadas")


# =========================================================
# CALLBACK RECEPCIÓN MENSAJES
# =========================================================

def al_recibir_mensaje(cliente, userdata, mensaje):
    """
    Callback ejecutado automáticamente cuando
    llega telemetría desde la ESP32.
    """

    # Timestamp actual
    timestamp = datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"
    )

    topico = mensaje.topic
    contenido = mensaje.payload.decode()

    print("====================================")
    print("TIMESTAMP:", timestamp)
    print("TOPICO:", topico)
    print("MENSAJE:", contenido)
    print("====================================")


# =========================================================
# CREACIÓN CLIENTE MQTT
# =========================================================

cliente = mqtt.Client()

# Registro de callbacks
cliente.on_connect = al_conectar
cliente.on_message = al_recibir_mensaje

# =========================================================
# CONEXIÓN AL BROKER
# =========================================================

print("Conectando al broker MQTT...")

cliente.connect(BROKER, PUERTO, 60)

# =========================================================
# LOOP MQTT
# =========================================================

cliente.loop_start()

print("Servidor MQTT iniciado")

# =========================================================
# ENVÍO DE COMANDOS
# =========================================================

while True:

    print("\n=========== MENÚ MQTT ===========")
    print("1 -> Activar Zumbador")
    print("2 -> Activar Vibración")
    print("3 -> Activar Bocina")
    print("4 -> Salir")
    print("=================================")

    opcion = input("Seleccione una opción: ")

    # ================================================
    # ZUMBADOR
    # ================================================

    if opcion == "1":

        cliente.publish(
            TOPICO_ZUMBADOR,
            "ON"
        )

        print("Comando enviado al zumbador")

    # ================================================
    # VIBRACIÓN
    # ================================================

    elif opcion == "2":

        cliente.publish(
            TOPICO_VIBRACION,
            "ON"
        )

        print("Comando enviado al motor")

    # ================================================
    # BOCINA
    # ================================================

    elif opcion == "3":

        cliente.publish(
            TOPICO_BOCINA,
            "ON"
        )

        print("Comando enviado a la bocina")

    # ================================================
    # SALIR
    # ================================================

    elif opcion == "4":

        print("Finalizando servidor MQTT...")

        cliente.loop_stop()
        cliente.disconnect()

        break

    else:

        print("Opción inválida")

    time.sleep(1)