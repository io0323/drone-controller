package com.io.dronecontroller.data.di

import com.io.dronecontroller.data.repository.MavlinkRepository
import com.io.dronecontroller.domain.repository.MavlinkRepositoryContract
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoryModule: Module = module {
    single<MavlinkRepositoryContract> { MavlinkRepository(get()) }
}

expect val dataSourceModule: Module
