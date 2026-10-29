package com.lagniappe.theblock.vehicle.domain.repository

import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.model.Vehicle
import kotlinx.coroutines.flow.StateFlow

interface VehicleRepository {
    val inventory: StateFlow<VehicleInventory>

    /** Loads once per repository instance. A failed load may be retried; bids survive repeat calls. */
    suspend fun loadInventory()

    /** Demo-only bid submission; implementations must check the latest state atomically. */
    suspend fun placeBid(vehicleId: String, amount: Money): BidResult
}

sealed interface VehicleInventory {
    data object NotLoaded : VehicleInventory
    data object Loading : VehicleInventory
    data class Ready(val vehicles: List<Vehicle>) : VehicleInventory
    data class Failed(val reason: InventoryFailure) : VehicleInventory
}

enum class InventoryFailure {
    UNAVAILABLE,
    INVALID_DATA,
}

sealed interface BidResult {
    data class Accepted(val vehicle: Vehicle) : BidResult
    data class AmountTooLow(val minimum: Money) : BidResult
    data object VehicleNotFound : BidResult
    data object InventoryNotLoaded : BidResult
    data object BidLimitReached : BidResult
}
