package com.lagniappe.theblock

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class VehicleNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun selectedVehicleSurvivesRecreationAndBackRestoresSearch() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("200 vehicles").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Search vehicles").performTextInput("2025 mazda cx-5")
        compose.onAllNodesWithText("2025 Mazda CX-5")[0].performClick()

        val detailsTag = "vehicle-details:4e3cd74f-bb88-4efe-b234-bcb2f7474b40"
        compose.onNodeWithTag(detailsTag).assertIsDisplayed()
        compose.onNodeWithText("Vehicle details").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag(detailsTag).assertIsDisplayed()

        compose.onNodeWithContentDescription("Back to inventory").performClick()
        compose.onNodeWithText("Search vehicles").assertTextContains("2025 mazda cx-5")
        compose.onAllNodesWithText("2025 Mazda CX-5")[0].assertIsDisplayed()

        compose.onAllNodesWithText("2025 Mazda CX-5")[0].performClick()
        compose.onNodeWithTag(detailsTag).assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("Search vehicles").assertTextContains("2025 mazda cx-5")
    }
}
