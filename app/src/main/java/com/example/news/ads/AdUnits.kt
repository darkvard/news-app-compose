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

internal const val ADS_LOG_TAG = "NewsAds"
