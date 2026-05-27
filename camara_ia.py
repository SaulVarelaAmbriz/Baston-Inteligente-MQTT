#OBJETIVO:
#Integración de IA para detección de personas
#mediante ESP32-CAM utilizando OpenCV y MQTT.
#Cuando se detecta una persona se activa
#el motor de vibración del Bastón Inteligente
#y se guarda una fotografía automáticamente.

#INTEGRANTES:
#Ramirez Abundiz Berenice 22240234
#Rivera Ponce David Eduardo 22240226
#Varela Ambriz Saul 22240256

#PROYECTO:
#Bastón Inteligente



import cv2
import time
import os
from datetime import datetime

import paho.mqtt.client as mqtt


# CONFIGURACIÓN MQTT

BROKER = "broker.emqx.io"
PUERTO = 1883

TOPICO_VIBRACION = "baston_equipo_7718/actuadores/vibracion"


# CONEXIÓN MQTT


cliente = mqtt.Client()

cliente.connect(BROKER, PUERTO, 60)

cliente.loop_start()

print("MQTT conectado")


# URL ESP32 CAM


URL = "http://10.67.197.80:81/stream"


# MODELO IA OpenCV

"""
Modelo:
HOG + SVM Detector Personas OpenCV

Precisión aproximada:
70% - 85%

El modelo detecta:
- Personas completas
- Cuerpo humano
- Movimiento humano
"""

hog = cv2.HOGDescriptor()

hog.setSVMDetector(
    cv2.HOGDescriptor_getDefaultPeopleDetector()
)


# VIDEO STREAM


cap = cv2.VideoCapture(URL)

if not cap.isOpened():

    print("No se pudo abrir la cámara")
    exit()

print("Cámara conectada")


# CARPETA FOTOS


if not os.path.exists("fotos"):
    os.makedirs("fotos")


ultimo_guardado = 0
TIEMPO_ESPERA = 5



while True:

    ret, frame = cap.read()

    if not ret:
        print("Error leyendo frame")
        continue

   
    # REDUCCIÓN DE LATENCIA
    
    frame = cv2.resize(frame, (320, 240))

    
    # DETECCIÓN PERSONAS
   
    personas, pesos = hog.detectMultiScale(
        frame,
        winStride=(4, 4),
        padding=(8, 8),
        scale=1.05
    )

    persona_detectada = False

   
    # DIBUJAR DETECCIONES
   
    for (x, y, w, h) in personas:

        # FILTRO FALSOS POSITIVOS
        if w < 60 or h < 120:
            continue

        persona_detectada = True

        cv2.rectangle(
            frame,
            (x, y),
            (x + w, y + h),
            (0, 255, 0),
            2
        )

        cv2.putText(
            frame,
            "PERSONA",
            (x, y - 10),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.5,
            (0, 255, 0),
            2
        )

    
    # SI DETECTA PERSONA

    tiempo_actual = time.time()

    if persona_detectada:

        # ACTIVAR VIBRACIÓN MQTT
        cliente.publish(
            TOPICO_VIBRACION,
            "1"
        )

        # GUARDAR FOTO CADA 5 SEGUNDOS
        if (tiempo_actual - ultimo_guardado) > TIEMPO_ESPERA:

            timestamp = datetime.now().strftime(
                "%Y%m%d_%H%M%S"
            )

            nombre_foto = f"fotos/persona_{timestamp}.jpg"

            cv2.imwrite(
                nombre_foto,
                frame
            )

            print("FOTO GUARDADA:")
            print(nombre_foto)

            ultimo_guardado = tiempo_actual

    
    # TEXTO PANTALLA
    
    cv2.putText(
        frame,
        f"Personas: {len(personas)}",
        (10, 30),
        cv2.FONT_HERSHEY_SIMPLEX,
        0.7,
        (0, 255, 0),
        2
    )

    
    # MOSTRAR VIDEO
   
    cv2.imshow(
        "Baston Inteligente IA",
        frame
    )

    
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break



cap.release()

cv2.destroyAllWindows()

cliente.loop_stop()

cliente.disconnect()