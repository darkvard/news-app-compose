package com.example.news.ads.fullscreen

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.news.ads.ADS_LOG_TAG
import com.example.news.ads.AdUnits
import com.example.news.ads.AdsInitializer
import com.example.news.ads.policy.Clock
import com.example.news.ads.policy.InterstitialPolicy
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interstitial = full-screen ad at a natural break (here: opening an article).
 * Lifecycle: preload → (policy says yes) show → dismissed → preload the next one.
 * We **never make the user wait** for an ad to load: no ad ready = no ad this time.
 */
@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adsInitializer: AdsInitializer,
    private val tracker: FullScreenAdTracker
) {
    private val policy = InterstitialPolicy(Clock.System)
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun preload() {
        if (!adsInitializer.isReady.value || interstitialAd != null || isLoading) return
        if (AdUnits.interstitial.isBlank()) return

        isLoading = true
        InterstitialAd.load(
            context,
            AdUnits.interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    Log.w(ADS_LOG_TAG, "Interstitial failed to load: ${error.code} ${error.message}")
                }
            }
        )
    }

    /**
     * Call when the user opens an article (after starting the navigation, so the article is
     * already behind the ad when it closes). Shows the ad only if the policy allows it.
     */
    fun onArticleOpened(activity: Activity) {
        val shouldShow = policy.onArticleOpened(lastAppOpenShownAtMillis = tracker.lastAppOpenShownAtMillis)
        val ad = interstitialAd
        if (!shouldShow || ad == null || tracker.isShowing) {
            preload()
            return
        }

        interstitialAd = null // an ad object can only be shown once
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                tracker.onShown(isAppOpen = false)
                policy.onAdShown()
            }

            override fun onAdDismissedFullScreenContent() {
                tracker.onDismissed()
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(ADS_LOG_TAG, "Interstitial failed to show: ${error.message}")
                tracker.onDismissed()
                preload()
            }
        }
        ad.show(activity)
    }
}
