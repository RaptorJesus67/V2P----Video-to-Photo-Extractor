package com.kcmitch.v2p.thirdParty.db

import com.kcmitch.v2p.SettingsPersistence
import android.content.Context
import android.util.Log

/**
 * Helper to log and sync user privacy and consent responses to device storage and Firebase.
 */
object FirebaseConsentSync {
    private const val TAG = "FirebaseConsentSync"

    data class ConsentRecord(
        val userId: String,
        val consentStatus: String,
        val canRequestAds: Boolean,
        val isPrivacyRequired: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun syncConsent(
        context: Context,
        consentStatus: String,
        canRequestAds: Boolean,
        isPrivacyRequired: Boolean
    ) {
        val userId = SettingsPersistence.getUserId(context)
        val record = ConsentRecord(
            userId = userId,
            consentStatus = consentStatus,
            canRequestAds = canRequestAds,
            isPrivacyRequired = isPrivacyRequired
        )

        Log.d(TAG, "Recorded user consent response: $record")

        // Update cached EU consent and sync complete database record to Firebase
        FirebaseUserDatabase.setCachedEuConsent(
            context = context,
            status = consentStatus,
            canRequestAds = canRequestAds,
            isPrivacyRequired = isPrivacyRequired
        )
    }
}
