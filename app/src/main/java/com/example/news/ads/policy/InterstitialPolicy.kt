package com.example.news.ads.policy

/**
 * Frequency capping for the interstitial shown when opening an article.
 * Too many full-screen ads = users leave (and AdMob may limit your ads), so:
 * - never on the first article opens: at most once every [showEvery] opens
 * - at least [minIntervalMillis] between two interstitials
 * - not within [appOpenCooldownMillis] after an App Open ad (two full-screen ads back to back)
 *
 * Pure Kotlin: no Android / AdMob types, fully unit tested.
 */
class InterstitialPolicy(
    private val clock: Clock,
    private val showEvery: Int = 3,
    private val minIntervalMillis: Long = 60_000,
    private val appOpenCooldownMillis: Long = 30_000
) {
    private var opensSinceLastAd = 0
    private var lastShownAtMillis: Long? = null

    /** Call on every article open. Returns true if an interstitial should be shown now. */
    fun onArticleOpened(lastAppOpenShownAtMillis: Long?): Boolean {
        opensSinceLastAd++
        val now = clock.nowMillis()
        return opensSinceLastAd >= showEvery &&
            elapsedSince(lastShownAtMillis, now) >= minIntervalMillis &&
            elapsedSince(lastAppOpenShownAtMillis, now) >= appOpenCooldownMillis
    }

    /** Call when the interstitial actually appeared on screen. */
    fun onAdShown() {
        opensSinceLastAd = 0
        lastShownAtMillis = clock.nowMillis()
    }

    private fun elapsedSince(time: Long?, now: Long) = if (time == null) Long.MAX_VALUE else now - time
}
