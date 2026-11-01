package com.lagniappe.theblock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import com.lagniappe.theblock.vehicle.domain.model.Dealership
import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.model.Vehicle
import com.lagniappe.theblock.vehicle.domain.model.VehicleAuction
import com.lagniappe.theblock.vehicle.domain.model.VehicleCondition
import com.lagniappe.theblock.vehicle.domain.model.VehicleSpecs
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.ui.inventory.InventoryContent
import com.lagniappe.theblock.vehicle.ui.inventory.InventoryScreen
import com.lagniappe.theblock.vehicle.ui.inventory.InventoryUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InventoryScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun acceptedBidIndicatorUpdatesWhenOutbid() {
        val vehicle = mutableStateOf(
            Vehicle(
                id = "vehicle-1", vin = "VIN1", year = 2025, make = "Mazda",
                model = "CX-5", trim = "GT", lot = "A-0001",
                specs = VehicleSpecs("SUV", "Blue", "Black", "2.5L", "Automatic", "AWD", 1000, "Gas"),
                condition = VehicleCondition(4.0, "Good", emptyList(), "Clean"),
                dealership = Dealership("Demo dealer", "Toronto", "ON"),
                auction = VehicleAuction(
                    scheduledStart = "2026-01-01T09:00:00",
                    startingBid = Money(2_000_000), reservePrice = null,
                    buyNowPrice = null, currentBid = Money(2_100_000), bidCount = 1,
                ),
                images = emptyList(),
            ),
        )
        compose.setContent {
            MaterialTheme {
                InventoryScreen(
                    InventoryUiState(content = InventoryContent.Vehicles(listOf(vehicle.value))),
                    onQueryChanged = {},
                    onRetry = {},
                )
            }
        }
        compose.onNodeWithText("Your bid · Leading").assertDoesNotExist()
        compose.onNodeWithText("Your bid · Outbid").assertDoesNotExist()

        compose.runOnIdle {
            vehicle.value = vehicle.value.copy(
                auction = vehicle.value.auction.copy(
                    currentBid = Money(2_110_000), userBid = Money(2_110_000), bidCount = 2,
                ),
            )
        }
        compose.onNodeWithText("Your bid · Leading").assertIsDisplayed()
        compose.onNodeWithText("Your bid · Outbid").assertDoesNotExist()

        compose.runOnIdle {
            vehicle.value = vehicle.value.copy(
                auction = vehicle.value.auction.copy(currentBid = Money(2_120_000), bidCount = 3),
            )
        }
        compose.onNodeWithText("Your bid · Outbid").assertIsDisplayed()
        compose.onNodeWithText("Your bid · Leading").assertDoesNotExist()
    }

    @Test
    fun myBidsPillAppearsAfterBidAndCanBeToggledOffFromEmptyResults() {
        val state = mutableStateOf(
            InventoryUiState(content = InventoryContent.Vehicles(emptyList())),
        )
        compose.setContent {
            MaterialTheme {
                InventoryScreen(
                    state = state.value,
                    onQueryChanged = {},
                    onRetry = {},
                    onMyBidsOnlyChanged = { state.value = state.value.copy(myBidsOnly = it) },
                )
            }
        }
        compose.onNodeWithText("My bids").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(hasUserBids = true) }
        compose.onNodeWithText("My bids").assertIsDisplayed().assertIsNotSelected().performClick()
        compose.onNodeWithText("My bids").assertIsSelected()
        compose.onNodeWithText("Try another search or turn off My bids to see more vehicles.")
            .assertIsDisplayed()
        compose.onNodeWithText("Show all vehicles").performClick()
        compose.onNodeWithText("My bids").assertIsNotSelected()
        compose.onNodeWithText("My bids").performClick().performClick()
        compose.onNodeWithText("My bids").assertIsNotSelected()
    }

    @Test
    fun loadingIsVisible() {
        compose.setContent {
            MaterialTheme { InventoryScreen(InventoryUiState(), {}, {}) }
        }
        compose.onNodeWithText("Loading vehicles…").assertIsDisplayed()
    }

    @Test
    fun failureOffersRetry() {
        var retries = 0
        compose.setContent {
            MaterialTheme {
                InventoryScreen(
                    InventoryUiState(content = InventoryContent.Error(InventoryFailure.UNAVAILABLE)),
                    onQueryChanged = {},
                    onRetry = { retries++ },
                )
            }
        }
        compose.onNodeWithText("Unable to load vehicles").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun emptySearchOffersClearAction() {
        var query = "missing"
        compose.setContent {
            MaterialTheme {
                InventoryScreen(
                    InventoryUiState(query, InventoryContent.Vehicles(emptyList())),
                    onQueryChanged = { query = it },
                    onRetry = {},
                )
            }
        }
        compose.onNodeWithText("No matching vehicles").assertIsDisplayed()
        compose.onNodeWithText("Clear search").performClick()
        assertEquals("", query)
    }

    @Test
    fun emptyInventoryHasItsOwnMessage() {
        compose.setContent {
            MaterialTheme {
                InventoryScreen(
                    InventoryUiState(content = InventoryContent.Vehicles(emptyList())),
                    onQueryChanged = {},
                    onRetry = {},
                )
            }
        }
        compose.onNodeWithText("No vehicles available").assertIsDisplayed()
    }
}
