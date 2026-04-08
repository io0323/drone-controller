package com.io.dronecontroller.data.di

import com.io.dronecontroller.data.datasource.BleDataSource
import com.io.dronecontroller.data.datasource.BleDataSourceContract
import com.io.dronecontroller.data.datasource.DroneProvider
import com.io.dronecontroller.data.datasource.MavlinkDataSource
import com.io.dronecontroller.data.datasource.MavlinkDataSourceContract
import com.io.dronecontroller.data.datasource.MissionDataSource
import com.io.dronecontroller.data.datasource.MissionDataSourceContract
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataSourceModule: Module = module {
    single { DroneProvider() }
    single<MavlinkDataSourceContract> { MavlinkDataSource(get()) }
    single<MissionDataSourceContract> { MissionDataSource(get()) }
    single<BleDataSourceContract> { BleDataSource(androidContext()) }
}
