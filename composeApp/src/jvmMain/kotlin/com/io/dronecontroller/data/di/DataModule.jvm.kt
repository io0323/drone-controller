package com.io.dronecontroller.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

// JVM（Desktop）向けMAVLink実装は将来対応
actual val dataSourceModule: Module = module {}
