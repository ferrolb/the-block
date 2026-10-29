package com.lagniappe.theblock

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
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
    fun selectedVehicleDisplaysCompleteDetailsAndRestoresPhoto() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("200 vehicles").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText("2025 Mazda CX-5")[0].performClick()
        compose.onNodeWithText("Photo 1 of 4").assertIsDisplayed()
        compose.onNodeWithContentDescription("Next photo").performClick()
        compose.onNodeWithText("Photo 2 of 4").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Photo 2 of 4").assertIsDisplayed()

        val list = compose.onNodeWithTag("vehicle-details-list")
        list.performScrollToNode(hasText("Engine"))
        compose.onNodeWithText("2.5L I4").assertIsDisplayed()
        list.performScrollToNode(hasText("Condition grade"))
        compose.onNodeWithText("4 / 5").assertIsDisplayed()
        list.performScrollToNode(hasText("No damage notes reported."))
        compose.onNodeWithText("No damage notes reported.").assertIsDisplayed()
        list.performScrollToNode(hasText("Highway 7 Auto Sales"))
        compose.onNodeWithText("Highway 7 Auto Sales").assertIsDisplayed()
        list.performScrollToNode(hasText("Reserve status"))
        compose.onNodeWithText("Reserve not met").assertIsDisplayed()
        list.performScrollToNode(hasText("Scheduled start (local time)"))
        compose.onNodeWithText("2026-04-05 19:00:00").assertIsDisplayed()
        compose.onNodeWithText("Not available").assertIsDisplayed()
    }

    @Test
    fun damageNotesAndMissingReserveAreDisplayed() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("200 vehicles").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Search vehicles").performTextInput("A-0002")
        compose.onAllNodesWithText("2019 Chevrolet Equinox")[0].performClick()
        val list = compose.onNodeWithTag("vehicle-details-list")
        list.performScrollToNode(hasText("• Curb rash on front-right wheel"))
        compose.onNodeWithText("• Curb rash on front-right wheel").assertIsDisplayed()
        compose.onNodeWithText("• Minor scratches on front bumper").assertIsDisplayed()
        list.performScrollToNode(hasText("Reserve price"))
        compose.onNodeWithText("Not provided").assertIsDisplayed()
    }

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
