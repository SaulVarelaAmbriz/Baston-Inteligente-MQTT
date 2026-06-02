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
from mqtt_y_firebase import Servidor
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

servidor = Servidor(
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
ultima_firebase = time.time() #usamos una variable separada para firebase ya que se reiniciria en tiempo distinto al mqtt

# Sincronizacion NTP para timestamp
servidor.sincronizar_ntp()



while True:

    try:
        print('Hola')
        
        # REVISAR MENSAJES MQTT
        servidor.escuchar()

        # Se toma el tiempo para "actuar" en 'publicar sensores y gps' y 'firebase'
        ahora = time.time()        
        
        # 1.- Publicar sensores y gps cada 5 segundos al broker mqtt:        
        if (ahora - ultima_publicacion) >= 5:            
            servidor.publicar_sensores()
            
            datos = gps.obtener_coordenadas()
            if datos != None:
                servidor.publicar_gps(datos["latitud"], datos["longitud"])
            
            ultima_publicacion = ahora
        
        
        
        
        
        # 2.- SE ENCARGA DE PUBLICAR Y OBTENER DATOS DE FIREBASE CADA 20 segundos
        if (ahora - ultima_firebase) >= 20:
            try:
                servidor.enviar_sensores_firebase() #enviamos los datos a firebase
                #Leemos los datos de firebase con ello tambien podemos tomar desiciones al igual que el control remoto
                servidor.leer_actuadores_firebase()
                ultima_firebase = ahora
            except Exception as e:
                print("Error Firebase:", e)
        
        
       
        
        
        
        # 3 -- Lógica del funcionamiento del bastón:
        control_valor = sensores.control_obtener_valor()        
    
        if control_valor == "ultrasonico_leer_cm":
            print("\nActivando ultrasonico y bocina...")            
            servidor.bocina_publicar_encendido()
            
        elif control_valor == "pir_movimiento":
            pir_valor = sensores.pir_obtener_valor()
            
            print("\nValor del PIR...")            
            print(pir_valor)
            # Falta la implementación de la cámara cuando 'pir_valor = 1'
            # y el motor de vibración            
            #
            #
            if pir_valor == 1:
                servidor.vibracion_publicar_encendido()
        
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