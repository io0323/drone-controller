package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus
import kotlinx.coroutines.delay

class LoginUseCase : LoginUseCaseContract {
    override suspend fun invoke(username: String, password: String): RunStatus<Unit> {
        delay(400)

        if (username == MOCK_USERNAME && password == MOCK_PASSWORD) {
            return RunStatus.Success(Unit)
        }

        return RunStatus.Error("ユーザー名またはパスワードが正しくありません")
    }

    private companion object {
        private const val MOCK_USERNAME = "demo"
        private const val MOCK_PASSWORD = "demo123"
    }
}

