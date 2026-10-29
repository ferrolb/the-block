package com.lagniappe.theblock.vehicle.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun InventoryRoute(viewModel: InventoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InventoryScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onRetry = viewModel::retry,
    )
}

@Composable
fun InventoryScreen(
    state: InventoryUiState,
    onQueryChanged: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.inventory_brand),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.inventory_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        modifier = Modifier.widthIn(max = 720.dp).fillMaxSize()
                            .consumeWindowInsets(innerPadding),
                        contentPadding = PaddingValues(
                            start = innerPadding.calculateLeftPadding(direction) + 20.dp,
                            top = innerPadding.calculateTopPadding(),
                            end = innerPadding.calculateRightPadding(direction) + 20.dp,
                            bottom = innerPadding.calculateBottomPadding() + 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(content.vehicles, key = { it.id }) { vehicle ->
                            VehicleCard(vehicle)
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
            modifier = Modifier.widthIn(max = 480.dp).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
    MaterialTheme {
        InventoryScreen(InventoryUiState(), onQueryChanged = {}, onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryEmptySearchPreview() {
    MaterialTheme {
        InventoryScreen(
            InventoryUiState("unknown", InventoryContent.Vehicles(emptyList())),
            onQueryChanged = {},
            onRetry = {},
        )
    }
}
