package com.example.news.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Gives composables access to ads without passing [AdsManager] through every screen.
 * Only Routes / the navigator read it: stateless Screens get plain lambdas and slots,
 * so they stay previewable without any ads setup.
 */
val LocalAdsManager = staticCompositionLocalOf<AdsManager> {
    error("No AdsManager provided: wrap the UI in AdsProvider")
}

@Composable
fun AdsProvider(adsManager: AdsManager, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAdsManager provides adsManager, content = content)
}
