#OBJETIVO:
#Obtener coordenadas GPS del Bastón Inteligente
#y publicarlas mediante MQTT.

#INTEGRANTES:
#Ramirez Abundiz Berenice 22240234
#Rivera Ponce David Eduardo 22240226
#Varela Ambriz Saul 22240256

#PROYECTO:
#Bastón Inteligente


from machine import UART
import time


class GPSManager:

    def __init__(self):

        # UART2 ESP32
        self.gps = UART(
            2,
            baudrate=9600,
            tx=17,
            rx=16
        )

    def convertir(self, valor, direccion):

        grados = int(valor[:2])
        minutos = float(valor[2:])

        decimal = grados + (minutos / 60)

        if direccion in ['S', 'W']:
            decimal *= -1

        return decimal

    def obtener_coordenadas(self):

        if self.gps.any():

            linea = self.gps.readline()

            if linea:

                try:

                    linea = linea.decode('utf-8')

                    if "$GPGGA" in linea:

                        datos = linea.split(",")

                        latitud = self.convertir(
                            datos[2],
                            datos[3]
                        )

                        longitud = self.convertir(
                            datos[4],
                            datos[5]
                        )

                        return {
                            "latitud": latitud,
                            "longitud": longitud
                        }

                except:
                    pass

        return None