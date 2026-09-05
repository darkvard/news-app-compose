package com.example.news

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.news.domain.manager.LocalUserManger
import com.example.news.presentation.onboarding.OnboardingScreen
import com.example.news.ui.theme.NewsTheme
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var localUserManager: LocalUserManger

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NewsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OnboardingScreen()
                }
            }
        }

        testInitHilt()
    }


    private fun testInitHilt() {
        lifecycleScope.launch {
            localUserManager.readAppEntry().collect {
                Log.d("check_hilt", "testInitHilt: $it")
            }
        }
    }


}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    NewsTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            OnboardingScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
