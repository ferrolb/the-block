package com.lagniappe.theblock.vehicle.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lagniappe.theblock.vehicle.domain.model.BidRules
import com.lagniappe.theblock.vehicle.domain.repository.BidResult
import com.lagniappe.theblock.vehicle.domain.usecase.PlaceBidUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.LoadInventoryUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehicleUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.VehicleDetails
import com.lagniappe.theblock.vehicle.ui.navigation.VehicleDetailsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class VehicleDetailsViewModel internal constructor(
    destination: VehicleDetailsDestination,
    observeVehicle: ObserveVehicleUseCase,
    private val loadInventory: LoadInventoryUseCase,
    private val placeBid: PlaceBidUseCase,
) : ViewModel() {
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeVehicle: ObserveVehicleUseCase,
        loadInventory: LoadInventoryUseCase,
        placeBid: PlaceBidUseCase,
    ) : this(savedStateHandle.toRoute<VehicleDetailsDestination>(), observeVehicle, loadInventory, placeBid)

    val vehicleId = destination.vehicleId
    private var loadJob: Job? = null
    private val mutableBidState = MutableStateFlow<BidUiState>(BidUiState.Closed)
    val bidState = mutableBidState.asStateFlow()

    val details = channelFlow {
        // Observe updates concurrently with the subscription-owned initial load.
        if (loadJob?.isActive != true) {
            loadJob = launch { loadInventory() }
        }
        observeVehicle(vehicleId).collect { send(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VehicleDetails.Loading,
    )

    fun startBid() {
        if (mutableBidState.value != BidUiState.Closed) return
        val vehicle = (details.value as? VehicleDetails.Ready)?.vehicle ?: return
        val minimum = BidRules.minimumBid(vehicle.auction) ?: return
        mutableBidState.value = BidUiState.Editing(minimum.toBidInput())
    }

    fun changeBidAmount(amount: String) {
        if (mutableBidState.value is BidUiState.Editing) {
            mutableBidState.value = BidUiState.Editing(amount)
        }
    }

    fun reviewBid(decimalSeparator: Char) {
        val draft = mutableBidState.value as? BidUiState.Editing ?: return
        val amount = parseBidAmount(draft.amount, decimalSeparator)
        val vehicle = (details.value as? VehicleDetails.Ready)?.vehicle
        val minimum = vehicle?.let { BidRules.minimumBid(it.auction) }
        mutableBidState.value = when {
            amount == null -> draft.copy(error = BidError.InvalidAmount)
            minimum == null -> draft.copy(error = BidError.Unavailable)
            amount < minimum -> draft.copy(error = BidError.TooLow(minimum))
            else -> BidUiState.Confirming(amount)
        }
    }

    fun editBid() {
        val confirmation = mutableBidState.value as? BidUiState.Confirming ?: return
        mutableBidState.value = BidUiState.Editing(confirmation.amount.toBidInput())
    }

    fun confirmBid() {
        val confirmation = mutableBidState.value as? BidUiState.Confirming ?: return
        mutableBidState.value = BidUiState.Submitting(confirmation.amount)
        viewModelScope.launch {
            try {
                mutableBidState.value = when (val result = placeBid(vehicleId, confirmation.amount)) {
                    is BidResult.Accepted -> BidUiState.Success(
                        requireNotNull(result.vehicle.auction.currentBid),
                    )
                    is BidResult.AmountTooLow -> BidUiState.Editing(
                        confirmation.amount.toBidInput(), BidError.TooLow(result.minimum),
                    )
                    BidResult.VehicleNotFound, BidResult.InventoryNotLoaded, BidResult.BidLimitReached ->
                        BidUiState.Editing(confirmation.amount.toBidInput(), BidError.Unavailable)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                mutableBidState.value = BidUiState.Editing(
                    confirmation.amount.toBidInput(), BidError.SubmissionFailed,
                )
            }
        }
    }

    fun dismissBid() {
        if (mutableBidState.value !is BidUiState.Submitting) {
            mutableBidState.value = BidUiState.Closed
        }
    }

    fun retry() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch { loadInventory() }
    }
}
