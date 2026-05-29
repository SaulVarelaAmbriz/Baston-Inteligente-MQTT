#OBJETIVO:
#Obtener coordenadas GPS del Bastón Inteligente
#utilizando la librería MicropyGPS y publicar
#la ubicación mediante MQTT.

#INTEGRANTES:
#Ramirez Abundiz Berenice 22240234
#Rivera Ponce David Eduardo 22240226
#Varela Ambriz Saul 22240256

#PROYECTO:
#Bastón Inteligente

from machine import UART
from micropyGPS import MicropyGPS
import time


class GPSManager:

    def __init__(self):

       
        self.uart = UART(
            2,
            baudrate=115200,
            tx=17,
            rx=16
        )

        # Parser GPS
        self.gps = MicropyGPS()

        # Variables para diagnóstico
        self.datos_recibidos = False
        self.ultima_sentencia = None


    def leer_gps(self):

        while self.uart.any():

            dato = self.uart.read(1)

            if dato:

                try:
                    caracter = dato.decode("utf-8")

                    resultado = self.gps.update(caracter)

                    self.datos_recibidos = True

                    if resultado:
                        self.ultima_sentencia = resultado

                except:
                    pass


    def obtener_coordenadas(self):

        self.leer_gps()

        # Validar si ya hay fix real
        if self.gps.valid:

            latitud = (
                self.gps.latitude[0] +
                (self.gps.latitude[1] / 60)
            )

            if self.gps.latitude[2] == "S":
                latitud *= -1

            longitud = (
                self.gps.longitude[0] +
                (self.gps.longitude[1] / 60)
            )

            if self.gps.longitude[2] == "W":
                longitud *= -1

            return {
                "latitud": latitud,
                "longitud": longitud,
                "satelites": self.gps.satellites_in_use,
                "sentencia": self.ultima_sentencia
            }

        return None


    def obtener_estado(self):

        self.leer_gps()

        return {
            "datos_recibidos": self.datos_recibidos,
            "ultima_sentencia": self.ultima_sentencia,
            "satelites_en_uso": self.gps.satellites_in_use,
            "satelites_visibles": self.gps.satellites_in_view,
            "fix_stat": self.gps.fix_stat,
            "valido": self.gps.valid
        }