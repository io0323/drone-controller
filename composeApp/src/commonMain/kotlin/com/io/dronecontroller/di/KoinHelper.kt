package com.io.dronecontroller.di

import com.io.dronecontroller.data.di.dataSourceModule
import com.io.dronecontroller.data.di.repositoryModule
import com.io.dronecontroller.domain.di.useCaseModule
import com.io.dronecontroller.ui.di.viewModelModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(
            viewModelModule,
            useCaseModule,
            repositoryModule,
            dataSourceModule,
        )
    }
}
