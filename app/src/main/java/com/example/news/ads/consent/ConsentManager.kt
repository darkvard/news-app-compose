package com.example.news.ads.consent

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.news.BuildConfig
import com.example.news.ads.ADS_LOG_TAG
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * GDPR / US-states privacy consent with Google's UMP SDK.
 * Rule: **no ad may be requested before consent is gathered** ([canRequestAds]).
 * Users outside regulated regions get no form at all and can request ads right away.
 */
@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /** Also true when consent was given in a previous session (so ads can start immediately). */
    val canRequestAds: Boolean get() = consentInformation.canRequestAds()

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)

    /** True for users in regulated regions: the app must offer a way to change their choice. */
    val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    /**
     * Refreshes the consent status and shows the consent form if this user needs one.
     * Never throws: on error we fall back to whatever consent we already have.
     * @return whether ads can be requested.
     */
    suspend fun gatherConsent(activity: Activity): Boolean = suspendCancellableCoroutine { continuation ->
        fun finish() {
            updatePrivacyOptionsRequirement()
            if (continuation.isActive) continuation.resume(canRequestAds)
        }

        consentInformation.requestConsentInfoUpdate(
            activity,
            consentRequestParameters(activity),
            {
                // Shows the form only if required (e.g. EEA user without a stored choice)
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let { Log.w(ADS_LOG_TAG, "Consent form error: ${it.errorCode} ${it.message}") }
                    finish()
                }
            },
            { requestError ->
                Log.w(ADS_LOG_TAG, "Consent info update failed: ${requestError.errorCode} ${requestError.message}")
                finish()
            }
        )
    }

    /** Re-opens the consent form so the user can change their choice (required by GDPR). */
    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            formError?.let { Log.w(ADS_LOG_TAG, "Privacy options form error: ${it.message}") }
        }
    }

    private fun updatePrivacyOptionsRequirement() {
        _isPrivacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    private fun consentRequestParameters(activity: Activity): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
        if (BuildConfig.ADS_DEBUG_CONSENT_EEA) {
            // Debug only: behave as if the device were in the EEA so the form is testable here
            builder.setConsentDebugSettings(
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .setForceTesting(true)
                    .build()
            )
        }
        return builder.build()
    }
}
