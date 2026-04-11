package com.io.dronecontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.io.dronecontroller.ui.connection.ConnectionScreen
import com.io.dronecontroller.ui.controller.DroneControllerScreen
import com.io.dronecontroller.ui.mission.MissionPlanningScreen

private sealed class Screen {
    data object Connection : Screen()
    data object Controller : Screen()

    data class MissionPlanning(
        val droneLat: Double,
        val droneLng: Double,
    ) : Screen()
}

@Composable
fun App(onStartService: () -> Unit = {}) {
    var screen by remember { mutableStateOf<Screen>(Screen.Connection) }

    when (val s = screen) {
        is Screen.Controller ->
            DroneControllerScreen(
                onNavigateToMission = { lat, lng -> screen = Screen.MissionPlanning(lat, lng) },
            )
        is Screen.MissionPlanning ->
            MissionPlanningScreen(
                onBack = { screen = Screen.Controller },
                droneLat = s.droneLat,
                droneLng = s.droneLng,
            )
    }
}
