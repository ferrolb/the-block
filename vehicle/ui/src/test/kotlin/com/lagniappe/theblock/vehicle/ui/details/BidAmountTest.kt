package com.lagniappe.theblock.vehicle.ui.details

import com.lagniappe.theblock.vehicle.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BidAmountTest {
    @Test
    fun `decimal amounts become exact cents without rounding`() {
        assertEquals(Money(2_110_025), parseBidAmount("21100.25", '.'))
        assertEquals(Money(10), parseBidAmount("0.1", '.'))
        assertEquals(Money(2_110_000), parseBidAmount(" 21100 ", '.'))
        assertEquals(Money(Long.MAX_VALUE), parseBidAmount("92233720368547758.07", '.'))
    }

    @Test
    fun `local decimal comma and canonical dot both work`() {
        assertEquals(Money(2_110_025), parseBidAmount("21100,25", ','))
        assertEquals(Money(2_110_025), parseBidAmount("21100.25", ','))
        assertNull(parseBidAmount("21.100,25", ','))
        assertNull(parseBidAmount("21,100.25", '.'))
        assertNull(parseBidAmount("1,000", ','))
    }

    @Test
    fun `invalid nonpositive overprecise and overflowing amounts are rejected`() {
        for (input in listOf("", " ", "0", "-1", "+1", "1e3", "NaN", "12.345", "$100", "1 000", "92233720368547758.08")) {
            assertNull(input, parseBidAmount(input, '.'))
        }
    }
}
