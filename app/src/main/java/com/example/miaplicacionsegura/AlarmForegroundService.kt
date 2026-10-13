package com.example.miaplicacionsegura

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AlarmForegroundService : Service() {

    private val db = FirebaseFirestore.getInstance()
    private val channelId = "alarm_foreground_channel"
    private val foregroundNotificationId = 999

    private val deviceAlertStates = mutableMapOf<String, Boolean>()

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificacionForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = construirNotificacionPersistente()
        startForeground(foregroundNotificationId, notification)

        iniciarEscuchadoresBackground()

        return START_STICKY
    }

    private fun crearCanalNotificacionForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Servicio de Monitoreo en Segundo Plano",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene la alarma activa 24/7"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun construirNotificacionPersistente(): Notification {
        val intent = Intent(this, DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Hogar Seguro - Monitoreo Activo")
            .setContentText("El sistema de alarma está vigilando tus dispositivos IoT en segundo plano.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun iniciarEscuchadoresBackground() {
        val prefs = getSharedPreferences("iot_prefs", MODE_PRIVATE)
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val viewingShared = prefs.getBoolean("viewing_shared", false)
        val linkedUid = prefs.getString("linked_owner_uid", null)
        val activeOwnerUid = if (viewingShared && linkedUid != null) linkedUid else currentUid

        db.collection("dispositivos")
            .whereEqualTo("ownerUid", activeOwnerUid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val alerta = doc.getBoolean("alertaActiva") ?: false
                        val nombre = doc.getString("nombre") ?: "Dispositivo IoT"
                        val deviceId = doc.id

                        val estadoAnterior = deviceAlertStates[deviceId] ?: false
                        if (alerta && !estadoAnterior) {
                            NotificationHelper.mostrarAlerta(this, nombre, "Se detectó movimiento en $nombre.")
                        }
                        deviceAlertStates[deviceId] = alerta
                    }
                }
            }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
