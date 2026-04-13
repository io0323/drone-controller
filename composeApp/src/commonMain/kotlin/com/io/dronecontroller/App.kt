package com.io.dronecontroller

import androidx.compose.runtime.*
import com.io.dronecontroller.ui.connection.ConnectionScreen
import com.io.dronecontroller.ui.controller.DroneControllerScreen
import com.io.dronecontroller.ui.mission.MissionPlanningScreen
import com.io.dronecontroller.ui.mission.MockMissionPlanningViewModel

private sealed class Screen {
    data object Connection : Screen()
    data object Controller : Screen()
    data class MissionPlanning(
        val droneLat: Double,
        val droneLng: Double,
        val isMock: Boolean = false,
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
                    screen = Screen.MissionPlanning(lat, lng, isMock = true)
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
            if (s.isMock) {
                val mockVm = remember { MockMissionPlanningViewModel() }
                MissionPlanningScreen(
                    onBack = { screen = Screen.Mock },
                    droneLat = s.droneLat,
                    droneLng = s.droneLng,
                    viewModel = mockVm,
                )
            } else {
                MissionPlanningScreen(
                    onBack = { screen = Screen.Controller },
                    droneLat = s.droneLat,
                    droneLng = s.droneLng,
                )
            }
        }
    }
}
