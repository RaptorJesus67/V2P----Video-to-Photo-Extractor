package com.kcmitch.v2p.thirdParty.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.kcmitch.v2p.OfflineAdCache
import com.kcmitch.v2p.SettingsPersistence
import com.kcmitch.v2p.config.AppConfig
import com.kcmitch.v2p.settings.TestSettings
import com.kcmitch.v2p.ui.theme.*
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private const val TAG = "AdManager"

    // Dynamically resolved via master AppConfig
    val bannerAdUnitId: String
        get() = AppConfig.activeBannerAdUnitId

    val interstitialAdUnitId: String
        get() = AppConfig.activeInterstitialAdUnitId

    val nativeAdUnitId: String
        get() = AppConfig.activeNativeAdUnitId

    private var customBannerAdUnitId: String? = null
    private var customInterstitialAdUnitId: String? = null
    private var customNativeAdUnitId: String? = null

    fun setAdUnitIds(bannerId: String, interstitialId: String, nativeId: String = "") {
        if (bannerId.isNotBlank()) customBannerAdUnitId = bannerId
        if (interstitialId.isNotBlank()) customInterstitialAdUnitId = interstitialId
        if (nativeId.isNotBlank()) customNativeAdUnitId = nativeId
    }

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var consentManager: GoogleMobileAdsConsentManager? = null

    fun initialize(context: Context) {
        try {
            val gmsCheck = try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            } catch (_: Throwable) {
                ConnectionResult.SERVICE_MISSING
            }
            if (gmsCheck != ConnectionResult.SUCCESS) {
                Log.d(TAG, "Google Play Services unavailable on this device ($gmsCheck); skipping AdMob / UMP broker initialization.")
                return
            }

            val googleConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
            consentManager = googleConsentManager

            val activity = findActivity(context)
            if (activity == null) {
                Log.e(TAG, "Cannot initialize consent: context is not or does not wrap an Activity.")
                if (googleConsentManager.canRequestAds) {
                    initializeMobileAds(context)
                }
                return
            }

            // Gather consent for EEA/UK (GDPR) and US state regulations (CCPA, etc.)
            googleConsentManager.gatherConsent(activity) { consentError ->
                if (consentError != null) {
                    Log.w(TAG, "Consent gathering note: ${consentError.errorCode}: ${consentError.message}")
                }

                if (googleConsentManager.canRequestAds) {
                    initializeMobileAds(activity)
                }
            }

            if (googleConsentManager.canRequestAds) {
                initializeMobileAds(activity)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Consent/AdMob initialization handled safely: ${e.message}")
        }
    }

    /**
     * Checks if consent privacy options / revocation form is required for the user (GDPR or US state regulations).
     */
    fun isPrivacyOptionsRequired(context: Context): Boolean {
        return try {
            val manager = consentManager ?: GoogleMobileAdsConsentManager.getInstance(context)
            manager.isPrivacyOptionsRequired
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Determines whether European GDPR applies or US State Privacy (CCPA) / Global applies.
     */
    fun isEeaOrGdpr(context: Context): Boolean {
        return try {
            val manager = consentManager ?: GoogleMobileAdsConsentManager.getInstance(context)
            manager.isEeaOrGdpr(context)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Presents the Google UMP Consent Privacy Options / Revocation form.
     * Allows users (in EEA, UK, Switzerland, and US states) to change or revoke their ad consent preferences at any time.
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: ((com.google.android.ump.FormError?) -> Unit)? = null) {
        try {
            val manager = consentManager ?: GoogleMobileAdsConsentManager.getInstance(activity)
            manager.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null) {
                    Log.w(TAG, "Privacy options form info: ${formError.errorCode}: ${formError.message}")
                } else {
                    Log.d(TAG, "Privacy options form dismissed.")
                }
                onDismissed?.invoke(formError)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Safe fallback for privacy options form: ${e.message}")
            onDismissed?.invoke(null)
        }
    }

    private fun initializeMobileAds(context: Context) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        try {
            val gmsCheck = try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            } catch (_: Throwable) {
                ConnectionResult.SERVICE_MISSING
            }
            if (gmsCheck != ConnectionResult.SUCCESS) {
                Log.d(TAG, "Google Play Services unavailable on this device ($gmsCheck); skipping MobileAds.initialize.")
                return
            }

            if (TestSettings.isTestMode || AppConfig.testMode) {
                val testDeviceIds = TestSettings.getAllTestDeviceHashedIds(context)
                val configuration = com.google.android.gms.ads.RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
                MobileAds.setRequestConfiguration(configuration)
            }
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds initialized: $status")
                loadInterstitialAd(context.applicationContext)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "MobileAds initialization safe catch: ${e.message}")
        }
    }

    fun loadInterstitialAd(context: Context, isRetryWithTestUnit: Boolean = false) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        val unitId = if (isRetryWithTestUnit) AppConfig.TEST_INTERSTITIAL_AD_UNIT_ID else interstitialAdUnitId
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                unitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "InterstitialAd loaded successfully (unit: $unitId).")
                        interstitialAd = ad
                        isAdLoading = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(TAG, "Interstitial ad load info (${unitId}): ${error.message} (code: ${error.code})")
                        interstitialAd = null
                        isAdLoading = false
                        // If live unit fails due to publisher setup or no fill, attempt fallback to test unit once
                        if (!isRetryWithTestUnit && unitId != AppConfig.TEST_INTERSTITIAL_AD_UNIT_ID) {
                            Log.d(TAG, "Attempting fallback to test Interstitial unit ID for continuity...")
                            loadInterstitialAd(context, isRetryWithTestUnit = true)
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            Log.w(TAG, "Interstitial ad load exception: ${e.message}")
            isAdLoading = false
        }
    }

    fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    fun showInterstitialAd(context: Context, onAdClosed: (wasAdShown: Boolean) -> Unit = {}) {
        val activity = findActivity(context)
        if (activity == null) {
            Log.e(TAG, "Cannot show Interstitial ad: context is not or does not wrap an Activity.")
            onAdClosed(false)
            return
        }
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad was dismissed.")
                    interstitialAd = null
                    onAdClosed(true)
                    // Preload the next ad
                    loadInterstitialAd(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Failed to show Interstitial ad: ${error.message}")
                    interstitialAd = null
                    onAdClosed(false)
                    // Preload the next ad
                    loadInterstitialAd(activity.applicationContext)
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not loaded yet, trying to load it.")
            onAdClosed(false)
            loadInterstitialAd(activity.applicationContext)
        }
    }
}

@Composable
fun RealAdBanner(
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isLarge) 100.dp else 50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(if (isLarge) AdSize.LARGE_BANNER else AdSize.BANNER)
                adUnitId = AdManager.bannerAdUnitId
                adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w("AdManager", "Banner load info (${adUnitId}): ${error.message}")
                        if (adUnitId != AppConfig.TEST_BANNER_AD_UNIT_ID) {
                            // Retry with test banner id
                            adUnitId = AppConfig.TEST_BANNER_AD_UNIT_ID
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                }
                try {
                    loadAd(AdRequest.Builder().build())
                } catch (e: Throwable) {
                    Log.w("AdManager", "AdView loadAd catch: ${e.message}")
                }
            }
        }
    )
}

@Composable
fun MockAdBanner(isLarge: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isAdFree = remember { SettingsPersistence.isAdFree(context) }
    
    if (isAdFree) {
        return
    }

    val isOnline = remember { OfflineAdCache.isNetworkAvailable(context) }
    
    if (isOnline) {
        RealAdBanner(modifier = modifier, isLarge = isLarge)
    } else {
        val (cachedTitle, cachedSubtitle) = remember(isLarge) { OfflineAdCache.getCachedBanner(isLarge) }
        val height = if (isLarge) 100.dp else 50.dp
        val width = 320.dp
        
        Box(
            modifier = modifier
                .width(width)
                .height(height)
                .background(Color(0xFF1E2528), RoundedCornerShape(8.dp))
                .border(1.dp, TechCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(WarningAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OFFLINE CACHED AD",
                        style = TextStyle(
                            color = WarningAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = cachedTitle,
                        style = TextStyle(
                            color = TextLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (isLarge) {
                        Text(
                            text = cachedSubtitle,
                            style = TextStyle(
                                color = CoolGrey,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InterstitialAdDialog(
    onAdClosed: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(5) }
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            kotlinx.coroutines.delay(1000)
            timeLeft--
        }
    }
    
    AlertDialog(
        onDismissRequest = { /* Force watch the ad! */ },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(TechCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SPONSOR AD",
                            style = TextStyle(
                                color = TechCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
                Text(
                    text = if (timeLeft > 0) "Close in ${timeLeft}s" else "Close [X]",
                    style = TextStyle(
                        color = if (timeLeft > 0) CoolGrey else TechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable(enabled = timeLeft <= 0) {
                        onAdClosed()
                    }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = TechCyan,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Upgrade to VideoToPics Premium",
                    style = TextStyle(
                        color = TextLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Support development and get unlimited processing, zero ads, and up to 120 FPS high-definition exports!",
                    style = TextStyle(
                        color = CoolGrey,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdClosed() },
                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                enabled = timeLeft <= 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (timeLeft > 0) "Please wait..." else "RESUME TO EXTRACTION",
                    style = TextStyle(color = DarkSlateBg, fontWeight = FontWeight.Bold)
                )
            }
        },
        containerColor = SlateCard,
        textContentColor = TextLight,
        titleContentColor = TextLight
    )
}
