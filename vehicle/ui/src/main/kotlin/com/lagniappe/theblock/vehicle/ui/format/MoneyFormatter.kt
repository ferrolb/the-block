package com.lagniappe.theblock.vehicle.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.lagniappe.theblock.vehicle.domain.model.Money
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** One locale-aware CAD currency marker, without an additional currency suffix. */
internal class MoneyFormatter(locale: Locale) {
    private val formatter = NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance("CAD")
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    fun format(money: Money): String = formatter.format(BigDecimal.valueOf(money.cents, 2))
}

@Composable
internal fun rememberMoneyFormatter(): MoneyFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { MoneyFormatter(locale) }
}
