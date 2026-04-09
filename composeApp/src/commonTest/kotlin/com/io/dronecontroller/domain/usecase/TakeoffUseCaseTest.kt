package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.fake.FakeMavlinkRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TakeoffUseCaseTest {

    private val repo = FakeMavlinkRepository()
    private val useCase = TakeoffUseCase(repo)

    @Test
    fun `離陸成功時はSuccessを返す`() = runTest {
        repo.takeoffResult = RunStatus.Success(Unit)

        val result = useCase(5f)

        assertTrue(result is RunStatus.Success)
    }

    @Test
    fun `離陸失敗時はErrorを返す`() = runTest {
        repo.takeoffResult = RunStatus.Error("離陸コマンド失敗")

        val result = useCase(5f)

        assertTrue(result is RunStatus.Error)
        assertEquals("離陸コマンド失敗", (result as RunStatus.Error).message)
    }

    @Test
    fun `高度パラメータが正しく渡される`() = runTest {
        useCase(10f)

        // ★ Fakeに記録された値を検証（これが本来のテスト）
        assertEquals(10f, repo.lastTakeoffAltitude)
    }
}