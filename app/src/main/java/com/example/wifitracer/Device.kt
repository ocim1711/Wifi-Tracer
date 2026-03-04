package com.example.wifitracer

data class Device(
    val ip: String,
    val mac: String = "Unknown",
    val vendor: String = "Unknown",
    val isGateway: Boolean = false,
    var isCut: Boolean = false
)
