package com.kcmitch.v2p.pages.workspace

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.kcmitch.v2p.config.AppConfig
import com.kcmitch.v2p.SettingsPersistence
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
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.kcmitch.v2p.pages.GalleryGroupMode
import com.kcmitch.v2p.pages.GallerySortOption
import com.kcmitch.v2p.pages.GallerySortDirection

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
    val forceSequentialRendering: Boolean = false,
    val galleryIsGridView: Boolean = false,
    val galleryGroupMode: GalleryGroupMode = GalleryGroupMode.DIRECTORY,
    val gallerySortOption: GallerySortOption = GallerySortOption.DATE_EXTRACTED,
    val gallerySortDirection: GallerySortDirection = GallerySortDirection.DESCENDING
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

data class VideoNativeLocation(
    val volumeName: String,
    val relativePath: String,
    val directoryName: String
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
    // Base64-encoded RSA public key from Google Play Console sourced from AppConfig
    val GOOGLE_PLAY_LICENSING_KEY: String
        get() = AppConfig.GOOGLE_PLAY_LICENSING_KEY

    private const val SKU_AD_FREE = "ad_free_lifetime"
    private var billingClient: BillingClient? = null

    fun queryPurchasesAsync(context: Context, onResult: (Boolean) -> Unit = {}) {
        try {
            val gmsCheck = try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            } catch (_: Throwable) {
                ConnectionResult.SERVICE_MISSING
            }
            if (gmsCheck != ConnectionResult.SUCCESS) {
                onResult(SettingsPersistence.isAdFree(context))
                return
            }

            val client = try {
                BillingClient.newBuilder(context)
                    .setListener { _, _ -> }
                    .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                    .build()
            } catch (e: Throwable) {
                onResult(SettingsPersistence.isAdFree(context))
                return
            }
            billingClient = client

            try {
                client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        try {
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
                        } catch (_: Throwable) {
                            onResult(SettingsPersistence.isAdFree(context))
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        onResult(SettingsPersistence.isAdFree(context))
                    }
                })
            } catch (e: Throwable) {
                onResult(SettingsPersistence.isAdFree(context))
            }
        } catch (_: Throwable) {
            onResult(SettingsPersistence.isAdFree(context))
        }
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

fun parseLocationFromDataPath(dataPath: String): VideoNativeLocation? {
    if (dataPath.isBlank()) return null
    val cleanPath = dataPath.replace('\\', '/')
    val lastSlash = cleanPath.lastIndexOf('/')
    if (lastSlash == -1) return null
    val parentPath = cleanPath.substring(0, lastSlash)
    val leafName = parentPath.substringAfterLast('/')

    var volumeName = MediaStore.VOLUME_EXTERNAL_PRIMARY
    var relativePath = "Pictures/v2p"

    val lower = parentPath.lowercase()
    val storageIdx = lower.indexOf("/storage/")
    if (storageIdx != -1) {
        val afterStorage = parentPath.substring(storageIdx + "/storage/".length)
        val parts = afterStorage.split('/')
        if (parts.isNotEmpty()) {
            val firstPart = parts[0]
            val isEmulated = firstPart.equals("emulated", ignoreCase = true)
            val subParts = if (isEmulated && parts.size > 1 && parts[1] == "0") {
                volumeName = MediaStore.VOLUME_EXTERNAL_PRIMARY
                parts.drop(2)
            } else if (!isEmulated) {
                volumeName = firstPart.lowercase()
                parts.drop(1)
            } else {
                parts.drop(1)
            }
            if (subParts.isNotEmpty()) {
                relativePath = subParts.joinToString("/")
            }
        }
    } else {
        val knownFolders = listOf("pictures", "dcim", "movies", "download", "documents")
        for (folder in knownFolders) {
            val idx = lower.indexOf("/$folder/")
            if (idx != -1) {
                relativePath = parentPath.substring(idx + 1)
                break
            }
        }
    }

    val dirName = if (leafName.isNotBlank()) leafName else "v2p"
    return VideoNativeLocation(volumeName, relativePath, dirName)
}

fun queryMediaStoreVideoLocation(context: Context, uri: Uri): VideoNativeLocation {
    var volumeName = MediaStore.VOLUME_EXTERNAL_PRIMARY
    var relativePath: String? = null
    var bucketName: String? = null
    var dataPath: String? = null

    try {
        val projection = mutableListOf(MediaStore.Video.Media._ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection.add(MediaStore.Video.Media.RELATIVE_PATH)
            projection.add(MediaStore.Video.Media.VOLUME_NAME)
        }
        projection.add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        projection.add(MediaStore.Video.Media.DATA)

        context.contentResolver.query(uri, projection.toTypedArray(), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val relIdx = cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                    if (relIdx != -1) relativePath = cursor.getString(relIdx)
                    val volIdx = cursor.getColumnIndex(MediaStore.Video.Media.VOLUME_NAME)
                    if (volIdx != -1) {
                        val vol = cursor.getString(volIdx)
                        if (!vol.isNullOrBlank()) volumeName = vol.lowercase()
                    }
                }
                val bucketIdx = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                if (bucketIdx != -1) bucketName = cursor.getString(bucketIdx)
                val dataIdx = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                if (dataIdx != -1) dataPath = cursor.getString(dataIdx)
            }
        }
    } catch (_: Exception) {}

    if (!dataPath.isNullOrBlank()) {
        val parsed = parseLocationFromDataPath(dataPath!!)
        if (parsed != null) {
            return if (!bucketName.isNullOrBlank()) parsed.copy(directoryName = bucketName!!) else parsed
        }
    }

    if (!relativePath.isNullOrBlank()) {
        val cleanRel = relativePath!!.trim('/')
        val leaf = cleanRel.substringAfterLast('/')
        val dir = if (!bucketName.isNullOrBlank()) bucketName!! else (if (leaf.isNotBlank()) leaf else "v2p")
        return VideoNativeLocation(volumeName, cleanRel, dir)
    }

    if (!bucketName.isNullOrBlank()) {
        return VideoNativeLocation(volumeName, "Pictures/$bucketName", bucketName!!)
    }

    return VideoNativeLocation(volumeName, "Pictures/v2p", "v2p")
}

fun getVideoNativeLocation(context: Context, videoUri: Uri): VideoNativeLocation {
    var volumeName = MediaStore.VOLUME_EXTERNAL_PRIMARY
    var relativePath = "Pictures/v2p"
    var directoryName = "v2p"

    // 1. Check if DocumentUri (SAF)
    try {
        if (DocumentsContract.isDocumentUri(context, videoUri)) {
            val docId = DocumentsContract.getDocumentId(videoUri)
            val decoded = Uri.decode(docId)
            if (decoded.startsWith("video:", ignoreCase = true)) {
                val mediaId = decoded.substringAfter("video:")
                val mediaUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId)
                return queryMediaStoreVideoLocation(context, mediaUri)
            } else if (decoded.contains(":")) {
                val parts = decoded.split(":")
                val volPart = parts[0]
                val pathPart = parts.drop(1).joinToString(":")
                volumeName = if (volPart.equals("primary", ignoreCase = true)) {
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                } else {
                    volPart.lowercase()
                }
                val dirOnly = if (pathPart.contains("/")) pathPart.substringBeforeLast("/") else pathPart
                val cleanDir = dirOnly.trim('/')
                if (cleanDir.isNotBlank()) {
                    relativePath = cleanDir
                    directoryName = cleanDir.substringAfterLast('/')
                    if (directoryName.isBlank()) directoryName = cleanDir
                    return VideoNativeLocation(volumeName, relativePath, directoryName)
                }
            }
        }
    } catch (_: Exception) {}

    // 2. Direct MediaStore Query
    val mediaStoreLocation = queryMediaStoreVideoLocation(context, videoUri)
    if (mediaStoreLocation.directoryName != "v2p" || mediaStoreLocation.relativePath != "Pictures/v2p") {
        return mediaStoreLocation
    }

    // 3. Check data path from direct query or uri path
    val dataPath = getVideoDataPath(context, videoUri) ?: videoUri.path
    if (!dataPath.isNullOrBlank()) {
        val parsed = parseLocationFromDataPath(dataPath)
        if (parsed != null) {
            return parsed
        }
    }

    return VideoNativeLocation(volumeName, relativePath, directoryName)
}

fun extractDirectoryName(context: Context, uri: Uri): String {
    try {
        val scheme = uri.scheme
        if (scheme == "content") {
            if (uri.authority == MediaStore.AUTHORITY || uri.toString().contains("media")) {
                val projection = mutableListOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    projection.add(MediaStore.Images.Media.RELATIVE_PATH)
                }
                projection.add(MediaStore.Images.Media.DATA)

                context.contentResolver.query(uri, projection.toTypedArray(), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val bucketIdx = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                        if (bucketIdx != -1) {
                            val bucket = cursor.getString(bucketIdx)
                            if (!bucket.isNullOrBlank()) return bucket
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val relIdx = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                            if (relIdx != -1) {
                                val rel = cursor.getString(relIdx)
                                if (!rel.isNullOrBlank()) {
                                    val leaf = rel.trim('/').substringAfterLast('/')
                                    if (leaf.isNotBlank()) return leaf
                                }
                            }
                        }
                        val dataIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                        if (dataIdx != -1) {
                            val data = cursor.getString(dataIdx)
                            if (!data.isNullOrBlank()) {
                                val parent = java.io.File(data).parentFile?.name
                                if (!parent.isNullOrBlank()) return parent
                            }
                        }
                    }
                }
            } else if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val decoded = Uri.decode(docId)
                val clean = decoded.substringBeforeLast('/')
                val leaf = clean.substringAfterLast(':').substringAfterLast('/')
                if (leaf.isNotBlank()) return leaf
            } else if (DocumentsContract.isTreeUri(uri)) {
                val treeDocId = DocumentsContract.getTreeDocumentId(uri)
                val decoded = Uri.decode(treeDocId)
                val leaf = decoded.substringAfterLast(':').trim('/').substringAfterLast('/')
                if (leaf.isNotBlank()) return leaf
            }
        } else if (scheme == "file") {
            val file = java.io.File(uri.path ?: "")
            val parent = file.parentFile?.name
            if (!parent.isNullOrBlank()) return parent
        }
        val path = uri.path ?: ""
        if (path.isNotBlank()) {
            val leaf = path.trim('/').substringBeforeLast('/').substringAfterLast('/')
            if (leaf.isNotBlank() && leaf != "media" && leaf != "external" && leaf != "images") {
                return leaf
            }
        }
    } catch (_: Exception) {}
    return "v2p"
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
    if (cleanDir.isEmpty()) return "Pictures/v2p"
    val firstSeg = cleanDir.split('/').firstOrNull()?.lowercase()
    if (firstSeg != "pictures" && firstSeg != "dcim" && firstSeg != "download" && firstSeg != "movies") {
        return "Pictures/$cleanDir"
    }
    return cleanDir
}

fun getCustomDirectoryDisplayName(context: Context, customDir: String): String {
    if (customDir.isBlank()) return "v2p"
    try {
        if (customDir.startsWith("content://")) {
            val treeUri = Uri.parse(customDir)
            val doc = DocumentFile.fromTreeUri(context, treeUri)
            val docName = doc?.name
            if (!docName.isNullOrBlank()) return docName
            val decoded = Uri.decode(customDir)
            val leaf = decoded.substringAfterLast(':').trim('/').substringAfterLast('/')
            if (leaf.isNotBlank()) return leaf
        } else {
            val trimmed = customDir.trim().trim('/')
            val leaf = trimmed.substringAfterLast('/')
            if (leaf.isNotBlank()) return leaf
        }
    } catch (_: Exception) {}
    return "v2p"
}

fun getDestinationRelativePath(context: Context, videoUri: Uri, videoDataPath: String? = null): String {
    if (!videoDataPath.isNullOrBlank()) {
        val parsed = parseLocationFromDataPath(videoDataPath)
        if (parsed != null && parsed.relativePath.isNotBlank()) {
            return parsed.relativePath
        }
    }
    return getVideoNativeLocation(context, videoUri).relativePath
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
