package com.io.dronecontroller.domain.di

import com.io.dronecontroller.domain.usecase.ConnectBleDeviceUseCase
import com.io.dronecontroller.domain.usecase.ConnectBleDeviceUseCaseContract
import com.io.dronecontroller.domain.usecase.DisconnectBleDeviceUseCase
import com.io.dronecontroller.domain.usecase.DisconnectBleDeviceUseCaseContract
import com.io.dronecontroller.domain.usecase.LandUseCase
import com.io.dronecontroller.domain.usecase.LandUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveBleConnectionStatusUseCase
import com.io.dronecontroller.domain.usecase.ObserveBleConnectionStatusUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveBleControllerStateUseCase
import com.io.dronecontroller.domain.usecase.ObserveBleControllerStateUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveConnectionUseCase
import com.io.dronecontroller.domain.usecase.ObserveConnectionUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveDroneStateUseCase
import com.io.dronecontroller.domain.usecase.ObserveDroneStateUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveMissionProgressUseCase
import com.io.dronecontroller.domain.usecase.ObserveMissionProgressUseCaseContract
import com.io.dronecontroller.domain.usecase.PauseMissionUseCase
import com.io.dronecontroller.domain.usecase.PauseMissionUseCaseContract
import com.io.dronecontroller.domain.usecase.ReturnToLaunchUseCase
import com.io.dronecontroller.domain.usecase.ReturnToLaunchUseCaseContract
import com.io.dronecontroller.domain.usecase.ScanBleDevicesUseCase
import com.io.dronecontroller.domain.usecase.ScanBleDevicesUseCaseContract
import com.io.dronecontroller.domain.usecase.SendManualControlUseCase
import com.io.dronecontroller.domain.usecase.SendManualControlUseCaseContract
import com.io.dronecontroller.domain.usecase.StartMissionUseCase
import com.io.dronecontroller.domain.usecase.StartMissionUseCaseContract
import com.io.dronecontroller.domain.usecase.StopMissionUseCase
import com.io.dronecontroller.domain.usecase.StopMissionUseCaseContract
import com.io.dronecontroller.domain.usecase.TakeoffUseCase
import com.io.dronecontroller.domain.usecase.TakeoffUseCaseContract
import com.io.dronecontroller.domain.usecase.UploadMissionUseCase
import com.io.dronecontroller.domain.usecase.UploadMissionUseCaseContract
import org.koin.core.module.Module
import org.koin.dsl.module

val useCaseModule: Module =
    module {
        single<ObserveConnectionUseCaseContract> { ObserveConnectionUseCase(get()) }
        single<ObserveDroneStateUseCaseContract> { ObserveDroneStateUseCase(get()) }
        single<TakeoffUseCaseContract> { TakeoffUseCase(get()) }
        single<LandUseCaseContract> { LandUseCase(get()) }
        single<ReturnToLaunchUseCaseContract> { ReturnToLaunchUseCase(get()) }
        single<SendManualControlUseCaseContract> { SendManualControlUseCase(get()) }
        single<ScanBleDevicesUseCaseContract> { ScanBleDevicesUseCase(get()) }
        single<ConnectBleDeviceUseCaseContract> { ConnectBleDeviceUseCase(get()) }
        single<DisconnectBleDeviceUseCaseContract> { DisconnectBleDeviceUseCase(get()) }
        single<ObserveBleConnectionStatusUseCaseContract> { ObserveBleConnectionStatusUseCase(get()) }
        single<ObserveBleControllerStateUseCaseContract> { ObserveBleControllerStateUseCase(get()) }
        single<UploadMissionUseCaseContract> { UploadMissionUseCase(get()) }
        single<StartMissionUseCaseContract> { StartMissionUseCase(get()) }
        single<StopMissionUseCaseContract> { StopMissionUseCase(get()) }
        single<PauseMissionUseCaseContract> { PauseMissionUseCase(get()) }
        single<ObserveMissionProgressUseCaseContract> { ObserveMissionProgressUseCase(get()) }
    }
