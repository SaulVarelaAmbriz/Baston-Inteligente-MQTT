# OBJETIVO:Gestión de Firebase y Dashboard de usuario
# Almacenar los datos previamente publicados en el broker de MQTT en una base de datos persistente en la nube (Firestore)

# INTEGRANTES:
# Ramirez Abundiz Berenice 22240234
# Rivera Ponce David Eduardo 22240226
# Varela Ambriz Saul 22240256

# PROYECTO:
# Bastón Inteligente

import time
from datetime import datetime
import firebase_admin
import requests
from firebase_admin import credentials, firestore
import paho.mqtt.client as mqtt  # Librería MQTT estándar para PC

class Firebase:
    '''
    Incializa la conexion con Firestore y configura MQTT
    '''
    def __init__(self):
        """
        Constructor: Inicializa la conexión con Firestore y configura MQTT.
        Hacemos uso del archivo JSON que contiene la clave privada , necesaria para autenticar y poder almacenar datos en Firestore
        """
        # RUTA CONEXION A FIREBASE
        self.FIREBASE_URL  = 'https://bastoninteligente-311b5-default-rtdb.firebaseio.com/'
        try:
            cred = credentials.Certificate("bastoninteligente-311b5-firebase-adminsdk-fbsvc-872f6c099c.json") 
            firebase_admin.initialize_app(cred)
            
            # Guardamos la base de datos en el objeto (self.db)
            self.db = firestore.client()
            print("Conexión con Firestore correcto")
         
            # Inicializamos la configuración de MQTT
            self.configurar_mqtt()

            
        except Exception as e:
            print("Error al establecer conexion a Firestore", e)
            exit()

    '''
    Configura el protoloco MQTT, hacemos uso de broker publico 
    '''
    def configurar_mqtt(self):   
        # CONFIGURACIÓN MQTT
        self.BROKER = "broker.emqx.io"
        self.PUERTO = 1883
        self.CLIENT_ID = "BastonServidor_PC"  # ID fijo identificable en el broker

        # Creación del Cliente MQTT
        self.cliente = mqtt.Client(client_id=self.CLIENT_ID)

        # Configuración de los Callbacks apuntando a los métodos de la clase
        self.cliente.on_connect = self.al_conectar
        self.cliente.on_message = self.al_recibir_mensaje

        # Tópicos utilizados
        self.TOPICO_BOCINA = "baston_equipo_7718/actuadores/bocina"
        self.TOPICO_MOTOR = "baston_equipo_7718/actuadores/vibracion"
        self.TOPICO_ZUMBADOR = "baston_equipo_7718/actuadores/zumbador"
        self.TOPICO_PIR = "baston_equipo_7718/sensores/pir"
        self.TOPICO_ULTRASONICO= "baston_equipo_7718/sensores/ultrasonico"
        self.TOPICO_CONTROL = "baston_equipo_7718/sensores/control"
    
    '''
    Retorna la fecha y hora actual del servidor (PC) en formato de string
    '''
    def timestamp_legible(self):
        now = datetime.now()
        
        return now.strftime("%Y-%m-%d %H:%M:%S")
    

    '''
    Encargado de suscribirse a los topicos de los cuales recibiremos datos para publicar
    Se ejecuta automáticamente cuando la PC se conecta al Broker
    '''
    def al_conectar(self, client, userdata, flags, rc):
        if rc == 0:
            print("Conectado exitosamente al Broker MQTT.")
            self.cliente.subscribe(self.TOPICO_BOCINA)
            self.cliente.subscribe(self.TOPICO_MOTOR)
            self.cliente.subscribe(self.TOPICO_ZUMBADOR)
            self.cliente.subscribe(self.TOPICO_PIR)
            self.cliente.subscribe(self.TOPICO_ULTRASONICO)
            self.cliente.subscribe(self.TOPICO_CONTROL)
            print("Suscrito. Esperando datos...")
        else:
            print(f"Error de conexión al Broker.Código de retorno: {rc}")

    '''
    Se ejecuta automaticamente por medio del callback que acciona cuando recibe un mensaje de MQTT
    Encargado de guardar los datos en Firestore y Real time databse, una vez recibidos.
    Guarda los datos relevantes, como cuando la distancia es mayor a 20, de esta manera la persona de apoyo del no vidente
    sabe que tan expuesto al riesgo esta el usuario y que ubicaciones evitar para evitar accidentes.
    '''
    def al_recibir_mensaje(self, client, userdata, msg):
        ts = self.timestamp_legible()
        
        # En paho-mqtt, el tópico viene en msg.topic y el mensaje en msg.payload (en bytes, por eso necesitamos decodificarlo)
        topico = msg.topic
        mensaje = msg.payload.decode('utf-8')

        print(f"[{ts}] Telemetría Recibida -> {topico.split('/')[-1]}: {mensaje}")

        self.enviar_a_realtime_database(topico,mensaje,ts)
        self.enviar_firestore(topico,mensaje,ts)

    def enviar_firestore(self,topico,mensaje,ts):
        # EVALUACIÓN DEL EVENTO: BOCINA
        if topico == self.TOPICO_BOCINA:
            try:
                
                if mensaje in ['alto', 'medio', 'bajo']:
                    
                    if mensaje == 'alto':                        
                        clasificacion = "Desnivel alto"
                        distancia = "> 50"
                    elif mensaje == 'medio':
                        clasificacion = "Desnivel medio"
                        distancia = "< 50"
                    else:
                        clasificacion = "Desnivel bajo"
                        distancia = "< 20"
                    
                    registro_desnivel = {
                        "fecha_hora": ts,
                        "evento": "Alerta de Desnivel",
                        "clasificacion": clasificacion,
                        "lectura": f"{distancia} CM"
                        }
                    
                    # Guardamos en la colección de Firestore usando self.db
                    self.db.collection("historial_desniveles").add(registro_desnivel)
                    print(f" Histórico registrado: {clasificacion} ({distancia} CM)")                   
            except Exception as e:
                print("Error al procesar datos de la bocina:", e)

        elif topico == self.TOPICO_MOTOR:
            try:
                if mensaje == '1':
                    registro_presencia = {
                        "fecha_hora": ts,
                        "evento": "Alerta presencia persona"
                        }

                    #Guardamos el registro
                    self.db.collection("historial_presencia").add(registro_presencia)
            except Exception as e:
                print("Error al procesar el valor del motor de vibracion")
        elif topico == self.TOPICO_ZUMBADOR:
            try:
                if mensaje == '1':
                    registro_localizacion = {
                        "fecha_hora": ts,
                        "evento": "Se activo el buscador del baston"
                        }

                    #Guardamos el registro
                    self.db.collection("historial_localizacion").add(registro_localizacion)
            except Exception as e:
                print("Error al procesar el valor del zumbador")


    '''
        Conecta al broker e inicia un bucle asincrono (no bloqueante)
    '''
    def iniciar(self):
        print("Conectando al broker MQTT...")
        self.cliente.connect(self.BROKER, self.PUERTO, 60)
        self.cliente.loop_start()  # Esto mantiene la escucha activa en segundo plano

    '''
    Detiene el bucle y cierra la conexión de forma limpia
    '''
    def detener(self):
        self.cliente.loop_stop()
        self.cliente.disconnect()
        print("La conexion a terminado")

#--------------------FIREBASE---------------------------

    """
    Recibimos la ruta del sensor para el cual vamos a escribir datos, asi como su dato
    Realizamos un Put a la API,serializamos a Json y cerramos la respuesta con .close .Esto evita que sigamos consumiendo RAM
    """
    def fb_put(self, ruta, valor):
        url = self.FIREBASE_URL + ruta + ".json"
        try:
            respuesta = requests.put(url, json=valor, timeout=5)
            respuesta.close()
        except Exception as e:
            print(f"Error en Realtime DB PUT [{ruta}]: {e}")
 

    """
    Se encarga de escribir los datos en firebase
    bg = False garantiza la escritura 
    Adicional mandamos timestamp en su respectivo formato
    """
    def enviar_a_realtime_database(self,topico,mensaje,ts):
        ts = self.timestamp_legible()

        if topico == self.TOPICO_ULTRASONICO:
            # Escritura agrupada: un solo PUT por sub-nodo reduce llamadas HTTP
            self.fb_put("sensores/ultrasonico", {
                "distancia_cm": mensaje,
            })
        elif topico == self.TOPICO_PIR:
            self.fb_put("sensores/pir", {
                "movimiento": mensaje,
                "actualizado": ts
            })
        elif topico == self.TOPICO_CONTROL:
            self.fb_put("sensores/control", {
                "boton": mensaje,
                "actualizado": ts
            })
        elif topico == self.TOPICO_BOCINA:
            self.fb_put("actuadores/bocina", {
                "sonido": mensaje,
                "actualizado": ts
            }) 
        elif topico == self.TOPICO_ZUMBADOR:
            self.fb_put("actuadores/zumbador", {
                "sonido": mensaje,
                "actualizado": ts
            })  
        elif topico == self.TOPICO_MOTOR:
            self.fb_put("actuadores/motor", {
                "vibracion": mensaje,
                "actualizado": ts
            })       
        print("Firebase ► Enviados [%s]:" % ts, mensaje)


# --------- Main ------------------
if __name__ == "__main__":
    # Creamos la instancia de nuestra clase
    servidor = Firebase()   
    # Conectamos a MQTT
    servidor.iniciar()

    # nos ayuda a establecer una diferencia de tiempo y poder publicar en firebase
    ultima_publicacion = time.time()

    #El ciclo no ejecuta nada , simplemente mantiene vivo el codito , parq  cuaando haya una peticion desde un topico
    while True:
        time.sleep(1)