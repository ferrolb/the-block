package com.lagniappe.theblock

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class InventoryJourneyTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun inventoryLoadsSearchesAndRestoresQueryAfterRecreation() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("200 vehicles").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("200 vehicles").assertIsDisplayed()
        compose.onAllNodesWithText("2025 Mazda CX-5")[0].assertIsDisplayed()
        compose.onNodeWithText("Search vehicles").performTextInput("zzzz-no-match")
        compose.onNodeWithText("No matching vehicles").assertIsDisplayed()

        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Search vehicles").assertTextContains("zzzz-no-match")
        compose.onNodeWithText("No matching vehicles").assertIsDisplayed()
        compose.onNodeWithContentDescription("Clear search").performClick()
        compose.onNodeWithText("200 vehicles").assertIsDisplayed()

        compose.onNodeWithText("Search vehicles").performTextInput("2025 mazda cx-5")
        compose.onAllNodesWithText("2025 Mazda CX-5")[0].assertIsDisplayed()
    }
}
