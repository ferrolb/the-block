package com.lagniappe.theblock.vehicle.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lagniappe.theblock.vehicle.domain.usecase.LoadInventoryUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehicleUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.VehicleDetails
import com.lagniappe.theblock.vehicle.ui.navigation.VehicleDetailsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class VehicleDetailsViewModel internal constructor(
    destination: VehicleDetailsDestination,
    observeVehicle: ObserveVehicleUseCase,
    private val loadInventory: LoadInventoryUseCase,
) : ViewModel() {
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeVehicle: ObserveVehicleUseCase,
        loadInventory: LoadInventoryUseCase,
    ) : this(savedStateHandle.toRoute<VehicleDetailsDestination>(), observeVehicle, loadInventory)

    val vehicleId = destination.vehicleId
    private var loadJob: Job? = null

    val details = observeVehicle(vehicleId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VehicleDetails.Loading,
    )

    init {
        retry()
    }

    fun retry() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch { loadInventory() }
    }
}
