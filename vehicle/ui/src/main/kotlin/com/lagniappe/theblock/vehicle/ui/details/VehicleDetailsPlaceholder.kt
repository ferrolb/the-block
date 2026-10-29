package com.lagniappe.theblock.vehicle.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lagniappe.theblock.vehicle.ui.R

/** Navigation scaffold for the upcoming details feature. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailsPlaceholder(vehicleId: String, onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.testTag("vehicle-details:$vehicleId"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vehicle_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.vehicle_details_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .consumeWindowInsets(innerPadding).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.vehicle_details_placeholder))
        }
    }
}
