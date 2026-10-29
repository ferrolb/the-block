package com.lagniappe.theblock.vehicle.domain.usecase

import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository

class LoadInventoryUseCase(
    private val repository: VehicleRepository,
) {
    /** Initial load or retry. The repository preserves accepted bids after a successful load. */
    suspend operator fun invoke() = repository.loadInventory()
}
