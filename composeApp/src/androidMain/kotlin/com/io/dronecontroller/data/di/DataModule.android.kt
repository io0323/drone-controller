package com.io.dronecontroller.data.di

import com.io.dronecontroller.data.datasource.MavlinkDataSource
import com.io.dronecontroller.data.datasource.MavlinkDataSourceContract
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataSourceModule: Module = module {
    single<MavlinkDataSourceContract> { MavlinkDataSource() }
}
