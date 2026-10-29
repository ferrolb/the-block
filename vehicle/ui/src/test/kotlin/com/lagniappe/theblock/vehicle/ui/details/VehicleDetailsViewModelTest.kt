package com.lagniappe.theblock.vehicle.ui.details

import com.lagniappe.theblock.vehicle.ui.navigation.VehicleDetailsDestination
import com.lagniappe.theblock.vehicle.domain.model.Dealership
import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.model.Vehicle
import com.lagniappe.theblock.vehicle.domain.model.VehicleAuction
import com.lagniappe.theblock.vehicle.domain.model.VehicleCondition
import com.lagniappe.theblock.vehicle.domain.model.VehicleSpecs
import com.lagniappe.theblock.vehicle.domain.repository.BidResult
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import com.lagniappe.theblock.vehicle.domain.usecase.LoadInventoryUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehicleUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.VehicleDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleDetailsViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `typed destination selects the correct vehicle`() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        assertEquals("target", viewModel.vehicleId)
        assertEquals(VehicleDetails.Ready(vehicle), viewModel.details.value)
        assertEquals(1, repository.loads)
    }

    @Test
    fun `unknown identity produces not found after loading`() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository, "missing")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        assertEquals(VehicleDetails.NotFound, viewModel.details.value)
    }

    @Test
    fun `fresh repository loads independently and later bids update details`() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        assertEquals(VehicleDetails.Loading, viewModel.details.value)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()

        val updated = vehicle.copy(auction = vehicle.auction.copy(currentBid = Money(1_510_000), bidCount = 1))
        repository.state.value = VehicleInventory.Ready(listOf(updated))
        advanceUntilIdle()
        assertEquals(VehicleDetails.Ready(updated), viewModel.details.value)
        assertEquals(1, repository.loads)
    }

    @Test
    fun `failed load can be retried for the same restored identity`() = runTest {
        val repository = FakeRepository()
        repository.next = VehicleInventory.Failed(InventoryFailure.UNAVAILABLE)
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        assertEquals(VehicleDetails.Failed(InventoryFailure.UNAVAILABLE), viewModel.details.value)

        repository.next = VehicleInventory.Ready(listOf(vehicle))
        viewModel.retry()
        advanceUntilIdle()
        assertEquals(VehicleDetails.Ready(vehicle), viewModel.details.value)
        assertEquals(2, repository.loads)
    }

    private fun viewModel(repository: VehicleRepository, id: String = "target") =
        VehicleDetailsViewModel(
            VehicleDetailsDestination(id),
            ObserveVehicleUseCase(repository),
            LoadInventoryUseCase(repository),
        )

    private class FakeRepository : VehicleRepository {
        val state = MutableStateFlow<VehicleInventory>(VehicleInventory.NotLoaded)
        override val inventory = state.asStateFlow()
        var loads = 0
        var next: VehicleInventory = VehicleInventory.Ready(
            listOf(vehicle.copy(id = "other"), vehicle),
        )

        override suspend fun loadInventory() {
            loads++
            state.value = next
        }

        override suspend fun placeBid(vehicleId: String, amount: Money): BidResult =
            error("Details does not submit bids yet")
    }

    private companion object {
        val vehicle = Vehicle(
            id = "target", vin = "VIN123", year = 2023, make = "Ford", model = "Bronco",
            trim = "Big Bend", lot = "A-0043",
            specs = VehicleSpecs("SUV", "Blue", "Black", "V6", "automatic", "4WD", 47_731, "gasoline"),
            condition = VehicleCondition(3.8, "Normal wear", emptyList(), "clean"),
            dealership = Dealership("Demo Auto", "Toronto", "Ontario"),
            auction = VehicleAuction("2026-04-05T14:00:00", Money(1_450_000), null, null, null, 0),
            images = emptyList(),
        )
    }
}
