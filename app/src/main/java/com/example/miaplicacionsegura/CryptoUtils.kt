package com.example.miaplicacionsegura

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    // Clave AES de 16 bytes para encriptación simétrica de identificadores IoT (MAC)
    private const val ALGORITHM = "AES"
    private const val SECRET_KEY = "MiClaveIoTSuperS"

    fun encrypt(value: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
        } catch (e: Exception) {
            value
        }
    }

    fun decrypt(encryptedValue: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedValue, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedValue
        }
    }
}
