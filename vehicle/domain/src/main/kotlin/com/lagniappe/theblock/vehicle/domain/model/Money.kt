package com.lagniappe.theblock.vehicle.domain.model

/** CAD in cents. Floating-point values are deliberately excluded from bid calculations. */
data class Money(val cents: Long) : Comparable<Money> {
    init {
        require(cents >= 0) { "Money cannot be negative" }
    }

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)
}
