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

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository = DeviceRepository()
    private val authRepository = AuthRepository()
    private val historyRepository = HistoryRepository(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("iot_prefs", Context.MODE_PRIVATE)

    val currentUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
    val currentUserEmail: String = FirebaseAuth.getInstance().currentUser?.email ?: "usuario@iot.com"

    val hasLinkedHome: Boolean
        get() = prefs.getString("linked_owner_uid", null) != null

    var viewingSharedHome: Boolean
        get() = prefs.getBoolean("viewing_shared", false)
        set(value) = prefs.edit().putBoolean("viewing_shared", value).apply()

    val activeOwnerUid: String
        get() {
            val linkedUid = prefs.getString("linked_owner_uid", null)
            return if (viewingSharedHome && linkedUid != null) linkedUid else currentUid
        }

    fun toggleHomeView() {
        viewingSharedHome = !viewingSharedHome
    }

    fun desvincularHogar() {
        prefs.edit()
            .remove("linked_owner_uid")
            .remove("linked_owner_email")
            .putBoolean("is_guest", false)
            .putBoolean("viewing_shared", false)
            .apply()
    }

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

    fun cargarConfiguracionCompleta(ownerUid: String, onLoaded: (Boolean, Boolean) -> Unit) {
        firestore.collection("configuracion").document(ownerUid).get()
            .addOnSuccessListener { doc ->
                val mostrar = doc.getBoolean("mostrar_correo") ?: false
                val permitirGestion = doc.getBoolean("permitir_gestion_invitados") ?: false
                onLoaded(mostrar, permitirGestion)
            }
            .addOnFailureListener {
                onLoaded(false, false)
            }
    }

    fun guardarConfiguracionCompleta(mostrarCorreo: Boolean, permitirGestion: Boolean, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf(
            "mostrar_correo" to mostrarCorreo,
            "permitir_gestion_invitados" to permitirGestion
        )
        firestore.collection("configuracion").document(currentUid).set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Mantener compatibilidad con llamadas anteriores de cargarConfiguracionDueño
    fun cargarConfiguracionDueño(ownerUid: String, onLoaded: (Boolean) -> Unit) {
        cargarConfiguracionCompleta(ownerUid) { mostrar, _ ->
            onLoaded(mostrar)
        }
    }

    fun observarDispositivos(onDevicesChanged: (List<IoTDevice>) -> Unit) {
        deviceRepository.observarDispositivos(activeOwnerUid, onDevicesChanged)
    }

    fun registrarDispositivoIoT(nombre: String, mac: String, ubicacion: String, onResult: (Boolean) -> Unit) {
        deviceRepository.registrarDispositivo(currentUid, nombre, mac, ubicacion, onResult)
    }

    fun editarDispositivoIoT(deviceId: String, nombre: String, mac: String, ubicacion: String, onResult: (Boolean) -> Unit) {
        deviceRepository.actualizarDispositivo(deviceId, nombre, mac, ubicacion) {
            onResult(true)
        }
    }

    fun eliminarDispositivoIoT(deviceId: String, onResult: (Boolean) -> Unit) {
        deviceRepository.eliminarDispositivo(deviceId) {
            onResult(true)
        }
    }

    fun cambiarEstadoDispositivo(deviceId: String, dispositivoNombre: String, alerta: Boolean, onSuccess: () -> Unit) {
        deviceRepository.actualizarEstadoDispositivo(deviceId, alerta) {
            if (alerta) {
                cargarConfiguracionCompleta(activeOwnerUid) { mostrarCorreo, _ ->
                    historyRepository.registrarAlerta(activeOwnerUid, getHomeTitle(mostrarCorreo), dispositivoNombre, "Movimiento detectado en dispositivo IoT")
                }
            }
            onSuccess()
        }
    }

    fun obtenerHistorialActual(mostrarCorreoDueño: Boolean): List<EventoLog> {
        return historyRepository.obtenerHistorialLocalPorHogar(getHomeTitle(mostrarCorreoDueño))
    }

    fun limpiarHistorialLocal(mostrarCorreoDueño: Boolean) {
        val hogarActual = getHomeTitle(mostrarCorreoDueño)
        val db = com.example.miaplicacionsegura.data.local.SecurityDatabaseHelper(getApplication())
        val dbWritable = db.writableDatabase
        dbWritable.delete("eventos_log", "hogar = ?", arrayOf(hogarActual))
        dbWritable.close()
        db.close()
    }

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

    fun cerrarSesion() {
        prefs.edit().clear().apply()
        authRepository.signOut()
    }
}
