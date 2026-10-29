package com.lagniappe.theblock.vehicle.domain.usecase

import com.lagniappe.theblock.vehicle.domain.FakeVehicleRepository
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import com.lagniappe.theblock.vehicle.domain.vehicle
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveVehicleUseCaseTest {
    @Test
    fun `finds exact vehicle identity rather than matching a display field`() = runTest {
        val vehicle = vehicle()
        val repository = FakeVehicleRepository(VehicleInventory.Ready(listOf(vehicle)))
        val observe = ObserveVehicleUseCase(repository)

        assertEquals(VehicleDetails.Ready(vehicle), observe(vehicle.id).first())
        assertEquals(VehicleDetails.NotFound, observe(vehicle.vin).first())
        assertEquals(VehicleDetails.NotFound, observe("missing").first())
        assertEquals(0, repository.loadCalls)
    }

    @Test
    fun `not found is emitted only after inventory is available`() = runTest {
        val repository = FakeVehicleRepository()
        val values = async(start = CoroutineStart.UNDISPATCHED) {
            ObserveVehicleUseCase(repository)("missing").take(5).toList()
        }

        repository.state.value = VehicleInventory.Loading
        yield()
        repository.state.value = VehicleInventory.Failed(InventoryFailure.INVALID_DATA)
        yield()
        repository.state.value = VehicleInventory.Ready(listOf(vehicle()))
        yield()
        val found = vehicle(id = "missing")
        repository.state.value = VehicleInventory.Ready(listOf(found))

        assertEquals(
            listOf(
                VehicleDetails.NotLoaded,
                VehicleDetails.Loading,
                VehicleDetails.Failed(InventoryFailure.INVALID_DATA),
                VehicleDetails.NotFound,
                VehicleDetails.Ready(found),
            ),
            values.await(),
        )
    }
}
