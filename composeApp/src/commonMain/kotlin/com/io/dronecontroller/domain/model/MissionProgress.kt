package com.io.dronecontroller.domain.model

data class MissionProgress(
    val currentItemIndex: Int,
    val missionCount: Int,
) {
    val isComplete: Boolean get() = missionCount > 0 && currentItemIndex >= missionCount
}

enum class MissionStatus {
    Idle,
    Uploading,
    Running,
    Paused,
    Complete,
    Error,
}
