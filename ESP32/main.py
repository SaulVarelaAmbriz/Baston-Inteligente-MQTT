"""
OBJETIVO:
Integrar y coordinar el funcionamiento del
Bastón Inteligente mediante la inicialización
de la HAL y la comunicación MQTT para el envío
de telemetría y recepción de comandos remotos.

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
"""


# IMPORTACIÓN DE MÓDULOS
from dispositivos import SensorBox
from dispositivos import ActuatorBox
from mqtt import MQTT
from gps_manager import GPSManager
import time


# INICIALIZACIÓN HAL

print("Inicializando HAL...")

# Instancia de sensores
sensores = SensorBox()

# Instancia de actuadores
actuadores = ActuatorBox()

# Instancia de GPS
gps = GPSManager()

print("HAL inicializada correctamente")


# INICIALIZACIÓN MQTT
print("Inicializando MQTT + Firebase...")

servidor = MQTT(
    sensores,
    actuadores
)


# CONEXIÓN WIFI
servidor.conectar_wifi()


# CONEXIÓN MQTT
servidor.conectar_mqtt()

print("Sistema MQTT + Firebase iniciado correctamente")


# LOOP PRINCIPAL
ultima_publicacion = time.time()
ultima_firebase = time.time()

while True:

    try:

        # REVISAR MENSAJES MQTT
        servidor.escuchar()

        # TIEMPO ACTUAL
        ahora = time.time()

        # PUBLICAR SENSORES Y GPS CADA 5 SEGUNDOS
        if (ahora - ultima_publicacion) >= 5:

            servidor.publicar_sensores()

            datos = gps.obtener_coordenadas()

            if datos != None:
                servidor.publicar_gps(
                    datos["latitud"],
                    datos["longitud"]
                )

            ultima_publicacion = ahora

        # ==================================================
        # LÓGICA PRINCIPAL DEL BASTÓN
        # ==================================================

        control_valor = sensores.control_obtener_valor()

        # ULTRASÓNICO + BOCINA
        if control_valor == "ultrasonico_leer_cm":

            print("\nActivando ultrasonico y bocina...")

            servidor.bocina_publicar_encendido()

        # IA + PIR
        elif control_valor == "pir_movimiento":

            print("\nModo IA activado")

            servidor.control_publicar_encendido(control_valor)

            pir_valor = sensores.pir_obtener_valor()
            print("\nPIR: ", pir_valor)            
            servidor.pir_publicar_encendido(pir_valor)

            sensores.control_limpiar_valor()
            



        # LOCALIZADOR
        elif control_valor == "zumbador_localizador":

            print("\nActivando Zumbador...")

            servidor.zumbador_publicar_encendido()

        time.sleep(0.1)

    except Exception as e:

        print("================================")
        print("ERROR EN LOOP PRINCIPAL")
        print(e)
        print("================================")

        # Intento de reconexión MQTT
        servidor.reconectar()

        time.sleep(2)
