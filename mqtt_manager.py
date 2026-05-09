#OBJETIVO:
#Gestionar la comunicación MQTT entre la ESP32
#y el servidor Python del proyecto Bastón Inteligente,
#permitiendo la publicación de telemetría de sensores
#y la recepción de comandos para actuadores utilizando
#la capa HAL del sistema.

#INTEGRANTES:
#Ramirez Abundiz Berenice 22240234
#Rivera Ponce David Eduardo 22240226
#Varela Ambriz Saul 22240256

#PROYECTO:
#Bastón Inteligente


#LIBRERÍAS
from umqtt.simple import MQTTClient
import network
import ubinascii
import machine
import time


class MQTTManager:
    """
    Clase encargada de administrar la conexión MQTT,
    la publicación de sensores y la recepción de
    comandos remotos para actuadores.
    """

    def __init__(self, sensores, actuadores):
        """
        Constructor principal del administrador MQTT.

        Parámetros:
        sensores -> instancia de SensorBox
        actuadores -> instancia de ActuatorBox
        """

        
        # REFERENCIAS A LA HAL
        # Se almacenan las referencias de la HAL para
        # evitar acceso directo al hardware.
        self.sensores = sensores
        self.actuadores = actuadores


        #WIFI DATOS
        self.SSID = ""
        self.PASSWORD = ""


        # CONFIGURACIÓN MQTT
        self.BROKER = ""

        self.PUERTO = 1883

        # ID único del cliente MQTT
        self.CLIENT_ID = ubinascii.hexlify(
            machine.unique_id()
        )

        # CREACIÓN CLIENTE MQTT
        self.cliente = MQTTClient(
            self.CLIENT_ID,
            self.BROKER,
            self.PUERTO
        )

        # Callback principal MQTT
        self.cliente.set_callback(
            self.mensaje_recibido
        )

    # WIFI
    def conectar_wifi(self):
        """
        Establece conexión WiFi con la red local.
        """

        wifi = network.WLAN(network.STA_IF)

        wifi.active(True)

        if not wifi.isconnected():

            print("Conectando a WiFi...")

            wifi.connect(
                self.SSID,
                self.PASSWORD
            )

            while not wifi.isconnected():
                time.sleep(1)
                print(".")

        print("WiFi conectado")
        print(wifi.ifconfig())


    # MQTT
    def conectar_mqtt(self):
        """
        Establece conexión con el broker MQTT
        y realiza las suscripciones necesarias.
        """

        self.cliente.connect()

        print("Broker MQTT conectado")


        # SUSCRIPCIONES ACTUADORES
        self.cliente.subscribe(
            b"baston/actuadores/zumbador"
        )

        self.cliente.subscribe(
            b"baston/actuadores/vibracion"
        )

        self.cliente.subscribe(
            b"baston/actuadores/bocina"
        )

        print("Suscripciones MQTT listas")


    # CALLBACK MQTT
    def mensaje_recibido(self, topico, mensaje):
        """
        Callback ejecutado automáticamente cuando
        llega un mensaje MQTT.

        IMPORTANTE:
        Esta función NO accede directamente
        al hardware. Todas las acciones se
        realizan mediante la HAL.
        """

        print("================================")
        print("Mensaje MQTT recibido")
        print("Topico:", topico)
        print("Mensaje:", mensaje)
        print("================================")


        # ZUMBADOR
        if topico == b"baston/actuadores/zumbador":

            if mensaje == b"ON":

                print("Activando localizador")

                # HAL
                self.actuadores.activar_modo_localizacion()


        # MOTOR DE VIBRACIÓN
        elif topico == b"baston/actuadores/vibracion":

            if mensaje == b"ON":

                print("Activando vibración")

                # HAL
                self.actuadores.vibracion_intermitente()


        # BOCINA
        elif topico == b"baston/actuadores/bocina":

            if mensaje == b"ON":

                print("Activando bocina")

                distancia = self.sensores.ultrasonico_obtener_valor()

                # HAL
                self.actuadores.reproducir_distancia(
                    distancia
                )


    # PUBLICAR SENSORES
    def publicar_sensores(self):
        """
        Publica la telemetría de todos los sensores
        del Bastón Inteligente hacia el broker MQTT.
        """

        # Obtención centralizada de sensores
        datos = self.sensores.obtener_resumen_sensores()

  
        # ULTRASÓNICO
        self.cliente.publish(
            b"baston/sensores/ultrasonico",
            str(datos["ultrasonico"])
        )

        # PIR
        self.cliente.publish(
            b"baston/sensores/pir",
            str(datos["pir"])
        )


        # CONTROL IR
        self.cliente.publish(
            b"baston/sensores/control",
            str(datos["control"])
        )

        print("Sensores publicados")


    # ESCUCHAR MENSAJES MQTT
    def escuchar(self):
        """
        Revisa constantemente si existen mensajes
        MQTT entrantes desde el broker.
        """

        self.cliente.check_msg()

  
    # RECONEXIÓN MQTT
    def reconectar(self):
        """
        Intenta restablecer la conexión MQTT
        en caso de fallo.
        """

        try:

            print("Reconectando MQTT...")

            self.cliente.connect()

            print("Reconexión exitosa")

        except Exception as e:

            print("Error reconectando MQTT")
            print(e)

            machine.reset()