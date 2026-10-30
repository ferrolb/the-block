package com.lagniappe.theblock.vehicle.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lagniappe.theblock.vehicle.domain.model.Dealership
import com.lagniappe.theblock.vehicle.domain.model.VehicleAuction
import com.lagniappe.theblock.vehicle.domain.model.VehicleCondition
import com.lagniappe.theblock.vehicle.domain.model.VehicleSpecs
import com.lagniappe.theblock.vehicle.ui.R
import com.lagniappe.theblock.vehicle.ui.format.rememberMoneyFormatter
import java.text.NumberFormat

@Composable
internal fun VehicleSpecifications(specs: VehicleSpecs) {
    val locale = LocalConfiguration.current.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    DetailsSection(stringResource(R.string.vehicle_details_specs)) {
        DetailValue(stringResource(R.string.vehicle_details_body), specs.bodyStyle)
        DetailValue(stringResource(R.string.vehicle_details_engine), specs.engine)
        DetailValue(stringResource(R.string.vehicle_details_transmission), specs.transmission)
        DetailValue(stringResource(R.string.vehicle_details_drivetrain), specs.drivetrain)
        DetailValue(stringResource(R.string.vehicle_details_fuel), specs.fuelType)
        DetailValue(
            stringResource(R.string.vehicle_details_odometer),
            stringResource(R.string.inventory_mileage, numberFormat.format(specs.odometerKm)),
        )
        DetailValue(stringResource(R.string.vehicle_details_exterior), specs.exteriorColor)
        DetailValue(stringResource(R.string.vehicle_details_interior), specs.interiorColor)
    }
}

@Composable
internal fun VehicleConditionReport(condition: VehicleCondition) {
    val locale = LocalConfiguration.current.locales[0]
    val format = remember(locale) { NumberFormat.getNumberInstance(locale) }
    DetailsSection(stringResource(R.string.vehicle_details_condition)) {
        DetailValue(
            stringResource(R.string.vehicle_details_grade),
            stringResource(R.string.vehicle_details_grade_value, format.format(condition.grade)),
        )
        DetailValue(stringResource(R.string.vehicle_details_title_status), condition.titleStatus)
        Text(condition.report, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun VehicleDamageNotes(notes: List<String>) {
    DetailsSection(stringResource(R.string.vehicle_details_damage)) {
        if (notes.isEmpty()) {
            Text(stringResource(R.string.vehicle_details_no_damage_notes))
        } else {
            notes.forEach { note ->
                Text(stringResource(R.string.vehicle_details_damage_note, note))
            }
        }
    }
}

@Composable
internal fun VehicleDealership(dealership: Dealership) {
    DetailsSection(stringResource(R.string.vehicle_details_dealership)) {
        Text(dealership.name, style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.inventory_location, dealership.city, dealership.province),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
internal fun VehicleAuctionInformation(auction: VehicleAuction) {
    val format = rememberMoneyFormatter()
    val unavailable = stringResource(R.string.vehicle_details_not_provided)
    DetailsSection(stringResource(R.string.vehicle_details_auction)) {
        DetailValue(
            stringResource(R.string.inventory_current_bid),
            auction.currentBid?.let { format.format(it) }
                ?: stringResource(R.string.vehicle_details_no_bids),
        )
        DetailValue(stringResource(R.string.vehicle_details_bid_count), auction.bidCount.toString())
        DetailValue(
            stringResource(R.string.inventory_starting_bid),
            format.format(auction.startingBid),
        )
        DetailValue(
            stringResource(R.string.vehicle_details_reserve),
            auction.reservePrice?.let { format.format(it) } ?: unavailable,
        )
        auction.reservePrice?.let { reserve ->
            val met = auction.currentBid?.let { it >= reserve } == true
            DetailValue(
                stringResource(R.string.vehicle_details_reserve_status),
                stringResource(
                    if (met) R.string.vehicle_details_reserve_met else R.string.vehicle_details_reserve_not_met,
                ),
            )
        }
        DetailValue(
            stringResource(R.string.vehicle_details_buy_now),
            auction.buyNowPrice?.let { format.format(it) }
                ?: stringResource(R.string.vehicle_details_buy_now_unavailable),
        )
        DetailValue(
            stringResource(R.string.vehicle_details_scheduled_start),
            auction.scheduledStart.replace('T', ' '),
        )
        Text(
            stringResource(R.string.vehicle_details_auction_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
private fun DetailValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            modifier = Modifier.weight(1.4f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
        )
    }
}
