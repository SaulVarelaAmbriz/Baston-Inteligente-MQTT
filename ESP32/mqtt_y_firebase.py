#OBJETIVO:
#Gestionar la comunicación MQTT y Firebase entre la ESP32
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
#from typing import Self
from umqtt.simple import MQTTClient
import network,gc,time,ntptime,urequests ,machine,ubinascii,ujson



class Servidor:
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
        self.SSID = "Megacable_2.4G_8DDA"
        self.PASSWORD = "Mqap7dUM"        


        # CONFIGURACIÓN MQTT
        self.BROKER = "broker.emqx.io"

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

        # RUTA CONEXION A FIREBASE
        self.FIREBASE_URL  = 'https://bastoninteligente-311b5-default-rtdb.firebaseio.com/'

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
            b"baston_equipo_7718/actuadores/zumbador"
        )

        self.cliente.subscribe(
            b"baston_equipo_7718/actuadores/vibracion"
        )

        self.cliente.subscribe(
            b"baston_equipo_7718/actuadores/bocina"
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
        if topico == b"baston_equipo_7718/actuadores/zumbador":

            if mensaje == b'1':                                
                print("Activando localizador")
                
                # HAL
                self.actuadores.activar_modo_localizacion()
                
                """
                Después de encender el zumbador,
                cuando se apague de nuevo, publicamos su 'estado' en 0
                """
                self.cliente.publish(
                    b"baston_equipo_7718/actuadores/zumbador",
                    str(0)
                    )


        # MOTOR DE VIBRACIÓN
        elif topico == b"baston_equipo_7718/actuadores/vibracion":

            if mensaje == b'1':                
                print("Activando vibración")
                
                
                # HAL
                self.actuadores.vibracion_intermitente()
                
                """
                Después de encender la vibración,
                cuando se apague de nuevo, publicamos su 'estado' en 0
                """
                self.cliente.publish(
                    b"baston_equipo_7718/actuadores/vibracion",
                    str(0)
                    )


        # BOCINA
        elif topico == b"baston_equipo_7718/actuadores/bocina":

            if mensaje == b'1':
                
                print("Activando bocina")
                
                distancia = self.sensores.ultrasonico_obtener_valor()
                
                # se publica la distancia del ultrasónico en el broker:
                self.cliente.publish(
                    b"baston_equipo_7718/sensores/ultrasonico",
                    str(distancia)
                    )
                
                # HAL
                self.actuadores.reproducir_distancia(distancia)
                
                """
                Actualizamos el 'estado' en que se encuentra
                funcionando la bocina
                """
                self.cliente.publish(
                    b"baston_equipo_7718/actuadores/bocina",
                    self.actuadores.bocina_obtener_estado()
                    )
                
                #pausa de 1 segundo necesaria para visualizar en el broker mqtt qué audio reprodujo la bocina
                time.sleep(1)
                
                """
                Cuando la bocina se apaga de nuevo,
                cambiamos a su estado actual
                """
                self.cliente.publish(
                    b"baston_equipo_7718/actuadores/bocina",
                    "Sin mensaje"
                    )
                
                

    """
    Se publica el 'estado' del zumbador en '1'.
    Esto activa el 'CALLBACK MQTT'
    """
    def zumbador_publicar_encendido(self):
        self.cliente.publish(
            b"baston_equipo_7718/actuadores/zumbador",
            str(1)
        )
       
    """
    Se publica el 'estado' de la vibración en '1'.
    Esto activa el 'CALLBACK MQTT'
    """
    def vibracion_publicar_encendido(self):
        self.cliente.publish(
            b"baston_equipo_7718/actuadores/vibracion",
            str(1)
        )
        
        
    """
    Se publica el 'estado' de la bocina en '1'.
    Esto activa el 'CALLBACK MQTT'
    """  
    def bocina_publicar_encendido(self):
        self.cliente.publish(
            b"baston_equipo_7718/actuadores/bocina",
            str(1)
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
            b"baston_equipo_7718/sensores/ultrasonico",
            str(datos["ultrasonico"])
        )

         # PIR
        self.cliente.publish(
            b"baston_equipo_7718/sensores/pir",
            str(datos["pir"])
        )


        # CONTROL IR
        self.cliente.publish(
            b"baston_equipo_7718/sensores/control",
            str(datos["control"])
        )
    


    #PUBLICAR GPS
    def publicar_gps(self, latitud, longitud):
        
        self.cliente.publish(
            b"baston_equipo_7718/gps/latitud",
            str(latitud)
            )
        
        self.cliente.publish(
            b"baston_equipo_7718/gps/longitud",
            str(longitud)
            )
    


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
    
    """
    Recibimos la ruta del sensor para el cual vamos a escribir datos, asi como su dato
    Realizamos un Put a la API,serializamos a Json y cerramos la respuesta con .close .Esto evita que sigamos consumiendo RAM
    """
    def fb_put(self,ruta, valor):
        url = self.FIREBASE_URL + ruta + ".json"
        try:
            respuesta = urequests.put(url, data=ujson.dumps(valor))
            respuesta.close()   # CRÍTICO: liberar socket en MicroPython
        except Exception as e:
            print("Firebase PUT error [%s]: %s" % (ruta, e))
        finally:
            gc.collect()
 
    """
    Recibimos la ruta del actuador del cual leeremos los datos.
    El metodo que hacemos es un GET, deserealizamos la respuesta y cerramos la conexion.
    Retornamos los datos o none si hay un error
    """
    def fb_get(self,ruta):
        url = self.FIREBASE_URL + ruta + ".json"
        try:
            respuesta = urequests.get(url)
            datos     = respuesta.json()
            respuesta.close()   # CRÍTICO: liberar socket en MicroPython
            gc.collect()
            return datos
        except Exception as e:
            print("Firebase GET error [%s]: %s" % (ruta, e))
            gc.collect()
            return None

    """
    Sincroniza el reloj del ESP32 con el NTP de Micropython.Si no devolver una fecha muy antigua
    """

    def sincronizar_ntp(self):
        try:
            ntptime.settime()
            print("Reloj sincronizado con NTP.")
        except Exception as e:
            print("NTP falló (%s). Se usará tiempo relativo." % e)
 
 
    """
    Despues de la sincronizacion NTP , pasamos a formato YYYY-MM-DD HH:MM:SS.
    El offset -6 ajusta a hora de Mexico
    Retorna la fecha y hora actual en formato string
    """
    def timestamp_legible(self):
        OFFSET_HORAS = -6          # UTC-6 México Centro (ajustar si es necesario)
        t = time.time() + OFFSET_HORAS * 3600
        año, mes, dia, hora, minuto, segundo = time.localtime(t)[:6]
        return "{:04d}-{:02d}-{:02d} {:02d}:{:02d}:{:02d}".format(
            año, mes, dia, hora, minuto, segundo
    )
 

    """
    Se encarga de escribir los datos en firebase
    bg = False garantiza la escritura 
    Adicional mandamos timestamp en su respectivo formato
    """
    def enviar_sensores_firebase(self):
        ts = self.timestamp_legible()
        
        datos = self.sensores.obtener_resumen_sensores()

        # Escritura agrupada: un solo PUT por sub-nodo reduce llamadas HTTP
        self.fb_put("sensores/ultrasonico", {
            "distancia_cm": datos["ultrasonico"],
            "actualizado":  ts
        })
        self.fb_put("sensores/pir", {
            "movimiento":  datos["pir"],
            "actualizado": ts
        })
        self.fb_put("sensores/control", {
            "boton":       datos["control"],
            "actualizado": ts
        })
 
        print("Firebase ► Enviados [%s]:" % ts, datos)
        gc.collect()
        print("Memoria libre:", gc.mem_free())
 


    """
    Consulta el nodo donde se encuentran los actuadores, se activan si la bandera es True
    """
    def leer_actuadores_firebase(self):

        # BOCINA
        sonido = self.fb_get("actuadores/bocina/sonido")
        if sonido:
            print("Activando bocina")
            distancia = self.sensores.ultrasonico_obtener_valor()
                
            # HAL
            self.actuadores.reproducir_distancia(distancia)

            """
            Restablecemos la bandera para el proximo ciclo
            """
            self.fb_put("actuadores/bocina/sonido",False)
                
 
        # Zumbador: activar si la bandera está en True 
        activar = self.fb_get("actuadores/zumbador/sonido")
        if activar:
            print("Activando localizador")
            self.actuadores.activar_modo_localizacion()
                
            """
            Restablecemos la bandera para el proximo ciclo
            """
            self.fb_put("actuadores/zumbador/sonido",False)
 
        # MOTOR DE VIBRACION
        vibrar = self.fb_get("actuadores/motor/vibracion")
        if vibrar:
            print("Activando vibración")
                
            self.actuadores.vibracion_intermitente()
                
            """Restablecemos el valor para el proximo ciclo"""
            self.fb_put("actuadores/motor/vibracion",False)

