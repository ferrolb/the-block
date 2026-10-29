package com.lagniappe.theblock.vehicle.domain.model

import com.lagniappe.theblock.vehicle.domain.vehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BidRulesTest {
    private val auction = vehicle().auction

    @Test
    fun `first bid may equal starting price even below reserve`() {
        assertEquals(auction.startingBid, BidRules.minimumBid(auction))
    }

    @Test
    fun `subsequent bids increase by CAD 100`() {
        val active = auction.copy(currentBid = Money(2_000_000), bidCount = 3)
        assertEquals(Money(2_010_000), BidRules.minimumBid(active))
    }

    @Test
    fun `starting price remains a floor when fixture current bid is lower`() {
        val active = auction.copy(currentBid = Money(100), bidCount = 1)
        assertEquals(auction.startingBid, BidRules.minimumBid(active))
    }

    @Test
    fun `optional reserve and buy now prices do not alter minimum bid`() {
        val withoutReserve = auction.copy(reservePrice = null, buyNowPrice = Money(9_000_000))
        assertEquals(auction.startingBid, BidRules.minimumBid(withoutReserve))
    }

    @Test
    fun `maximum bid count prevents another bid`() {
        assertNull(BidRules.minimumBid(auction.copy(bidCount = Int.MAX_VALUE)))
    }

    @Test
    fun `price overflow prevents another bid`() {
        assertNull(
            BidRules.minimumBid(auction.copy(currentBid = Money(Long.MAX_VALUE), bidCount = 1)),
        )
    }

    @Test
    fun `largest representable next bid is permitted`() {
        assertEquals(
            Money(Long.MAX_VALUE),
            BidRules.minimumBid(
                auction.copy(currentBid = Money(Long.MAX_VALUE - 10_000), bidCount = 1),
            ),
        )
    }
}
