package com.lagniappe.theblock.vehicle.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.ui.R
import com.lagniappe.theblock.ui.theme.TheBlockTheme

@Composable
fun InventoryRoute(
    onVehicleClick: (String) -> Unit,
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InventoryScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onRetry = viewModel::retry,
        onVehicleClick = onVehicleClick,
    )
}

@Composable
fun InventoryScreen(
    state: InventoryUiState,
    onQueryChanged: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onVehicleClick: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        if (
                            MaterialTheme.colorScheme.surface.luminance() >
                            InventoryScreenDefaults.LightSurfaceLuminanceThreshold
                        ) {
                            InventoryScreenDefaults.LightHeaderColor
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = InventoryScreenDefaults.ContentMaxWidth)
                        .fillMaxWidth()
                        .padding(InventoryScreenDefaults.HeaderPadding),
                    verticalArrangement = Arrangement.spacedBy(InventoryScreenDefaults.SectionSpacing),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(InventoryScreenDefaults.BrandSpacing),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.width(InventoryScreenDefaults.BrandAccentWidth)
                                    .height(InventoryScreenDefaults.BrandAccentHeight)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                            Text(
                                text = stringResource(R.string.inventory_brand),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = stringResource(R.string.inventory_openlane),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(R.string.inventory_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        label = { Text(stringResource(R.string.inventory_search_label)) },
                        placeholder = { Text(stringResource(R.string.inventory_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChanged("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.inventory_clear_search),
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    )
                    val vehicles = (state.content as? InventoryContent.Vehicles)?.vehicles
                    Text(
                        text = if (vehicles == null) {
                            stringResource(R.string.inventory_subtitle)
                        } else {
                            pluralStringResource(R.plurals.inventory_count, vehicles.size, vehicles.size)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.fillMaxSize()
        when (val content = state.content) {
            InventoryContent.Loading -> Box(
                modifier = contentModifier.padding(innerPadding).consumeWindowInsets(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(InventoryScreenDefaults.LoadingSpacing),
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.inventory_loading))
                }
            }

            is InventoryContent.Error -> InventoryMessage(
                title = stringResource(R.string.inventory_error_title),
                message = stringResource(
                    if (content.reason == InventoryFailure.INVALID_DATA) {
                        R.string.inventory_invalid_data
                    } else {
                        R.string.inventory_unavailable
                    },
                ),
                action = stringResource(R.string.inventory_retry),
                onAction = onRetry,
                modifier = contentModifier.padding(innerPadding).consumeWindowInsets(innerPadding),
            )

            is InventoryContent.Vehicles -> if (content.vehicles.isEmpty()) {
                val searching = state.query.isNotBlank()
                InventoryMessage(
                    title = stringResource(
                        if (searching) R.string.inventory_no_matches else R.string.inventory_empty_title,
                    ),
                    message = stringResource(
                        if (searching) R.string.inventory_search_empty_message else R.string.inventory_empty_message,
                    ),
                    action = if (searching) stringResource(R.string.inventory_clear_search) else null,
                    onAction = { onQueryChanged("") },
                    modifier = contentModifier.padding(innerPadding).consumeWindowInsets(innerPadding),
                )
            } else {
                val direction = LocalLayoutDirection.current
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(
                        modifier = Modifier.widthIn(max = InventoryScreenDefaults.ContentMaxWidth).fillMaxSize()
                            .consumeWindowInsets(innerPadding),
                        contentPadding = PaddingValues(
                            start = innerPadding.calculateLeftPadding(direction) +
                                InventoryScreenDefaults.ContentHorizontalPadding,
                            top = innerPadding.calculateTopPadding() + InventoryScreenDefaults.ContentTopPadding,
                            end = innerPadding.calculateRightPadding(direction) +
                                InventoryScreenDefaults.ContentHorizontalPadding,
                            bottom = innerPadding.calculateBottomPadding() +
                                InventoryScreenDefaults.ContentBottomPadding,
                        ),
                        verticalArrangement = Arrangement.spacedBy(InventoryScreenDefaults.ListItemSpacing),
                    ) {
                        items(content.vehicles, key = { it.id }) { vehicle ->
                            VehicleCard(vehicle, onClick = { onVehicleClick(vehicle.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryMessage(
    title: String,
    message: String,
    action: String?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = InventoryScreenDefaults.MessageMaxWidth)
                .padding(InventoryScreenDefaults.MessagePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(InventoryScreenDefaults.SectionSpacing),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            if (action != null) {
                Button(onClick = onAction) { Text(action) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryLoadingPreview() {
    TheBlockTheme {
        InventoryScreen(InventoryUiState(), onQueryChanged = {}, onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryEmptySearchPreview() {
    TheBlockTheme {
        InventoryScreen(
            InventoryUiState("unknown", InventoryContent.Vehicles(emptyList())),
            onQueryChanged = {},
            onRetry = {},
        )
    }
}

private object InventoryScreenDefaults {
    const val LightSurfaceLuminanceThreshold = 0.5f
    val LightHeaderColor = Color(0xFFD6EBFA)
    val ContentMaxWidth = 720.dp
    val HeaderPadding = 20.dp
    val ContentHorizontalPadding = 20.dp
    val ContentTopPadding = 16.dp
    val ContentBottomPadding = 24.dp
    val SectionSpacing = 12.dp
    val BrandSpacing = 8.dp
    val BrandAccentWidth = 4.dp
    val BrandAccentHeight = 20.dp
    val ListItemSpacing = 16.dp
    val LoadingSpacing = 16.dp
    val MessageMaxWidth = 480.dp
    val MessagePadding = 24.dp
}
