package com.lagniappe.theblock.vehicle.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object InventoryDestination

/** Pass identity only; the details feature will observe current data from the repository. */
@Serializable
data class VehicleDetailsDestination(val vehicleId: String)
