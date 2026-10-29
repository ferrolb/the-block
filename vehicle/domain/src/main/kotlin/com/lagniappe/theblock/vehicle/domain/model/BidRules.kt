package com.lagniappe.theblock.vehicle.domain.model

object BidRules {
    /**
     * First bids must meet the starting price; subsequent bids increase by CAD $100
     * and must also meet the starting price. Reserve and buy-now prices do not gate bidding.
     * Returns null if another bid would overflow the price or count representation.
     */
    fun minimumBid(auction: VehicleAuction): Money? {
        val currentCents = auction.currentBid?.cents
        if (auction.bidCount == Int.MAX_VALUE ||
            (currentCents != null && currentCents > Long.MAX_VALUE - INCREMENT_CENTS)
        ) {
            return null
        }
        return Money(
            maxOf(auction.startingBid.cents, currentCents?.plus(INCREMENT_CENTS) ?: 0L),
        )
    }

    private const val INCREMENT_CENTS = 10_000L
}
