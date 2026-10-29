package com.lagniappe.theblock

import android.app.Application
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TheBlockApplication : Application() {
    @Inject
    lateinit var vehicleRepository: VehicleRepository
}
