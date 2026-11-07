package com.example.news.ads.nativead

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.news.ads.policy.NativeAdPlacement
import com.example.news.ui.theme.Spacing

/**
 * Drop-in for a feed's `afterItem` slot: draws a native ad after the items chosen by [placement].
 * Nothing at all is drawn until an ad is loaded (no empty gap when there's no ad / no consent).
 */
@Composable
fun FeedNativeAd(
    index: Int,
    placement: NativeAdPlacement,
    viewModel: FeedNativeAdsViewModel,
    modifier: Modifier = Modifier
) {
    if (!placement.isAdAfter(index)) return
    val slot = placement.slotAfter(index)
    val ad by remember(slot) { viewModel.adFor(slot) }.collectAsStateWithLifecycle()
    ad?.let { NativeAdCard(nativeAd = it, modifier = modifier.padding(vertical = Spacing.Small)) }
}
