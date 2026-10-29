package com.lagniappe.theblock

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.domain.usecase.VehicleDetails
import com.lagniappe.theblock.vehicle.ui.details.VehicleDetailsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VehicleDetailsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun loadingIsVisibleAndBackRemainsAvailable() {
        var backs = 0
        compose.setContent {
            MaterialTheme {
                VehicleDetailsScreen("unknown", VehicleDetails.Loading, { backs++ }, {})
            }
        }
        compose.onNodeWithText("Loading vehicle…").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to inventory").performClick()
        assertEquals(1, backs)
    }

    @Test
    fun failedLoadOffersRetry() {
        var retries = 0
        compose.setContent {
            MaterialTheme {
                VehicleDetailsScreen(
                    "unknown", VehicleDetails.Failed(InventoryFailure.UNAVAILABLE), {}, { retries++ },
                )
            }
        }
        compose.onNodeWithText("Unable to load vehicle").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun unknownVehicleHasExplicitNotFoundState() {
        var backs = 0
        compose.setContent {
            MaterialTheme {
                VehicleDetailsScreen("unknown", VehicleDetails.NotFound, { backs++ }, {})
            }
        }
        compose.onNodeWithText("Vehicle not found").assertIsDisplayed()
        compose.onNodeWithText("Back to inventory").performClick()
        assertEquals(1, backs)
    }
}
