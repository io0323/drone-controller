package com.io.dronecontroller.data.di

import com.io.dronecontroller.data.repository.BleRepository
import com.io.dronecontroller.data.repository.MavlinkRepository
import com.io.dronecontroller.domain.repository.BleRepositoryContract
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoryModule: Module = module {
    single<MavlinkRepositoryContract> { MavlinkRepository(get()) }
    single<BleRepositoryContract> { BleRepository(get()) }
}

expect val dataSourceModule: Module
