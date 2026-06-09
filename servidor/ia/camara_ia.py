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


# ==================================================
# MQTT
# ==================================================

BROKER = "broker.emqx.io"
PUERTO = 1883

TOPICO_CONTROL = "baston_equipo_7718/sensores/control"
TOPICO_PIR = "baston_equipo_7718/sensores/pir"
TOPICO_VIBRACION = "baston_equipo_7718/actuadores/vibracion"

modo_ia = False
pir_activo = False


def on_connect(client, userdata, flags, rc):

    print("================================")
    print("MQTT CONECTADO")
    print("================================")

    client.subscribe(TOPICO_CONTROL)
    client.subscribe(TOPICO_PIR)

    print("Suscrito:", TOPICO_CONTROL)
    print("Suscrito:", TOPICO_PIR)


def on_message(client, userdata, msg):

    global modo_ia
    global pir_activo

    mensaje = msg.payload.decode().strip()

    # ---------------------------
    # CONTROL REMOTO
    # ---------------------------

    if msg.topic == TOPICO_CONTROL:

        print("CONTROL:", mensaje)

        if mensaje == "pir_movimiento":

            modo_ia = True

            print("MODO IA ACTIVADO")

    # ---------------------------
    # PIR
    # ---------------------------

    elif msg.topic == TOPICO_PIR:

        if not modo_ia:
            return

        modo_ia = False

        if mensaje == "1":
            pir_activo = True
            print("MOVIMIENTO DETECTADO")

        else:
            pir_activo = False


cliente = mqtt.Client()

cliente.on_connect = on_connect
cliente.on_message = on_message

cliente.connect(
    BROKER,
    PUERTO,
    60
)

cliente.loop_start()


# ==================================================
# ESP32 CAM
# ==================================================

URL = "http://10.67.197.80:81/stream"

cap = cv2.VideoCapture(URL)

if not cap.isOpened():

    print("ERROR: No se pudo abrir la cámara")
    exit()

print("Cámara conectada")


# ==================================================
# IA OpenCV
# ==================================================

hog = cv2.HOGDescriptor()

hog.setSVMDetector(
    cv2.HOGDescriptor_getDefaultPeopleDetector()
)


# ==================================================
# FOTOS
# ==================================================

if not os.path.exists("fotos"):
    os.makedirs("fotos")

ultimo_guardado = 0
ultimo_mqtt = 0

TIEMPO_FOTO = 5
TIEMPO_MQTT = 3


# ==================================================
# LOOP PRINCIPAL
# ==================================================

while True:

    try:

        ret, frame = cap.read()

        if not ret:
            continue

        frame = cv2.resize(
            frame,
            (640, 480)
        )

        # ---------------------------------
        # SI EL PIR NO ESTÁ ACTIVO
        # ---------------------------------

        if not (modo_ia and pir_activo):

            cv2.putText(
                frame,
                "ESPERANDO PIR...",
                (10, 30),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.8,
                (0, 255, 255),
                2
            )

            cv2.imshow(
                "Baston Inteligente IA",
                frame
            )

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

            continue

        # ---------------------------------
        # DETECCION
        # ---------------------------------

        gris = cv2.cvtColor(
            frame,
            cv2.COLOR_BGR2GRAY
        )

        personas, pesos = hog.detectMultiScale(
            gris,
            winStride=(8, 8),
            padding=(8, 8),
            scale=1.10
        )

        persona_detectada = False

        for i, (x, y, w, h) in enumerate(personas):

            if len(pesos) > i:

                if pesos[i] < 0.6:
                    continue

            if w < 80:
                continue

            if h < 160:
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
                0.7,
                (0, 255, 0),
                2
            )

        tiempo_actual = time.time()

        # ---------------------------------
        # PERSONA DETECTADA
        # ---------------------------------

        if persona_detectada:

            if (tiempo_actual - ultimo_mqtt) > TIEMPO_MQTT:

                cliente.publish(
                    TOPICO_VIBRACION,
                    "1"
                )

                print(
                    "PERSONA DETECTADA -> VIBRACION"
                )

                ultimo_mqtt = tiempo_actual

            if (tiempo_actual - ultimo_guardado) > TIEMPO_FOTO:

                timestamp = datetime.now().strftime(
                    "%Y%m%d_%H%M%S"
                )

                nombre = (
                    f"fotos/persona_{timestamp}.jpg"
                )

                cv2.imwrite(
                    nombre,
                    frame
                )

                print(
                    "FOTO GUARDADA:",
                    nombre
                )

                ultimo_guardado = tiempo_actual

        # ---------------------------------
        # TEXTO
        # ---------------------------------

        cv2.putText(
            frame,
            f"PIR: {'ACTIVO' if pir_activo else 'INACTIVO'}",
            (10, 30),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.7,
            (0, 255, 0),
            2
        )

        cv2.putText(
            frame,
            f"Personas: {len(personas)}",
            (10, 65),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.7,
            (0, 255, 0),
            2
        )

        cv2.imshow(
            "Baston Inteligente IA",
            frame
        )

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    except Exception as e:

        print("ERROR IA:")
        print(e)

        time.sleep(1)


cap.release()

cv2.destroyAllWindows()

cliente.loop_stop()

cliente.disconnect()

print("Servidor IA detenido")