package com.example.news.ads.policy

/**
 * When an App Open ad may appear as the user comes back to the app:
 * - not before the user finished onboarding (first impression matters more than one ad)
 * - not while another full-screen ad is on screen (e.g. the user clicked an interstitial,
 *   visited the advertiser and is coming back: that also triggers "app returned to foreground")
 * - not right after the user deliberately left the app (share sheet, browser) from inside it
 * - not within [cooldownMillis] after a full-screen ad was closed
 *
 * A loaded App Open ad expires after [adExpiryMillis] (Google: 4 hours), then it must be reloaded.
 */
class AppOpenPolicy(
    private val clock: Clock,
    private val cooldownMillis: Long = 30_000,
    private val adExpiryMillis: Long = 4 * 60 * 60 * 1000L
) {
    fun canShowOnResume(
        isUserOnboarded: Boolean,
        isFullScreenAdShowing: Boolean,
        isReturningFromExternalAction: Boolean,
        lastFullScreenDismissedAtMillis: Long?
    ): Boolean {
        val cooledDown = lastFullScreenDismissedAtMillis == null ||
            clock.nowMillis() - lastFullScreenDismissedAtMillis >= cooldownMillis
        return isUserOnboarded && !isFullScreenAdShowing && !isReturningFromExternalAction && cooledDown
    }

    fun isExpired(loadedAtMillis: Long): Boolean = clock.nowMillis() - loadedAtMillis >= adExpiryMillis
}
