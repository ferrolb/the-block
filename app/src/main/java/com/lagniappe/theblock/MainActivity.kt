package com.lagniappe.theblock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lagniappe.theblock.ui.theme.TheBlockTheme
import com.lagniappe.theblock.vehicle.ui.inventory.InventoryRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheBlockTheme {
                InventoryRoute()
            }
        }
    }
}
