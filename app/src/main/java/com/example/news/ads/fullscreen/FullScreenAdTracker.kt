package com.example.news.ads.fullscreen

import com.example.news.ads.policy.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared state between the full-screen formats (App Open + Interstitial) so they never
 * stack on top of each other or appear back to back. Main thread only.
 */
@Singleton
class FullScreenAdTracker @Inject constructor() {
    private val clock = Clock.System

    var isShowing: Boolean = false
        private set
    var lastDismissedAtMillis: Long? = null
        private set
    var lastAppOpenShownAtMillis: Long? = null
        private set

    fun onShown(isAppOpen: Boolean) {
        isShowing = true
        if (isAppOpen) lastAppOpenShownAtMillis = clock.nowMillis()
    }

    fun onDismissed() {
        isShowing = false
        lastDismissedAtMillis = clock.nowMillis()
    }
}
