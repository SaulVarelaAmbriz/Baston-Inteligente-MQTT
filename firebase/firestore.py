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
    
    def __init__(self):
        """
        Constructor: Inicializa la conexión con Firestore y configura MQTT.
        """
        print("Inicializando conexión con Cloud Firestore desde PC...")
        try:
            cred = credentials.Certificate("bastoninteligente-311b5-firebase-adminsdk-fbsvc-90f9867445.json") 
            firebase_admin.initialize_app(cred)
            
            # Guardamos la base de datos en el objeto (self.db)
            self.db = firestore.client()
            print("Conexión con Firestore establecida exitosamente.")
            
            # Inicializamos la configuración de MQTT
            self.configurar_mqtt()
        except Exception as e:
            print("Error crítico al conectar con Firebase:", e)
            exit()

    def configurar_mqtt(self):   
        # CONFIGURACIÓN MQTT
        self.BROKER = "broker.emqx.io"
        self.PUERTO = 1883
        self.CLIENT_ID = "BastonServidor_PC"  # ID fijo identificable en el broker

        # Creación del Cliente MQTT (Sintaxis de paho-mqtt)
        self.cliente = mqtt.Client(client_id=self.CLIENT_ID)

        # Configuración de los Callbacks apuntando a los métodos de la clase
        self.cliente.on_connect = self.al_conectar
        self.cliente.on_message = self.al_recibir_mensaje

        # Tópicos guardados en el objeto
        self.TOPICO_ULTRASONICO = "baston_equipo_7718/sensores/ultrasonico"
        self.TOPICO_PIR = "baston_equipo_7718/sensores/pir"

    def timestamp_legible(self):
        """Retorna la fecha y hora actual de la PC en formato string"""
        # Al estar en PC, datetime ya maneja la hora local del sistema
        now = datetime.now()
        return now.strftime("%Y-%m-%d %H:%M:%S")

    def al_conectar(self, client, userdata, flags, rc):
        """Se ejecuta automáticamente cuando la PC se conecta al Broker"""
        if rc == 0:
            print("Conectado exitosamente al Broker MQTT.")
            # Nos suscribimos usando self.TOPICO_...
            self.cliente.subscribe(self.TOPICO_ULTRASONICO)
            self.cliente.subscribe(self.TOPICO_PIR)
            print("Suscrito a la telemetría. Esperando datos...")
        else:
            print(f"Error de conexión al Broker. Código de retorno: {rc}")

    def al_recibir_mensaje(self, client, userdata, msg):
        """Se ejecuta cada vez que llega un mensaje a los tópicos suscritos"""
        ts = self.timestamp_legible()
        
        # En paho-mqtt, el tópico viene en msg.topic y el mensaje en msg.payload (en bytes, hay que decodificarlo)
        topico = msg.topic
        mensaje = msg.payload.decode('utf-8')

        print(f"[{ts}] Telemetría Recibida -> {topico.split('/')[-1]}: {mensaje}")

        # EVALUACIÓN DEL EVENTO: ULTRASÓNICO (Alertas de Desnivel)
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
                    print(f"[Firestore] Histórico registrado: {tipo_riesgo} ({distancia} CM)")
            except Exception as e:
                print("[Firestore] Error al procesar datos del ultrasónico:", e)

    def iniciar(self):
        """Conecta al broker e inicia el bucle asíncrono (no bloqueante)"""
        print("Conectando al broker MQTT...")
        self.cliente.connect(self.BROKER, self.PUERTO, 60)
        self.cliente.loop_start()  # Esto mantiene la escucha activa en segundo plano

    def detener(self):
        """Detiene el bucle y cierra la conexión de forma limpia"""
        print("Cerrando servidor de fondo...")
        self.cliente.loop_stop()
        self.cliente.disconnect()


# --------- Main ------------------
if __name__ == "__main__":
    # Creamos la instancia de nuestra clase
    servidor = Firestore()
    
    # Arrancamos las conexiones
    servidor.iniciar()

    # Menú interactivo en consola (Funciona gracias a loop_start)
    while True:
        print("\n=========== MENÚ DE CONTROL REMOTO ===========")
        print("1 -> Forzar Alerta Bocina (MQTT)")
        print("2 -> Forzar Alerta Vibración (MQTT)")
        print("3 -> Forzar Alerta Zumbador (MQTT)")
        print("4 -> Salir")
        print("==============================================")
        
        opcion = input("Seleccione una opción: ")
        
        if opcion == "1":
            servidor.cliente.publish("baston_equipo_7718/actuadores/bocina", "1")
            print("Comando enviado: Activar Bocina.")
        elif opcion == "2":
            servidor.cliente.publish("baston_equipo_7718/actuadores/vibracion", "1")
            print("Comando enviado: Activar Vibración.")
        elif opcion == "3":
            servidor.cliente.publish("baston_equipo_7718/actuadores/zumbador", "1")
            print("Comando enviado: Activar Zumbador.")
        elif opcion == "4":
            servidor.detener()
            print("Servidor finalizado.")
            break
        else:
            print("Opción no válida.")
            
        time.sleep(1)