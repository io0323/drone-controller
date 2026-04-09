package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.fake.FakeMavlinkRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReturnToLaunchUseCaseTest {

    private val repo = FakeMavlinkRepository()
    private val useCase = ReturnToLaunchUseCase(repo)

    @Test
    fun `RTL成功時はSuccessを返す`() = runTest {
        repo.returnToLaunchResult = RunStatus.Success(Unit)

        val result = useCase()

        // ★ 型で検証（安定）
        assertTrue(result is RunStatus.Success)
    }

    @Test
    fun `RTL失敗時はErrorを返す`() = runTest {
        repo.returnToLaunchResult = RunStatus.Error("RTLコマンド失敗")

        val result = useCase()

        assertTrue(result is RunStatus.Error)
        assertEquals("RTLコマンド失敗", (result as RunStatus.Error).message)
    }
}