package com.lagniappe.theblock

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.lagniappe.theblock.navigation.TheBlockNavHost
import com.lagniappe.theblock.ui.theme.TheBlockTheme
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Test

/** Exercise the real themed navigation and bid dialog, including enlarged text. */
class AppearanceJourneyTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun lightAppearance() = verifyAppearance(dark = false, fontScale = 1f, name = "light")

    @Test
    fun darkAppearance() = verifyAppearance(dark = true, fontScale = 1f, name = "dark")

    @Test
    fun largeTextAppearance() = verifyAppearance(dark = false, fontScale = 1.5f, name = "large-text")

    private fun verifyAppearance(dark: Boolean, fontScale: Float, name: String) {
        compose.activityRule.scenario.onActivity { activity ->
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
            activity.setContent {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                    TheBlockTheme(darkTheme = dark) { TheBlockNavHost() }
                }
            }
        }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("200 vehicles").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Search vehicles").assertIsDisplayed()
        capture("$name-inventory")
        compose.onAllNodesWithText("2025 Mazda CX-5")[0].performScrollTo().performClick()
        compose.onNodeWithText("Place bid").assertIsDisplayed()
        capture("$name-details")
        compose.onNodeWithText("Place bid").performClick()
        compose.onNodeWithText("Amount (CAD)").assertIsDisplayed()
        compose.onNodeWithText("Review bid").assertIsDisplayed()
        capture("$name-bid")
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Vehicle details").assertIsDisplayed()
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        // WindowManager dialog transitions are outside Compose's animation clock.
        SystemClock.sleep(350)
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val directory = File(compose.activity.filesDir, "appearance").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
