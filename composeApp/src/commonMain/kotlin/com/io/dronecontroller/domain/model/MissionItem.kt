package com.io.dronecontroller.domain.model

data class MissionItem(
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val altitudeMeters: Float,
    val speedMS: Float = 5f
)
