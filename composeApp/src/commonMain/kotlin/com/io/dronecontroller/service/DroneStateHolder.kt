package com.io.dronecontroller.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel と Foreground Service が同一の通知状態を共有するためのホルダー。
 * Koin で single として登録する。
 */
class DroneStateHolder {
    private val _state = MutableStateFlow(DroneNotificationState())
    val state: StateFlow<DroneNotificationState> = _state.asStateFlow()

    fun update(batteryPercent: Int, isConnected: Boolean) {
        _state.value = DroneNotificationState(batteryPercent, isConnected)
    }
}
