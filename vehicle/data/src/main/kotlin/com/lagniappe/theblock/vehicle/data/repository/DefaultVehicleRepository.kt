package com.lagniappe.theblock.vehicle.data.repository

import com.lagniappe.theblock.vehicle.data.mapper.toDomain
import com.lagniappe.theblock.vehicle.data.remote.VehicleApi
import com.lagniappe.theblock.vehicle.domain.model.BidRules
import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.repository.BidResult
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/** One instance is shared by the app. Accepted bids last for this process only. */
internal class DefaultVehicleRepository(
    private val api: VehicleApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VehicleRepository {
    private val mutableInventory = MutableStateFlow<VehicleInventory>(VehicleInventory.NotLoaded)
    override val inventory = mutableInventory.asStateFlow()
    private val mutex = Mutex()

    override suspend fun loadInventory() = withContext(ioDispatcher) {
        mutex.withLock {
            if (mutableInventory.value is VehicleInventory.Ready) return@withLock

            val previous = mutableInventory.value
            mutableInventory.value = VehicleInventory.Loading
            try {
                val vehicles = api.getVehicles().map { it.toDomain() }
                require(vehicles.map { it.id }.distinct().size == vehicles.size) {
                    "Duplicate vehicle IDs"
                }
                mutableInventory.value = VehicleInventory.Ready(vehicles)
            } catch (exception: CancellationException) {
                mutableInventory.value = previous
                throw exception
            } catch (exception: Exception) {
                val reason = when (exception) {
                    is IOException, is HttpException -> InventoryFailure.UNAVAILABLE
                    is SerializationException, is IllegalArgumentException,
                    is ArithmeticException -> InventoryFailure.INVALID_DATA
                    else -> {
                        mutableInventory.value = previous
                        throw exception
                    }
                }
                mutableInventory.value = VehicleInventory.Failed(reason)
            }
        }
    }

    override suspend fun placeBid(vehicleId: String, amount: Money): BidResult = mutex.withLock {
        val ready = mutableInventory.value as? VehicleInventory.Ready
            ?: return@withLock BidResult.InventoryNotLoaded
        val vehicle = ready.vehicles.find { it.id == vehicleId }
            ?: return@withLock BidResult.VehicleNotFound
        val auction = vehicle.auction
        val minimum = BidRules.minimumBid(auction)
            ?: return@withLock BidResult.BidLimitReached
        if (amount < minimum) return@withLock BidResult.AmountTooLow(minimum)

        val updated = vehicle.copy(
            auction = auction.copy(currentBid = amount, bidCount = auction.bidCount + 1),
        )
        mutableInventory.value = VehicleInventory.Ready(
            ready.vehicles.map { if (it.id == vehicleId) updated else it },
        )
        BidResult.Accepted(updated)
    }
}
