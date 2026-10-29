package com.lagniappe.theblock.vehicle.domain.usecase

import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.repository.BidResult
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository

class PlaceBidUseCase(
    private val repository: VehicleRepository,
) {
    // The repository applies domain bid rules under its lock; prechecking a snapshot here
    // would let concurrent submissions validate against an outdated price.
    suspend operator fun invoke(vehicleId: String, amount: Money): BidResult =
        repository.placeBid(vehicleId, amount)
}
