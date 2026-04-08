package com.io.dronecontroller.data.datasource

import com.io.dronecontroller.domain.model.MissionProgress
import com.io.dronecontroller.domain.model.RunStatus
import io.mavsdk.mission.Mission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.io.dronecontroller.domain.model.MissionItem as DomainMissionItem

class MissionDataSource(
    private val droneProvider: DroneProvider
) : MissionDataSourceContract {

    override suspend fun uploadMission(items: List<DomainMissionItem>): RunStatus<Unit> {
        val drone = droneProvider.drone ?: return RunStatus.Error("未接続")
        val missionItems = items.map { wp ->
            Mission.MissionItem(
                wp.latitudeDeg,
                wp.longitudeDeg,
                wp.altitudeMeters,
                wp.speedMS,
                false,
                0f,
                0f,
                Mission.MissionItem.CameraAction.NONE,
                0f,
                0.0,
                1f,
                Float.NaN,
                0f,
                Mission.MissionItem.VehicleAction.NONE
            )
        }
        val plan = Mission.MissionPlan(missionItems)
        return suspendCancellableCoroutine { cont ->
            val disposable = drone.mission.uploadMission(plan).subscribe(
                { cont.resume(RunStatus.Success(Unit)) },
                { e: Throwable -> cont.resume(RunStatus.Error(e.message ?: "ミッション送信失敗", e)) }
            )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    override suspend fun startMission(): RunStatus<Unit> {
        val drone = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable = drone.mission.startMission().subscribe(
                { cont.resume(RunStatus.Success(Unit)) },
                { e: Throwable -> cont.resume(RunStatus.Error(e.message ?: "ミッション開始失敗", e)) }
            )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    override suspend fun stopMission(): RunStatus<Unit> {
        val drone = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable = drone.mission.clearMission().subscribe(
                { cont.resume(RunStatus.Success(Unit)) },
                { e: Throwable -> cont.resume(RunStatus.Error(e.message ?: "ミッション停止失敗", e)) }
            )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    override suspend fun pauseMission(): RunStatus<Unit> {
        val drone = droneProvider.drone ?: return RunStatus.Error("未接続")
        return suspendCancellableCoroutine { cont ->
            val disposable = drone.mission.pauseMission().subscribe(
                { cont.resume(RunStatus.Success(Unit)) },
                { e: Throwable -> cont.resume(RunStatus.Error(e.message ?: "ミッション一時停止失敗", e)) }
            )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    override fun observeMissionProgress(): Flow<MissionProgress> = callbackFlow {
        val drone = droneProvider.drone
        if (drone == null) {
            close()
            return@callbackFlow
        }
        val disposable = drone.mission.getMissionProgress().subscribe(
            { progress ->
                trySend(
                    MissionProgress(
                        currentItemIndex = progress.current,
                        missionCount = progress.total
                    )
                )
            },
            { e: Throwable -> close(e) }
        )
        awaitClose { disposable.dispose() }
    }
}
