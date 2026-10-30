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
import com.lagniappe.theblock.vehicle.domain.usecase.PlaceBidUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.LoadInventoryUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehicleUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.VehicleDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
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

    @Test
    fun `review validates input and cancellation never submits a bid`() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        viewModel.startBid()
        viewModel.changeBidAmount("")
        viewModel.reviewBid('.')
        assertEquals(BidError.InvalidAmount, (viewModel.bidState.value as BidUiState.Editing).error)
        viewModel.changeBidAmount("14499.99")
        viewModel.reviewBid('.')
        assertEquals(BidError.TooLow(Money(1_450_000)), (viewModel.bidState.value as BidUiState.Editing).error)
        viewModel.changeBidAmount("14500.25")
        viewModel.reviewBid('.')
        assertEquals(BidUiState.Confirming(Money(1_450_025)), viewModel.bidState.value)
        assertEquals(0, repository.bidCalls)
        viewModel.dismissBid()
        viewModel.confirmBid()
        advanceUntilIdle()
        assertEquals(0, repository.bidCalls)
        assertEquals(BidUiState.Closed, viewModel.bidState.value)
    }

    @Test
    fun `confirmation submits once and accepted state updates details`() = runTest {
        val repository = FakeRepository()
        repository.bidGate = CompletableDeferred()
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        viewModel.startBid()
        viewModel.changeBidAmount("14500.25")
        viewModel.reviewBid('.')
        viewModel.confirmBid()
        viewModel.confirmBid()
        viewModel.dismissBid()
        advanceUntilIdle()
        assertEquals(1, repository.bidCalls)
        assertEquals(BidUiState.Submitting(Money(1_450_025)), viewModel.bidState.value)

        repository.bidGate?.complete(Unit)
        advanceUntilIdle()
        assertEquals(BidUiState.Success(Money(1_450_025)), viewModel.bidState.value)
        val updated = (viewModel.details.value as VehicleDetails.Ready).vehicle
        assertEquals(Money(1_450_025), updated.auction.currentBid)
        assertEquals(1, updated.auction.bidCount)
    }

    @Test
    fun `competing higher bid returns to entry with latest minimum and requires new review`() = runTest {
        val repository = FakeRepository()
        repository.bidResult = BidResult.AmountTooLow(Money(1_600_000))
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        viewModel.startBid()
        viewModel.reviewBid('.')
        viewModel.confirmBid()
        advanceUntilIdle()
        assertEquals(BidError.TooLow(Money(1_600_000)), (viewModel.bidState.value as BidUiState.Editing).error)
        viewModel.confirmBid()
        advanceUntilIdle()
        assertEquals(1, repository.bidCalls)
        assertEquals(VehicleDetails.Ready(vehicle), viewModel.details.value)
    }

    @Test
    fun `unavailable vehicle leaves entered amount available for correction`() = runTest {
        val repository = FakeRepository()
        repository.bidResult = BidResult.VehicleNotFound
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        viewModel.startBid()
        viewModel.reviewBid('.')
        viewModel.confirmBid()
        advanceUntilIdle()
        assertEquals(BidError.Unavailable, (viewModel.bidState.value as BidUiState.Editing).error)
        assertEquals(1, repository.bidCalls)
    }

    @Test
    fun `details creation and bid state observation do not load inventory`() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        viewModel.details.value
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.bidState.collect {} }
        advanceUntilIdle()
        assertEquals(0, repository.loads)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        assertEquals(1, repository.loads)
        assertEquals(VehicleDetails.Ready(vehicle), viewModel.details.value)
    }

    @Test
    fun `unobserved details cancels its pending load after the timeout and can restart`() = runTest {
        val repository = FakeRepository()
        repository.loadGate = CompletableDeferred()
        val viewModel = viewModel(repository)
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.details.collect {}
        }
        runCurrent()
        assertEquals(1, repository.loads)
        collector.cancel()
        advanceTimeBy(4_999)
        runCurrent()
        assertEquals(0, repository.cancelledLoads)
        advanceTimeBy(2)
        runCurrent()
        assertEquals(1, repository.cancelledLoads)

        repository.loadGate = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.details.collect {} }
        advanceUntilIdle()
        assertEquals(2, repository.loads)
        assertEquals(VehicleDetails.Ready(vehicle), viewModel.details.value)
    }

    private fun viewModel(repository: VehicleRepository, id: String = "target") =
        VehicleDetailsViewModel(
            VehicleDetailsDestination(id),
            ObserveVehicleUseCase(repository),
            LoadInventoryUseCase(repository),
            PlaceBidUseCase(repository),
        )

    private class FakeRepository : VehicleRepository {
        val state = MutableStateFlow<VehicleInventory>(VehicleInventory.NotLoaded)
        override val inventory = state.asStateFlow()
        var loads = 0
        var cancelledLoads = 0
        var loadGate: CompletableDeferred<Unit>? = null
        var bidCalls = 0
        var bidGate: CompletableDeferred<Unit>? = null
        var bidResult: BidResult? = null
        var next: VehicleInventory = VehicleInventory.Ready(
            listOf(vehicle.copy(id = "other"), vehicle),
        )

        override suspend fun loadInventory() {
            loads++
            state.value = VehicleInventory.Loading
            try {
                loadGate?.await()
                state.value = next
            } catch (exception: CancellationException) {
                cancelledLoads++
                throw exception
            }
        }

        override suspend fun placeBid(vehicleId: String, amount: Money): BidResult {
            bidCalls++
            bidGate?.await()
            val result = bidResult ?: BidResult.Accepted(
                vehicle.copy(auction = vehicle.auction.copy(currentBid = amount, bidCount = 1)),
            )
            if (result is BidResult.Accepted) {
                state.value = VehicleInventory.Ready(listOf(result.vehicle))
            }
            return result
        }
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
