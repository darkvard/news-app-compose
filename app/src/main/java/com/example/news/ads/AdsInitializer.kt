package com.example.news.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Starts the Mobile Ads SDK exactly once, on a background thread (it does disk/network work
 * and would otherwise risk an ANR). Must only be called once consent allows requesting ads.
 */
@Singleton
class AdsInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isReady = MutableStateFlow(false)

    /** Every ad format waits for this before loading. False forever if consent was refused. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    fun initialize() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            MobileAds.initialize(context) { status ->
                Log.d(ADS_LOG_TAG, "Mobile Ads initialized: ${status.adapterStatusMap.keys}")
                _isReady.value = true
            }
        }
    }
}
