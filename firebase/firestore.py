# OBJETIVO:
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
from firebase_admin import credentials, firestore
import paho.mqtt.client as mqtt  # Librería MQTT estándar para PC

class Firestore:
    '''
    Incializa la conexion con Firestore y configura MQTT
    '''
    def __init__(self):
        """
        Constructor: Inicializa la conexión con Firestore y configura MQTT.
        Hacemos uso del archivo JSON que contiene la clave privada , necesaria para autenticar y poder almacenar datos en Firestore
        """
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
        self.TOPICO_ULTRASONICO = "baston_equipo_7718/sensores/ultrasonico"
    
    '''
    Retorna la fecha y hora actual del servidor (PC) en formato de string
    '''
    def timestamp_legible(self):
        now = datetime.now()1
        
        return now.strftime("%Y-%m-%d %H:%M:%S")
    

    '''
    Encargado de suscribirse a los topicos de los cuales recibiremos datos para publicar
    Se ejecuta automáticamente cuando la PC se conecta al Broker
    '''
    def al_conectar(self, client, userdata, flags, rc):
        if rc == 0:
            print("Conectado exitosamente al Broker MQTT.")
            self.cliente.subscribe(self.TOPICO_ULTRASONICO)
            print("Suscrito. Esperando datos...")
        else:
            print(f"Error de conexión al Broker.Código de retorno: {rc}")

    '''
    Se ejecuta automaticamente por medio del callback que acciona cuando recibe un mensaje de MQTT
    Encargado de guardar los datos en Firestore , una vez recibidos.
    Guarda los datos relevantes, como cuando la distancia es mayor a 20, de esta manera la persona de apoyo del no vidente
    sabe que tan expuesto al riesgo esta el usuario y que ubicaciones evitar para evitar accidentes.
    '''
    def al_recibir_mensaje(self, client, userdata, msg):
        ts = self.timestamp_legible()
        
        # En paho-mqtt, el tópico viene en msg.topic y el mensaje en msg.payload (en bytes, por eso necesitamos decodificarlo)
        topico = msg.topic
        mensaje = msg.payload.decode('utf-8')

        print(f"[{ts}] Telemetría Recibida -> {topico.split('/')[-1]}: {mensaje}")

        # EVALUACIÓN DEL EVENTO: ULTRASÓNICO
        if topico == self.TOPICO_ULTRASONICO:
            try:
                distancia = int(mensaje)
                tipo_riesgo = "Normal"
                
                if 20 < distancia <= 50:
                    tipo_riesgo = "Desnivel medio"
                elif distancia > 50 and distancia < 800:
                    tipo_riesgo = "Desnivel alto"
            
                if tipo_riesgo != "Normal":
                    registro_desnivel = {
                        "fecha_hora": ts,
                        "evento": "Alerta de Desnivel",
                        "clasificacion": tipo_riesgo,
                        "lectura": f"{distancia} CM"
                    }
                
                    # Guardamos en la colección de Firestore usando self.db
                    self.db.collection("historial_desniveles").add(registro_desnivel)
                    print(f" Histórico registrado: {tipo_riesgo} ({distancia} CM)")
            except Exception as e:
                print("Error al procesar datos del ultrasónico:", e)

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

    '''
    Usado para insertar datos fijos de pruebas en Firestore
    '''
    def probar_insercion_directa(self):
        ts = self.timestamp_legible()
        print(f"\n[{ts}] Iniciando prueba de inserción directa a Firestore...")
        
        # Simulamos una lectura de 150 CM (Desnivel alto)
        distancia_prueba = 150
        tipo_riesgo_prueba = "Desnivel alto"
        
        registro_prueba = {
            "fecha_hora": ts,
            "evento": "Alerta de Desnivel",
            "clasificacion": tipo_riesgo_prueba,
            "lectura": f"{distancia_prueba} CM"
        }
        
        try:
            # Forzamos la escritura en la misma colección
            self.db.collection("historial_desniveles").add(registro_prueba)
            print("El dato fijo se guardó correctamente.")
        except Exception as e:
            print("Error al intentar guardar el dato fijo:", e)


# --------- Main ------------------
if __name__ == "__main__":
    # Creamos la instancia de nuestra clase
    servidor = Firestore()   
    # Conectamos a MQTT
    servidor.iniciar()

    # Menú interactivo en consola (Funciona gracias a loop_start)
    while True:
        print("\n=== OPCIONES MENU ===")
        print("1 -> Insertar datos fijos en Firestore ")
        print("2 -> Salir y cerrar conexiones")
        print("============================")

        opcion = input("Ingrese una opcion: ")
        
        if opcion == "1":
            servidor.probar_insercion_directa()
        elif opcion == 2:
            servidor.detener()
            break
        else:
            print("Opción no válida.")
            
        time.sleep(1)