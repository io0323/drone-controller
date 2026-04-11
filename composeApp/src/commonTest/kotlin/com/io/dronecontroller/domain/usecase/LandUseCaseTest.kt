package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.fake.FakeMavlinkRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LandUseCaseTest {
    private val repo = FakeMavlinkRepository()
    private val useCase = LandUseCase(repo)

    @Test
    fun `着陸成功時はSuccessを返す`() =
        runTest {
            repo.landResult = RunStatus.Success(Unit)

            val result = useCase()

            // ★ 型で検証（最も安全）
            assertTrue(result is RunStatus.Success)
        }

    @Test
    fun `着陸失敗時はErrorを返す`() =
        runTest {
            repo.landResult = RunStatus.Error("着陸コマンド失敗")

            val result = useCase()

            assertTrue(result is RunStatus.Error)
            assertEquals("着陸コマンド失敗", (result as RunStatus.Error).message)
        }
}
