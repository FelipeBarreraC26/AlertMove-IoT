package com.example.miaplicacionsegura.data.repository

import android.content.Context
import com.example.miaplicacionsegura.data.local.EventoLog
import com.example.miaplicacionsegura.data.local.SecurityDatabaseHelper
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryRepository(context: Context) {
    private val dbHelper = SecurityDatabaseHelper(context)
    private val firestore = FirebaseFirestore.getInstance()

    fun registrarAlerta(ownerUid: String, hogar: String, zona: String, descripcion: String) {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val timestampActual = formatoFecha.format(Date())

        // 1. Guardar localmente en SQLite (para acceso rápido offline)
        dbHelper.insertarEvento(hogar, zona, timestampActual, descripcion)

        // 2. Guardar en la nube de Firebase Firestore (Segmentado por propietario/usuario)
        val eventoCloud = hashMapOf(
            "ownerUid" to ownerUid,
            "hogar" to hogar,
            "zona" to zona,
            "timestamp" to timestampActual,
            "descripcion" to descripcion
        )
        firestore.collection("historial_eventos")
            .add(eventoCloud)
    }

    fun obtenerHistorialLocal(): List<EventoLog> {
        return dbHelper.obtenerHistorial()
    }

    fun obtenerHistorialLocalPorHogar(hogar: String): List<EventoLog> {
        return dbHelper.obtenerHistorialPorHogar(hogar)
    }

    fun borrarHistorial() {
        dbHelper.limpiarHistorial()
    }
}
