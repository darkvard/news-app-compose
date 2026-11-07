package com.example.news

import android.app.Application
import com.example.news.ads.fullscreen.AppOpenAdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NewsApplication : Application() {

    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        // App Open must watch the whole app's foreground/background changes from the start
        appOpenAdManager.register(this)
    }
}
