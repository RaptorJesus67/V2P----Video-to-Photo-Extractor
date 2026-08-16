package com.kcmitch.v2p.applets.ads

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.kcmitch.v2p.OfflineAdCache
import com.kcmitch.v2p.SettingsPersistence
import com.kcmitch.v2p.config.AppConfig
import com.kcmitch.v2p.thirdParty.ads.AdManager
import com.kcmitch.v2p.ui.theme.*

private const val TAG = "NativeAdvancedAd"

/**
 * Material 3 / Cyberpunk-styled Native Advanced Ad card.
 *
 * Configured with Ad Unit ID: ca-app-pub-8741391110749449/4302256369 (or Test ID in testMode).
 * Replaces standard banner ads with a seamless native experience matching V2P's UI design.
 */
@Composable
fun NativeAdvancedAdCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdFree = remember { SettingsPersistence.isAdFree(context) }
    if (isAdFree) return

    val isOnline = remember { OfflineAdCache.isNetworkAvailable(context) }
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        if (isOnline) {
            val adUnitId = AdManager.nativeAdUnitId
            loadNativeAd(context, adUnitId) { loadedAd, error ->
                if (loadedAd != null) {
                    nativeAdState = loadedAd
                    loadFailed = false
                } else {
                    Log.w(TAG, "Native ad primary load failed: ${error?.message}")
                    // Retry with test native ad if primary failed and wasn't already test
                    if (adUnitId != AppConfig.TEST_NATIVE_AD_UNIT_ID) {
                        loadNativeAd(context, AppConfig.TEST_NATIVE_AD_UNIT_ID) { retryAd, retryErr ->
                            if (retryAd != null) {
                                nativeAdState = retryAd
                                loadFailed = false
                            } else {
                                Log.w(TAG, "Native ad fallback load failed: ${retryErr?.message}")
                                loadFailed = true
                            }
                        }
                    } else {
                        loadFailed = true
                    }
                }
            }
        }
        onDispose {
            nativeAdState?.destroy()
        }
    }

    if (isOnline && nativeAdState != null) {
        val loadedAd = nativeAdState!!
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (ThemeConfig.isDarkTheme) Color(0xFF1E2836) else Color(0xFFF1F5F9)
            ),
            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.35f))
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    createNativeAdView(ctx, loadedAd)
                },
                update = { view ->
                    populateNativeAdView(view, loadedAd)
                }
            )
        }
    } else {
        // Offline / Fallback Native Card
        OfflineNativeAdCard(modifier = modifier)
    }
}

/**
 * Loads a Native Ad using AdLoader.
 */
private fun loadNativeAd(
    context: Context,
    adUnitId: String,
    onResult: (NativeAd?, LoadAdError?) -> Unit
) {
    try {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad: NativeAd ->
                onResult(ad, null)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    onResult(null, error)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    } catch (e: Throwable) {
        Log.e(TAG, "Exception during native ad build/load", e)
        onResult(null, null)
    }
}

/**
 * Programmatically builds the NativeAdView hierarchy styled to match V2P's design language.
 */
private fun createNativeAdView(context: Context, nativeAd: NativeAd): NativeAdView {
    val nativeAdView = NativeAdView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dpToPx(context, 14), dpToPx(context, 12), dpToPx(context, 14), dpToPx(context, 12))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // Top Attribution Header: "Ad" badge + Advertiser / Star rating
    val headerRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    val adBadge = TextView(context).apply {
        text = "AD"
        textSize = 10f
        typeface = Typeface.MONOSPACE
        setTextColor(android.graphics.Color.parseColor("#12181F"))
        setPadding(dpToPx(context, 6), dpToPx(context, 2), dpToPx(context, 6), dpToPx(context, 2))
        background = GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor("#00E5FF"))
            cornerRadius = dpToPx(context, 4).toFloat()
        }
    }
    headerRow.addView(adBadge)

    val advertiserView = TextView(context).apply {
        textSize = 11f
        setTextColor(android.graphics.Color.parseColor("#90A4AE"))
        setPadding(dpToPx(context, 8), 0, 0, 0)
        maxLines = 1
    }
    nativeAdView.advertiserView = advertiserView
    headerRow.addView(advertiserView)

    rootLayout.addView(headerRow)

    // Middle Content Row: Icon + Headline & Body
    val contentRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dpToPx(context, 10), 0, dpToPx(context, 10))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    val iconView = ImageView(context).apply {
        val size = dpToPx(context, 48)
        layoutParams = LinearLayout.LayoutParams(size, size).apply {
            marginEnd = dpToPx(context, 12)
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
    }
    nativeAdView.iconView = iconView
    contentRow.addView(iconView)

    val textColumn = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val headlineView = TextView(context).apply {
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        setTextColor(if (ThemeConfig.isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#1C1B1F"))
        maxLines = 1
    }
    nativeAdView.headlineView = headlineView
    textColumn.addView(headlineView)

    val bodyView = TextView(context).apply {
        textSize = 12f
        setTextColor(android.graphics.Color.parseColor("#90A4AE"))
        maxLines = 2
        setPadding(0, dpToPx(context, 2), 0, 0)
    }
    nativeAdView.bodyView = bodyView
    textColumn.addView(bodyView)

    contentRow.addView(textColumn)
    rootLayout.addView(contentRow)

    // MediaView (rendered if the native ad has image/video assets)
    val mediaView = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 140)
        ).apply {
            topMargin = dpToPx(context, 4)
            bottomMargin = dpToPx(context, 10)
        }
    }
    nativeAdView.mediaView = mediaView
    rootLayout.addView(mediaView)

    // Call To Action Button (Full width, Cyberpunk Cyan theme)
    val ctaButton = Button(context).apply {
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        setTextColor(android.graphics.Color.parseColor("#12181F"))
        background = GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor("#00E5FF"))
            cornerRadius = dpToPx(context, 8).toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 38)
        )
    }
    nativeAdView.callToActionView = ctaButton
    rootLayout.addView(ctaButton)

    nativeAdView.addView(rootLayout)
    populateNativeAdView(nativeAdView, nativeAd)

    return nativeAdView
}

/**
 * Binds NativeAd data to the view elements.
 */
private fun populateNativeAdView(nativeAdView: NativeAdView, nativeAd: NativeAd) {
    (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline ?: ""

    val body = nativeAd.body
    val bodyView = nativeAdView.bodyView as? TextView
    if (body.isNullOrBlank()) {
        bodyView?.visibility = View.GONE
    } else {
        bodyView?.visibility = View.VISIBLE
        bodyView?.text = body
    }

    val icon = nativeAd.icon
    val iconView = nativeAdView.iconView as? ImageView
    if (icon?.drawable != null) {
        iconView?.visibility = View.VISIBLE
        iconView?.setImageDrawable(icon.drawable)
    } else {
        iconView?.visibility = View.GONE
    }

    val advertiser = nativeAd.advertiser
    val advertiserView = nativeAdView.advertiserView as? TextView
    if (advertiser.isNullOrBlank()) {
        advertiserView?.visibility = View.GONE
    } else {
        advertiserView?.visibility = View.VISIBLE
        advertiserView?.text = "• $advertiser"
    }

    val mediaView = nativeAdView.mediaView as? MediaView
    if (nativeAd.mediaContent != null && nativeAd.mediaContent!!.hasVideoContent()) {
        mediaView?.visibility = View.VISIBLE
        mediaView?.mediaContent = nativeAd.mediaContent
    } else if (nativeAd.images.isNotEmpty()) {
        mediaView?.visibility = View.VISIBLE
        mediaView?.mediaContent = nativeAd.mediaContent
    } else {
        mediaView?.visibility = View.GONE
    }

    val cta = nativeAd.callToAction
    val ctaButton = nativeAdView.callToActionView as? Button
    if (cta.isNullOrBlank()) {
        ctaButton?.text = "LEARN MORE"
    } else {
        ctaButton?.text = cta.uppercase()
    }

    nativeAdView.setNativeAd(nativeAd)
}

/**
 * Offline / Placeholder fallback card when disconnected.
 */
@Composable
private fun OfflineNativeAdCard(modifier: Modifier = Modifier) {
    val (cachedTitle, cachedSubtitle) = remember { OfflineAdCache.getCachedBanner(isLarge = true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ThemeConfig.isDarkTheme) Color(0xFF1E2836) else Color(0xFFF1F5F9)
        ),
        border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = WarningAmber.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "SPONSORED",
                        style = TextStyle(
                            color = WarningAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Featured Recommendation",
                    style = TextStyle(
                        color = CoolGrey,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TechCyan.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Ad Icon",
                            tint = TechCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = cachedTitle,
                        style = TextStyle(
                            color = if (ThemeConfig.isDarkTheme) TextLight else Color(0xFF1C1B1F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = cachedSubtitle,
                        style = TextStyle(
                            color = CoolGrey,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* Offline placeholder */ },
                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "DISCOVER MORE",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF12181F)
                    )
                )
            }
        }
    }
}

private fun dpToPx(context: Context, dp: Int): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).toInt()
}
