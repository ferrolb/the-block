package com.lagniappe.theblock.vehicle.data.repository

import com.lagniappe.theblock.vehicle.data.remote.VehicleApi
import com.lagniappe.theblock.vehicle.data.remote.VehicleDto
import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.model.Vehicle
import com.lagniappe.theblock.vehicle.domain.repository.BidResult
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DefaultVehicleRepositoryTest {
    @Test
    fun `user leads immediately then competing bids update only that vehicle`() = runTest {
        val repository = repository(simulationEnabled = true)
        repository.loadInventory()
        val vehicle = repository.vehicles().first { it.auction.currentBid == null }
        val others = repository.vehicles().filter { it.id != vehicle.id }
        repository.placeBid(vehicle.id, vehicle.auction.startingBid)
        val leading = repository.vehicles().first { it.id == vehicle.id }.auction
        assertEquals(leading.userBid, leading.currentBid)
        runCurrent()
        advanceTimeBy(2_999)
        assertEquals(leading, repository.vehicles().first { it.id == vehicle.id }.auction)
        advanceTimeBy(7_002)
        runCurrent()
        val outbid = repository.vehicles().first { it.id == vehicle.id }.auction
        assertTrue(requireNotNull(outbid.currentBid) > requireNotNull(outbid.userBid))
        assertTrue(outbid.bidCount > leading.bidCount)
        assertEquals(others, repository.vehicles().filter { it.id != vehicle.id })
    }

    @Test
    fun `new user bid invalidates previous simulation timer`() = runTest {
        val repository = repository(simulationEnabled = true)
        repository.loadInventory()
        val vehicle = repository.vehicles().first { it.auction.currentBid == null }
        repository.placeBid(vehicle.id, vehicle.auction.startingBid)
        runCurrent()
        advanceTimeBy(2_999)
        val next = Money(vehicle.auction.startingBid.cents + 10_000)
        repository.placeBid(vehicle.id, next)
        runCurrent()
        advanceTimeBy(2_999)
        runCurrent()
        val auction = repository.vehicles().first { it.id == vehicle.id }.auction
        assertEquals(next, auction.currentBid)
        assertEquals(next, auction.userBid)
    }

    @Test
    fun `zero competing bids leaves the user in the lead`() = runTest {
        val repository = repository(simulationEnabled = true, bidCount = { 0 })
        repository.loadInventory()
        val vehicle = repository.vehicles().first { it.auction.currentBid == null }
        repository.placeBid(vehicle.id, vehicle.auction.startingBid)
        val leading = repository.vehicles().first { it.id == vehicle.id }
        runCurrent()
        advanceTimeBy(30_001)
        runCurrent()
        assertEquals(leading, repository.vehicles().first { it.id == vehicle.id })
    }

    @Test
    fun `multiple competing bids each advance the expected auction`() = runTest {
        val repository = repository(simulationEnabled = true, bidCount = { 3 })
        repository.loadInventory()
        val vehicle = repository.vehicles().first { it.auction.currentBid == null }
        repository.placeBid(vehicle.id, vehicle.auction.startingBid)
        runCurrent()
        advanceTimeBy(30_001)
        runCurrent()
        val auction = repository.vehicles().first { it.id == vehicle.id }.auction
        assertEquals(4, auction.bidCount)
        assertEquals(Money(vehicle.auction.startingBid.cents + 30_000), auction.currentBid)
        assertEquals(vehicle.auction.startingBid, auction.userBid)
    }

    @Test
    fun `first bid must meet starting price and later bids require CAD 100 increase`() = runTest {
        val repository = repository()
        repository.loadInventory()
        val vehicle = repository.vehicles().first { it.auction.currentBid == null }
        val starting = vehicle.auction.startingBid

        assertEquals(
            BidResult.AmountTooLow(starting),
            repository.placeBid(vehicle.id, Money(starting.cents - 1)),
        )
        assertEquals(vehicle, repository.vehicles().first { it.id == vehicle.id })

        val firstBid = repository.placeBid(vehicle.id, starting) as BidResult.Accepted
        assertEquals(1, firstBid.vehicle.auction.bidCount)
        val nextMinimum = Money(starting.cents + 10_000)
        assertEquals(
            BidResult.AmountTooLow(nextMinimum),
            repository.placeBid(vehicle.id, Money(nextMinimum.cents - 1)),
        )
        val secondBid = repository.placeBid(vehicle.id, nextMinimum) as BidResult.Accepted
        assertEquals(nextMinimum, secondBid.vehicle.auction.currentBid)
        assertEquals(2, secondBid.vehicle.auction.bidCount)
    }

    @Test
    fun `accepted bid is emitted and repeat loading does not overwrite it`() = runTest {
        var calls = 0
        val repository = repository {
            calls++
            fixture()
        }
        repository.loadInventory()
        val original = repository.vehicles().first()
        val observer = async {
            repository.inventory.filterIsInstance<VehicleInventory.Ready>()
                .first { it.vehicles.first().auction.bidCount == original.auction.bidCount + 1 }
        }
        val amount = Money(requireNotNull(original.auction.currentBid).cents + 10_000)

        val result = repository.placeBid(original.id, amount) as BidResult.Accepted
        assertEquals(result.vehicle, observer.await().vehicles.first())
        repository.loadInventory()

        assertEquals(1, calls)
        assertEquals(result.vehicle, repository.vehicles().first())
        assertEquals(fixture()[1].id, repository.vehicles()[1].id)
    }

    @Test
    fun `concurrent bids revalidate against the latest price`() = runTest {
        val repository = repository()
        repository.loadInventory()
        val vehicle = repository.vehicles().first()
        val amount = Money(requireNotNull(vehicle.auction.currentBid).cents + 10_000)

        val results = List(2) {
            async(Dispatchers.Default) { repository.placeBid(vehicle.id, amount) }
        }.awaitAll()

        assertEquals(1, results.count { it is BidResult.Accepted })
        assertEquals(1, results.count { it is BidResult.AmountTooLow })
        assertEquals(vehicle.auction.bidCount + 1, repository.vehicles().first().auction.bidCount)
    }

    @Test
    fun `bids reject unavailable inventory and unknown vehicles`() = runTest {
        val repository = repository()
        assertEquals(
            BidResult.InventoryNotLoaded,
            repository.placeBid("missing", Money(100)),
        )
        repository.loadInventory()
        val before = repository.inventory.value
        assertEquals(BidResult.VehicleNotFound, repository.placeBid("missing", Money(100)))
        assertEquals(before, repository.inventory.value)
    }

    @Test
    fun `duplicate identities are rejected rather than publishing ambiguous state`() = runTest {
        val vehicle = fixture().first()
        val repository = repository { listOf(vehicle, vehicle) }
        repository.loadInventory()
        assertEquals(
            VehicleInventory.Failed(InventoryFailure.INVALID_DATA),
            repository.inventory.value,
        )
    }

    @Test
    fun `negative fixture prices are rejected`() = runTest {
        val vehicle = fixture().first().copy(startingBid = -1)
        val repository = repository { listOf(vehicle) }
        repository.loadInventory()
        assertEquals(
            VehicleInventory.Failed(InventoryFailure.INVALID_DATA),
            repository.inventory.value,
        )
    }

    @Test
    fun `cancelling a load restores prior state and allows retry`() = runTest {
        var block = true
        val pending = CompletableDeferred<List<VehicleDto>>()
        val repository = repository { if (block) pending.await() else fixture() }
        val job = launch { repository.loadInventory() }
        repository.inventory.first { it == VehicleInventory.Loading }
        assertEquals(VehicleInventory.Loading, repository.inventory.value)

        job.cancelAndJoin()
        assertTrue(job.isCancelled)
        assertEquals(VehicleInventory.NotLoaded, repository.inventory.value)
        block = false
        repository.loadInventory()
        assertEquals(200, repository.vehicles().size)
    }

    @Test
    fun `money conversion rejects overflow instead of wrapping prices`() = runTest {
        val vehicle = fixture().first().copy(startingBid = Long.MAX_VALUE)
        val repository = repository { listOf(vehicle) }
        repository.loadInventory()
        assertEquals(
            VehicleInventory.Failed(InventoryFailure.INVALID_DATA),
            repository.inventory.value,
        )
    }

    private fun TestScope.repository(
        simulationEnabled: Boolean = false,
        bidCount: () -> Int = { 1 },
        load: suspend () -> List<VehicleDto> = { fixture() },
    ): DefaultVehicleRepository = DefaultVehicleRepository(
        api = object : VehicleApi {
            override suspend fun getVehicles(): List<VehicleDto> = load()
        },
        ioDispatcher = StandardTestDispatcher(testScheduler),
        simulationScope = backgroundScope,
        simulationEnabled = simulationEnabled,
        simulatedBidCount = bidCount,
    )

    private fun DefaultVehicleRepository.vehicles(): List<Vehicle> =
        (inventory.value as VehicleInventory.Ready).vehicles

    private fun fixture(): List<VehicleDto> =
        requireNotNull(javaClass.getResourceAsStream("/vehicles.json"))
            .bufferedReader().use { Json.decodeFromString(it.readText()) }
}
