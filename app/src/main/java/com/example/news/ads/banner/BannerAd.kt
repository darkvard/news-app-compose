package com.example.news.ads.banner

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.news.ads.ADS_LOG_TAG
import com.example.news.ads.AdUnits
import com.example.news.ui.adaptive.LANDSCAPE_PHONE_PREVIEW
import com.example.news.ui.theme.NewsTheme
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/** Approximate banner heights, only used to draw the placeholder in Compose previews. */
private object BannerAdDefaults {
    val PreviewHeight = 64.dp
    val PreviewCompactHeight = 50.dp
}

/**
 * Anchored adaptive banner: full screen width, height chosen by Google for that width.
 * The caller decides *whether* to show it (consent / ads ready); this handles the AdView lifecycle:
 * resume/pause with the screen, destroy when it leaves composition.
 * Nothing is drawn until the banner has loaded, so a missing ad leaves no empty space.
 *
 * [isCompact]: short window (phone in landscape) -> the standard anchored size, which is much
 * shorter than the "large" one used otherwise.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier, isCompact: Boolean = false) {
    if (AdUnits.banner.isBlank()) return
    // The Ads SDK can't run inside Android Studio's preview: draw a same-size placeholder instead
    if (LocalInspectionMode.current) {
        BannerAdPlaceholder(isCompact, modifier)
        return
    }
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt()
        var isLoaded by remember { mutableStateOf(false) }

        val adView = remember(widthDp, isCompact) {
            AdView(context).apply {
                setAdSize(
                    if (isCompact) {
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
                    } else {
                        AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
                    }
                )
                adUnitId = AdUnits.banner
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        isLoaded = true
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(ADS_LOG_TAG, "Banner failed to load: ${error.code} ${error.message}")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }

        // Pause refresh/animations when the screen isn't visible, resume when it is
        LifecycleResumeEffect(adView) {
            adView.resume()
            onPauseOrDispose { adView.pause() }
        }
        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }

        if (isLoaded) {
            AndroidView(factory = { adView }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun BannerAdPlaceholder(isCompact: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isCompact) BannerAdDefaults.PreviewCompactHeight else BannerAdDefaults.PreviewHeight)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Banner ad (preview)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "portrait", showBackground = true)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BannerAdPreview() {
    NewsTheme { BannerAd() }
}

@Preview(name = "landscape (compact)", device = LANDSCAPE_PHONE_PREVIEW, showBackground = true)
@Composable
private fun BannerAdCompactPreview() {
    NewsTheme { BannerAd(isCompact = true) }
}
