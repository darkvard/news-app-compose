package com.example.news.ads

import com.example.news.ads.policy.NativeAdPlacement
import org.junit.Assert.assertEquals
import org.junit.Test

class NativeAdPlacementTest {

    private val placement = NativeAdPlacement(firstAfter = 4, interval = 8)

    @Test
    fun `ads go after items 4, 12, 20 (1-based)`() {
        val adIndices = (0 until 25).filter(placement::isAdAfter)
        assertEquals(listOf(3, 11, 19), adIndices)
    }

    @Test
    fun `each ad position gets its own slot number`() {
        assertEquals(listOf(0, 1, 2), listOf(3, 11, 19).map(placement::slotAfter))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid configuration is rejected`() {
        NativeAdPlacement(firstAfter = 0, interval = 8)
    }
}
