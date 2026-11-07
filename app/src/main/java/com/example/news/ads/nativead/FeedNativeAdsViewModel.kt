package com.example.news.ads.nativead

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.news.ads.ADS_LOG_TAG
import com.example.news.ads.AdUnits
import com.example.news.ads.AdsInitializer
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns the native ads of ONE feed screen (Home or Search each get their own instance, scoped
 * to their navigation entry). An infrastructure ViewModel, not a screen one: it exists because
 * native ads must live exactly as long as the screen showing them, which is what a ViewModel is.
 *
 * - One ad per slot, loaded lazily the first time the slot scrolls into view
 * - Cached: scrolling back up shows the same ad, no reload
 * - Every ad is destroyed when the screen goes away (avoids memory leaks)
 */
@HiltViewModel
class FeedNativeAdsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adsInitializer: AdsInitializer
) : ViewModel() {

    private val slots = HashMap<Int, MutableStateFlow<NativeAd?>>()
    private var isCleared = false

    fun adFor(slot: Int): StateFlow<NativeAd?> =
        slots.getOrPut(slot) { MutableStateFlow<NativeAd?>(null).also(::load) }

    private fun load(target: MutableStateFlow<NativeAd?>) {
        if (AdUnits.native.isBlank()) return
        viewModelScope.launch {
            adsInitializer.isReady.first { it } // no consent -> never ready -> no ads
            AdLoader.Builder(context, AdUnits.native)
                .forNativeAd { ad ->
                    // The screen may be gone by the time the ad arrives
                    if (isCleared) ad.destroy() else target.value = ad
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(ADS_LOG_TAG, "Native ad failed to load: ${error.code} ${error.message}")
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build()
                )
                .build()
                .loadAd(AdRequest.Builder().build())
        }
    }

    override fun onCleared() {
        isCleared = true
        slots.values.forEach { it.value?.destroy() }
        slots.clear()
    }
}
