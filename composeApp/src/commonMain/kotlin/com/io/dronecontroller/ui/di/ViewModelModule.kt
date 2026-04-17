package com.io.dronecontroller.ui.di

import com.io.dronecontroller.ui.ble.BleControllerViewModel
import com.io.dronecontroller.ui.connection.ConnectionViewModel
import com.io.dronecontroller.ui.controller.DroneControllerViewModel
import com.io.dronecontroller.ui.mission.MissionPlanningViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule: Module =
    module {
        viewModel { ConnectionViewModel(get(), get(), get()) }
        viewModel { DroneControllerViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { BleControllerViewModel(get(), get(), get(), get()) }
        viewModel { MissionPlanningViewModel(get(), get(), get(), get(), get()) }
    }
