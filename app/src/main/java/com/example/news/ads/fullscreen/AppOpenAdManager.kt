package com.example.news.ads.fullscreen

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.news.ads.ADS_LOG_TAG
import com.example.news.ads.AdUnits
import com.example.news.ads.AdsInitializer
import com.example.news.ads.AdCooldowns
import com.example.news.ads.policy.AppOpenPolicy
import com.example.news.ads.policy.Clock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * App Open = full-screen ad shown when the app is opened or brought back to the foreground.
 *
 * Two entry points:
 * 1. **Cold start**: [showOnColdStart] while the splash screen is still visible (with a timeout,
 *    so a slow network never blocks the launch).
 * 2. **Return from background**: [ProcessLifecycleOwner] calls [onStart] when the whole app
 *    comes back to the foreground; we show the ad only if one is ready and [AppOpenPolicy] agrees.
 *
 * It also tracks the current Activity (via ActivityLifecycleCallbacks) because showing a
 * full-screen ad needs one.
 */
@Singleton
class AppOpenAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adsInitializer: AdsInitializer,
    private val tracker: FullScreenAdTracker
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private val clock = Clock.System
    private val policy = AppOpenPolicy(clock, cooldownMillis = AdCooldowns.appOpenAfterFullScreenMillis)

    private var appOpenAd: AppOpenAd? = null
    private var loadedAtMillis = 0L
    private var isLoading = false
    private val pendingLoadCallbacks = mutableListOf<(AppOpenAd?) -> Unit>()

    private var currentActivity: Activity? = null
    private var isUserOnboarded = false
    private var isColdStartHandled = false
    private var skipNextResume = false

    /** Call once from Application.onCreate. */
    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun setUserOnboarded(isOnboarded: Boolean) {
        isUserOnboarded = isOnboarded
    }

    /** Call right before leaving the app on purpose (share sheet, browser...). */
    fun skipNextResume() {
        skipNextResume = true
    }

    /** Load an ad in the background so the next return to the app can show it instantly. */
    fun preload() = load()

    /** Called when no cold-start ad will be shown (e.g. no consent), so resumes may show ads later. */
    fun markColdStartHandled() {
        isColdStartHandled = true
    }

    /**
     * Cold start: load (or reuse) an ad and show it, giving up after [timeoutMillis].
     * Returns as soon as the ad is on screen (or was skipped), so the caller can drop the splash.
     */
    suspend fun showOnColdStart(activity: Activity, timeoutMillis: Long) {
        try {
            if (!isUserOnboarded || !adsInitializer.isReady.value || AdUnits.appOpen.isBlank()) return
            val ad = withTimeoutOrNull(timeoutMillis) { awaitAd() } ?: return
            suspendCancellableCoroutine { continuation ->
                show(activity, ad, onShownOrFailed = { if (continuation.isActive) continuation.resume(Unit) })
            }
        } finally {
            isColdStartHandled = true
        }
    }

    // region Return from background (ProcessLifecycleOwner)
    override fun onStart(owner: LifecycleOwner) {
        if (!isColdStartHandled) return // the launch itself is handled by showOnColdStart
        val returningFromExternalAction = skipNextResume.also { skipNextResume = false }
        val activity = currentActivity ?: return

        val allowed = policy.canShowOnResume(
            isUserOnboarded = isUserOnboarded,
            isFullScreenAdShowing = tracker.isShowing,
            isReturningFromExternalAction = returningFromExternalAction,
            lastFullScreenDismissedAtMillis = tracker.lastDismissedAtMillis
        )
        val ad = appOpenAd?.takeUnless { policy.isExpired(loadedAtMillis) }
        if (allowed && ad != null) show(activity, ad) else load()
    }
    // endregion

    private fun show(activity: Activity, ad: AppOpenAd, onShownOrFailed: () -> Unit = {}) {
        appOpenAd = null // an ad object can only be shown once
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                tracker.onShown(isAppOpen = true)
                onShownOrFailed()
            }

            override fun onAdDismissedFullScreenContent() {
                tracker.onDismissed()
                load() // ready for the next return
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(ADS_LOG_TAG, "App open failed to show: ${error.message}")
                tracker.onDismissed()
                onShownOrFailed()
                load()
            }
        }
        ad.show(activity)
    }

    private suspend fun awaitAd(): AppOpenAd? = suspendCancellableCoroutine { continuation ->
        load { ad -> if (continuation.isActive) continuation.resume(ad) }
    }

    private fun load(onResult: (AppOpenAd?) -> Unit = {}) {
        val cached = appOpenAd
        if (cached != null && !policy.isExpired(loadedAtMillis)) {
            onResult(cached)
            return
        }
        if (!adsInitializer.isReady.value || AdUnits.appOpen.isBlank()) {
            onResult(null)
            return
        }
        pendingLoadCallbacks += onResult
        if (isLoading) return

        isLoading = true
        AppOpenAd.load(
            context,
            AdUnits.appOpen,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadedAtMillis = clock.nowMillis()
                    finishLoading(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(ADS_LOG_TAG, "App open failed to load: ${error.code} ${error.message}")
                    finishLoading(null)
                }
            }
        )
    }

    private fun finishLoading(ad: AppOpenAd?) {
        isLoading = false
        val callbacks = pendingLoadCallbacks.toList()
        pendingLoadCallbacks.clear()
        callbacks.forEach { it(ad) }
    }

    // region Current activity tracking
    override fun onActivityStarted(activity: Activity) {
        // While a full-screen ad is showing, the "current" activity is the SDK's AdActivity: ignore it
        if (!tracker.isShowing) currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    // endregion
}
