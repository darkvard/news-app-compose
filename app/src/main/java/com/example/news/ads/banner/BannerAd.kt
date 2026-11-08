package com.example.news.ads.banner

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.news.ads.ADS_LOG_TAG
import com.example.news.ads.AdUnits
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

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
