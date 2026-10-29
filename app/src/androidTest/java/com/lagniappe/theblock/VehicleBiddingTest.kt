package com.lagniappe.theblock

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test

class VehicleBiddingTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun validatedAndConfirmedBidUpdatesDetailsAndInventory() {
        openFirstVehicle()
        compose.onNodeWithText("Place bid").performClick()
        compose.onNodeWithText("Amount (CAD)").performTextReplacement("0")
        compose.onNodeWithText("Review bid").performClick()
        compose.onNodeWithText("Enter a positive amount with up to 2 decimal places, without currency symbols or thousands separators.")
            .assertIsDisplayed()

        compose.onNodeWithText("Amount (CAD)").performTextReplacement("21099.99")
        compose.onNodeWithText("Review bid").performClick()
        compose.onNodeWithText("The minimum bid is now", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Amount (CAD)").performTextReplacement("21100.25")
        compose.onNodeWithText("Review bid").performClick()
        compose.onNodeWithText("Confirm your bid").assertIsDisplayed()
        compose.onNodeWithText("Confirm bid").performClick()
        compose.onNodeWithText("Bid placed").assertIsDisplayed()
        compose.onNodeWithText("Done").performClick()

        compose.onNodeWithTag("vehicle-details-list").performScrollToNode(hasText("Bid count"))
        compose.onNodeWithText("12").assertIsDisplayed()
        compose.onNodeWithText("21,100.25", substring = true).assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to inventory").performClick()
        compose.onNodeWithText("21,100.25", substring = true).assertIsDisplayed()
        compose.onNodeWithText("12 bids").assertIsDisplayed()

        compose.onAllNodesWithText("2025 Mazda CX-5")[0].performClick()
        compose.onNodeWithText("Minimum next bid:", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Place bid").performClick()
        compose.onNodeWithText("Amount (CAD)").performTextReplacement("22000")
        compose.onNodeWithText("Review bid").performClick()
        compose.onNodeWithText("Edit amount").performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithContentDescription("Back to inventory").performClick()
        compose.onNodeWithText("12 bids").assertIsDisplayed()
    }

    private fun openFirstVehicle() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("200 vehicles").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Search vehicles").performTextInput("A-0001")
        compose.onAllNodesWithText("2025 Mazda CX-5")[0].performClick()
    }
}
