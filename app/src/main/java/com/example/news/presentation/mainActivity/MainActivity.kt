package com.example.news.presentation.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.news.ads.AdsManager
import com.example.news.ads.AdsProvider
import com.example.news.presentation.navgraph.Destination
import com.example.news.presentation.navgraph.NavGraph
import com.example.news.ui.theme.NewsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var adsManager: AdsManager

    /** Splash stays until both the start destination is known AND the launch ads flow is done. */
    @Volatile
    private var isAdsStartupDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.value.isLoading || !isAdsStartupDone
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startAds(isFreshLaunch = savedInstanceState == null)

        setContent {
            NewsTheme {
                AdsProvider(adsManager) {
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        if (!state.isLoading) NavGraph(startDestination = state.startDestination)
                    }
                }
            }
        }
    }

    /** Consent → SDK init → App Open, while the splash is on screen (see AdsManager.startOnLaunch). */
    private fun startAds(isFreshLaunch: Boolean) {
        lifecycleScope.launch {
            val state = viewModel.state.first { !it.isLoading }
            adsManager.startOnLaunch(
                activity = this@MainActivity,
                isUserOnboarded = state.startDestination == Destination.NewsGraph,
                // No App Open on rotation / restore, only when the user really opens the app
                showAppOpenAd = isFreshLaunch
            )
            isAdsStartupDone = true
        }
        // Finishing onboarding during this session enables App Open on the next return
        viewModel.state
            .map { it.startDestination == Destination.NewsGraph }
            .onEach(adsManager::setUserOnboarded)
            .launchIn(lifecycleScope)
    }
}
