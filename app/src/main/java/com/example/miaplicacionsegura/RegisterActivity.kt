package com.example.miaplicacionsegura

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.miaplicacionsegura.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etRegCorreo = findViewById<EditText>(R.id.etRegCorreo)
        val etRegPassword = findViewById<EditText>(R.id.etRegPassword)
        val etRegConfirmPassword = findViewById<EditText>(R.id.etRegConfirmPassword)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val btnVolverLogin = findViewById<Button>(R.id.btnVolverLogin)

        btnCrearCuenta.setOnClickListener {
            val email = etRegCorreo.text.toString().trim()
            val password = etRegPassword.text.toString().trim()
            val confirmPassword = etRegConfirmPassword.text.toString().trim()

            if (!authViewModel.validarEmailGmailHotmail(email)) {
                Toast.makeText(this, "Debe ser un correo válido de Gmail, Hotmail u Outlook", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!authViewModel.validarPasswordEstricta(password)) {
                Toast.makeText(this, "La contraseña debe tener mín. 6 caracteres, 1 letra, 1 número y 1 símbolo", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            authViewModel.register(email, password) { success, errorMsg ->
                if (success) {
                    Toast.makeText(this, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                    val serviceIntent = Intent(this, AlarmForegroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(this, serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, errorMsg ?: "Error en el registro", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnVolverLogin.setOnClickListener {
            finish()
        }
    }
}
