package com.example.news.ads

import com.example.news.ads.policy.AppOpenPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppOpenPolicyTest {

    private val clock = FakeClock()
    private val policy = AppOpenPolicy(clock, cooldownMillis = 30_000, adExpiryMillis = 4 * 3_600_000L)

    private fun canShow(
        onboarded: Boolean = true,
        showing: Boolean = false,
        external: Boolean = false,
        lastDismissed: Long? = null
    ) = policy.canShowOnResume(onboarded, showing, external, lastDismissed)

    @Test
    fun `shows when nothing prevents it`() = assertTrue(canShow())

    @Test
    fun `not before onboarding is done`() = assertFalse(canShow(onboarded = false))

    @Test
    fun `not while another full screen ad is showing`() = assertFalse(canShow(showing = true))

    @Test
    fun `not when coming back from share or browser`() = assertFalse(canShow(external = true))

    @Test
    fun `cooldown after a full screen ad was closed`() {
        assertFalse(canShow(lastDismissed = clock.now - 10_000))
        assertTrue(canShow(lastDismissed = clock.now - 31_000))
    }

    @Test
    fun `loaded ads expire after four hours`() {
        val loadedAt = clock.now
        clock.advanceBy(3 * 3_600_000L)
        assertFalse(policy.isExpired(loadedAt))
        clock.advanceBy(3_600_000L)
        assertTrue(policy.isExpired(loadedAt))
    }
}
