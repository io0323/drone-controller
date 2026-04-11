package com.io.dronecontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.io.dronecontroller.ui.connection.ConnectionScreen
import com.io.dronecontroller.ui.controller.DroneControllerScreen
import com.io.dronecontroller.ui.controller.MockDroneControllerViewModel
import com.io.dronecontroller.ui.login.LoginScreen
import com.io.dronecontroller.ui.mission.MissionPlanningScreen

private sealed class Screen {
    data object Login : Screen()
    data object Connection : Screen()
    data object Controller : Screen()
    data object DebugController : Screen()
    data class MissionPlanning(val droneLat: Double, val droneLng: Double) : Screen()
}

@Composable
fun App(onStartService: () -> Unit = {}) {
    var screen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (val s = screen) {
        is Screen.Login ->
            LoginScreen(onLoggedIn = { screen = Screen.Connection })
        is Screen.Connection ->
            ConnectionScreen(
                onStartService = onStartService,
                onConnected = { screen = Screen.Controller },
                onDebugMode = { screen = Screen.DebugController },
            )
        is Screen.Controller ->
            DroneControllerScreen(
                onNavigateToMission = { lat, lng -> screen = Screen.MissionPlanning(lat, lng) },
            )
        is Screen.DebugController ->
            DroneControllerScreen(
                onNavigateToMission = { lat, lng -> screen = Screen.MissionPlanning(lat, lng) },
                viewModel = MockDroneControllerViewModel(),
            )
        is Screen.MissionPlanning ->
            MissionPlanningScreen(
                onBack = { screen = Screen.Controller },
                droneLat = s.droneLat,
                droneLng = s.droneLng,
            )
    }
}
