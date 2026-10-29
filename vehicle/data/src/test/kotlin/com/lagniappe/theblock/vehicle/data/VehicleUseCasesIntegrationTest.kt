package com.lagniappe.theblock.vehicle.data

import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.repository.BidResult
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import com.lagniappe.theblock.vehicle.domain.usecase.LoadInventoryUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehicleUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehiclesUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.PlaceBidUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.VehicleDetails
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleUseCasesIntegrationTest {
    @Test
    fun `search details and bids operate together without Android or UI`() = runTest {
        val repository = DemoVehicleRepositoryFactory.create()
        val load = LoadInventoryUseCase(repository)
        val search = ObserveVehiclesUseCase(repository)
        val details = ObserveVehicleUseCase(repository)
        val bid = PlaceBidUseCase(repository)
        assertEquals(VehicleInventory.NotLoaded, search().first())

        load()
        val matches = (search("  2025  mAzDa CX-5 ").first() as VehicleInventory.Ready).vehicles
        val vehicle = matches.first()
        assertEquals(VehicleDetails.Ready(vehicle), details(vehicle.id).first())
        val minimum = Money(requireNotNull(vehicle.auction.currentBid).cents + 10_000)

        assertEquals(
            BidResult.AmountTooLow(minimum),
            bid(vehicle.id, Money(minimum.cents - 1)),
        )
        assertEquals(VehicleDetails.Ready(vehicle), details(vehicle.id).first())

        val updatedDetail = async(start = CoroutineStart.UNDISPATCHED) {
            details(vehicle.id).first {
                it is VehicleDetails.Ready &&
                    it.vehicle.auction.bidCount == vehicle.auction.bidCount + 1
            }
        }
        val updatedSearch = async(start = CoroutineStart.UNDISPATCHED) {
            search("2025 mazda cx-5").first { state ->
                state is VehicleInventory.Ready && state.vehicles.any {
                    it.id == vehicle.id && it.auction.bidCount == vehicle.auction.bidCount + 1
                }
            }
        }
        val accepted = bid(vehicle.id, minimum) as BidResult.Accepted

        assertEquals(minimum, accepted.vehicle.auction.currentBid)
        assertEquals(VehicleDetails.Ready(accepted.vehicle), updatedDetail.await())
        val searchResult = updatedSearch.await() as VehicleInventory.Ready
        assertEquals(accepted.vehicle, searchResult.vehicles.first { it.id == vehicle.id })
        load()
        assertEquals(VehicleDetails.Ready(accepted.vehicle), details(vehicle.id).first())
    }
}
