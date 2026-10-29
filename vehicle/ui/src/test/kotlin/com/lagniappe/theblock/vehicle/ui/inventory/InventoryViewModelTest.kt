package com.lagniappe.theblock.vehicle.ui.inventory

import androidx.lifecycle.SavedStateHandle
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
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehiclesUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
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
class InventoryViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load displays vehicles and search can be cleared`() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(1, repository.loadCalls)
        assertEquals(2, (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles.size)

        viewModel.onQueryChanged("fOrD 2023")
        advanceUntilIdle()
        assertEquals("fOrD 2023", viewModel.uiState.value.query)
        assertEquals(listOf(ford), (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles)

        viewModel.onQueryChanged("no-such-vehicle")
        advanceUntilIdle()
        assertEquals(emptyList<Vehicle>(), (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles)
        viewModel.onQueryChanged("")
        advanceUntilIdle()
        assertEquals(2, (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles.size)
        assertEquals(1, repository.loadCalls)
    }

    @Test
    fun `saved search is restored and subsequent input is saved`() = runTest {
        val handle = SavedStateHandle(mapOf("inventory_query" to "Ford"))
        val viewModel = viewModel(FakeRepository(), handle)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals("Ford", viewModel.uiState.value.query)
        assertEquals(listOf(ford), (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles)
        viewModel.onQueryChanged("Mazda")
        advanceUntilIdle()
        assertEquals("Mazda", handle.get<String>("inventory_query"))
        assertEquals(listOf(mazda), (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles)
    }

    @Test
    fun `failed load can be retried without losing search`() = runTest {
        val repository = FakeRepository()
        repository.next = VehicleInventory.Failed(InventoryFailure.UNAVAILABLE)
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.onQueryChanged("Ford")
        advanceUntilIdle()
        assertEquals(InventoryContent.Error(InventoryFailure.UNAVAILABLE), viewModel.uiState.value.content)

        repository.next = VehicleInventory.Ready(listOf(ford, mazda))
        viewModel.retry()
        advanceUntilIdle()
        assertEquals(2, repository.loadCalls)
        assertEquals("Ford", viewModel.uiState.value.query)
        assertEquals(listOf(ford), (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles)
    }

    @Test
    fun `duplicate retries do not launch overlapping loads`() = runTest {
        val repository = FakeRepository()
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(InventoryContent.Loading, viewModel.uiState.value.content)
        viewModel.retry()
        viewModel.retry()
        advanceUntilIdle()
        assertEquals(1, repository.loadCalls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(2, (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles.size)
    }

    @Test
    fun `shared repository changes update the currently filtered cards`() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.onQueryChanged("Ford")
        advanceUntilIdle()

        val updated = ford.copy(auction = ford.auction.copy(currentBid = Money(1_510_000), bidCount = 2))
        repository.state.value = VehicleInventory.Ready(listOf(updated, mazda))
        advanceUntilIdle()
        assertEquals(listOf(updated), (viewModel.uiState.value.content as InventoryContent.Vehicles).vehicles)
    }

    private fun viewModel(
        repository: VehicleRepository,
        handle: SavedStateHandle = SavedStateHandle(),
    ) = InventoryViewModel(handle, ObserveVehiclesUseCase(repository), LoadInventoryUseCase(repository))

    private class FakeRepository : VehicleRepository {
        val state = MutableStateFlow<VehicleInventory>(VehicleInventory.NotLoaded)
        override val inventory = state.asStateFlow()
        var next: VehicleInventory = VehicleInventory.Ready(listOf(ford, mazda))
        var loadCalls = 0
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun loadInventory() {
            loadCalls++
            state.value = VehicleInventory.Loading
            gate?.await()
            state.value = next
        }

        override suspend fun placeBid(vehicleId: String, amount: Money): BidResult =
            error("Inventory UI does not submit bids")
    }

    private companion object {
        val ford = Vehicle(
            id = "ford", vin = "VIN123", year = 2023, make = "Ford", model = "Bronco",
            trim = "Big Bend", lot = "A-0043",
            specs = VehicleSpecs("SUV", "Blue", "Black", "V6", "automatic", "4WD", 47_731, "gasoline"),
            condition = VehicleCondition(3.8, "Normal wear", emptyList(), "clean"),
            dealership = Dealership("Demo Auto", "Toronto", "Ontario"),
            auction = VehicleAuction("2026-04-05T14:00:00", Money(1_450_000), null, null, null, 0),
            images = emptyList(),
        )
        val mazda = ford.copy(id = "mazda", year = 2025, make = "Mazda", model = "CX-5")
    }
}
