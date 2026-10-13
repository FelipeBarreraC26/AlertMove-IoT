package com.example.miaplicacionsegura.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.miaplicacionsegura.data.local.EventoLog
import com.example.miaplicacionsegura.data.model.IoTDevice
import com.example.miaplicacionsegura.data.repository.AuthRepository
import com.example.miaplicacionsegura.data.repository.DeviceRepository
import com.example.miaplicacionsegura.data.repository.HistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ViewModel que gestiona la lógica de negocio y el estado reactivo del panel de control IoT (Dashboard).
 * 
 * Capa de Arquitectura Android: ViewModel (MVVM) - Actúa como puente entre la Vista (Activity)
 * y los Repositorios de datos (Firebase y SQLite).
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository = DeviceRepository()
    private val authRepository = AuthRepository()
    private val historyRepository = HistoryRepository(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("iot_prefs", Context.MODE_PRIVATE)

    /**
     * Identificador único (UID) del usuario actualmente autenticado en Firebase.
     */
    val currentUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: "default"

    /**
     * Correo electrónico del usuario actual.
     */
    val currentUserEmail: String = FirebaseAuth.getInstance().currentUser?.email ?: "usuario@iot.com"

    /**
     * Indica si el usuario actual tiene un hogar compartido vinculado como invitado.
     */
    val hasLinkedHome: Boolean
        get() = prefs.getString("linked_owner_uid", null) != null

    /**
     * Estado que determina si el usuario está visualizando el hogar compartido.
     */
    var viewingSharedHome: Boolean
        get() = prefs.getBoolean("viewing_shared", false)
        set(value) = prefs.edit().putBoolean("viewing_shared", value).apply()

    /**
     * Obtiene el UID del propietario activo (ya sea el propio usuario o el hogar compartido vinculado).
     * 
     * @return [String] UID del propietario activo de los dispositivos.
     */
    val activeOwnerUid: String
        get() {
            val linkedUid = prefs.getString("linked_owner_uid", null)
            return if (viewingSharedHome && linkedUid != null) linkedUid else currentUid
        }

    /**
     * Alterna la vista entre el hogar personal y el hogar compartido.
     */
    fun toggleHomeView() {
        viewingSharedHome = !viewingSharedHome
    }

    /**
     * Desvincula el hogar compartido actual, restableciendo la vista al hogar personal.
     */
    fun desvincularHogar() {
        prefs.edit()
            .remove("linked_owner_uid")
            .remove("linked_owner_email")
            .putBoolean("is_guest", false)
            .putBoolean("viewing_shared", false)
            .apply()
    }

    /**
     * Obtiene el título formateado de la cabecera del panel según la configuración de privacidad del propietario.
     * 
     * @param mostrarCorreoDueño [Boolean] Define si se muestra el correo real o una versión enmascarada por privacidad.
     * @return [String] Título descriptivo del hogar actual.
     */
    fun getHomeTitle(mostrarCorreoDueño: Boolean): String {
        val ownerEmail = prefs.getString("linked_owner_email", "") ?: ""
        return if (viewingSharedHome && ownerEmail.isNotEmpty()) {
            if (mostrarCorreoDueño) {
                "Monitoreo IoT - Hogar de ($ownerEmail)"
            } else {
                val maskedEmail = if (ownerEmail.contains("@")) {
                    val parts = ownerEmail.split("@")
                    val name = parts[0]
                    val maskedName = if (name.length > 2) name.take(2) + "***" else "***"
                    "$maskedName@${parts[1]}"
                } else {
                    "Hogar Protegido"
                }
                "Monitoreo IoT - $maskedEmail"
            }
        } else {
            "Monitoreo IoT - Mi Hogar ($currentUserEmail)"
        }
    }

    /**
     * Carga la configuración de privacidad del propietario desde Cloud Firestore.
     * 
     * @param ownerUid [String] UID del propietario cuyas preferencias se desean consultar.
     * @param onLoaded [Function1] Callback que recibe [Boolean] indicando si el correo debe mostrarse públicamente.
     */
    fun cargarConfiguracionDueño(ownerUid: String, onLoaded: (Boolean) -> Unit) {
        firestore.collection("configuracion").document(ownerUid).get()
            .addOnSuccessListener { doc ->
                val mostrar = doc.getBoolean("mostrar_correo") ?: false
                onLoaded(mostrar)
            }
            .addOnFailureListener {
                onLoaded(false)
            }
    }

    /**
     * Guarda la configuración de privacidad del propietario en Cloud Firestore.
     * 
     * @param mostrarCorreo [Boolean] Nuevo estado de la preferencia de privacidad.
     * @param onComplete [Function1] Callback que retorna [Boolean] indicando si la operación fue exitosa.
     */
    fun guardarConfiguracionDueño(mostrarCorreo: Boolean, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf("mostrar_correo" to mostrarCorreo)
        firestore.collection("configuracion").document(currentUid).set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Observa en tiempo real los dispositivos IoT registrados para el propietario activo.
     * 
     * @param onDevicesChanged [Function1] Callback que recibe una lista actualizada de [IoTDevice].
     */
    fun observarDispositivos(onDevicesChanged: (List<IoTDevice>) -> Unit) {
        deviceRepository.observarDispositivos(activeOwnerUid, onDevicesChanged)
    }

    /**
     * Registra un nuevo dispositivo IoT en Cloud Firestore asociado al usuario actual.
     * 
     * @param nombre [String] Nombre descriptivo del sensor.
     * @param mac [String] Dirección MAC física o simulada del dispositivo.
     * @param ubicacion [String] Ubicación o zona de la casa donde se ubica.
     * @param onResult [Function1] Callback que retorna [Boolean] indicando el éxito del registro.
     */
    fun registrarDispositivoIoT(nombre: String, mac: String, ubicacion: String, onResult: (Boolean) -> Unit) {
        deviceRepository.registrarDispositivo(currentUid, nombre, mac, ubicacion, onResult)
    }

    /**
     * Actualiza la información de un dispositivo IoT existente.
     * 
     * @param deviceId [String] Identificador único del dispositivo.
     * @param nombre [String] Nuevo nombre del sensor.
     * @param mac [String] Nueva dirección MAC.
     * @param ubicacion [String] Nueva ubicación.
     * @param onResult [Function1] Callback que retorna [Boolean] indicando éxito.
     */
    fun editarDispositivoIoT(deviceId: String, nombre: String, mac: String, ubicacion: String, onResult: (Boolean) -> Unit) {
        deviceRepository.actualizarDispositivo(deviceId, nombre, mac, ubicacion) {
            onResult(true)
        }
    }

    /**
     * Elimina un dispositivo IoT registrado de Cloud Firestore.
     * 
     * @param deviceId [String] Identificador único del dispositivo a eliminar.
     * @param onResult [Function1] Callback que retorna [Boolean] indicando éxito.
     */
    fun eliminarDispositivoIoT(deviceId: String, onResult: (Boolean) -> Unit) {
        deviceRepository.eliminarDispositivo(deviceId) {
            onResult(true)
        }
    }

    /**
     * Cambia el estado de alerta de un dispositivo IoT y registra el evento en el historial local y en la nube.
     * 
     * @param deviceId [String] Identificador único del dispositivo.
     * @param dispositivoNombre [String] Nombre del sensor.
     * @param alerta [Boolean] True si se activa la alerta, false si se restablece.
     * @param onSuccess [Function0] Callback ejecutado al completar la actualización.
     */
    fun cambiarEstadoDispositivo(deviceId: String, dispositivoNombre: String, alerta: Boolean, onSuccess: () -> Unit) {
        deviceRepository.actualizarEstadoDispositivo(deviceId, alerta) {
            if (alerta) {
                cargarConfiguracionDueño(activeOwnerUid) { mostrarCorreo ->
                    historyRepository.registrarAlerta(activeOwnerUid, getHomeTitle(mostrarCorreo), dispositivoNombre, "Movimiento detectado en dispositivo IoT")
                }
            }
            onSuccess()
        }
    }

    /**
     * Obtiene el historial local de eventos almacenados en SQLite para el hogar actual.
     * 
     * @param mostrarCorreoDueño [Boolean] Preferencia de privacidad para evaluar el título del hogar.
     * @return [List] Lista de objetos [EventoLog].
     */
    fun obtenerHistorialActual(mostrarCorreoDueño: Boolean): List<EventoLog> {
        return historyRepository.obtenerHistorialLocalPorHogar(getHomeTitle(mostrarCorreoDueño))
    }

    /**
     * Limpia el historial local de eventos en SQLite para el hogar actual.
     * 
     * @param mostrarCorreoDueño [Boolean] Preferencia de privacidad del hogar a limpiar.
     */
    fun limpiarHistorialLocal(mostrarCorreoDueño: Boolean) {
        val hogarActual = getHomeTitle(mostrarCorreoDueño)
        val db = com.example.miaplicacionsegura.data.local.SecurityDatabaseHelper(getApplication())
        val dbWritable = db.writableDatabase
        dbWritable.delete("eventos_log", "hogar = ?", arrayOf(hogarActual))
        dbWritable.close()
        db.close()
    }

    /**
     * Genera un token único de invitación para compartir acceso al hogar en Cloud Firestore.
     * 
     * @param onResult [Function1] Callback que devuelve el token generado en formato [String].
     */
    fun generarToken(onResult: (String) -> Unit) {
        val token = "IOT-${currentUid.take(6).uppercase()}-${(100..999).random()}"
        val data = hashMapOf(
            "ownerUid" to currentUid,
            "ownerEmail" to currentUserEmail
        )

        firestore.collection("invitaciones").document(token).set(data)
            .addOnSuccessListener { onResult(token) }
            .addOnFailureListener { onResult("IOT-SECURE-2024") }
    }

    /**
     * Valida un token de invitación y la contraseña del propietario para vincular el dispositivo de un invitado.
     * 
     * @param tokenInput [String] Token ingresado por el invitado.
     * @param ownerPass [String] Contraseña del propietario para verificación de seguridad.
     * @param onResult [Function2] Callback que retorna [Boolean] de éxito y un mensaje descriptivo [String].
     */
    fun validarVinculacionInvitado(tokenInput: String, ownerPass: String, onResult: (Boolean, String) -> Unit) {
        val token = tokenInput.trim().uppercase()
        firestore.collection("invitaciones").document(token).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val ownerUid = document.getString("ownerUid") ?: ""
                    val ownerEmail = document.getString("ownerEmail") ?: ""

                    if (ownerUid.isNotEmpty() && ownerEmail.isNotEmpty()) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(ownerEmail, ownerPass)
                            .addOnSuccessListener {
                                prefs.edit()
                                    .putString("linked_owner_uid", ownerUid)
                                    .putString("linked_owner_email", ownerEmail)
                                    .putBoolean("is_guest", true)
                                    .putBoolean("viewing_shared", true)
                                    .apply()

                                onResult(true, "Dispositivo vinculado con éxito al hogar de $ownerEmail")
                            }
                            .addOnFailureListener {
                                onResult(false, "Contraseña del propietario incorrecta.")
                            }
                    } else {
                        onResult(false, "Token de invitación inválido.")
                    }
                } else {
                    onResult(false, "El token ingresado no existe.")
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Error: ${e.message}")
            }
    }

    /**
     * Cierra la sesión activa del usuario y limpia los datos temporales en preferencias.
     */
    fun cerrarSesion() {
        prefs.edit().clear().apply()
        authRepository.signOut()
    }
}
