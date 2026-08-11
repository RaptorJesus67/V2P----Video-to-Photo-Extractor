package com.example

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast

// Data models
data class VideoItem(
    val id: String,
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val thumbnail: Bitmap? = null,
    val isAnalyzing: Boolean = false,
    val fps: Double = 30.0
)

enum class RateType {
    Interval, Fps
}

enum class IntervalUnit {
    Ms, S, M
}

enum class OutputFormat {
    Jpeg, Png
}

data class ExtractionSettings(
    val rateType: RateType = RateType.Interval,
    val intervalAmount: Double = 250.0,
    val intervalUnit: IntervalUnit = IntervalUnit.Ms,
    val fps: Double = 5.0,
    val isPrecise: Boolean = true,
    val format: OutputFormat = OutputFormat.Jpeg,
    val jpegQuality: Int = 90,
    val prefix: String = "",
    val deleteAfterSuccess: Boolean = false,
    val customDirectory: String = "",
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val playAudioByDefault: Boolean = true,
    val maxThreads: Int = 4,
    val overrideFramesSameName: Boolean = true,
    val forceSequentialRendering: Boolean = false
)

sealed class ExtractionStatus {
    object Idle : ExtractionStatus()
    data class Processing(
        val progress: Float,
        val currentFrame: Int,
        val totalFrames: Int = 0,
        val expectedFrames: Int? = totalFrames,
        val currentVideoName: String = "",
        val videoIndex: Int = 0,
        val totalVideos: Int = 1,
        val savedFolder: String = ""
    ) : ExtractionStatus()
    data class Success(
        val count: Int,
        val pathDisplay: String = "",
        val totalFrames: Int = count,
        val savedFolder: String = pathDisplay,
        val name: String = ""
    ) : ExtractionStatus()
    data class Error(val message: String) : ExtractionStatus()
}

data class SavedFrame(
    val uri: Uri,
    val timestampMs: Long = 0L,
    val videoName: String = "",
    val fileName: String = "",
    val name: String = fileName,
    val sizeBytes: Long = 0L
)

data class SavedPhotoEntry(
    val uriString: String,
    val fileName: String = "",
    val name: String = fileName,
    val timestampMs: Long = 0L,
    val videoName: String = "",
    val dateSavedMs: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0L,
    val directoryName: String = ""
)

object V2pBillingManager {
    // Base64-encoded RSA public key from Google Play Console used for purchase validation
    const val GOOGLE_PLAY_LICENSING_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxboh0WKHc9nnrksVb6CMup5VVzJnuEB0WNNywEmotjLHp62SHjPJK8ONgO55dkTmX30J72APsYRrBzrkZuFY0KpIsbWPEyoQS4yKMpjRtmr3vcD/Ne9RwI7SV3RJd/mZScgzFYFXMpoPi/Do64QpcUq222JW5b3qR5vCN43WhiRdXnM6zFZMgPZ02K8aoe5awO6yIMyHSRjio4FQXKIiWMuhqkqXszFxbu3IPxvsy38jLWemA1X43t50+8aY/QfVbb0lkfSNK9R/ZkRb1EtgB+t7U+1tY90uU1vZyVKQtCFqOklU14SBbn0KnpHvae3jGlwKQdnFzkBg0S/QR87HjQIDAQAB"

    private const val SKU_AD_FREE = "ad_free_lifetime"
    private var billingClient: BillingClient? = null

    fun queryPurchasesAsync(context: Context, onResult: (Boolean) -> Unit = {}) {
        val client = BillingClient.newBuilder(context)
            .setListener { _, _ -> }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        billingClient = client

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                    client.queryPurchasesAsync(params) { result, purchases ->
                        val isPurchased = result.responseCode == BillingClient.BillingResponseCode.OK &&
                                purchases.any { purchase ->
                                    purchase.products.contains(SKU_AD_FREE) &&
                                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                                }
                        SettingsPersistence.setAdFree(context, isPurchased)
                        onResult(isPurchased)
                    }
                } else {
                    onResult(SettingsPersistence.isAdFree(context))
                }
            }

            override fun onBillingServiceDisconnected() {
                onResult(SettingsPersistence.isAdFree(context))
            }
        })
    }
}

object PhotoCacheManager {
    private const val CACHE_PREFS = "v2p_photo_cache"

    fun saveSettingsCache(context: Context, settings: ExtractionSettings) {
        try {
            val prefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("last_custom_dir", settings.customDirectory)
                putString("last_prefix", settings.prefix)
                putInt("last_jpeg_quality", settings.jpegQuality)
                putString("last_format", settings.format.name)
                apply()
            }
        } catch (_: Exception) {}
    }
}

// Global pure helpers
fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun formatFileSize(bytes: Long): String = formatSize(bytes)

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0s"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = ms % 1000
    return if (minutes > 0) {
        String.format("%dm %02ds", minutes, seconds)
    } else if (seconds > 0) {
        String.format("%d.%02ds", seconds, millis / 10)
    } else {
        String.format("%dms", ms)
    }
}

fun extractDirectoryName(context: Context, uri: Uri): String {
    try {
        val path = uri.path ?: return "Video2Photos"
        if (path.contains("Pictures/")) {
            val sub = path.substringAfter("Pictures/")
            val dir = sub.substringBefore("/")
            if (dir.isNotBlank()) return dir
        }
    } catch (_: Exception) {}
    return "Video2Photos"
}

fun getVolumeNameForPath(context: Context, path: String?): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (path != null && path.contains("/storage/") && !path.contains("/emulated/0")) {
            val parts = path.split("/")
            val storageIdx = parts.indexOf("storage")
            if (storageIdx != -1 && storageIdx + 1 < parts.size) {
                val vol = parts[storageIdx + 1]
                if (vol.isNotBlank() && vol != "emulated" && vol != "self") {
                    return vol.lowercase()
                }
            }
        }
        return MediaStore.VOLUME_EXTERNAL_PRIMARY
    }
    return "external"
}

fun getVideoDataPath(context: Context, videoUri: Uri): String? {
    try {
        context.contentResolver.query(videoUri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (idx != -1) return cursor.getString(idx)
            }
        }
    } catch (_: Exception) {}
    return null
}

fun getCustomRelativePath(customDirName: String): String {
    val cleanDir = customDirName.trim().trim('/')
    return if (cleanDir.startsWith("Pictures")) cleanDir else "Pictures/$cleanDir"
}

fun getDestinationRelativePath(context: Context, videoUri: Uri, videoDataPath: String?): String {
    val defaultSubFolder = "Pictures/Video2Photos"
    if (videoDataPath != null && videoDataPath.contains("/DCIM/Camera")) {
        return "DCIM/Camera"
    }
    return defaultSubFolder
}

fun saveFrameToMediaStore(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
    format: OutputFormat,
    quality: Int,
    relativePath: String,
    volumeName: String,
    overrideFramesSameName: Boolean = true
): Uri? {
    val mimeType = if (format == OutputFormat.Jpeg) "image/jpeg" else "image/png"
    val compressFormat = if (format == OutputFormat.Jpeg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG
    val extension = if (format == OutputFormat.Jpeg) ".jpg" else ".png"
    val displayName = if (fileName.endsWith(extension, ignoreCase = true)) fileName else "$fileName$extension"

    val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(volumeName)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    if (!overrideFramesSameName) {
        try {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
            context.contentResolver.query(contentUri, projection, selection, arrayOf(displayName), null)?.use { cursor ->
                if (cursor.count > 0) {
                    return Uri.parse("content://skipped")
                }
            }
        } catch (_: Exception) {}
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val imageUri = resolver.insert(contentUri, contentValues) ?: return null

    try {
        resolver.openOutputStream(imageUri)?.use { out ->
            bitmap.compress(compressFormat, quality, out)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
        }
        return imageUri
    } catch (e: Exception) {
        try { resolver.delete(imageUri, null, null) } catch (_: Exception) {}
        return null
    }
}

fun resolveToMediaStoreVideoUri(context: Context, uri: Uri): Uri {
    if (uri.scheme == "content" && uri.authority == MediaStore.AUTHORITY) return uri
    val videoDataPath = getVideoDataPath(context, uri) ?: return uri
    val projection = arrayOf(MediaStore.Video.Media._ID)
    val selection = "${MediaStore.Video.Media.DATA} = ?"
    try {
        context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, arrayOf(videoDataPath), null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIdx = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                if (idIdx != -1) {
                    val id = cursor.getLong(idIdx)
                    return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                }
            }
        }
    } catch (_: Exception) {}
    return uri
}

fun getCacheSizeBytes(context: Context): Long {
    return try {
        val cacheDir = context.cacheDir
        fun dirSize(dir: java.io.File): Long {
            var size = 0L
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) dirSize(file) else file.length()
            }
            return size
        }
        dirSize(cacheDir)
    } catch (_: Exception) { 0L }
}

fun clearAppCache(context: Context) {
    try {
        context.cacheDir.deleteRecursively()
    } catch (_: Exception) {}
}

fun getFrameAtTime(context: Context, uri: Uri, timeMs: Long): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.getFrameAtTime(timeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (_: Exception) {
        null
    } finally {
        try { retriever.release() } catch (_: Exception) {}
    }
}
