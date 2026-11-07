package com.example.news.ads

import com.example.news.ads.policy.InterstitialPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialPolicyTest {

    private val clock = FakeClock()
    private val policy = InterstitialPolicy(clock, showEvery = 3, minIntervalMillis = 60_000, appOpenCooldownMillis = 30_000)

    /** Opens [count] articles, returns the decisions; shown ads are reported back to the policy. */
    private fun open(count: Int, lastAppOpen: Long? = null): List<Boolean> = List(count) {
        policy.onArticleOpened(lastAppOpen).also { show -> if (show) policy.onAdShown() }
    }

    @Test
    fun `first two articles never show an ad, the third does`() {
        assertEquals(listOf(false, false, true), open(3))
    }

    @Test
    fun `no second ad within the minimum interval even after enough opens`() {
        open(3) // ad shown
        clock.advanceBy(10_000)
        assertEquals(listOf(false, false, false), open(3))
    }

    @Test
    fun `after the interval, the next eligible open shows an ad again`() {
        open(3)
        clock.advanceBy(61_000)
        assertEquals(listOf(false, false, true), open(3))
    }

    @Test
    fun `no interstitial right after an app open ad`() {
        open(2)
        val appOpenShownAt = clock.now - 5_000
        assertFalse(policy.onArticleOpened(lastAppOpenShownAtMillis = appOpenShownAt))
    }

    @Test
    fun `an eligible open without a shown ad keeps the next open eligible`() {
        open(2)
        assertTrue(policy.onArticleOpened(null)) // eligible, but e.g. the ad wasn't loaded: not shown
        assertTrue(policy.onArticleOpened(null)) // so the very next open may try again
    }
}
