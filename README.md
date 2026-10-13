
# AlertMove IoT

> Sistema móvil avanzado de control de acceso y alerta temprana para entornos IoT sensibles, desarrollado de manera nativa en Android con Kotlin.

**AlertMove IoT** es una solución de seguridad inteligente diseñada para supervisar áreas restringidas (como oficinas privadas, bodegas o salas de servidores) en tiempo real. Permite la interconexión con nodos de hardware simulados (ESP32 / sensores PIR), gestión multiusuario colaborativa, notificaciones push persistentes en segundo plano y encriptación de datos de hardware.

---

##  Características Principales (Features)

- **Autenticación Segura y Cifrada:** Integración con Firebase Authentication con validación estricta de correos (Gmail/Hotmail/Outlook) y contraseñas robustas.
- **Sesión Persistente y Auto-Login:** Mantenimiento de sesión activo ante reinicios de dispositivo y cierres forzosos de la aplicación.
- **Monitoreo 24/7 en Segundo Plano:** Servicio en primer plano (*Foreground Service*) y receptor de arranque (*Boot Receiver*) que garantizan la recepción de alertas incluso con la app completamente cerrada.
- **Aislamiento Multiusuario y Hogares Compartidos:** Vinculación segura entre propietarios e invitados mediante tokens de invitación únicos y confirmación con contraseña del administrador.
- **Privacidad Configurable:** Panel de configuración para que el administrador decida si muestra su correo real o una versión enmascarada a los invitados.
- **Gestión Dinámica de Dispositivos IoT:** Registro, edición y eliminación de sensores con generación de MAC addresses aleatorias.
- **Cifrado Simétrico AES:** Los identificadores de hardware (MAC) se encriptan antes de ser almacenados en la nube para cumplir rigurosos estándares de ciberseguridad.
- **Historial Local Offline (SQLite):** Almacenamiento estructurado de eventos de intrusión segmentados por hogar, con opción de limpieza de registros.
- **Notificaciones y Vibración Háptica:** Alertas flotantes de alta prioridad con patrones de vibración estilo mensajería instantánea y redirección automática al historial.

---

##  Stack Tecnológico

- **Lenguaje:** [Kotlin](https://kotlinlang.org/)
- **Interfaz de Usuario:** Android Views, Material Design 3, ConstraintLayout, ScrollView (Diseño 100% responsivo).
- **Arquitectura:** [MVVM (Model-View-ViewModel)](https://developer.android.com/topic/architecture) con Patrón Repositorio.
- **Backend y Nube:** [Firebase Authentication](https://firebase.google.com/docs/auth) y [Cloud Firestore](https://firebase.google.com/docs/firestore).
- **Base de Datos Local:** SQLite (`SQLiteOpenHelper`) para persistencia offline de logs.
- **Seguridad y Criptografía:** `javax.crypto` (Cifrado simétrico AES para MAC addresses).
- **Procesamiento en Segundo Plano:** Android `Service` (Foreground Service) y `BroadcastReceiver`.

---

##  Arquitectura

El proyecto sigue estrictamente el patrón arquitectónico **MVVM**, separando las responsabilidades en capas independientes para garantizar mantenibilidad, escalabilidad y facilidad de pruebas:

```text
com.example.miaplicacionsegura/
│
├── data/
│   ├── local/        <- Base de datos local SQLite y modelos de log
│   ├── model/        <- Clases de datos (IoTDevice)
│   └── repository/   <- Repositorios de datos (Auth, Device, History, Sensor)
│
├── viewmodel/        <- Lógica de negocio y estados reactivos (AuthViewModel, DashboardViewModel)
│
└── ui / activities/  <- Vistas y controladores de UI (MainActivity, RegisterActivity, DashboardActivity)
```

---

## 📦 Instalación y Configuración

Sigue estos pasos rápidos para clonar y ejecutar el proyecto en tu entorno de desarrollo:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/AlertMove-IoT.git
   ```
2. **Abrir el proyecto:**
    * Abre [Android Studio](https://developer.android.com/studio) y selecciona *Open an Existing Project*, eligiendo la carpeta clonada.
3. **Configurar Firebase:**
    * Crea un proyecto en la [Consola de Firebase](https://console.firebase.google.com/).
    * Habilita **Firebase Authentication** (proveedor Correo/Contraseña) y **Cloud Firestore**.
    * Descarga el archivo de configuración `google-services.json` y colócalo dentro de la carpeta `app/` de tu proyecto.
4. **Sincronizar y Ejecutar:**
    * Espera a que Gradle sincronice las dependencias y ejecuta la aplicación en un emulador Android (API 26+) o dispositivo físico.




---  
You:  
pedi que me dieran un promt para un readme pero basicamente me dieron esto Actúa como un Desarrollador Android Senior y Technical Writer. Debes escribir un archivo README.md profesional, altamente estructurado y atractivo para un repositorio de GitHub basado en la información de mi proyecto.

El README debe incluir las siguientes secciones en formato Markdown:
1. # Nombre del Proyecto: Un título limpio con una descripción corta (2 frases) de su propuesta de valor.
2. 🚀 Características Principales (Features): Lista con viñetas de lo que hace la app.
3. 🛠️ Stack Tecnológico: Detalla las tecnologías usadas (ej. Kotlin, Jetpack Compose/XML, Coroutines, Flow, Dagger Hilt/Koin, Retrofit, Room, etc.).
4. 📐 Arquitectura: Explica el patrón utilizado (MVVM, MVI, Clean Architecture) y la estructura de carpetas de forma breve.
5. 📦 Instalación y Configuración: Pasos rápidos para clonar el repositorio y correrlo en Android Studio (incluyendo si requiere alguna API Key).
6. 📄 Licencia: Un cierre estándar (ej. MIT o Apache 2.0).

Aquí tienes los detalles, módulos y contexto de mi proyecto:
[PEGA AQUÍ UN RESUMEN DE TU APP, LOS NOMBRES DE TUS MÓDULOS PRINCIPALES O UNA LISTA DE PANTALLAS] y bueno basicamente hay cosas que tu ya 
sabes asi que podrias hacerme el readmi y de paso me da
s un 
resumen de la app de los nombres de los modulos principales
Agent:  
> Thought  
> **Crafting README.md**  
>   
> I'm now focused on the professional presentation of the AlertMove IoT project. I'm structuring a compelling README.md file, aiming for high quality, strong structure, and attractive visuals. I'm incorporating detailed information about the architecture, key features, and overall context of the project.  
>   
>   
> **Defining Core Application Modules**  
>   
> I'm now identifying the app's essential modules, following the prompt template's guidelines. The project is "AlertMove IoT," a Kotlin-built mobile app. I'm noting the main features, like authentication, background alarms, multi-tenant security, and custom IoT device management. I need to ensure a clear structure for these modules within the documentation.  
>   
>   
> **Defining Security & Logging**  
>   
> I'm now detailing event logging within this AlertMove project, specifically the offline-first SQLite database (`SecurityDatabaseHelper`) segmented by home, with Cloud Firestore synchronization and local clearing options. I'm also describing the AES encryption for the generated simulated IoT devices.  
>   
>   
>   
Aquí tienes el resumen completo de los módulos principales de tu aplicación **AlertMove IoT** y, a continuación, el archivo `README.md` profesional y listo para tu repositorio de GitHub.

---

### 📦 Resumen de la App y Módulos Principales

1. **Módulo de Autenticación (`MainActivity` & `RegisterActivity` + `AuthViewModel` + `AuthRepository`):**
   * Gestiona el acceso seguro de usuarios con validación estricta de dominios permitidos (Gmail, Hotmail, Outlook) y complejidad de contraseñas.
   * Incluye doble contraseña en el registro y persistencia de sesión automática (`Auto-Login` vía Firebase Auth y `onStart`), garantizando que el usuario nunca pierda su sesión ante reinicios o cierres de app.

2. **Módulo del Panel de Control y Dispositivos (`DashboardActivity` + `DashboardViewModel` + `DeviceRepository`):**
   * Pantalla principal responsiva con barra superior, menú hamburguesa (`≡`) y scroll adaptado a cualquier dispositivo (incluyendo Poco F7).
   * Permite registrar, editar y eliminar dispositivos IoT personalizados generando direcciones MAC aleatorias.
   * **Cifrado AES de Hardware:** Las MAC address se encriptan simétricamente en el cliente antes de ser enviadas a Cloud Firestore mediante `CryptoUtils`.

3. **Módulo de Seguridad Multiusuario y Compartida (`DashboardViewModel`):**
   * Permite al propietario generar un **Token de Invitación** único.
   * El invitado ingresa el token junto con la **contraseña del propietario** para vincular su dispositivo de forma segura.
   * **Aislamiento total:** Las consultas y listeners están estrictamente filtrados por `ownerUid`, impidiendo filtraciones de alertas entre usuarios ajenos.
   * **Configuración de Privacidad:** Permite al dueño decidir si muestra su correo real o una versión enmascarada (`ju***@gmail.com`) a sus invitados.

4. **Módulo de Alertas en Segundo Plano y Arranque (`AlarmForegroundService` & `AlarmBootReceiver` + `NotificationHelper`):**
   * **Foreground Service:** Mantiene la escucha en tiempo real de Firestore 24/7 incluso si la aplicación está completamente cerrada.
   * **Boot Receiver:** Reactiva automáticamente el servicio de monitoreo en segundo plano tan pronto como el celular termina de encender o reiniciarse.
   * **Notificaciones y Vibración:** Genera alertas hápticas de alta prioridad (`IMPORTANCE_HIGH`) y abre directamente el historial al tocar la notificación.

5. **Módulo de Historial Local (`SecurityDatabaseHelper` & `HistoryRepository`):**
   * Base de datos local SQLite (`seguridad_iot.db`) que almacena de forma offline las marcas de tiempo y eventos de seguridad.
   * Segmentado por hogar (Muestra solo los eventos del panel activo actual) y con opción interactiva para limpiar el historial.

---

# 📄 Archivo `README.md` para GitHub

Puedes copiar y pegar el siguiente contenido directamente en un archivo llamado `README.md` en la raíz de tu repositorio:

```markdown
# 🚨 AlertMove IoT

> Sistema móvil avanzado de control de acceso y alerta temprana para entornos IoT sensibles, desarrollado de manera nativa en Android con Kotlin.

**AlertMove IoT** es una solución de seguridad inteligente diseñada para supervisar áreas restringidas (como oficinas privadas, bodegas o salas de servidores) en tiempo real. Permite la interconexión con nodos de hardware simulados (ESP32 / sensores PIR), gestión multiusuario colaborativa, notificaciones push persistentes en segundo plano y encriptación de datos de hardware.

---

## 🚀 Características Principales (Features)

- **Autenticación Segura y Cifrada:** Integración con Firebase Authentication con validación estricta de correos (Gmail/Hotmail/Outlook) y contraseñas robustas.
- **Sesión Persistente y Auto-Login:** Mantenimiento de sesión activo ante reinicios de dispositivo y cierres forzosos de la aplicación.
- **Monitoreo 24/7 en Segundo Plano:** Servicio en primer plano (*Foreground Service*) y receptor de arranque (*Boot Receiver*) que garantizan la recepción de alertas incluso con la app completamente cerrada.
- **Aislamiento Multiusuario y Hogares Compartidos:** Vinculación segura entre propietarios e invitados mediante tokens de invitación únicos y confirmación con contraseña del administrador.
- **Privacidad Configurable:** Panel de configuración para que el administrador decida si muestra su correo real o una versión enmascarada a los invitados.
- **Gestión Dinámica de Dispositivos IoT:** Registro, edición y eliminación de sensores con generación de MAC addresses aleatorias.
- **Cifrado Simétrico AES:** Los identificadores de hardware (MAC) se encriptan antes de ser almacenados en la nube para cumplir rigurosos estándares de ciberseguridad.
- **Historial Local Offline (SQLite):** Almacenamiento estructurado de eventos de intrusión segmentados por hogar, con opción de limpieza de registros.
- **Notificaciones y Vibración Háptica:** Alertas flotantes de alta prioridad con patrones de vibración estilo mensajería instantánea y redirección automática al historial.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** [Kotlin](https://kotlinlang.org/)
- **Interfaz de Usuario:** Android Views, Material Design 3, ConstraintLayout, ScrollView (Diseño 100% responsivo).
- **Arquitectura:** [MVVM (Model-View-ViewModel)](https://developer.android.com/topic/architecture) con Patrón Repositorio.
- **Backend y Nube:** [Firebase Authentication](https://firebase.google.com/docs/auth) y [Cloud Firestore](https://firebase.google.com/docs/firestore).
- **Base de Datos Local:** SQLite (`SQLiteOpenHelper`) para persistencia offline de logs.
- **Seguridad y Criptografía:** `javax.crypto` (Cifrado simétrico AES para MAC addresses).
- **Procesamiento en Segundo Plano:** Android `Service` (Foreground Service) y `BroadcastReceiver`.

---

## 📐 Arquitectura

El proyecto sigue estrictamente el patrón arquitectónico **MVVM**, separando las responsabilidades en capas independientes para garantizar mantenibilidad, escalabilidad y facilidad de pruebas:

```text
com.example.miaplicacionsegura/
│
├── data/
│   ├── local/        <- Base de datos local SQLite y modelos de log
│   ├── model/        <- Clases de datos (IoTDevice)
│   └── repository/   <- Repositorios de datos (Auth, Device, History, Sensor)
│
├── viewmodel/        <- Lógica de negocio y estados reactivos (AuthViewModel, DashboardViewModel)
│
└── ui / activities/  <- Vistas y controladores de UI (MainActivity, RegisterActivity, DashboardActivity)
```

---

## 📦 Instalación y Configuración

Sigue estos pasos rápidos para clonar y ejecutar el proyecto en tu entorno de desarrollo:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/AlertMove-IoT.git
   ```
2. **Abrir el proyecto:**
   * Abre [Android Studio](https://developer.android.com/studio) y selecciona *Open an Existing Project*, eligiendo la carpeta clonada.
3. **Configurar Firebase:**
   * Crea un proyecto en la [Consola de Firebase](https://console.firebase.google.com/).
   * Habilita **Firebase Authentication** (proveedor Correo/Contraseña) y **Cloud Firestore**.
   * Descarga el archivo de configuración `google-services.json` y colócalo dentro de la carpeta `app/` de tu proyecto.
4. **Sincronizar y Ejecutar:**
   * Espera a que Gradle sincronice las dependencias y ejecuta la aplicación en un emulador Android (API 26+) o dispositivo físico.

---

## 📄 Licencia

Este proyecto se encuentra distribuido bajo los términos de la licencia [MIT](LICENSE).
```



