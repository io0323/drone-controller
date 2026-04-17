package com.io.dronecontroller.data.di

import com.io.dronecontroller.data.repository.BleRepository
import com.io.dronecontroller.data.repository.MavlinkRepository
import com.io.dronecontroller.data.repository.MissionRepository
import com.io.dronecontroller.domain.repository.BleRepositoryContract
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract
import com.io.dronecontroller.domain.repository.MissionRepositoryContract
import com.io.dronecontroller.service.ConnectionModeHolder
import com.io.dronecontroller.service.DroneStateHolder
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoryModule: Module =
    module {
        single<MavlinkRepositoryContract> { MavlinkRepository(get()) }
        single<BleRepositoryContract> { BleRepository(get()) }
        single<MissionRepositoryContract> { MissionRepository(get()) }
        single { DroneStateHolder() }
        single { ConnectionModeHolder() }
    }

expect val dataSourceModule: Module
