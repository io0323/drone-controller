package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.RunStatus

interface LoginUseCaseContract {
    suspend operator fun invoke(username: String, password: String): RunStatus<Unit>
}

