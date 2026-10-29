package com.lagniappe.theblock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lagniappe.theblock.vehicle.ui.details.VehicleDetailsRoute
import com.lagniappe.theblock.vehicle.ui.inventory.InventoryRoute
import com.lagniappe.theblock.vehicle.ui.navigation.InventoryDestination
import com.lagniappe.theblock.vehicle.ui.navigation.VehicleDetailsDestination

@Composable
fun TheBlockNavHost(navController: NavHostController = rememberNavController()) {
    val focusManager = LocalFocusManager.current
    NavHost(navController = navController, startDestination = InventoryDestination) {
        composable<InventoryDestination> {
            InventoryRoute(
                onVehicleClick = { vehicleId ->
                    focusManager.clearFocus()
                    navController.navigate(VehicleDetailsDestination(vehicleId)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<VehicleDetailsDestination> {
            VehicleDetailsRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
