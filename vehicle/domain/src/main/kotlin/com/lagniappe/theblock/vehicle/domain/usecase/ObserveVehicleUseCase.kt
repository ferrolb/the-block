package com.lagniappe.theblock.vehicle.domain.usecase

import com.lagniappe.theblock.vehicle.domain.model.Vehicle
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveVehicleUseCase(
    private val repository: VehicleRepository,
) {
    /** Observes by stable identity, including bid changes. Does not trigger a load. */
    operator fun invoke(vehicleId: String): Flow<VehicleDetails> =
        repository.inventory.map { inventory ->
            when (inventory) {
                VehicleInventory.NotLoaded -> VehicleDetails.NotLoaded
                VehicleInventory.Loading -> VehicleDetails.Loading
                is VehicleInventory.Failed -> VehicleDetails.Failed(inventory.reason)
                is VehicleInventory.Ready -> inventory.vehicles.find { it.id == vehicleId }
                    ?.let { VehicleDetails.Ready(it) }
                    ?: VehicleDetails.NotFound
            }
        }.distinctUntilChanged()
}

sealed interface VehicleDetails {
    data object NotLoaded : VehicleDetails
    data object Loading : VehicleDetails
    data object NotFound : VehicleDetails
    data class Ready(val vehicle: Vehicle) : VehicleDetails
    data class Failed(val reason: InventoryFailure) : VehicleDetails
}
