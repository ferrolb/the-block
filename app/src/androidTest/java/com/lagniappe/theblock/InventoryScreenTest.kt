package com.lagniappe.theblock

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
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
