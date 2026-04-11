package com.io.dronecontroller

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
