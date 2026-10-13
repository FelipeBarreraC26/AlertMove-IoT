package com.example.miaplicacionsegura.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class SensorRepository {
    private val db = FirebaseFirestore.getInstance()

    fun observeSensor(ownerUid: String, zona: String, onStatusChanged: (Boolean) -> Unit) {
        val documentId = "${zona}_$ownerUid"
        db.collection("sensores").document(documentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val alerta = snapshot.getBoolean("alertaActiva") ?: false
                    onStatusChanged(alerta)
                }
            }
    }

    fun updateSensorStatus(ownerUid: String, zona: String, activo: Boolean, dispositivo: String, onSuccess: () -> Unit) {
        val documentId = "${zona}_$ownerUid"
        val datosEvento = hashMapOf(
            "alertaActiva" to activo,
            "timestamp" to Date().toString(),
            "dispositivo" to dispositivo
        )
        db.collection("sensores").document(documentId).set(datosEvento)
            .addOnSuccessListener { onSuccess() }
    }
}
