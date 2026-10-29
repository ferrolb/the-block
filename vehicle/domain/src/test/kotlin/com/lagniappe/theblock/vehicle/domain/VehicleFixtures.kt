package com.lagniappe.theblock.vehicle.domain

import com.lagniappe.theblock.vehicle.domain.model.Dealership
import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.model.Vehicle
import com.lagniappe.theblock.vehicle.domain.model.VehicleAuction
import com.lagniappe.theblock.vehicle.domain.model.VehicleCondition
import com.lagniappe.theblock.vehicle.domain.model.VehicleSpecs
import com.lagniappe.theblock.vehicle.domain.repository.BidResult
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal fun vehicle(
    id: String = "ford",
    make: String = "Ford",
    model: String = "Bronco",
    year: Int = 2023,
    vin: String = "FORDVIN123",
    lot: String = "A-0043",
): Vehicle = Vehicle(
    id = id,
    vin = vin,
    year = year,
    make = make,
    model = model,
    trim = "Big Bend",
    lot = lot,
    specs = VehicleSpecs("SUV", "Blue", "Black", "V6", "automatic", "4WD", 47_731, "gasoline"),
    condition = VehicleCondition(3.8, "Normal wear", emptyList(), "clean"),
    dealership = Dealership("Demo Auto", "Toronto", "Ontario"),
    auction = VehicleAuction(
        scheduledStart = "2026-04-05T14:00:00",
        startingBid = Money(1_450_000),
        reservePrice = Money(2_500_000),
        buyNowPrice = null,
        currentBid = null,
        bidCount = 0,
    ),
    images = emptyList(),
)

internal class FakeVehicleRepository(
    initial: VehicleInventory = VehicleInventory.NotLoaded,
) : VehicleRepository {
    val state = MutableStateFlow(initial)
    override val inventory = state.asStateFlow()
    var loadCalls = 0
        private set

    override suspend fun loadInventory() {
        loadCalls++
    }

    override suspend fun placeBid(vehicleId: String, amount: Money): BidResult =
        error("Observation must not place bids")
}
