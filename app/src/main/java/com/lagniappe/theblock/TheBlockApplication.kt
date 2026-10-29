package com.lagniappe.theblock

import android.app.Application
import com.lagniappe.theblock.vehicle.data.DemoVehicleRepositoryFactory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository

class TheBlockApplication : Application() {
    // Application-owned so all future screens observe the same inventory and demo bids.
    val vehicleRepository: VehicleRepository by lazy {
        DemoVehicleRepositoryFactory.create()
    }
}
