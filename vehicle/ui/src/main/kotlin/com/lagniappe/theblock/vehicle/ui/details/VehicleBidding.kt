package com.lagniappe.theblock.vehicle.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.ui.R
import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
internal fun VehicleBidBar(minimum: Money?, onPlaceBid: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    Surface(tonalElevation = 3.dp) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (minimum == null) stringResource(R.string.bid_unavailable) else
                    stringResource(R.string.bid_minimum, formatBidMoney(minimum, locale)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onPlaceBid, enabled = minimum != null, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.bid_place))
            }
        }
    }
}

@Composable
internal fun VehicleBidDialog(
    state: BidUiState,
    vehicleTitle: String,
    onAmountChanged: (String) -> Unit,
    onReview: (Char) -> Unit,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (state == BidUiState.Closed) return
    val locale = LocalConfiguration.current.locales[0]
    val decimalSeparator = DecimalFormatSymbols.getInstance(locale).decimalSeparator
    val focus = LocalFocusManager.current
    val review = { focus.clearFocus(); onReview(decimalSeparator) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    when (state) {
                        is BidUiState.Editing -> R.string.bid_place
                        is BidUiState.Success -> R.string.bid_success
                        else -> R.string.bid_confirm_title
                    },
                ),
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(vehicleTitle, style = MaterialTheme.typography.titleMedium)
                when (state) {
                    is BidUiState.Editing -> {
                        OutlinedTextField(
                            value = state.amount,
                            onValueChange = onAmountChanged,
                            label = { Text(stringResource(R.string.bid_amount)) },
                            singleLine = true,
                            isError = state.error != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = { review() }),
                            supportingText = {
                                Text(
                                    when (val error = state.error) {
                                        null -> stringResource(R.string.bid_amount_hint, decimalSeparator.toString())
                                        BidError.InvalidAmount -> stringResource(R.string.bid_invalid_amount)
                                        is BidError.TooLow -> stringResource(
                                            R.string.bid_too_low, formatBidMoney(error.minimum, locale),
                                        )
                                        BidError.Unavailable -> stringResource(R.string.bid_unavailable)
                                        BidError.SubmissionFailed -> stringResource(R.string.bid_failed)
                                    },
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    is BidUiState.Confirming -> Text(
                        stringResource(R.string.bid_confirm_amount, formatBidMoney(state.amount, locale)),
                    )
                    is BidUiState.Submitting -> {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.bid_submitting))
                    }
                    is BidUiState.Success -> Text(
                        stringResource(R.string.bid_success_amount, formatBidMoney(state.amount, locale)),
                    )
                    BidUiState.Closed -> Unit
                }
                Text(
                    stringResource(R.string.bid_demo_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            when (state) {
                is BidUiState.Editing -> TextButton(onClick = review) {
                    Text(stringResource(R.string.bid_review))
                }
                is BidUiState.Confirming -> TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.bid_confirm))
                }
                is BidUiState.Success -> TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.bid_done))
                }
                else -> Unit
            }
        },
        dismissButton = {
            when (state) {
                is BidUiState.Editing -> TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.bid_cancel))
                }
                is BidUiState.Confirming -> TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.bid_edit))
                }
                else -> Unit
            }
        },
    )
}

private fun formatBidMoney(money: Money, locale: Locale): String =
    NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance("CAD")
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(BigDecimal.valueOf(money.cents, 2))
