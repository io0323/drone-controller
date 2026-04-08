package com.io.dronecontroller.domain.usecase

import com.io.dronecontroller.domain.model.MissionItem
import com.io.dronecontroller.domain.model.RunStatus

interface UploadMissionUseCaseContract {
    suspend operator fun invoke(items: List<MissionItem>): RunStatus<Unit>
}
