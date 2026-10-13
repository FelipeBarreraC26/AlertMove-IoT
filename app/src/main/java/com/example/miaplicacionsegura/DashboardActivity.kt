package com.example.miaplicacionsegura

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.example.miaplicacionsegura.data.model.IoTDevice
import com.example.miaplicacionsegura.viewmodel.DashboardViewModel

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        verificarPermisoNotificaciones()
        iniciarServicioMonitoreo()

        val tvTituloPanel = findViewById<TextView>(R.id.tvTituloPanel)
        
        // Cargar configuración de privacidad y mostrar título del panel
        viewModel.cargarConfiguracionCompleta(viewModel.activeOwnerUid) { mostrarCorreo, _ ->
            tvTituloPanel.text = viewModel.getHomeTitle(mostrarCorreo)
        }

        val btnRegistrarDispositivo = findViewById<Button>(R.id.btnRegistrarDispositivo)
        val btnCambiarHogar = findViewById<Button>(R.id.btnCambiarHogar)
        val btnDesvincular = findViewById<Button>(R.id.btnDesvincular)
        val btnMenu = findViewById<Button>(R.id.btnMenu)

        // Configurar botones de hogar compartido si el usuario tiene vínculos
        if (viewModel.hasLinkedHome) {
            btnCambiarHogar.visibility = View.VISIBLE
            btnDesvincular.visibility = View.VISIBLE
            if (viewModel.viewingSharedHome) {
                btnCambiarHogar.text = "Cambiar a Mi Hogar (Personal)"
            } else {
                btnCambiarHogar.text = "Cambiar a Hogar Compartido"
            }
        }

        // Verificar si se abrió la Activity haciendo clic en la notificación
        if (intent.getBooleanExtra("ABRIR_HISTORIAL", false)) {
            mostrarHistorialLocal()
            intent.removeExtra("ABRIR_HISTORIAL")
        }

        // Cargar configuración completa para validar permisos de gestión del invitado
        viewModel.cargarConfiguracionCompleta(viewModel.activeOwnerUid) { _, permitirGestion ->
            val esInvitado = viewModel.viewingSharedHome
            val puedeGestionar = !esInvitado || permitirGestion

            if (puedeGestionar) {
                btnRegistrarDispositivo.visibility = View.VISIBLE
            } else {
                btnRegistrarDispositivo.visibility = View.GONE
            }

            // Observar dispositivos IoT registrados estrictamente por ownerUid
            viewModel.observarDispositivos { devices ->
                actualizarListaDispositivos(devices, puedeGestionar)
            }
        }

        // Registrar Nuevo Dispositivo IoT
        btnRegistrarDispositivo.setOnClickListener {
            mostrarDialogoRegistroDispositivo()
        }

        // Cambiar de vista entre Mi Hogar y Hogar Compartido
        btnCambiarHogar.setOnClickListener {
            viewModel.toggleHomeView()
            recreate()
        }

        // Desvincular Hogar Compartido
        btnDesvincular.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Desvincular Hogar")
                .setMessage("¿Estás seguro de que deseas desvincular el hogar compartido? Dejarás de recibir sus alertas.")
                .setPositiveButton("Sí, desvincular") { _, _ ->
                    viewModel.desvincularHogar()
                    Toast.makeText(this, "Hogar desvinculado con éxito", Toast.LENGTH_SHORT).show()
                    recreate()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Menú de Opciones (3 líneas)
        btnMenu.setOnClickListener { v ->
            val popup = PopupMenu(this, v)
            popup.menu.add(0, 1, 0, "Configuración de Usuario")
            popup.menu.add(0, 2, 1, "Generar / Ver Token de Acceso")
            popup.menu.add(0, 3, 2, "Vincular Dispositivo con Token")
            popup.menu.add(0, 4, 3, "Ver Historial de Eventos")
            popup.menu.add(0, 5, 4, "Cerrar Sesión")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        mostrarDialogoConfiguracion()
                        true
                    }
                    2 -> {
                        viewModel.generarToken { token ->
                            AlertDialog.Builder(this)
                                .setTitle("Token de Invitación")
                                .setMessage("Comparte este token con tu invitado:\n\n$token\n\nTu invitado deberá ingresarlo junto con tu contraseña para sincronizarse.")
                                .setPositiveButton("Entendido", null)
                                .show()
                        }
                        true
                    }
                    3 -> {
                        if (viewModel.hasLinkedHome) {
                            AlertDialog.Builder(this)
                                .setTitle("Aviso")
                                .setMessage("Ya estás vinculado a un hogar compartido. Debes desvincularlo primero desde el panel principal si deseas unirte a otro.")
                                .setPositiveButton("Entendido", null)
                                .show()
                        } else {
                            mostrarDialogoVinculacion()
                        }
                        true
                    }
                    4 -> {
                        mostrarHistorialLocal()
                        true
                    }
                    5 -> {
                        viewModel.cerrarSesion()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun esMacValida(mac: String): Boolean {
        val macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
        return mac.matches(macRegex)
    }

    private fun actualizarListaDispositivos(devices: List<IoTDevice>, puedeGestionar: Boolean) {
        val contenedor = findViewById<LinearLayout>(R.id.layoutContenedorDispositivos)
        contenedor.removeAllViews()

        if (devices.isEmpty()) {
            val tvVacio = TextView(this).apply {
                text = "No hay dispositivos IoT registrados.\n¡Registra uno nuevo para comenzar el monitoreo!"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                gravity = Gravity.CENTER
                setPadding(20, 60, 20, 60)
            }
            contenedor.addView(tvVacio)
            return
        }

        for (device in devices) {
            val cardView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                background = ContextCompat.getDrawable(context, android.R.drawable.dialog_holo_light_frame)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 20)
                }
            }

            val tvInfo = TextView(this).apply {
                text = "Sensor: ${device.nombre}\nUbicación: ${device.ubicacion}\nMAC: ${device.macAddress}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(0, 0, 0, 12)
            }

            val tvEstado = TextView(this).apply {
                text = if (device.alertaActiva) "¡ALERTA: Movimiento Detectado!" else "Estado: Zona Segura"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTypeface(null, Typeface.BOLD)
                setPadding(16, 16, 16, 16)
                setBackgroundColor(if (device.alertaActiva) Color.parseColor("#FFCDD2") else Color.parseColor("#E8F5E9"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            // Fila 1 de botones: Simular y Restablecer (Siempre disponible para supervisión)
            val layoutBotonesAccion = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, if (puedeGestionar) 8 else 0)
                }
            }

            val btnSimular = Button(this).apply {
                text = "Simular Alerta"
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(0, 0, 4, 0)
                }
                setOnClickListener {
                    viewModel.cambiarEstadoDispositivo(device.id, device.nombre, true) {
                        Toast.makeText(this@DashboardActivity, "Alerta activada en ${device.nombre}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val btnRestablecer = Button(this).apply {
                text = "Restablecer"
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#388E3C"))
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 0, 0)
                }
                setOnClickListener {
                    viewModel.cambiarEstadoDispositivo(device.id, device.nombre, false) {
                        Toast.makeText(this@DashboardActivity, "${device.nombre} restablecido", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            layoutBotonesAccion.addView(btnSimular)
            layoutBotonesAccion.addView(btnRestablecer)

            cardView.addView(tvInfo)
            cardView.addView(tvEstado)
            cardView.addView(layoutBotonesAccion)

            // Fila 2 de botones: Editar y Eliminar (Solo si puedeGestionar es true)
            if (puedeGestionar) {
                val layoutBotonesGestion = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 2f
                }

                val btnEditar = Button(this).apply {
                    text = "Editar"
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))
                    setTextColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(0, 0, 4, 0)
                    }
                    setOnClickListener {
                        mostrarDialogoEditarDispositivo(device)
                    }
                }

                val btnEliminar = Button(this).apply {
                    text = "Eliminar"
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#616161"))
                    setTextColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(4, 0, 0, 0)
                    }
                    setOnClickListener {
                        AlertDialog.Builder(this@DashboardActivity)
                            .setTitle("Eliminar Dispositivo")
                            .setMessage("¿Estás seguro de eliminar el sensor '${device.nombre}'?")
                            .setPositiveButton("Sí, eliminar") { _, _ ->
                                viewModel.eliminarDispositivoIoT(device.id) { exito ->
                                    if (exito) {
                                        Toast.makeText(this@DashboardActivity, "Dispositivo eliminado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }

                layoutBotonesGestion.addView(btnEditar)
                layoutBotonesGestion.addView(btnEliminar)
                cardView.addView(layoutBotonesGestion)
            }

            contenedor.addView(cardView)
        }
    }

    private fun generarMacAleatoria(): String {
        val hexChars = "0123456789ABCDEF"
        val parts = (1..6).map {
            "" + hexChars.random() + hexChars.random()
        }
        return parts.joinToString(":")
    }

    private fun mostrarDialogoRegistroDispositivo() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputNombre = EditText(this).apply {
            hint = "Nombre del Sensor (ej. Sensor Puerta)"
        }
        val inputMac = EditText(this).apply {
            hint = "MAC Address"
            setText(generarMacAleatoria())
        }
        val inputUbicacion = EditText(this).apply {
            hint = "Ubicación / Zona (ej. Garaje)"
        }

        val btnGenerarMac = Button(this).apply {
            text = "Generar MAC Aleatoria"
            setOnClickListener {
                inputMac.setText(generarMacAleatoria())
                Toast.makeText(this@DashboardActivity, "Nueva MAC aleatoria generada", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(inputNombre)
        layout.addView(inputMac)
        layout.addView(btnGenerarMac)
        layout.addView(inputUbicacion)

        AlertDialog.Builder(this)
            .setTitle("Registrar Dispositivo IoT")
            .setView(layout)
            .setPositiveButton("Registrar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val mac = inputMac.text.toString().trim()
                val ubicacion = inputUbicacion.text.toString().trim()

                if (nombre.isNotEmpty() && mac.isNotEmpty() && ubicacion.isNotEmpty()) {
                    if (!esMacValida(mac)) {
                        Toast.makeText(this, "Dirección MAC no válida. Formato requerido: XX:XX:XX:XX:XX:XX", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    viewModel.registrarDispositivoIoT(nombre, mac, ubicacion) { exito ->
                        if (exito) {
                            Toast.makeText(this, "Dispositivo registrado con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al registrar dispositivo", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarDispositivo(device: IoTDevice) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputNombre = EditText(this).apply {
            hint = "Nombre del Sensor"
            setText(device.nombre)
        }
        val inputMac = EditText(this).apply {
            hint = "MAC Address"
            val macPlana = try {
                CryptoUtils.decrypt(device.macAddress)
            } catch (e: Exception) {
                device.macAddress
            }
            setText(if (esMacValida(macPlana)) macPlana else generarMacAleatoria())
        }
        val inputUbicacion = EditText(this).apply {
            hint = "Ubicación / Zona"
            setText(device.ubicacion)
        }

        val btnGenerarMac = Button(this).apply {
            text = "Generar MAC Aleatoria"
            setOnClickListener {
                inputMac.setText(generarMacAleatoria())
                Toast.makeText(this@DashboardActivity, "Nueva MAC aleatoria generada", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(inputNombre)
        layout.addView(inputMac)
        layout.addView(btnGenerarMac)
        layout.addView(inputUbicacion)

        AlertDialog.Builder(this)
            .setTitle("Editar Dispositivo IoT")
            .setView(layout)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val mac = inputMac.text.toString().trim()
                val ubicacion = inputUbicacion.text.toString().trim()

                if (nombre.isNotEmpty() && mac.isNotEmpty() && ubicacion.isNotEmpty()) {
                    if (!esMacValida(mac)) {
                        Toast.makeText(this, "Dirección MAC no válida. Formato requerido: XX:XX:XX:XX:XX:XX", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    viewModel.editarDispositivoIoT(device.id, nombre, mac, ubicacion) { exito ->
                        if (exito) {
                            Toast.makeText(this, "Dispositivo actualizado con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al actualizar dispositivo", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun iniciarServicioMonitoreo() {
        val serviceIntent = Intent(this, AlarmForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun mostrarDialogoConfiguracion() {
        viewModel.cargarConfiguracionCompleta(viewModel.currentUid) { mostrarActual, permitirActual ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val tvEmail = TextView(this).apply {
                text = "Usuario actual:\n${viewModel.currentUserEmail}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 20)
            }

            val switchCorreo = CheckBox(this).apply {
                text = "Mostrar mi correo real a los invitados"
                isChecked = mostrarActual
            }

            val switchGestion = CheckBox(this).apply {
                text = "Permitir que invitados agreguen/editen/eliminen dispositivos"
                isChecked = permitirActual
            }

            layout.addView(tvEmail)
            layout.addView(switchCorreo)
            layout.addView(switchGestion)

            AlertDialog.Builder(this)
                .setTitle("Configuración de Usuario")
                .setView(layout)
                .setPositiveButton("Guardar") { _, _ ->
                    val nuevoCorreo = switchCorreo.isChecked
                    val nuevaGestion = switchGestion.isChecked
                    viewModel.guardarConfiguracionCompleta(nuevoCorreo, nuevaGestion) { exito ->
                        if (exito) {
                            Toast.makeText(this, "Configuración actualizada con éxito", Toast.LENGTH_SHORT).show()
                            recreate()
                        } else {
                            Toast.makeText(this, "Error al guardar configuración", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun mostrarHistorialLocal() {
        viewModel.cargarConfiguracionDueño(viewModel.activeOwnerUid) { mostrarCorreo ->
            val historial = viewModel.obtenerHistorialActual(mostrarCorreo)
            if (historial.isEmpty()) {
                Toast.makeText(this, "No hay eventos registrados para este hogar", Toast.LENGTH_SHORT).show()
                return@cargarConfiguracionDueño
            }
            val mensaje = historial.joinToString(separator = "\n\n────────────────────────\n\n") { evento ->
                "Hogar: ${evento.hogar}\nZona: ${evento.zona}\nFecha: ${evento.timestamp}\nDetalle: ${evento.descripcion}"
            }
            AlertDialog.Builder(this)
                .setTitle("Historial de Eventos (${viewModel.getHomeTitle(mostrarCorreo)})")
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Limpiar Historial") { _, _ ->
                    AlertDialog.Builder(this)
                        .setTitle("Confirmar")
                        .setMessage("¿Deseas eliminar el historial de este hogar?")
                        .setPositiveButton("Sí, limpiar") { _, _ ->
                            viewModel.limpiarHistorialLocal(mostrarCorreo)
                            Toast.makeText(this, "Historial borrado con éxito", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                .show()
        }
    }

    private fun mostrarDialogoVinculacion() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputTokenEdit = EditText(this).apply {
            hint = "Ingrese el Token de Invitación"
        }
        val inputPass = EditText(this).apply {
            hint = "Contraseña del Propietario"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(inputTokenEdit)
        layout.addView(inputPass)

        AlertDialog.Builder(this)
            .setTitle("Vincular Dispositivo (Invitado)")
            .setView(layout)
            .setPositiveButton("Vincular") { _, _ ->
                val token = inputTokenEdit.text.toString()
                val pass = inputPass.text.toString()
                viewModel.validarVinculacionInvitado(token, pass) { _, mensaje ->
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                    recreate()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
