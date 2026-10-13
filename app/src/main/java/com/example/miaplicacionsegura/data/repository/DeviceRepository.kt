package com.example.miaplicacionsegura.data.repository

import com.example.miaplicacionsegura.CryptoUtils
import com.example.miaplicacionsegura.data.model.IoTDevice
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class DeviceRepository {
    private val db = FirebaseFirestore.getInstance()

    fun registrarDispositivo(ownerUid: String, nombre: String, mac: String, ubicacion: String, onResult: (Boolean) -> Unit) {
        val deviceId = db.collection("dispositivos").document().id
        
        // Encriptar la MAC address con AES por seguridad (Criterio de Ciberseguridad IoT)
        val macCifrada = CryptoUtils.encrypt(mac)

        val device = IoTDevice(
            id = deviceId,
            ownerUid = ownerUid,
            nombre = nombre,
            macAddress = macCifrada,
            ubicacion = ubicacion,
            alertaActiva = false,
            timestamp = Date().toString()
        )
        db.collection("dispositivos").document(deviceId).set(device)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun observarDispositivos(ownerUid: String, onDevicesChanged: (List<IoTDevice>) -> Unit) {
        db.collection("dispositivos")
            .whereEqualTo("ownerUid", ownerUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val lista = mutableListOf<IoTDevice>()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val device = doc.toObject(IoTDevice::class.java)
                        if (device != null) {
                            // Se mantiene la MAC cifrada en la UI para demostrar el cifrado AES
                            lista.add(device)
                        }
                    }
                }
                onDevicesChanged(lista)
            }
    }

    fun actualizarEstadoDispositivo(deviceId: String, alerta: Boolean, onSuccess: () -> Unit) {
        db.collection("dispositivos").document(deviceId)
            .update(
                mapOf(
                    "alertaActiva" to alerta,
                    "timestamp" to Date().toString()
                )
            )
            .addOnSuccessListener { onSuccess() }
    }

    fun actualizarDispositivo(deviceId: String, nombre: String, mac: String, ubicacion: String, onSuccess: () -> Unit) {
        val macCifrada = CryptoUtils.encrypt(mac)
        db.collection("dispositivos").document(deviceId)
            .update(
                mapOf(
                    "nombre" to nombre,
                    "macAddress" to macCifrada,
                    "ubicacion" to ubicacion,
                    "timestamp" to Date().toString()
                )
            )
            .addOnSuccessListener { onSuccess() }
    }

    fun eliminarDispositivo(deviceId: String, onSuccess: () -> Unit) {
        db.collection("dispositivos").document(deviceId)
            .delete()
            .addOnSuccessListener { onSuccess() }
    }
}
