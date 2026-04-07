package com.io.dronecontroller.domain.di

import com.io.dronecontroller.domain.usecase.LandUseCase
import com.io.dronecontroller.domain.usecase.LandUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveConnectionUseCase
import com.io.dronecontroller.domain.usecase.ObserveConnectionUseCaseContract
import com.io.dronecontroller.domain.usecase.ObserveDroneStateUseCase
import com.io.dronecontroller.domain.usecase.ObserveDroneStateUseCaseContract
import com.io.dronecontroller.domain.usecase.ReturnToLaunchUseCase
import com.io.dronecontroller.domain.usecase.ReturnToLaunchUseCaseContract
import com.io.dronecontroller.domain.usecase.TakeoffUseCase
import com.io.dronecontroller.domain.usecase.TakeoffUseCaseContract
import org.koin.core.module.Module
import org.koin.dsl.module

val useCaseModule: Module = module {
    single<ObserveConnectionUseCaseContract> { ObserveConnectionUseCase(get()) }
    single<ObserveDroneStateUseCaseContract> { ObserveDroneStateUseCase(get()) }
    single<TakeoffUseCaseContract> { TakeoffUseCase(get()) }
    single<LandUseCaseContract> { LandUseCase(get()) }
    single<ReturnToLaunchUseCaseContract> { ReturnToLaunchUseCase(get()) }
}
