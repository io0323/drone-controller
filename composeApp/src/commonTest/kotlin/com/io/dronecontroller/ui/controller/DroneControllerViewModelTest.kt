package com.io.dronecontroller.ui.controller

import app.cash.turbine.test
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.model.BleDevice
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.model.DroneState
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.repository.BleRepositoryContract
import com.io.dronecontroller.domain.usecase.CapturePhotoUseCase
import com.io.dronecontroller.domain.usecase.LandUseCase
import com.io.dronecontroller.domain.usecase.ObserveBleConnectionStatusUseCase
import com.io.dronecontroller.domain.usecase.ObserveBleControllerStateUseCase
import com.io.dronecontroller.domain.usecase.ObserveDroneStateUseCase
import com.io.dronecontroller.domain.usecase.ReturnToLaunchUseCase
import com.io.dronecontroller.domain.usecase.SendManualControlUseCase
import com.io.dronecontroller.domain.usecase.StartVideoUseCase
import com.io.dronecontroller.domain.usecase.StopVideoUseCase
import com.io.dronecontroller.domain.usecase.TakeoffUseCase
import com.io.dronecontroller.fake.FakeMavlinkRepository
import com.io.dronecontroller.service.DroneStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DroneControllerViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val mavRepo = FakeMavlinkRepository()
    private val bleRepo = FakeBleRepository()
    private lateinit var vm: DroneControllerViewModel

    private fun createViewModel(): DroneControllerViewModel =
        DroneControllerViewModel(
            observeDroneState = ObserveDroneStateUseCase(mavRepo),
            takeoffUseCase = TakeoffUseCase(mavRepo),
            landUseCase = LandUseCase(mavRepo),
            returnToLaunchUseCase = ReturnToLaunchUseCase(mavRepo),
            sendManualControl = SendManualControlUseCase(mavRepo),
            observeBleControllerState = ObserveBleControllerStateUseCase(bleRepo),
            observeBleConnectionStatus = ObserveBleConnectionStatusUseCase(bleRepo),
            droneStateHolder = DroneStateHolder(),
            capturePhotoUseCase = CapturePhotoUseCase(mavRepo),
            startVideoUseCase = StartVideoUseCase(mavRepo),
            stopVideoUseCase = StopVideoUseCase(mavRepo),
        )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        vm = createViewModel()
    }

    @AfterTest
    fun tearDown() {
        vm.stopObserving()
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態は未接続`() =
        runTest(dispatcher) {
            val state = vm.uiState.value
            assertTrue(state.connectionStatus is ConnectionStatus.Disconnected)
            assertEquals(0, state.batteryPercent)
        }

    @Test
    fun `takeoff成功時にcommandStatusがSuccessになる`() =
        runTest(dispatcher) {
            mavRepo.takeoffResult = RunStatus.Success(Unit)

            vm.uiState.test(timeout = 3.seconds) {
                awaitItem()

                vm.takeoff()
                advanceUntilIdle()

                val loading = awaitItem()
                assertTrue(loading.commandStatus is RunStatus.Loading)

                val success = awaitItem()
                assertTrue(success.commandStatus is RunStatus.Success)

                cancelAndIgnoreRemainingEvents()
            }

            advanceUntilIdle() // ★ 終了保証
        }

    @Test
    fun `takeoff失敗時にcommandStatusがErrorになる`() =
        runTest(dispatcher) {
            mavRepo.takeoffResult = RunStatus.Error("離陸失敗")

            vm.uiState.test(timeout = 3.seconds) {
                awaitItem()

                vm.takeoff()
                advanceUntilIdle()

                awaitItem() // Loading

                val error = awaitItem()
                assertTrue(error.commandStatus is RunStatus.Error)
                assertEquals("離陸コマンドが失敗しました", (error.commandStatus as RunStatus.Error).message)

                cancelAndIgnoreRemainingEvents()
            }

            advanceUntilIdle()
        }

    @Test
    fun `land成功時にcommandStatusがSuccessになる`() =
        runTest(dispatcher) {
            mavRepo.landResult = RunStatus.Success(Unit)

            vm.uiState.test(timeout = 3.seconds) {
                awaitItem()

                vm.land()
                advanceUntilIdle()

                val loading = awaitItem()
                assertTrue(loading.commandStatus is RunStatus.Loading)

                val success = awaitItem()
                assertTrue(success.commandStatus is RunStatus.Success)

                cancelAndIgnoreRemainingEvents()
            }

            advanceUntilIdle()
        }

    @Test
    fun `returnToLaunch成功時にcommandStatusがSuccessになる`() =
        runTest(dispatcher) {
            mavRepo.returnToLaunchResult = RunStatus.Success(Unit)

            vm.uiState.test(timeout = 3.seconds) {
                awaitItem()

                vm.returnToLaunch()
                advanceUntilIdle()

                val loading = awaitItem()
                assertTrue(loading.commandStatus is RunStatus.Loading)

                val success = awaitItem()
                assertTrue(success.commandStatus is RunStatus.Success)

                cancelAndIgnoreRemainingEvents()
            }

            advanceUntilIdle()
        }

    @Test
    fun `toggleMapModeでisMapModeが反転する`() =
        runTest(dispatcher) {
            assertEquals(false, vm.uiState.value.isMapMode)

            vm.toggleMapMode()
            assertEquals(true, vm.uiState.value.isMapMode)

            vm.toggleMapMode()
            assertEquals(false, vm.uiState.value.isMapMode)
        }

    @Test
    fun `startObservingでドローン状態が反映される`() =
        runTest(dispatcher) {
            vm.startObserving()

            vm.uiState.test(timeout = 3.seconds) {
                awaitItem()

                val droneState =
                    DroneState(
                        altitudeMeters = 25.0f,
                        batteryPercent = 80,
                        speedKmh = 10f,
                        satelliteCount = 12,
                        connectionStatus =
                            ConnectionStatus.Connected(
                                Clock.System.now().toEpochMilliseconds(),
                            ),
                        isArmed = true,
                        latitude = 35.6762,
                        longitude = 139.6503,
                        bearing = 45f,
                    )

                mavRepo.droneStateFlow.emit(droneState)

                val updated = awaitItem()
                assertEquals(25.0f, updated.altitudeMeters)
                assertEquals(80, updated.batteryPercent)
                assertTrue(updated.connectionStatus is ConnectionStatus.Connected)

                cancelAndIgnoreRemainingEvents()
            }

            vm.stopObserving()
            advanceUntilIdle()
        }
}

// Fake BleRepository
private class FakeBleRepository : BleRepositoryContract {
    val controllerStateFlow = MutableSharedFlow<BleControllerState>()

    override fun scanDevices(): Flow<List<BleDevice>> = emptyFlow()

    override suspend fun connect(address: String): RunStatus<Unit> = RunStatus.Success(Unit)

    override fun disconnect() = Unit

    override fun observeConnectionStatus(): Flow<BleConnectionStatus> = emptyFlow()

    override fun observeControllerState(): Flow<BleControllerState> = controllerStateFlow
}
