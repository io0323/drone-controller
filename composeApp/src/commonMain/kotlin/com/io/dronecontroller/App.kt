package com.io.dronecontroller

import androidx.compose.runtime.*
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
    data object Mock : Screen()
}

@Composable
fun App(
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onMock: () -> Unit,
) {
    var screen by remember { mutableStateOf<Screen>(Screen.Connection) }
//    var screen by remember { mutableStateOf<Screen>(Screen.Mock) }
    when (val s = screen) {
        is Screen.Mock -> {
            DroneControllerScreen(
                onNavigateToMission = { lat, lng ->
                    screen = Screen.MissionPlanning(lat, lng)
                },
                onStopService = {
                    onStopService()
                    screen = Screen.Connection
                }
            )
        }
        is Screen.Connection -> {
            ConnectionScreen(
                onConnected = {
                    onStartService()
                    screen = Screen.Controller
                },
                onMock = {
                    screen = Screen.Mock
                }
            )
        }
        is Screen.Controller -> {
            DroneControllerScreen(
                onNavigateToMission = { lat, lng ->
                    screen = Screen.MissionPlanning(lat, lng)
                },
                onStopService = {
                    onStopService()
                    screen = Screen.Connection
                }
            )
        }
        is Screen.MissionPlanning -> {
            MissionPlanningScreen(
                onBack = {
                    screen = Screen.Controller
                },
                droneLat = s.droneLat,
                droneLng = s.droneLng,
            )
        }
    }
}
