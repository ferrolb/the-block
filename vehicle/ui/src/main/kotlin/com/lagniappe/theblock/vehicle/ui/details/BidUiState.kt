package com.lagniappe.theblock.vehicle.ui.details

import com.lagniappe.theblock.vehicle.domain.model.Money
import java.math.BigDecimal

sealed interface BidUiState {
    data object Closed : BidUiState
    data class Editing(val amount: String, val error: BidError? = null) : BidUiState
    data class Confirming(val amount: Money) : BidUiState
    data class Submitting(val amount: Money) : BidUiState
    data class Success(val amount: Money) : BidUiState
}

sealed interface BidError {
    data object InvalidAmount : BidError
    data class TooLow(val minimum: Money) : BidError
    data object Unavailable : BidError
    data object SubmissionFailed : BidError
}

/** Accepts local decimal notation, without grouping, signs, or currency symbols. */
internal fun parseBidAmount(input: String, decimalSeparator: Char): Money? {
    val text = input.trim().replace(decimalSeparator, '.')
    if (!Regex("[0-9]+(\\.[0-9]{1,2})?").matches(text)) return null
    return try {
        val cents = BigDecimal(text.replace(decimalSeparator, '.')).movePointRight(2).longValueExact()
        if (cents > 0) Money(cents) else null
    } catch (_: ArithmeticException) {
        null
    } catch (_: NumberFormatException) {
        null
    }
}

internal fun Money.toBidInput(): String = BigDecimal.valueOf(cents, 2).toPlainString()
