package com.example.news.ads

import com.example.news.BuildConfig

/**
 * The one place that knows the ad unit IDs (they come from app/build.gradle.kts per build type:
 * Google test IDs in debug, real IDs from local.properties in release).
 * A blank ID means "this format is turned off".
 */
object AdUnits {
    val appOpen: String = BuildConfig.ADMOB_APP_OPEN_ID
    val banner: String = BuildConfig.ADMOB_BANNER_ID
    val interstitial: String = BuildConfig.ADMOB_INTERSTITIAL_ID
    val native: String = BuildConfig.ADMOB_NATIVE_ID
}

/**
 * Time-based frequency caps. Debug sets them to 0 (BuildConfig.ADS_NO_COOLDOWN) so every ad
 * can be seen right away while learning/recording; release keeps realistic values.
 */
object AdCooldowns {
    private val off = BuildConfig.ADS_NO_COOLDOWN
    val appOpenAfterFullScreenMillis: Long = if (off) 0 else 30_000
    val interstitialMinIntervalMillis: Long = if (off) 0 else 60_000
    val interstitialAfterAppOpenMillis: Long = if (off) 0 else 30_000
}

internal const val ADS_LOG_TAG = "NewsAds"
