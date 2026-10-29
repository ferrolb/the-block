package com.lagniappe.theblock.vehicle.domain.usecase

import com.lagniappe.theblock.vehicle.domain.FakeVehicleRepository
import com.lagniappe.theblock.vehicle.domain.model.Money
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

class ObserveVehiclesUseCaseTest {
    private val ford = vehicle()
    private val mazda = vehicle(
        id = "mazda", make = "Mazda", model = "CX-5", year = 2025,
        vin = "MAZDAVIN456", lot = "B-0001",
    )
    private val ready = VehicleInventory.Ready(listOf(mazda, ford))

    @Test
    fun `blank search returns full inventory in source order`() = runTest {
        val repository = FakeVehicleRepository(ready)
        val observe = ObserveVehiclesUseCase(repository)

        assertEquals(ready, observe().first())
        assertEquals(ready, observe("  \t ").first())
        assertEquals(0, repository.loadCalls)
    }

    @Test
    fun `each supported field is searchable ignoring case`() = runTest {
        val observe = ObserveVehiclesUseCase(FakeVehicleRepository(ready))

        for (query in listOf("fOrD", "bRoNcO", "2023", "fordvin123", "a-0043")) {
            assertEquals(VehicleInventory.Ready(listOf(ford)), observe(query).first())
        }
        assertEquals(VehicleInventory.Ready(listOf(mazda)), observe("cx-5").first())
    }

    @Test
    fun `all terms must match across fields and extra whitespace is ignored`() = runTest {
        val observe = ObserveVehiclesUseCase(FakeVehicleRepository(ready))

        assertEquals(
            VehicleInventory.Ready(listOf(ford)),
            observe("  2023\tFORD   Bronco  ").first(),
        )
        assertEquals(VehicleInventory.Ready(emptyList()), observe("ford 2025").first())
    }

    @Test
    fun `no matches produce empty success without altering repository inventory`() = runTest {
        val repository = FakeVehicleRepository(ready)

        assertEquals(
            VehicleInventory.Ready(emptyList()),
            ObserveVehiclesUseCase(repository)("not-a-vehicle").first(),
        )
        assertEquals(ready, repository.inventory.value)
    }

    @Test
    fun `search preserves load failures and continues observing recovery`() = runTest {
        val repository = FakeVehicleRepository()
        val observe = ObserveVehiclesUseCase(repository)
        val values = async(start = CoroutineStart.UNDISPATCHED) {
            observe("ford").take(4).toList()
        }

        repository.state.value = VehicleInventory.Loading
        yield()
        repository.state.value = VehicleInventory.Failed(InventoryFailure.UNAVAILABLE)
        yield()
        repository.state.value = ready

        assertEquals(
            listOf(
                VehicleInventory.NotLoaded,
                VehicleInventory.Loading,
                VehicleInventory.Failed(InventoryFailure.UNAVAILABLE),
                VehicleInventory.Ready(listOf(ford)),
            ),
            values.await(),
        )
    }

    @Test
    fun `existing search observes subsequent vehicle changes`() = runTest {
        val repository = FakeVehicleRepository(ready)
        val updated = ford.copy(auction = ford.auction.copy(currentBid = Money(1_450_000), bidCount = 1))
        val values = async(start = CoroutineStart.UNDISPATCHED) {
            ObserveVehiclesUseCase(repository)("ford").take(2).toList()
        }

        repository.state.value = VehicleInventory.Ready(listOf(mazda, updated))

        assertEquals(
            listOf(VehicleInventory.Ready(listOf(ford)), VehicleInventory.Ready(listOf(updated))),
            values.await(),
        )
    }
}
