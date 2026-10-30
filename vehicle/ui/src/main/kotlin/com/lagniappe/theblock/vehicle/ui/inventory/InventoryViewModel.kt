package com.lagniappe.theblock.vehicle.ui.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lagniappe.theblock.vehicle.domain.model.Vehicle
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import com.lagniappe.theblock.vehicle.domain.usecase.LoadInventoryUseCase
import com.lagniappe.theblock.vehicle.domain.usecase.ObserveVehiclesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val query: String = "",
    val content: InventoryContent = InventoryContent.Loading,
)

sealed interface InventoryContent {
    data object Loading : InventoryContent

    data class Vehicles(
        val vehicles: List<Vehicle>,
    ) : InventoryContent

    data class Error(
        val reason: InventoryFailure,
    ) : InventoryContent
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        observeVehicles: ObserveVehiclesUseCase,
        private val loadInventory: LoadInventoryUseCase,
    ) : ViewModel() {
        private val query = savedStateHandle.getStateFlow(QUERY_KEY, "")
        private var loadJob: Job? = null

        val uiState =
            channelFlow {
                // This child belongs to the observed flow and is canceled when sharing stops.
                if (loadJob?.isActive != true) {
                    loadJob = launch { loadInventory() }
                }
                query
                    .flatMapLatest { search ->
                        observeVehicles(search).map { inventory ->
                            InventoryUiState(
                                query = search,
                                content =
                                    when (inventory) {
                                        VehicleInventory.NotLoaded, VehicleInventory.Loading -> InventoryContent.Loading
                                        is VehicleInventory.Failed -> InventoryContent.Error(inventory.reason)
                                        is VehicleInventory.Ready -> InventoryContent.Vehicles(inventory.vehicles)
                                    },
                            )
                        }
                    }.collect { send(it) }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InventoryUiState(query = query.value),
            )

        fun onQueryChanged(query: String) {
            savedStateHandle[QUERY_KEY] = query
        }

        fun retry() {
            if (loadJob?.isActive == true) return
            loadJob = viewModelScope.launch { loadInventory() }
        }

        private companion object {
            const val QUERY_KEY = "inventory_query"
        }
    }
