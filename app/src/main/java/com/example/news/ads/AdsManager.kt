package com.example.news.ads

import android.app.Activity
import com.example.news.ads.consent.ConsentManager
import com.example.news.ads.fullscreen.AppOpenAdManager
import com.example.news.ads.fullscreen.InterstitialAdManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade the rest of the app talks to (via [LocalAdsManager]); the individual managers stay
 * internal details.
 */
@Singleton
class AdsManager @Inject constructor(
    private val consentManager: ConsentManager,
    private val adsInitializer: AdsInitializer,
    private val appOpenAdManager: AppOpenAdManager,
    private val interstitialAdManager: InterstitialAdManager
) {
    /**
     * Serializes [startOnLaunch]. If the activity is recreated (e.g. rotated) while the consent
     * form is open, the old activity's coroutine is cancelled mid-way: the lock is then released
     * and the new activity runs the sequence again (every step is safe to repeat).
     */
    private val startupLock = Mutex()
    private var isStartupDone = false

    /** True once consent allows ads and the SDK is initialized. */
    val canShowAds: StateFlow<Boolean> get() = adsInitializer.isReady

    val isPrivacyOptionsRequired: StateFlow<Boolean> get() = consentManager.isPrivacyOptionsRequired

    /**
     * App launch sequence, run while the splash screen is visible:
     * consent → SDK init → preload → App Open (cold start, with timeout).
     * Returns when the app content may be shown. Completes once per process.
     */
    suspend fun startOnLaunch(activity: Activity, isUserOnboarded: Boolean, showAppOpenAd: Boolean) {
        appOpenAdManager.setUserOnboarded(isUserOnboarded)
        startupLock.withLock {
            if (isStartupDone) return
            runStartup(activity, showAppOpenAd)
            isStartupDone = true
        }
    }

    private suspend fun runStartup(activity: Activity, showAppOpenAd: Boolean) {
        // 1. Consent first: nothing may be requested before the user answered (if they must)
        if (!consentManager.gatherConsent(activity)) {
            appOpenAdManager.markColdStartHandled()
            return
        }
        // 2. Start the SDK (background thread) and wait a little for it
        adsInitializer.initialize()
        withTimeoutOrNull(INIT_TIMEOUT_MILLIS) { adsInitializer.isReady.first { it } }

        // 3. Preload what's needed soon, so it's ready when the user gets there
        interstitialAdManager.preload()

        // 4. App Open on cold start (skipped on the very first launch / restored activity)
        if (showAppOpenAd) {
            appOpenAdManager.showOnColdStart(activity, APP_OPEN_TIMEOUT_MILLIS)
        } else {
            appOpenAdManager.markColdStartHandled()
        }

        // 5. Have an App Open ad ready for the next time the user comes back to the app
        appOpenAdManager.preload()
    }

    fun setUserOnboarded(isOnboarded: Boolean) = appOpenAdManager.setUserOnboarded(isOnboarded)

    /** Call right after navigating to an article; may show an interstitial on top of it. */
    fun onArticleOpened(activity: Activity) = interstitialAdManager.onArticleOpened(activity)

    /** Call before leaving the app on purpose (share, browser) so coming back shows no App Open. */
    fun skipNextAppOpen() = appOpenAdManager.skipNextResume()

    fun showPrivacyOptions(activity: Activity) = consentManager.showPrivacyOptionsForm(activity)

    private companion object {
        const val INIT_TIMEOUT_MILLIS = 3_000L
        const val APP_OPEN_TIMEOUT_MILLIS = 4_000L
    }
}
