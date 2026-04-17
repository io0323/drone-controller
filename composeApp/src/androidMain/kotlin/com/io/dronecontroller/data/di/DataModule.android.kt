package com.io.dronecontroller.data.di

import com.io.dronecontroller.data.datasource.BleDataSource
import com.io.dronecontroller.data.datasource.BleDataSourceContract
import com.io.dronecontroller.data.datasource.ConnectionStorageContract
import com.io.dronecontroller.data.datasource.ConnectionStorageImpl
import com.io.dronecontroller.data.datasource.DroneProvider
import com.io.dronecontroller.data.datasource.MavlinkDataSource
import com.io.dronecontroller.data.datasource.MavlinkDataSourceContract
import com.io.dronecontroller.data.datasource.RoutingMavlinkDataSource
import com.io.dronecontroller.data.datasource.UdpMavlinkDataSource
import com.io.dronecontroller.data.datasource.MissionDataSource
import com.io.dronecontroller.data.datasource.MissionDataSourceContract
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataSourceModule: Module =
    module {
        single { DroneProvider() }
        single { UdpMavlinkDataSource() }
        single<MavlinkDataSourceContract> { RoutingMavlinkDataSource(MavlinkDataSource(get()), get(), get()) }
        single<MissionDataSourceContract> { MissionDataSource(get()) }
        single<BleDataSourceContract> { BleDataSource(androidContext()) }
        single<ConnectionStorageContract> { ConnectionStorageImpl(androidContext()) }
    }
