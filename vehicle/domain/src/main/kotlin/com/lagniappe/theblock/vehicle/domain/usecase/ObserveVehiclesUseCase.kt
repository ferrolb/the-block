package com.lagniappe.theblock.vehicle.domain.usecase

import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveVehiclesUseCase(
    private val repository: VehicleRepository,
) {
    /**
     * Observes existing inventory without triggering a load.
     * All whitespace-separated terms must match make, model, year, VIN, or lot.
     * Matching ignores case and preserves the dataset's order.
     */
    operator fun invoke(query: String = ""): Flow<VehicleInventory> {
        val terms = query.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return repository.inventory.map { inventory ->
            if (inventory !is VehicleInventory.Ready || terms.isEmpty()) {
                inventory
            } else {
                VehicleInventory.Ready(
                    inventory.vehicles.filter { vehicle ->
                        val fields = listOf(
                            vehicle.make,
                            vehicle.model,
                            vehicle.year.toString(),
                            vehicle.vin,
                            vehicle.lot,
                        )
                        terms.all { term -> fields.any { it.contains(term, ignoreCase = true) } }
                    },
                )
            }
        }.distinctUntilChanged()
    }
}
