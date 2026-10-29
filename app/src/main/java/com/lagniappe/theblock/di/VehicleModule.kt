package com.lagniappe.theblock.di

import com.lagniappe.theblock.vehicle.data.DemoVehicleRepositoryFactory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import com.lagniappe.theblock.vehicle.domain.usecase.LoadInventoryUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehicleUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehiclesUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.PlaceBidUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VehicleModule {
    @Provides
    @Singleton
    fun provideVehicleRepository(): VehicleRepository = DemoVehicleRepositoryFactory.create()

    @Provides
    fun provideLoadInventoryUseCase(repository: VehicleRepository): LoadInventoryUseCase =
        LoadInventoryUseCase(repository)

    @Provides
    fun provideObserveVehiclesUseCase(repository: VehicleRepository): ObserveVehiclesUseCase =
        ObserveVehiclesUseCase(repository)

    @Provides
    fun provideObserveVehicleUseCase(repository: VehicleRepository): ObserveVehicleUseCase =
        ObserveVehicleUseCase(repository)

    @Provides
    fun providePlaceBidUseCase(repository: VehicleRepository): PlaceBidUseCase =
        PlaceBidUseCase(repository)
}
