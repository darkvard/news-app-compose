import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

/** Google's official sample IDs: always fill, safe to click. https://developers.google.com/admob/android/test-ads */
object AdMobTestIds {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
}

android {
    namespace = "com.example.news"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Install ID (must not start with com.example: Google ads servers and Play Store reject it).
        // The Kotlin package (namespace) stays com.example.news.
        applicationId = "io.github.darkvard.news"
        minSdk = 35
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // NewsAPI key is read from local.properties (NEWS_API_KEY=...) so it never lands in git
        buildConfigField(
            "String",
            "NEWS_API_KEY",
            "\"${localProperties.getProperty("NEWS_API_KEY", "")}\""
        )
    }

    buildTypes {
        // Ads: debug ALWAYS uses Google's test IDs (never click real ads on your own app!).
        // Release reads real IDs from local.properties (ADMOB_*); empty unit IDs = that format is off.
        debug {
            manifestPlaceholders["admobAppId"] = AdMobTestIds.APP_ID
            buildConfigField("String", "ADMOB_APP_OPEN_ID", "\"${AdMobTestIds.APP_OPEN}\"")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${AdMobTestIds.BANNER}\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${AdMobTestIds.INTERSTITIAL}\"")
            buildConfigField("String", "ADMOB_NATIVE_ID", "\"${AdMobTestIds.NATIVE}\"")
            // Pretend the device is in the EEA so the GDPR consent form shows while developing
            buildConfigField("boolean", "ADS_DEBUG_CONSENT_EEA", "true")
            // Learning/demo build: no waiting between full-screen ads (release keeps the real caps)
            buildConfigField("boolean", "ADS_NO_COOLDOWN", "true")
        }
        release {
            optimization {
                enable = false
            }
            // The SDK crashes at startup without an App ID, so fall back to the test one
            // (with empty unit IDs no ad is ever requested).
            manifestPlaceholders["admobAppId"] = localProperties.getProperty("ADMOB_APP_ID", AdMobTestIds.APP_ID)
            buildConfigField("String", "ADMOB_APP_OPEN_ID", "\"${localProperties.getProperty("ADMOB_APP_OPEN_ID", "")}\"")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${localProperties.getProperty("ADMOB_BANNER_ID", "")}\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${localProperties.getProperty("ADMOB_INTERSTITIAL_ID", "")}\"")
            buildConfigField("String", "ADMOB_NATIVE_ID", "\"${localProperties.getProperty("ADMOB_NATIVE_ID", "")}\"")
            buildConfigField("boolean", "ADS_DEBUG_CONSENT_EEA", "false")
            buildConfigField("boolean", "ADS_NO_COOLDOWN", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)

    // Splash API
    implementation(libs.androidx.core.splashscreen)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coil
    implementation(libs.coil.compose)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // Compose Foundation
    implementation(libs.androidx.compose.foundation)

    // Haze (backdrop blur for the floating bottom bar)
    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Ads (AdMob) + consent (UMP)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    // Accompanist
    implementation(libs.accompanist.systemuicontroller)

    // Paging
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.paging.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
