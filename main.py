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

from mqtt_manager import MQTTManager

import time
import machine


# INICIALIZACIÓN HAL

print("Inicializando HAL...")

# Instancia de sensores
sensores = SensorBox()

# Instancia de actuadores
actuadores = ActuatorBox()

print("HAL inicializada correctamente")


# INICIALIZACIÓN MQTT
print("Inicializando MQTT Manager...")

mqtt = MQTTManager(
    sensores,
    actuadores
)


# CONEXIÓN WIFI
mqtt.conectar_wifi()


# CONEXIÓN MQTT
mqtt.conectar_mqtt()

print("Sistema MQTT iniciado correctamente")

# LOOP PRINCIPAL

ultima_publicacion = time.time()

while True:

    try:
        # 1 -- REVISAR MENSAJES MQTT
        mqtt.escuchar()





        # 2 -- PUBLICAR TELEMETRÍA
        ahora = time.time()
        
        # Publicar sensores cada 5 segundos
        if (ahora - ultima_publicacion) >= 5:            
            mqtt.publicar_sensores()
            ultima_publicacion = ahora        
        
        
        
        
        
        
        # 3 -- Lógica del funcionamiento del bastón:
        control_valor = sensores.control_obtener_valor()        
    
        if control_valor == "ultrasonico_leer_cm":
            print("\nActivando ultrasonico y bocina...")            
            mqtt.bocina_publicar_encendido()
            
        elif control_valor == "pir_movimiento":
            pir_valor = sensores.pir_obtener_valor()
            
            print("\nValor del PIR...")            
            print(pir_valor)
            # Falta la implementación de la cámara cuando 'pir_valor = 1'
            # y el motor de vibración            
            #
            #
            if pir_valor == 1:
                mqtt.vibracion_publicar_encendido()
        
        elif control_valor == "zumbador_localizador":
            print("\nActivando Zumbador...")
            mqtt.zumbador_publicar_encendido()
                    
        time.sleep(0.1)
        
        


    except Exception as e:

        print("================================")
        print("ERROR EN LOOP PRINCIPAL")
        print(e)
        print("================================")

        # Intento de reconexión MQTT
        mqtt.reconectar()

        time.sleep(2)