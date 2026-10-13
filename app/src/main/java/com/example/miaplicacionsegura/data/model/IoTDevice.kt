package com.example.miaplicacionsegura.data.model

data class IoTDevice(
    val id: String = "",
    val ownerUid: String = "",
    val nombre: String = "",
    val macAddress: String = "",
    val ubicacion: String = "",
    val alertaActiva: Boolean = false,
    val timestamp: String = ""
)
