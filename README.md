
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






## 📄 Licencia

Este proyecto se encuentra distribuido bajo los términos de la licencia [MIT](LICENSE).

```



