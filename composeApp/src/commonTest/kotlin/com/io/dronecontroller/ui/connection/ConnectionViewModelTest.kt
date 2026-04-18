package com.io.dronecontroller.ui.connection

import com.io.dronecontroller.data.datasource.ConnectionStorageContract
import com.io.dronecontroller.domain.model.ConnectionStatus
import com.io.dronecontroller.domain.usecase.DisconnectDroneUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveConnectionUseCaseContract
import com.io.dronecontroller.service.ConnectionModeHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private lateinit var disconnectDrone: FakeDisconnectDroneUseCase
    private lateinit var viewModel: ConnectionViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        disconnectDrone = FakeDisconnectDroneUseCase()
        viewModel =
            ConnectionViewModel(
                observeConnection = FakeObserveConnectionUseCase(),
                disconnectDrone = disconnectDrone,
                storage = FakeConnectionStorage(),
                modeHolder = ConnectionModeHolder(),
            )
        dispatcher.scheduler.advanceUntilIdle()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun disconnectCallsDroneDisconnectAndSetsDisconnectedStatus() =
        runTest(dispatcher) {
            viewModel.disconnect()

            advanceUntilIdle()

            assertEquals(1, disconnectDrone.callCount)
            assertTrue(viewModel.uiState.value.status is ConnectionStatus.Disconnected)
        }

    @Test
    fun mockCallsDroneDisconnectAndSetsDisconnectedStatus() =
        runTest(dispatcher) {
            viewModel.mock()

            advanceUntilIdle()

            assertEquals(1, disconnectDrone.callCount)
            assertTrue(viewModel.uiState.value.status is ConnectionStatus.Disconnected)
        }
}

private class FakeObserveConnectionUseCase : ObserveConnectionUseCaseContract {
    override fun invoke(address: String, port: Int): Flow<ConnectionStatus> = emptyFlow()
}

private class FakeDisconnectDroneUseCase : DisconnectDroneUseCaseContract {
    var callCount = 0

    override fun invoke() {
        callCount++
    }
}

private class FakeConnectionStorage : ConnectionStorageContract {
    override suspend fun saveConnection(address: String, port: Int) = Unit

    override suspend fun loadLastAddress(): String = "192.168.3.11"

    override suspend fun loadLastPort(): Int = 50051

    override suspend fun loadHistory(): List<Pair<String, Int>> = emptyList()
}


