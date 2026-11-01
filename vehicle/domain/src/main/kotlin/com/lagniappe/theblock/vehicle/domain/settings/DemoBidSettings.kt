package com.lagniappe.theblock.vehicle.domain.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Process-local demo controls shared by the repository and presentation layer. */
class DemoBidSettings(initialEnabled: Boolean) {
    data class State(
        val simulationEnabled: Boolean,
        // Invalidates pending bids even if simulation is switched off and back on.
        val revision: Long = 0,
    )

    private val mutableState = MutableStateFlow(State(initialEnabled))
    val state = mutableState.asStateFlow()

    fun setSimulationEnabled(enabled: Boolean) {
        mutableState.update { previous ->
            if (previous.simulationEnabled == enabled) previous
            else State(enabled, previous.revision + 1)
        }
    }
}
