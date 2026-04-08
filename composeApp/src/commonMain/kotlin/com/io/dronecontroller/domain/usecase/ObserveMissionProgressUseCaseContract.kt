package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.MissionProgress
import kotlinx.coroutines.flow.Flow

interface ObserveMissionProgressUseCaseContract {
    operator fun invoke(): Flow<MissionProgress>
}
