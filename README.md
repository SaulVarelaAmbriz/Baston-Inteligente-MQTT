### INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

### OBJETIVO:
Diseñar, construir e implementar un sistema embebido asistencial de tipo "Bastón Inteligente" 
para personas con discapacidad visual, mediante una arquitectura IoT distribuida y full-stack. 
El dispositivo integra sensores de proximidad y presencia, actuadores sonoros y hápticos,
procesamiento de visión artificial (Inteligencia Artificial) en un servidor local, persistencia en
bases de datos en la nube (Firebase) y un panel de control móvil nativo (Android/Kotlin), garantizando 
un ecosistema robusto, desacoplado y de alta disponibilidad para mejorar la seguridad y autonomía del usuario final.

### INSTALACIÓN DE DEPENDENCIAS Y ARCHIVOS EN LA ESP32:
## 1.- Librerías de Hardware (Drivers)
Para que el bastón inteligente funcione correctamente y todo el hardware responda bien, es obligatorio subir las siguientes librerías y controladores
a la memoria interna de la ESP32 antes de ejecutar el código principal:

* `hcsr04.py`: Controlador necesario para que el sensor ultrasónico pueda lanzar los pulsos y medir la distancia en centímetros de forma correcta.
* `nec.py`: Controlador del receptor infrarrojo (IR). Se encarga de decodificar las señales del protocolo NEC del control remoto para que el bastón pueda cambiar de modo o activar funciones cuando el usuario presiona los botones.

---

## 2.- Librerías necesarias para el Servidor/PC (`firebase.py`)
Para ejecutar el script del servidor en la computadora encargado de recibir la telemetría por MQTT y guardarla en la nube, es necesario instalar las siguientes dependencias de Python mediante el gestor de paquetes `pip`:
* `firebase-admin`: El SDK oficial de Google Firebase para Python. Permite autenticar el script mediante la clave privada en formato `.json` y gestionar las colecciones de base de datos en **Cloud Firestore**.
* `requests`: Librería estándar para realizar peticiones HTTP de forma sencilla. En este proyecto se utiliza específicamente para enviar los métodos `PUT` directos hacia la API REST de **Firebase Realtime Database**.
* `paho-mqtt`: El cliente oficial del protocolo MQTT para Python. Se encarga de conectar el servidor al bróker público (`broker.emqx.io`), suscribirse a los canales de sensores y activar las alertas de los actuadores del bastón.

# Comando de instalación en la terminal de la PC:
Abre la terminal o símbolo del sistema (CMD) en tu computadora y ejecuta el siguiente comando para instalar todas las dependencias de un solo golpe:
```bash
pip install firebase-admin requests paho-mqtt
```

---

### Guía de Ejecución del Proyecto
## Estructura General del Proyecto
* `HAL/` (Firmware de los microcontroladores ESP32 y ESP32-CAM)
* `Interfaz/` (Código fuente de la aplicación móvil de Android)
* `Servidor/` (Scripts centrales de Inteligencia Artificial y Persistencia en la Nube)

---

## Paso 1: Ejecutar el Servidor Intermedio (`Servidor/`)
La laptop actuará como nuestro servidor central (*Middleware*) para procesar la IA y guardar el histórico en las bases de datos sin saturar el bastón.

1. **Servicio de Base de Datos y MQTT (`Servidor/firebase/`)**:
   * Abre una terminal en tu PC y navega a la carpeta: `cd Servidor/firebase`
   * Asegúrate de tener tu archivo privado `.json` de credenciales en esta ruta.
   * Ejecuta el script para iniciar la escucha de datos del bastón hacia la nube:
     ```bash
     python firebase.py
     ```

2. **Procesamiento de IA (`Servidor/ia/`)**:
   * Abre otra terminal en tu PC y navega a la carpeta: `cd Servidor/ia`
   * Ejecuta el script que recibirá el flujo de video de la cámara para detectar personas mediante IA:
     ```bash
     python camara_ia.py
     ```

---

### Paso 2: Encender el Hardware del Bastón (`HAL/`)
Una vez que el servidor de la PC está escuchando, procedemos a inicializar los componentes físicos del bastón.

1. **ESP32 Principal (`HAL/ESP32/`)**:
   * Conecta la ESP32 a su fuente de energía y sube todos los archivos que encuentres dentro al ESP32.
   * Al estar el código principal nombrado como `main.py`, la tarjeta lo ejecutará de forma automática al encenderse, cargando los audios de la carpeta `audios/`, enlazándose al Wi-Fi, conectándose al bróker MQTT e iniciando los sensores de la HAL (`dispositivos.py`).

2. **ESP32-CAM (`HAL/ESP32-CAM/`)**:
   * Alimenta la ESP32-CAM. El firmware `CameraWebServer.ino` (previamente cargado desde Arduino IDE) se iniciará de inmediato, activando el sensor de la cámara y levantando un servidor web local de streaming de video para que lo consuma nuestro script de IA en la PC.

---

### Paso 3: Abrir la Aplicación Móvil (`Interfaz/`)
El último paso es para que el familiar o cuidador pueda monitorear la actividad del usuario en tiempo real.

1. Abre el entorno **Android Studio**.
2. Importa el proyecto ubicado en la ruta: `Interfaz/BastonInteligente`.
3. Conecta un dispositivo Android físico (con la depuración USB activa) o levanta un emulador.
4. Presiona el botón **Run** (`Shift + F10`) para compilar e instalar la app.
5. Al abrir la app, esta se conectará de inmediato a MQTT y Firebase de forma segura para desplegar la localización GPS y el historial de alertas detectadas por el bastón.
