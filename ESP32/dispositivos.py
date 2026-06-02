#PROYECTO:Baston Inteligente
#INTEGRANTES: 
#Ramirez Abundiz Berenice 22240234
#Rivera Ponce David Eduardo 22240226
#Varela Ambriz Saul 22240256
#DESCRIPCION:Contiene las clases de Sensores y Actuadores , encargados de configurar los componentes y realizar lecturas de los mismo , asi como apagarlos para una mejor practica y seguridad

from hcsr04 import HCSR04 # librería para manejar el ultrasónico
from ir_rx.nec import NEC_16 # librería para manejar el control remoto
from machine import Pin, PWM,DAC # Se importan las clases necesarias para controlar pines digitales y señales PWM
import time  # librería para manejar tiempos (pausas)
from time import sleep,sleep_us

"""
Clase 'SensorBox' encargada de configurar,
establecer interrupciones y
obtener los valores leídos en timepo real
de los sensores:
1.- Control Remoto
2.- Ultrasónico
3.- PIR
"""
class SensorBox:
    """
    Constructor de la clase que permite la configuración de los
    sensores 1.- Control remoto 2.- PIR 3.- Ultrasónico
    """    
    def __init__(self):
        #Configuración de control remoto:
        self.control_configurar()
        #Configuración de PIR:        
        self.pir_configurar()
        #Configuración de ultrasónico:
        self.ultrasonico_configurar()
    
    """
    Control remoto
    1.- Configura los pines de conexión del control remoto
        y la función a ejecutar cuando un botón es presionado
    2.- Inicializa una estructura de datos que permite identificar la
        acción a realizar de acuerdo al botón presionado
    3.- Inicializa una variable para almacenar la acción a ejecutar
        de acuerdo al botón presionado
    """
    def control_configurar(self):
        # control remoto
        self.control = NEC_16(Pin(14, Pin.IN, Pin.PULL_UP), self.control_funcion)
        
        # Botones del control a utilizar
        """
        Los controles del control permiten activar las funciones
        del bastón inteligente, solo cuando el usuario lo decida.
        """
        self.control_botones = {            
            0x45: 'ultrasonico_leer_cm', # Botón 'APAGAR' que activa la función de escuchar los 'cm leídos por el ultrasónico' por la 'bocina'
            0x46: 'pir_movimiento', # Botón 'VOL+' que activa la función de 'leer el PIR' y activar la cámara.
            0x47: 'zumbador_localizador', # Botón 'FUNC/STOP' que sirve para localizar el bastón 'activando el zumbador'
            0x40: 'resumen' # Botón '>||' para obtener el resumen de lecturas de los 3 sensores.            
        }
        
        #variable que guarda el botón presionado para tomar decisiones de lógica
        self.control_decision = None
        # variable que guarda la última decision del control
        self.control_ultimo = None
    
    """
    Control remoto
    Funcionalidad del control remoto cuando un botón es presionado.
    Se guarda el número hexadecimal del botón que ha sido presionado
    para posteriormente tomar decisiones.
    """
    def control_funcion(self, data, addr, ctrl):        
        if data > 0:
            self.control_decision = data # solo guardar
    
    """
    Control remoto
    Permite obtener qué botón ha sido presionado
    """
    def control_obtener_valor(self):
        decision = self.control_botones.get(self.control_decision, "desconocido")        
        
        self.control_limpiar_valor()
        
        if decision not in ("desconocido", "resumen"):
            self.control_ultimo = decision
            
        return decision
    
    """
    Control remoto
    Permite limpiar la variable 'control_decision' para
    tomar nuevas decisiones.
    """
    def control_limpiar_valor(self):
        self.control_decision = None        
        
    """
    PIR
    1.- Configura los pines de conexión del sensor PIR
    2.- Configura interrupción por movimiento del PIR
    3.- Establece la variable de control (bandera) del PIR
    """
    def pir_configurar(self):
        # Configuramos el pin 
        self.pir = Pin(12, Pin.IN, Pin.PULL_DOWN)
        
        # La variable siempre reflejará el estado actual
        self.pir_es_movimiento_detectado = 0
        
        # INTERRUPCIÓN EN AMBOS FLANCOS (RISING y FALLING)
        # Esto dispara la función tanto cuando detecta como cuando deja de detectar.
        self.pir.irq(trigger=Pin.IRQ_RISING | Pin.IRQ_FALLING, handler=self.pir_controlador_automatico)
    
    """
    PIR
    El PIR cambia la bandera 'self.pir_es_movimiento_detectado'.
    Cuando se detecta movimiento, la bandera es True, de lo contrario
    será False
    """
    # ISR: PIR
    def pir_controlador_automatico(self, pin):
        self.pir_es_movimiento_detectado = pin.value()
                        
    """
    PIR
    Se retorna el estado actual que la interrupción mantiene al día.
    """
    def pir_obtener_valor(self):                
        return self.pir_es_movimiento_detectado
                                
    """
    Ultrasónico
    1.- Configura los pines de conexión del sensor ultrasónico
    2.- Creación de variables necesarias para realizar lecturas 
    eficientes del ultrasónico 
    """
    def ultrasonico_configurar(self):
        self.ultrasonico = HCSR04(trigger_pin=5, echo_pin=18, echo_timeout_us=15000)        
        self.ultrasonico_MIN_CM = 2 # punto ciego del sensor en cm
        self.ultrasonico_MAX_CM = 400 # límite máximo real en cm
        self.ultrasonico_N_MUESTRAS = 5 # lecturas para promedio
        self.ultrasonico_lecturas = [] # lista para almacenar los valores leídos en cm y obtener el promedio
      
    """
    Ultrasónico
    Se obtienen lecturas promedio en cm del sensor ultrasónico
    """
    def ultrasonico_obtener_valor(self):
        self.ultrasonico_lecturas = []
        for _ in range(self.ultrasonico_N_MUESTRAS):
            try:
                d = self.ultrasonico.distance_cm()
                if self.ultrasonico_MIN_CM < d < self.ultrasonico_MAX_CM: # filtrar
                    self.ultrasonico_lecturas.append(d)
            except OSError:
                pass # ignorar timeout
            sleep(0.07) #tiempo de espera para lecturas eficientes
            
        if self.ultrasonico_lecturas:
            return int(round(sum(self.ultrasonico_lecturas) / len(self.ultrasonico_lecturas))) #redondeando en entero , .6 hacia arriba y .5 hacia el entero inferior
        
        return 800 # sin lectura válida, retorna un numero fuera de lectura.Sabiendo que el ultrasonico lee hasta 250cm        
    
    """
    Retorna un diccionario con los valores leídos en tiempo
    real de los sensores.
    """
    def obtener_resumen_sensores(self):
        # Devuelve un diccionario con el estado actual de todos los sensores
        return {
            "ultrasonico": self.ultrasonico_obtener_valor(),
            "control": self.control_ultimo,
            "pir": self.pir_obtener_valor()
        }
        

"""
===================================================================================
"""


class ActuatorBox:
    """
    Clase encargada de controlar los actuadores del bastón inteligente.
    """

    """
    Configuracion e inicializacion de los componentes
    """
    def __init__(self):
        # Se configura la bocina en el pin 25 usando DAC para reproducir formato WAV
        self.bocina = DAC(Pin(25))        
        
        self.estado = "" # Ayuda a que el audio no se repita sin parar,si no solo una vez

        # Se configura el zumbador como salida digital (encendido/apagado)
        self.zumbador = Pin(26, Pin.OUT)

        # Se configura el motor de vibración como salida digital
        self.motor_vibracion = Pin(27, Pin.OUT)

    """Reproduce archivos WAV de 8-bit, 8kHz mono"""
    def reproduce_wav(self,archivo):
        try:
            with open(archivo, "rb") as f:
                f.seek(44) # Salta el header del WAV 
                buf = bytearray(1024) # Buffer para eficiencia 
                while True:
                    n = f.readinto(buf)
                    if n == 0: break
                    for i in range(n):
                        self.bocina.write(buf[i]) # Envía muestra al DAC 
                        sleep_us(125) # Delay para 8000Hz (1/8000)                                
            
        except:
            print("Error al leer", archivo)            


    """
    Reproduce el archivo de audio en cada caso, con base a la distancia que retorna el ultrasonico
    Distancia <= 20cm: Reproducira "Desnivel bajo,adelante"
    Distancia 20cm - 50cm: Reproducira "Desnivel medio, precaucion"
    Distancia >50cm: Reproducir "Desnivel alto,cuidado"
    La lectura del ultrasonico posicionado al suelo plano es de 2cm
    """
    def reproducir_distancia(self, distancia):

        if distancia > 50 and  distancia < 800 :
            self.reproduce_wav("alto.wav")
            self.estado = "alto"
        elif distancia > 20:
            self.reproduce_wav("medio.wav")
            self.estado = "medio"
        elif distancia > 0 :
            self.reproduce_wav("bajo.wav")
            self.estado = "bajo"
        else:
            self.estado = "anormal"

    def bocina_obtener_estado(self):
        return self.estado
    
    def activar_modo_localizacion(self):
        """
        Activa el zumbador en forma intermitente para localizar el bastón.

        Retorna:
        None
        """                
        # Se repite el sonido 10 veces
        for _ in range(10):

            # Se enciende el zumbador
            self.zumbador.value(1)

            # Se mantiene encendido 0.3 segundos
            time.sleep(0.3)

            # Se apaga el zumbador
            self.zumbador.value(0)

            # Se espera antes del siguiente pulso
            time.sleep(0.3)                                    

    def vibrar_alerta_persona(self, duracion=2):
        """
        Activa el motor de vibración cuando se detecta una persona.

        Parámetros:
        duracion (float): Tiempo en segundos de vibración.

        Retorna:
        None
        """

        # Se enciende el motor de vibración
        self.motor_vibracion.value(1)

        # Se mantiene vibrando durante el tiempo indicado
        time.sleep(duracion)

        # Se apaga el motor
        self.motor_vibracion.value(0)

    def vibracion_intermitente(self, repeticiones=6):
        """
        Genera una vibración intermitente como alerta discreta.

        Parámetros:
        repeticiones (int): Número de pulsos de vibración.

        Retorna:
        None
        """

        # Se repite el patrón de vibración
        for _ in range(repeticiones):

            # Se enciende el motor
            self.motor_vibracion.value(1)

            # Se mantiene encendido 0.2 segundos
            time.sleep(0.2)

            # Se apaga el motor
            self.motor_vibracion.value(0)

            # Pausa entre vibraciones
            time.sleep(0.2)

    def alerta_completa(self):
        """
        Activa todos los actuadores como alerta máxima.

        Retorna:
        None
        """

        # Se configura la bocina con una frecuencia alta
        self.bocina.freq(1500)

        # Se enciende la bocina con alta intensidad
        self.bocina.duty(700)

        # Se enciende el zumbador
        self.zumbador.value(1)

        # Se activa el motor de vibración
        self.motor_vibracion.value(1)

    def estado_seguro(self):
        """
        Apaga todos los actuadores del sistema.

        Retorna:
        None
        """

        # Se apaga la bocina
        self.bocina.duty(0)

        # Se apaga el zumbador
        self.zumbador.value(0)

        # Se apaga el motor de vibración
        self.motor_vibracion.value(0)
    