package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Production Publisher Details
    const val PROD_APP_ID = "ca-app-pub-8741391110749449~3347511713"
    const val PROD_PUBLISHER_ID = "pub-8741391110749449"

    // Standard AdMob test unit IDs for development / compliance testing
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    var bannerAdUnitId: String = TEST_BANNER_ID
    var interstitialAdUnitId: String = TEST_INTERSTITIAL_ID

    fun setAdUnitIds(bannerId: String, interstitialId: String) {
        if (bannerId.isNotBlank()) bannerAdUnitId = bannerId
        if (interstitialId.isNotBlank()) interstitialAdUnitId = interstitialId
    }

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    fun initialize(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "AdMob MobileAds initialized: $status")
            // Preload the interstitial ad right away
            loadInterstitialAd(context.applicationContext)
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "InterstitialAd loaded successfully.")
                    interstitialAd = ad
                    isAdLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load InterstitialAd: ${error.message}")
                    interstitialAd = null
                    isAdLoading = false
                }
            }
        )
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
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
