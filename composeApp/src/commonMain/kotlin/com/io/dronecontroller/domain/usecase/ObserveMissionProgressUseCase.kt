package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.repository.MissionRepositoryContract
import kotlinx.coroutines.flow.Flow

class ObserveMissionProgressUseCase(
    private val repository: MissionRepositoryContract,
) : ObserveMissionProgressUseCaseContract {
    override operator fun invoke(): Flow<MissionProgress> = repository.observeMissionProgress()
}
