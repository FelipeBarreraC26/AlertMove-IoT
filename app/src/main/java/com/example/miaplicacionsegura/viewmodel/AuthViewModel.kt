package com.example.miaplicacionsegura.viewmodel

import androidx.lifecycle.ViewModel
import com.example.miaplicacionsegura.data.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    fun validarEmailGmailHotmail(email: String): Boolean {
        val emailLimpio = email.trim().lowercase()
        // Acepta únicamente dominios de Gmail, Hotmail u Outlook
        val regex = "^[A-Za-z0-9._%+-]+@(gmail\\.com|hotmail\\.com|outlook\\.com)$".toRegex()
        return emailLimpio.matches(regex)
    }

    fun validarPasswordEstricta(password: String): Boolean {
        val regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&._-])[A-Za-z\\d@\$!%*#?&._-]{6,}$".toRegex()
        return password.matches(regex)
    }

    fun traducirErrorFirebase(errorMsg: String?): String {
        val message = errorMsg?.lowercase() ?: ""
        return when {
            message.contains("email-already-in-use") || message.contains("already in use") -> 
                "El correo electrónico ya se encuentra registrado."
            message.contains("invalid-email") || message.contains("badly formatted") -> 
                "El formato del correo electrónico no es válido."
            message.contains("wrong-password") || message.contains("invalid-credential") || message.contains("user-not-found") || message.contains("invalid_login_credentials") || message.contains("password is invalid") || message.contains("no user record") || message.contains("credentials") || message.contains("auth/") -> 
                "Correo electrónico o contraseña incorrectos."
            message.contains("weak-password") -> 
                "La contraseña es demasiado débil (mínimo 6 caracteres)."
            message.contains("network-request-failed") -> 
                "Error de conexión. Verifique su red de internet."
            else -> "Correo electrónico o contraseña incorrectos."
        }
    }

    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        if (!validarEmailGmailHotmail(email)) {
            onResult(false, "Debe ser un correo válido de Gmail, Hotmail u Outlook")
            return
        }
        if (pass.isBlank()) {
            onResult(false, "La contraseña no puede estar vacía")
            return
        }
        repository.login(email, pass) { success, msg ->
            if (success) {
                onResult(true, null)
            } else {
                onResult(false, traducirErrorFirebase(msg))
            }
        }
    }

    fun register(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        if (!validarEmailGmailHotmail(email)) {
            onResult(false, "Debe ser un correo válido de Gmail, Hotmail u Outlook")
            return
        }
        if (!validarPasswordEstricta(pass)) {
            onResult(false, "Debe tener mín. 6 caracteres, 1 letra, 1 número y 1 símbolo")
            return
        }
        repository.register(email, pass) { success, msg ->
            if (success) {
                onResult(true, null)
            } else {
                onResult(false, traducirErrorFirebase(msg))
            }
        }
    }
}
