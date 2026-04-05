package com.io.dronecontroller.domain.di

import com.io.dronecontroller.domain.usecase.ObserveConnectionUseCase
import com.io.dronecontroller.domain.usecase.ObserveConnectionUseCaseContract
import org.koin.core.module.Module
import org.koin.dsl.module

val useCaseModule: Module = module {
    single<ObserveConnectionUseCaseContract> { ObserveConnectionUseCase(get()) }
}
