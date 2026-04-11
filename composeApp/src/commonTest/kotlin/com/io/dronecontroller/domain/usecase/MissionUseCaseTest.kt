package com.io.dronecontroller.domain.usecase

import app.cash.turbine.test
import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.fake.FakeMissionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MissionUseCaseTest {
    private val repo = FakeMissionRepository()

    // ─── UploadMissionUseCase ────────────────────────────────────

    @Test
    fun `ミッション送信成功時はSuccessを返す`() =
        runTest {
            repo.uploadResult = RunStatus.Success(Unit)
            val useCase = UploadMissionUseCase(repo)

            val items =
                listOf(
                    MissionItem(35.676, 139.650, 10f, 5f),
                    MissionItem(35.677, 139.651, 15f, 5f),
                )

            val result = useCase(items)

            assertTrue(result is RunStatus.Success)
            assertEquals(items, repo.lastUploadedItems)
        }

    @Test
    fun `ミッション送信失敗時はErrorを返す`() =
        runTest {
            repo.uploadResult = RunStatus.Error("ミッション送信失敗")
            val useCase = UploadMissionUseCase(repo)

            val result = useCase(listOf(MissionItem(35.676, 139.650, 10f)))

            assertTrue(result is RunStatus.Error)
            assertEquals("ミッション送信失敗", (result as RunStatus.Error).message)
        }

    @Test
    fun `空リストでも送信できる`() =
        runTest {
            repo.uploadResult = RunStatus.Success(Unit)
            val useCase = UploadMissionUseCase(repo)

            val result = useCase(emptyList())

            assertTrue(result is RunStatus.Success)
            assertTrue(repo.lastUploadedItems.isEmpty())
        }

    // ─── StartMissionUseCase ─────────────────────────────────────

    @Test
    fun `ミッション開始成功時はSuccessを返す`() =
        runTest {
            repo.startResult = RunStatus.Success(Unit)

            val result = StartMissionUseCase(repo)()

            assertTrue(result is RunStatus.Success)
        }

    @Test
    fun `ミッション開始失敗時はErrorを返す`() =
        runTest {
            repo.startResult = RunStatus.Error("ミッション開始失敗")

            val result = StartMissionUseCase(repo)()

            assertTrue(result is RunStatus.Error)
            assertEquals("ミッション開始失敗", (result as RunStatus.Error).message)
        }

    // ─── StopMissionUseCase ──────────────────────────────────────

    @Test
    fun `ミッション停止成功時はSuccessを返す`() =
        runTest {
            repo.stopResult = RunStatus.Success(Unit)

            val result = StopMissionUseCase(repo)()

            assertTrue(result is RunStatus.Success)
        }

    @Test
    fun `ミッション停止失敗時はErrorを返す`() =
        runTest {
            repo.stopResult = RunStatus.Error("ミッション停止失敗")

            val result = StopMissionUseCase(repo)()

            assertTrue(result is RunStatus.Error)
            assertEquals("ミッション停止失敗", (result as RunStatus.Error).message)
        }

    // ─── PauseMissionUseCase ─────────────────────────────────────

    @Test
    fun `ミッション一時停止成功時はSuccessを返す`() =
        runTest {
            repo.pauseResult = RunStatus.Success(Unit)

            val result = PauseMissionUseCase(repo)()

            assertTrue(result is RunStatus.Success)
        }

    @Test
    fun `ミッション一時停止失敗時はErrorを返す`() =
        runTest {
            repo.pauseResult = RunStatus.Error("一時停止失敗")

            val result = PauseMissionUseCase(repo)()

            assertTrue(result is RunStatus.Error)
            assertEquals("一時停止失敗", (result as RunStatus.Error).message)
        }

    // ─── ObserveMissionProgressUseCase ───────────────────────────

    @Test
    fun `進捗が正しくFlowで流れる`() =
        runTest {
            val useCase = ObserveMissionProgressUseCase(repo)

            useCase().test {
                val progress1 = MissionProgress(1, 3)
                val progress2 = MissionProgress(2, 3)

                repo.progressFlow.emit(progress1)
                assertEquals(progress1, awaitItem())

                repo.progressFlow.emit(progress2)
                assertEquals(progress2, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `MissionProgressのisCompleteが正しく判定される`() {
        val inProgress = MissionProgress(1, 3)
        val complete = MissionProgress(3, 3)
        val empty = MissionProgress(0, 0)

        assertTrue(!inProgress.isComplete)
        assertTrue(complete.isComplete)
        assertTrue(!empty.isComplete)
    }
}
