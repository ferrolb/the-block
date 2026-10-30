package com.lagniappe.theblock.vehicle.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lagniappe.theblock.vehicle.domain.model.BidRules
import com.lagniappe.theblock.vehicle.domain.usecase.VehicleDetails
import com.lagniappe.theblock.vehicle.ui.R

@Composable
fun VehicleDetailsRoute(
    onBack: () -> Unit,
    viewModel: VehicleDetailsViewModel = hiltViewModel(),
) {
    val details by viewModel.details.collectAsStateWithLifecycle()
    val bidState by viewModel.bidState.collectAsStateWithLifecycle()
    VehicleDetailsScreen(
        vehicleId = viewModel.vehicleId,
        details = details,
        onBack = onBack,
        onRetry = viewModel::retry,
        onPlaceBid = viewModel::startBid,
    )
    val vehicle = (details as? VehicleDetails.Ready)?.vehicle
    VehicleBidDialog(
        state = bidState,
        vehicleTitle = vehicle?.let {
            stringResource(R.string.inventory_vehicle_title, it.year, it.make, it.model)
        } ?: stringResource(R.string.vehicle_details_title),
        onAmountChanged = viewModel::changeBidAmount,
        onReview = viewModel::reviewBid,
        onConfirm = viewModel::confirmBid,
        onEdit = viewModel::editBid,
        onDismiss = viewModel::dismissBid,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailsScreen(
    vehicleId: String,
    details: VehicleDetails,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onPlaceBid: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.testTag("vehicle-details:$vehicleId"),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (details is VehicleDetails.Ready) {
                VehicleBidBar(BidRules.minimumBid(details.vehicle.auction), onPlaceBid, details.vehicle.auction)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.vehicle_details_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
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
        when (details) {
            VehicleDetails.NotLoaded, VehicleDetails.Loading -> DetailsStatus(
                title = stringResource(R.string.vehicle_details_loading),
                modifier = Modifier.padding(innerPadding),
                loading = true,
            )
            VehicleDetails.NotFound -> DetailsStatus(
                title = stringResource(R.string.vehicle_details_not_found),
                message = stringResource(R.string.vehicle_details_not_found_message),
                action = stringResource(R.string.vehicle_details_back),
                onAction = onBack,
                modifier = Modifier.padding(innerPadding),
            )
            is VehicleDetails.Failed -> DetailsStatus(
                title = stringResource(R.string.vehicle_details_error),
                message = stringResource(R.string.inventory_unavailable),
                action = stringResource(R.string.inventory_retry),
                onAction = onRetry,
                modifier = Modifier.padding(innerPadding),
            )
            is VehicleDetails.Ready -> {
                val vehicle = details.vehicle
                val direction = LocalLayoutDirection.current
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(
                        modifier = Modifier.widthIn(max = VehicleDetailsScreenDefaults.ContentMaxWidth).fillMaxSize()
                            .consumeWindowInsets(innerPadding).testTag("vehicle-details-list"),
                        contentPadding = PaddingValues(
                            start = innerPadding.calculateLeftPadding(direction) +
                                VehicleDetailsScreenDefaults.ContentHorizontalPadding,
                            top = innerPadding.calculateTopPadding() + VehicleDetailsScreenDefaults.ContentTopPadding,
                            end = innerPadding.calculateRightPadding(direction) +
                                VehicleDetailsScreenDefaults.ContentHorizontalPadding,
                            bottom = innerPadding.calculateBottomPadding() +
                                VehicleDetailsScreenDefaults.ContentBottomPadding,
                        ),
                        verticalArrangement = Arrangement.spacedBy(VehicleDetailsScreenDefaults.SectionSpacing),
                    ) {
                        item(key = "photos") { VehiclePhotoGallery(vehicle.images) }
                        item(key = "identity") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(
                                    VehicleDetailsScreenDefaults.IdentitySpacing,
                                ),
                            ) {
                                Text(
                                    stringResource(
                                        R.string.inventory_vehicle_title,
                                        vehicle.year, vehicle.make, vehicle.model,
                                    ),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(vehicle.trim, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.inventory_lot, vehicle.lot))
                                SelectionContainer {
                                    Text(stringResource(R.string.vehicle_details_vin, vehicle.vin))
                                }
                            }
                        }
                        item(key = "specs") { VehicleSpecifications(vehicle.specs) }
                        item(key = "condition") { VehicleConditionReport(vehicle.condition) }
                        item(key = "damage") { VehicleDamageNotes(vehicle.condition.damageNotes) }
                        item(key = "dealer") { VehicleDealership(vehicle.dealership) }
                        item(key = "auction") { VehicleAuctionInformation(vehicle.auction) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsStatus(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    action: String? = null,
    onAction: () -> Unit = {},
    loading: Boolean = false,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(VehicleDetailsScreenDefaults.StatusPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(VehicleDetailsScreenDefaults.StatusSpacing),
        ) {
            if (loading) CircularProgressIndicator()
            Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            if (message != null) Text(message, textAlign = TextAlign.Center)
            if (action != null) Button(onClick = onAction) { Text(action) }
        }
    }
}

private object VehicleDetailsScreenDefaults {
    val ContentMaxWidth = 720.dp
    val ContentHorizontalPadding = 20.dp
    val ContentTopPadding = 16.dp
    val ContentBottomPadding = 24.dp
    val SectionSpacing = 20.dp
    val IdentitySpacing = 8.dp
    val StatusPadding = 24.dp
    val StatusSpacing = 16.dp
}
