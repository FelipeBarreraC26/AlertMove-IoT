package com.example.miaplicacionsegura

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.example.miaplicacionsegura.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            verificarYLimpiarSesionFantasma()
            iniciarServicioBackground()
            irAlDashboard()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tilCorreo = findViewById<TextInputLayout>(R.id.tilCorreo)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        // Botón de Inicio de Sesión
        btnIngresar.setOnClickListener {
            val email = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            tilCorreo.error = null
            tilPassword.error = null

            if (!authViewModel.validarEmailGmailHotmail(email)) {
                tilCorreo.error = "Debe ser un correo válido de Gmail, Hotmail u Outlook"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword.error = "La contraseña no puede estar vacía"
                return@setOnClickListener
            }

            authViewModel.login(email, password) { success, errorMsg ->
                if (success) {
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    verificarYLimpiarSesionFantasma()
                    iniciarServicioBackground()
                    irAlDashboard()
                } else {
                    val msg = errorMsg ?: "Correo electrónico o contraseña incorrectos"
                    if (msg.contains("correo") || msg.contains("formato")) {
                        tilCorreo.error = msg
                    } else {
                        tilPassword.error = msg
                    }
                }
            }
        }

        // Botón de Registro -> Abrir pantalla de registro con doble contraseña
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun verificarYLimpiarSesionFantasma() {
        val prefs = getSharedPreferences("iot_prefs", Context.MODE_PRIVATE)
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val savedUid = prefs.getString("logged_in_uid", null)

        if (savedUid != currentUid) {
            prefs.edit().clear().putString("logged_in_uid", currentUid).apply()
        }
    }

    private fun iniciarServicioBackground() {
        val serviceIntent = Intent(this, AlarmForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun irAlDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
