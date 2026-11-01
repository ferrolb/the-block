package com.lagniappe.theblock.vehicle.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lagniappe.theblock.vehicle.ui.R

@Composable
internal fun InventoryDemoSettings(
    simulationEnabled: Boolean,
    onSimulationEnabledChanged: (Boolean) -> Unit,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var dialogVisible by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.inventory_more_options))
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.demo_settings_title)) },
                onClick = {
                    menuExpanded = false
                    dialogVisible = true
                },
            )
        }
    }
    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            title = { Text(stringResource(R.string.demo_settings_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(DemoSettingsDefaults.Spacing)) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .toggleable(
                                value = simulationEnabled,
                                role = Role.Switch,
                                onValueChange = onSimulationEnabledChanged,
                            )
                            .padding(vertical = DemoSettingsDefaults.SwitchPadding),
                        horizontalArrangement = Arrangement.spacedBy(DemoSettingsDefaults.Spacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.demo_simulate_bids),
                            modifier = Modifier.weight(DemoSettingsDefaults.LabelWeight),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Switch(checked = simulationEnabled, onCheckedChange = null)
                    }
                    Text(stringResource(R.string.demo_simulation_explanation))
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text(stringResource(R.string.bid_done))
                }
            },
        )
    }
}

private object DemoSettingsDefaults {
    const val LabelWeight = 1f
    val Spacing = 12.dp
    val SwitchPadding = 8.dp
}
