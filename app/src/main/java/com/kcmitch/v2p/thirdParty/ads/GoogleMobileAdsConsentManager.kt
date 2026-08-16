package com.kcmitch.v2p.thirdParty.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.kcmitch.v2p.config.AppConfig
import com.kcmitch.v2p.settings.TestSettings
import com.kcmitch.v2p.SettingsPersistence
import com.kcmitch.v2p.thirdParty.db.FirebaseConsentSync

/**
 * The Google Mobile Ads SDK provides the User Messaging Platform (Google's
 * IAB Certified Consent Management Platform) that helps you comply with privacy laws
 * including GDPR (EEA/UK/Switzerland) and US State privacy regulations (CCPA/CPRA, CPA, VCDPA, etc.).
 *
 * Implemented based on official Google Mobile Ads SDK Kotlin privacy guide:
 * https://developers.google.com/admob/android/privacy#kotlin_5
 * and testing guide:
 * https://developers.google.com/admob/android/privacy#testing
 */
class GoogleMobileAdsConsentManager private constructor(private val appContext: Context) {
    private val consentInformation: ConsentInformation? by lazy {
        try {
            val gmsCheck = try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext)
            } catch (_: Throwable) {
                ConnectionResult.SERVICE_MISSING
            }
            if (gmsCheck == ConnectionResult.SUCCESS) {
                UserMessagingPlatform.getConsentInformation(appContext)
            } else {
                null
            }
        } catch (e: Throwable) {
            Log.w(TAG, "ConsentInformation initialization safe catch: ${e.message}")
            null
        }
    }

    /** Interface definition for a callback to be invoked when consent gathering completes. */
    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    /** Helper variable to determine if the app can request ads. */
    val canRequestAds: Boolean
        get() = try {
            consentInformation?.canRequestAds() ?: true
        } catch (_: Throwable) {
            true
        }

    /** Helper variable to determine if the privacy options form is required. */
    val isPrivacyOptionsRequired: Boolean
        get() = try {
            consentInformation?.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } catch (_: Throwable) {
            false
        }

    /**
     * Determines whether GDPR/EEA regulations apply to this user or if US State Privacy (CCPA/CPRA) / Global applies.
     */
    fun isEeaOrGdpr(context: Context): Boolean {
        // If in test mode, check configured debug geography
        if (TestSettings.isTestMode) {
            return TestSettings.debugGeography == ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
        }

        val info = consentInformation
        // If consent is specifically REQUIRED or OBTAINED under IAB TCF framework
        if (info != null && (info.consentStatus == ConsentInformation.ConsentStatus.REQUIRED ||
            info.consentStatus == ConsentInformation.ConsentStatus.OBTAINED)) {
            return true
        }

        // Check device locale / country code against EEA, UK, Switzerland
        return try {
            val config = context.resources.configuration
            val countryCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                config.locales.get(0)?.country?.uppercase()
            } else {
                @Suppress("DEPRECATION")
                config.locale?.country?.uppercase()
            } ?: java.util.Locale.getDefault().country.uppercase()

            EEA_COUNTRY_CODES.contains(countryCode)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Resets consent state on device for testing purposes.
     */
    fun resetConsent() {
        try {
            consentInformation?.reset()
        } catch (e: Throwable) {
            Log.w(TAG, "resetConsent exception: ${e.message}")
        }
    }

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/show a
     * consent form if necessary (for EU GDPR or US state regulations).
     *
     * Ensures privacy options are presented on first startup (or after app data is erased).
     * Responses are automatically persisted on-device and synced to Firebase.
     */
    fun gatherConsent(
        activity: Activity,
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
    ) {
        try {
            val gmsCheck = try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
            } catch (_: Throwable) {
                ConnectionResult.SERVICE_MISSING
            }
            if (gmsCheck != ConnectionResult.SUCCESS) {
                Log.d(TAG, "Google Play Services unavailable ($gmsCheck); using cached consent preferences.")
                persistConsentResponse(activity)
                onConsentGatheringCompleteListener.consentGatheringComplete(null)
                return
            }

            val info = consentInformation
            if (info == null) {
                persistConsentResponse(activity)
                onConsentGatheringCompleteListener.consentGatheringComplete(null)
                return
            }

            // Reset consent state if configured for testing
            if (TestSettings.isTestMode && TestSettings.resetConsentOnStartup) {
                info.reset()
                Log.d(TAG, "ConsentInformation reset for testing.")
            }

            // Configure ConsentDebugSettings based on TestSettings
            val debugSettingsBuilder = ConsentDebugSettings.Builder(activity)
            if (TestSettings.isTestMode) {
                debugSettingsBuilder.setDebugGeography(TestSettings.debugGeography)
                for (deviceId in TestSettings.getAllTestDeviceHashedIds(activity)) {
                    debugSettingsBuilder.addTestDeviceHashedId(deviceId)
                }
            }
            val debugSettings = debugSettingsBuilder.build()

            val params = ConsentRequestParameters.Builder()
                .setConsentDebugSettings(debugSettings)
                .setTagForUnderAgeOfConsent(false)
                .build()

            // Requesting an update to consent information should be called on every app launch and region change.
            info.requestConsentInfoUpdate(
                activity,
                params,
                {
                    try {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                            // Persist response to device and sync to Firebase
                            persistConsentResponse(activity)
                            onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                        }
                    } catch (e: Throwable) {
                        Log.w(TAG, "loadAndShowConsentFormIfRequired safe catch: ${e.message}")
                        persistConsentResponse(activity)
                        onConsentGatheringCompleteListener.consentGatheringComplete(null)
                    }
                },
                { requestConsentError ->
                    persistConsentResponse(activity)
                    onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
                }
            )
        } catch (e: Throwable) {
            Log.w(TAG, "gatherConsent safe catch: ${e.message}")
            persistConsentResponse(activity)
            onConsentGatheringCompleteListener.consentGatheringComplete(null)
        }
    }

    /**
     * Helper method to present the privacy options form (revocation/settings) using Google UMP / GMA Next-Gen SDK.
     *
     * If the SDK has not yet loaded the response or status is NOT_REQUIRED/UNKNOWN in the current test configuration,
     * it proactively requests an update or loads the consent form directly.
     */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener
    ) {
        try {
            val gmsCheck = try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
            } catch (_: Throwable) {
                ConnectionResult.SERVICE_MISSING
            }
            if (gmsCheck != ConnectionResult.SUCCESS) {
                Log.d(TAG, "Google Play Services unavailable ($gmsCheck); skipping privacy form.")
                persistConsentResponse(activity)
                onConsentFormDismissedListener.onConsentFormDismissed(null)
                return
            }

            val info = consentInformation
            if (info == null) {
                persistConsentResponse(activity)
                onConsentFormDismissedListener.onConsentFormDismissed(null)
                return
            }

            // Prepare debug settings in test mode
            val debugSettingsBuilder = ConsentDebugSettings.Builder(activity)
            if (TestSettings.isTestMode) {
                debugSettingsBuilder.setDebugGeography(TestSettings.debugGeography)
                for (deviceId in TestSettings.getAllTestDeviceHashedIds(activity)) {
                    debugSettingsBuilder.addTestDeviceHashedId(deviceId)
                }
            }
            val debugSettings = debugSettingsBuilder.build()
            val params = ConsentRequestParameters.Builder()
                .setConsentDebugSettings(debugSettings)
                .setTagForUnderAgeOfConsent(false)
                .build()

            // First attempt standard showPrivacyOptionsForm
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null) {
                    Log.w(TAG, "showPrivacyOptionsForm returned note: ${formError.errorCode} - ${formError.message}. Attempting refresh/loadConsentForm fallback...")
                    // If form isn't loaded yet ("No valid response received yet" / 3303), refresh consent info and load form directly
                    try {
                        info.requestConsentInfoUpdate(
                            activity,
                            params,
                            {
                                try {
                                    UserMessagingPlatform.loadConsentForm(
                                        activity,
                                        { consentForm ->
                                            try {
                                                consentForm.show(activity) { dismissError ->
                                                    persistConsentResponse(activity)
                                                    onConsentFormDismissedListener.onConsentFormDismissed(dismissError)
                                                }
                                            } catch (e: Throwable) {
                                                persistConsentResponse(activity)
                                                onConsentFormDismissedListener.onConsentFormDismissed(null)
                                            }
                                        },
                                        { loadError ->
                                            // Fallback to loadAndShowConsentFormIfRequired
                                            try {
                                                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { fallbackError ->
                                                    persistConsentResponse(activity)
                                                    onConsentFormDismissedListener.onConsentFormDismissed(fallbackError ?: loadError)
                                                }
                                            } catch (e: Throwable) {
                                                persistConsentResponse(activity)
                                                onConsentFormDismissedListener.onConsentFormDismissed(loadError)
                                            }
                                        }
                                    )
                                } catch (e: Throwable) {
                                    persistConsentResponse(activity)
                                    onConsentFormDismissedListener.onConsentFormDismissed(null)
                                }
                            },
                            { updateError ->
                                persistConsentResponse(activity)
                                onConsentFormDismissedListener.onConsentFormDismissed(updateError)
                            }
                        )
                    } catch (e: Throwable) {
                        persistConsentResponse(activity)
                        onConsentFormDismissedListener.onConsentFormDismissed(null)
                    }
                } else {
                    persistConsentResponse(activity)
                    onConsentFormDismissedListener.onConsentFormDismissed(null)
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "showPrivacyOptionsForm top catch: ${e.message}")
            persistConsentResponse(activity)
            onConsentFormDismissedListener.onConsentFormDismissed(null)
        }
    }

    /** Reset consent information (useful for testing or when user clears data). */
    fun reset() {
        try {
            consentInformation?.reset()
        } catch (e: Throwable) {
            Log.w(TAG, "reset exception: ${e.message}")
        }
    }

    /**
     * Persist consent response locally and sync to Firebase.
     */
    private fun persistConsentResponse(context: Context) {
        try {
            val info = consentInformation
            val statusString = when (info?.consentStatus) {
                ConsentInformation.ConsentStatus.OBTAINED -> "OBTAINED"
                ConsentInformation.ConsentStatus.REQUIRED -> "REQUIRED"
                ConsentInformation.ConsentStatus.NOT_REQUIRED -> "NOT_REQUIRED"
                else -> "UNKNOWN"
            }
            val canRequest = info?.canRequestAds() ?: true
            val privacyRequired = isPrivacyOptionsRequired

            // 1. Save to Device
            SettingsPersistence.saveConsentStatus(
                context = context,
                consentStatus = statusString,
                canRequestAds = canRequest,
                isPrivacyRequired = privacyRequired
            )

            // 2. Sync / Log to Firebase
            FirebaseConsentSync.syncConsent(
                context = context,
                consentStatus = statusString,
                canRequestAds = canRequest,
                isPrivacyRequired = privacyRequired
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist consent response: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "ConsentManager"
        private val EEA_COUNTRY_CODES = setOf(
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR",
            "DE", "GR", "HU", "IS", "IE", "IT", "LV", "LI", "LT", "LU",
            "MT", "NL", "NO", "PL", "PT", "RO", "SK", "SI", "ES", "SE",
            "GB", "UK", "CH"
        )
        @Volatile private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context): GoogleMobileAdsConsentManager =
            instance
                ?: synchronized(this) {
                    instance
                        ?: GoogleMobileAdsConsentManager(context.applicationContext).also {
                            instance = it
                        }
                }
    }
}
