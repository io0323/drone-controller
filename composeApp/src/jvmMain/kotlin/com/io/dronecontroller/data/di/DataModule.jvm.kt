package com.io.dronecontroller.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

import com.io.dronecontroller.data.datasource.BleDataSourceContract
import com.io.dronecontroller.data.datasource.BleDataSourceStub

// JVM（Desktop）向けMAVLink/BLE実装は将来対応
actual val dataSourceModule: Module = module {
    single<BleDataSourceContract> { BleDataSourceStub() }
}
