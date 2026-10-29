package com.lagniappe.theblock.di

import com.lagniappe.theblock.vehicle.data.DemoVehicleRepositoryFactory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
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
}
