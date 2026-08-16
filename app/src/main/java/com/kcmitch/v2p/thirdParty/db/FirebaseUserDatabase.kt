package com.kcmitch.v2p.thirdParty.db

import com.kcmitch.v2p.config.AppConfig
import com.kcmitch.v2p.SettingsPersistence
import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages syncing and storing User Data with Firebase Firestore.
 * 
 * Tracks:
 * - User ID (format: ^[0-9]{4}-[a-zA-Z0-9]{8}-[0-9]{2}-[a-zA-Z0-9]{8}-[0-9]{3}$)
 * - Account Tier Status (Ad-tier Free vs Pro-tier)
 * - Privacy Opt-In / Personalization Preferences
 * - DNS (Do Not Sell) My Data Policy (CCPA / US State privacy)
 * - EU / GDPR Consent Status
 * - Ad Availability & Request Capability
 * - App Version, Platform, and Timestamps
 */
object FirebaseUserDatabase {
    private const val TAG = "FirebaseUserDatabase"
    private const val COLLECTION_USERS = "users"
    private const val PREFS_NAME = "v2p_firebase_user_db"

    data class UserProfile(
        val userId: String,
        val tierStatus: String,
        val isAdFree: Boolean,
        val isAccountLinked: Boolean,
        val linkedAccountEmail: String,
        val privacyOptIn: Boolean,
        val dnsMyDataPolicy: String,
        val euConsent: String,
        val canRequestAds: Boolean,
        val isPrivacyRequired: Boolean,
        val appVersion: String = AppConfig.versionNumber,
        val platform: String = "Android",
        val lastActiveTimestamp: Long = System.currentTimeMillis()
    ) {
        fun toMap(): Map<String, Any> {
            return mapOf(
                "userId" to userId,
                "tierStatus" to tierStatus,
                "isAdFree" to isAdFree,
                "isAccountLinked" to isAccountLinked,
                "linkedAccountEmail" to linkedAccountEmail,
                "privacyOptIn" to privacyOptIn,
                "dnsMyDataPolicy" to dnsMyDataPolicy,
                "euConsent" to euConsent,
                "canRequestAds" to canRequestAds,
                "isPrivacyRequired" to isPrivacyRequired,
                "appVersion" to appVersion,
                "platform" to platform,
                "lastActiveTimestamp" to lastActiveTimestamp,
                "updatedAt" to System.currentTimeMillis()
            )
        }
    }

    /**
     * Get or set DNS (Do Not Sell) opt-out state locally
     */
    fun getDnsOptOut(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("dns_opt_out", false)
    }

    fun setDnsOptOut(context: Context, optOut: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dns_opt_out", optOut).apply()
        syncUserData(context)
    }

    /**
     * Get or set Privacy Personalization Opt-In state locally
     */
    fun getPrivacyOptIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("privacy_opt_in", true)
    }

    fun setPrivacyOptIn(context: Context, optIn: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("privacy_opt_in", optIn).apply()
        syncUserData(context)
    }

    /**
     * Get or set cached EU Consent status
     */
    fun getCachedEuConsent(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("eu_consent_status", "UNKNOWN") ?: "UNKNOWN"
    }

    fun setCachedEuConsent(context: Context, status: String, canRequestAds: Boolean, isPrivacyRequired: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("eu_consent_status", status)
            .putBoolean("can_request_ads", canRequestAds)
            .putBoolean("is_privacy_required", isPrivacyRequired)
            .apply()
        syncUserData(context)
    }

    /**
     * Compiles the full user data profile from local storage and syncs it to Firebase Firestore.
     */
    fun syncUserData(
        context: Context,
        additionalData: Map<String, Any>? = null
    ) {
        val userId = SettingsPersistence.getUserId(context)
        val isAdFree = SettingsPersistence.isAdFree(context)
        val isLinked = SettingsPersistence.isAccountLinked(context)
        val linkedEmail = SettingsPersistence.getLinkedAccountEmail(context)

        val tierStatus = when {
            isLinked -> "Pro-tier (Linked Account: $linkedEmail)"
            isAdFree -> "Pro-tier (Ad-Free)"
            else -> "Ad-tier (Standard Free)"
        }

        val dnsOptOut = getDnsOptOut(context)
        val dnsPolicyString = if (dnsOptOut) "DO_NOT_SELL_OPT_OUT" else "OPT_IN"
        val privacyOptIn = getPrivacyOptIn(context)
        val euConsent = getCachedEuConsent(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val canRequestAds = prefs.getBoolean("can_request_ads", !isAdFree)
        val isPrivacyRequired = prefs.getBoolean("is_privacy_required", false)

        val profile = UserProfile(
            userId = userId,
            tierStatus = tierStatus,
            isAdFree = isAdFree,
            isAccountLinked = isLinked,
            linkedAccountEmail = linkedEmail,
            privacyOptIn = privacyOptIn,
            dnsMyDataPolicy = dnsPolicyString,
            euConsent = euConsent,
            canRequestAds = canRequestAds,
            isPrivacyRequired = isPrivacyRequired,
            appVersion = AppConfig.versionNumber,
            platform = "Android"
        )

        val payload = profile.toMap().toMutableMap()
        if (additionalData != null) {
            payload.putAll(additionalData)
        }

        Log.d(TAG, "Syncing User Profile to Firebase Database: $payload")

        // Async dispatch to Firebase Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val gmsCheck = try {
                    GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                } catch (_: Throwable) {
                    ConnectionResult.SERVICE_MISSING
                }
                if (gmsCheck != ConnectionResult.SUCCESS) {
                    Log.d(TAG, "Google Play Services unavailable on this device ($gmsCheck); user record cached locally.")
                    return@launch
                }

                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection(COLLECTION_USERS)
                        .document(userId)
                        .set(payload, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.i(TAG, "Successfully synced user record to Firebase Firestore for $userId")
                        }
                        .addOnFailureListener { error ->
                            Log.w(TAG, "Failed to sync user record to Firebase Firestore: ${error.message}")
                        }
                } else {
                    Log.d(TAG, "FirebaseApp initialization pending; user record cached locally for $userId")
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Firebase Firestore sync exception (safe fallback): ${e.message}")
            }
        }
    }
}
