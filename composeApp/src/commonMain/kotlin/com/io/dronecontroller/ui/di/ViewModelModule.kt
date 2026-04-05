package com.io.dronecontroller.ui.di

import com.io.dronecontroller.ui.connection.ConnectionViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { ConnectionViewModel(get()) }
}
