package com.kcmitch.v2p

import com.kcmitch.v2p.data.db.*
import com.kcmitch.v2p.config.*
import com.kcmitch.v2p.settings.*
import com.kcmitch.v2p.thirdParty.ads.*
import com.kcmitch.v2p.thirdParty.db.*
import com.kcmitch.v2p.pages.*
import com.kcmitch.v2p.pages.gallery.*
import com.kcmitch.v2p.pages.workspace.*
import com.kcmitch.v2p.applets.ads.*
import com.kcmitch.v2p.applets.settings.*
import com.kcmitch.v2p.applets.rating.*

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Canvas
import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.delay
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import com.kcmitch.v2p.ui.theme.MyApplicationTheme
import com.kcmitch.v2p.ui.theme.ThemeConfig
import com.kcmitch.v2p.ui.theme.DeleteRed
import com.kcmitch.v2p.ui.theme.TerminalGreen
import com.kcmitch.v2p.ui.theme.TerminalBg
import com.kcmitch.v2p.ui.theme.TechCyan
import com.kcmitch.v2p.ui.theme.DarkSlateBg
import com.kcmitch.v2p.ui.theme.SlateCard
import com.kcmitch.v2p.ui.theme.SlateCardHeader
import com.kcmitch.v2p.ui.theme.WarningAmber
import com.kcmitch.v2p.ui.theme.CoolGrey
import com.kcmitch.v2p.ui.theme.BorderSlate
import com.kcmitch.v2p.ui.theme.TextLight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeConfig.initialize(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        V2pBillingManager.queryPurchasesAsync(this)
        AdManager.initialize(this)
        FirebaseUserDatabase.syncUserData(this)
        viewModel.initPipelineQueue(this)
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    V2pAppScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        val uris = mutableListOf<Uri>()

        if (Intent.ACTION_SEND == action && type != null) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            uri?.let { uris.add(it) }
        } else if (Intent.ACTION_SEND_MULTIPLE == action && type != null) {
            val list: ArrayList<Uri>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            }
            list?.let { uris.addAll(it.filterNotNull()) }
        }

        if (uris.isNotEmpty()) {
            if (viewModel.extractionStatus is ExtractionStatus.Processing) {
                viewModel.showProcessingDialogAndSelect(this, uris)
            } else {
                viewModel.selectVideos(this, uris)
            }
        }
    }
}

class MainViewModel : ViewModel() {
    var selectedVideos by mutableStateOf<List<VideoItem>>(emptyList())
        private set

    var extractionStatus by mutableStateOf<ExtractionStatus>(ExtractionStatus.Idle)
        private set

    var isExtractionPaused by mutableStateOf(false)
        private set

    var savedPhotos by mutableStateOf<List<SavedPhotoEntry>>(emptyList())
        private set

    var lastStateGalleryCount by mutableIntStateOf(0)
        private set

    var recentExtractedFrames by mutableStateOf<List<SavedFrame>>(emptyList())
        private set

    var pendingDeletes by mutableStateOf<List<Uri>?>(null)
        private set

    var showProcessingDialogMessage by mutableStateOf<String?>(null)
        private set

    private var appContext: Context? = null
    private var isPipelineRestored = false

    fun initPipelineQueue(context: Context) {
        appContext = context.applicationContext
        if (isPipelineRestored) return
        isPipelineRestored = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val repository = PipelineRepository.getInstance(context)
                val cachedEntities = repository.getCachedPipelineVideos()
                if (cachedEntities.isNotEmpty()) {
                    val restoredVideos = mutableListOf<VideoItem>()
                    for (entity in cachedEntities) {
                        val uri = Uri.parse(entity.uriString)
                        try {
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: Exception) {}

                        val freshDetails = getVideoDetails(context, uri)
                        if (freshDetails != null) {
                            restoredVideos.add(freshDetails)
                        } else {
                            restoredVideos.add(
                                VideoItem(
                                    id = entity.id,
                                    uri = uri,
                                    name = entity.name,
                                    durationMs = entity.durationMs,
                                    sizeBytes = entity.sizeBytes,
                                    fps = entity.fps
                                )
                            )
                        }
                    }

                    if (restoredVideos.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            if (selectedVideos.isEmpty()) {
                                selectedVideos = restoredVideos
                                addLogInternal("📦 Restored ${restoredVideos.size} cached video ${if (restoredVideos.size == 1) "item" else "items"} in pipeline queue.")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Non-blocking error handling for cache restoration
            }
        }
    }

    fun initLastStateGalleryCount(context: Context) {
        appContext = context.applicationContext
        if (lastStateGalleryCount == 0) {
            val savedCount = SettingsPersistence.getLastStateGalleryCount(context)
            lastStateGalleryCount = savedCount
        }
    }

    private fun syncGalleryCountToStorage(count: Int) {
        val positiveOrZero = count.coerceAtLeast(0)
        lastStateGalleryCount = positiveOrZero
        appContext?.let { ctx ->
            SettingsPersistence.setLastStateGalleryCount(ctx, positiveOrZero)
        }
    }

    init {
        // Gallery number refresh rate: 5Hz (every 200ms)
        viewModelScope.launch {
            while (coroutineContext.isActive) {
                delay(200L)
                val currentCount = savedPhotos.size
                if (currentCount > 0 || lastStateGalleryCount == 0) {
                    if (lastStateGalleryCount != currentCount) {
                        syncGalleryCountToStorage(currentCount)
                    }
                }
            }
        }

        // "Recent Extracted Frames" Container refresh rate: (1/3)Hz (every 3000ms = 3s)
        viewModelScope.launch {
            while (coroutineContext.isActive) {
                delay(3000L)
                val currentList = _extractedFrames.toList()
                if (recentExtractedFrames != currentList) {
                    recentExtractedFrames = currentList
                }
            }
        }
    }

    fun pauseExtraction() {
        isExtractionPaused = true
        addLogInternal("⏸ Extraction pipeline paused by user.")
    }

    fun resumeExtraction() {
        isExtractionPaused = false
        addLogInternal("▶ Resuming frame extraction pipeline...")
    }

    fun loadAndValidateSavedPhotos(context: Context) {
        initLastStateGalleryCount(context)
        viewModelScope.launch(Dispatchers.IO) {
            var registryFile = java.io.File(context.cacheDir, "saved_photos_registry.json")
            if (!registryFile.exists()) {
                val legacyFile = java.io.File(context.filesDir, "saved_photos_registry.json")
                if (legacyFile.exists()) {
                    try {
                        legacyFile.copyTo(registryFile, overwrite = true)
                        legacyFile.delete()
                    } catch (e: Exception) {}
                }
            }
            val loaded = mutableListOf<SavedPhotoEntry>()
            if (registryFile.exists()) {
                try {
                    val jsonStr = registryFile.readText()
                    val jsonArray = org.json.JSONArray(jsonStr)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val uriStr = obj.getString("uriString")
                        val uri = Uri.parse(uriStr)
                        var size = obj.optLong("sizeBytes", 0L)
                        if (size <= 0L) {
                            size = getUriSize(context, uri)
                        }
                        var dir = obj.optString("directoryName", "")
                        if (dir.isBlank() || dir.equals("Video2Photos", ignoreCase = true) || dir.equals("FrameGrabber", ignoreCase = true)) {
                            val extracted = extractDirectoryName(context, uri)
                            if (extracted.isNotBlank() && !extracted.equals("Video2Photos", ignoreCase = true) && !extracted.equals("FrameGrabber", ignoreCase = true)) {
                                dir = extracted
                            } else if (dir.isBlank()) {
                                dir = "v2p"
                            }
                        }
                        loaded.add(
                            SavedPhotoEntry(
                                uriString = uriStr,
                                fileName = obj.optString("fileName", "frame.jpg"),
                                timestampMs = obj.optLong("timestampMs", 0L),
                                videoName = obj.optString("videoName", "Video"),
                                dateSavedMs = obj.optLong("dateSavedMs", System.currentTimeMillis()),
                                sizeBytes = size,
                                directoryName = dir
                            )
                        )
                    }
                } catch (e: Exception) {}
            }

            val validList = loaded.filter { photo ->
                try {
                    val uri = Uri.parse(photo.uriString)
                    val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                    if (pfd != null) {
                        pfd.close()
                        true
                    } else false
                } catch (e: Exception) {
                    false
                }
            }

            withContext(Dispatchers.Main) {
                savedPhotos = validList
                syncGalleryCountToStorage(validList.size)
            }
            saveSavedPhotosToRegistry(context, validList)
        }
    }

    fun recordSavedPhoto(context: Context, photo: SavedPhotoEntry) {
        initLastStateGalleryCount(context)
        viewModelScope.launch(Dispatchers.IO) {
            val updated = (listOf(photo) + savedPhotos).distinctBy { it.uriString }
            withContext(Dispatchers.Main) {
                savedPhotos = updated
                syncGalleryCountToStorage(updated.size)
            }
            saveSavedPhotosToRegistry(context, updated)
        }
    }

    fun deleteSavedPhoto(context: Context, photo: SavedPhotoEntry) {
        deleteSavedPhotosBatch(context, listOf(photo))
    }

    fun deleteSavedPhotosBatch(context: Context, photos: List<SavedPhotoEntry>) {
        if (photos.isEmpty()) return
        initLastStateGalleryCount(context)
        viewModelScope.launch(Dispatchers.IO) {
            val permanently = SettingsPersistence.getDeletePhotosPermanently(context)
            val trashDir = java.io.File(context.cacheDir, "app_trash").apply { if (!exists()) mkdirs() }

            photos.forEach { photo ->
                try {
                    val uri = Uri.parse(photo.uriString)
                    if (permanently) {
                        context.contentResolver.delete(uri, null, null)
                        if (photo.uriString.startsWith("file://")) {
                            java.io.File(Uri.parse(photo.uriString).path ?: "").delete()
                        }
                    } else {
                        // Place in App Trash with timestamp record
                        val trashFile = java.io.File(trashDir, "trash_${System.currentTimeMillis()}_${photo.fileName}.json")
                        val json = org.json.JSONObject().apply {
                            put("uriString", photo.uriString)
                            put("fileName", photo.fileName)
                            put("trashedAtMs", System.currentTimeMillis())
                        }
                        trashFile.writeText(json.toString())
                        try { context.contentResolver.delete(uri, null, null) } catch (_: Exception) {}
                    }
                } catch (e: Exception) {}
            }

            // Clean up trash older than 3 days
            val threeDaysMs = 3 * 24 * 60 * 60 * 1000L
            val now = System.currentTimeMillis()
            trashDir.listFiles()?.forEach { file ->
                if (now - file.lastModified() > threeDaysMs) {
                    file.delete()
                }
            }

            val deleteUriSet = photos.map { it.uriString }.toSet()
            val updated = savedPhotos.filter { it.uriString !in deleteUriSet }
            withContext(Dispatchers.Main) {
                savedPhotos = updated
                syncGalleryCountToStorage(updated.size)
            }
            saveSavedPhotosToRegistry(context, updated)
        }
    }

    private fun saveSavedPhotosToRegistry(context: Context, list: List<SavedPhotoEntry>) {
        try {
            val jsonArray = org.json.JSONArray()
            list.forEach { photo ->
                val obj = org.json.JSONObject().apply {
                    put("uriString", photo.uriString)
                    put("fileName", photo.fileName)
                    put("timestampMs", photo.timestampMs)
                    put("videoName", photo.videoName)
                    put("dateSavedMs", photo.dateSavedMs)
                    put("sizeBytes", photo.sizeBytes)
                    put("directoryName", photo.directoryName)
                }
                jsonArray.put(obj)
            }
            val registryFile = java.io.File(context.cacheDir, "saved_photos_registry.json")
            registryFile.writeText(jsonArray.toString())
        } catch (e: Exception) {}
    }

    fun showProcessingDialogAndSelect(context: Context, uris: List<Uri>) {
        showProcessingDialogMessage = "A frame extraction job is currently processing in the background. Please wait for the current process to finish or cancel/abort it first before adding more videos."
    }

    fun dismissProcessingDialog() {
        showProcessingDialogMessage = null
    }

    fun clearPendingDeletes() {
        pendingDeletes = null
    }

    fun onVideosDeleted(deletedUris: List<Uri>) {
        val deletedUriStrings = deletedUris.map { it.toString() }
        selectedVideos = selectedVideos.filter { it.uri.toString() !in deletedUriStrings }
        appContext?.let { ctx ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    PipelineRepository.getInstance(ctx).removeVideosByUris(deletedUriStrings)
                } catch (e: Exception) {}
            }
        }
        viewModelScope.launch(Dispatchers.Main) {
            _logs.add("🧹 Successfully deleted ${deletedUris.size} source video ${if (deletedUris.size == 1) "file" else "files"} from device.")
        }
        pendingDeletes = null
    }

    fun onVideosDeletionFailed() {
        viewModelScope.launch(Dispatchers.Main) {
            _logs.add("❌ Video deletion permission was denied or failed.")
        }
        pendingDeletes = null
    }

    private val _logs = mutableStateListOf<String>("v2p engine initialized.", "Ready to extract frames.")
    val logs: List<String> get() = _logs

    private val _extractedFrames = mutableStateListOf<SavedFrame>()
    val extractedFrames: List<SavedFrame> get() = _extractedFrames

    private var extractionJob: Job? = null

    fun selectVideos(context: Context, uris: List<Uri>) {
        appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            addLogInternal("Selecting ${uris.size} ${if (uris.size == 1) "item" else "items"}...")
            val currentBatchVideos = mutableListOf<VideoItem>()
            var addedCount = 0
            var ignoredQueueCount = 0
            var rejectedNonVideoCount = 0
            val rejectedNames = mutableListOf<String>()

            for (uri in uris) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {}

                var filename = uri.lastPathSegment ?: "unknown"
                try {
                    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIdx != -1) {
                                filename = cursor.getString(nameIdx) ?: filename
                            }
                        }
                    }
                } catch (e: Exception) {}

                val mimeType = context.contentResolver.getType(uri) ?: ""
                val isVideo = mimeType.startsWith("video/") || 
                              uri.toString().endsWith(".mp4", ignoreCase = true) || 
                              uri.toString().endsWith(".mkv", ignoreCase = true) || 
                              uri.toString().endsWith(".webm", ignoreCase = true) || 
                              uri.toString().endsWith(".avi", ignoreCase = true) || 
                              uri.toString().endsWith(".mov", ignoreCase = true) || 
                              uri.toString().endsWith(".3gp", ignoreCase = true) ||
                              uri.toString().endsWith(".flv", ignoreCase = true) ||
                              uri.toString().endsWith(".ts", ignoreCase = true)

                if (isVideo) {
                    addLogInternal("Analyzing: $filename")
                    val isAlreadyInQueue = selectedVideos.any { it.uri.toString() == uri.toString() } || 
                                           currentBatchVideos.any { it.uri.toString() == uri.toString() }
                    if (isAlreadyInQueue) {
                        addLogInternal("Skipping: $filename is already in the active pipeline queue...")
                        ignoredQueueCount++
                    } else {
                        val details = getVideoDetails(context, uri)
                        if (details != null) {
                            currentBatchVideos.add(details)
                            addedCount++
                        } else {
                            addLogInternal("⚠️ Warning: Could not read video details for: $filename")
                            rejectedNonVideoCount++
                            rejectedNames.add(filename)
                        }
                    }
                } else {
                    addLogInternal("⚠️ Warning: Filtered out non-video file: $filename (Only videos are supported)")
                    rejectedNonVideoCount++
                    rejectedNames.add(filename)
                }
            }

            if (currentBatchVideos.isNotEmpty()) {
                selectedVideos = selectedVideos + currentBatchVideos
                try {
                    val repo = PipelineRepository.getInstance(context)
                    repo.savePipelineVideos(selectedVideos)
                } catch (e: Exception) {}
            }

            if (rejectedNonVideoCount > 0) {
                val listStr = rejectedNames.joinToString(", ")
                addLogInternal("⚠️ Warning: Ignored $rejectedNonVideoCount ${if (rejectedNonVideoCount == 1) "non-video file" else "non-video files"} from the selection: $listStr")
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ignored $rejectedNonVideoCount ${if (rejectedNonVideoCount == 1) "non-video file" else "non-video files"} (only videos allowed).",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            val parts = mutableListOf<String>()
            parts.add("Added $addedCount ${if (addedCount == 1) "video" else "videos"}")
            if (ignoredQueueCount > 0) {
                parts.add("Ignored $ignoredQueueCount ${if (ignoredQueueCount == 1) "video" else "videos"} currently in queue")
            }
            if (rejectedNonVideoCount > 0) {
                parts.add("Rejected $rejectedNonVideoCount ${if (rejectedNonVideoCount == 1) "non-video" else "non-videos"}")
            }
            val summaryMsg = parts.joinToString(". ") + ". Active total: ${selectedVideos.size}"
            addLogInternal(summaryMsg)
        }
    }

    fun clearVideos() {
        selectedVideos = emptyList()
        addLogInternal("Cleared all selected videos.")
        appContext?.let { ctx ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    PipelineRepository.getInstance(ctx).clearPipeline()
                } catch (e: Exception) {}
            }
        }
    }

    fun removeVideo(video: VideoItem) {
        selectedVideos = selectedVideos.filter { it.id != video.id }
        addLogInternal("Removed video: ${video.name}")
        appContext?.let { ctx ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    PipelineRepository.getInstance(ctx).removeVideo(video.id)
                } catch (e: Exception) {}
            }
        }
    }

    fun addLog(msg: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _logs.add(msg)
        }
    }

    private fun addLogInternal(msg: String) {
        _logs.add(msg)
    }

    fun clearLogs() {
        _logs.clear()
        _logs.add("Terminal cleared.")
    }

    fun cancelExtraction() {
        isExtractionPaused = false
        extractionJob?.cancel()
        extractionJob = null
        extractionStatus = ExtractionStatus.Idle
        _logs.add("🛑 Pipeline aborted by user.")
    }

    fun resetExtractionStatus() {
        extractionStatus = ExtractionStatus.Idle
    }


















    private fun getUriSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { 
                it.statSize 
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun captureSingleFrame(context: Context, video: VideoItem, timeMs: Long, settings: ExtractionSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            addLogInternal("📸 Taking single screenshot at ${formatDuration(timeMs)}...")
            val r = MediaMetadataRetriever()
            var bitmap: Bitmap? = null
            try {
                r.setDataSource(context, video.uri)
                val option = if (settings.isPrecise) {
                    MediaMetadataRetriever.OPTION_CLOSEST
                } else {
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                }
                bitmap = r.getFrameAtTime(timeMs * 1000L, option)
            } catch (e: Exception) {
                addLogInternal("Error taking screenshot: ${e.localizedMessage}")
            } finally {
                try { r.release() } catch (ex: Exception) {}
            }

            if (bitmap != null) {
                val originalName = video.name.substringBeforeLast(".")
                val totalVideoFrames = ((video.durationMs * video.fps / 1000.0) + 0.5).toLong().coerceAtLeast(1L)
                val padLength = totalVideoFrames.toString().length.coerceAtLeast(5)
                val absoluteFrame = ((timeMs * video.fps / 1000.0) + 0.5).toLong().coerceAtLeast(1L)
                val frameStr = String.format("%0${padLength}d", absoluteFrame)

                val destFileName = if (settings.prefix.isNotEmpty()) {
                    "${settings.prefix}_screenshot_$frameStr"
                } else {
                    var processedName = originalName
                    val namePattern = Regex("^([^-]+)-(post|reels|tagged|igtv|stories|highlights|clip|video)(\\\\s+\\\\[[^\\\\]]+\\\\])?-(.*)$", RegexOption.IGNORE_CASE)
                    val matchResult = namePattern.matchEntire(originalName)
                    if (matchResult != null) {
                        val dirName = matchResult.groupValues[1]
                        val bracketInfo = matchResult.groupValues[3]
                        val fileNamePart = matchResult.groupValues[4]
                        processedName = "$dirName-screenshot$bracketInfo-$fileNamePart"
                    } else {
                        processedName = originalName.replace(" ", "_")
                    }
                    "${processedName}_screenshot_$frameStr"
                }

                val nativeLocation = getVideoNativeLocation(context, video.uri)
                val volumeName = if (settings.customDirectory.isNotBlank()) {
                    getVolumeNameForPath(context, settings.customDirectory)
                } else {
                    nativeLocation.volumeName
                }
                val videoRelativePath = if (settings.customDirectory.isNotBlank()) {
                    getCustomRelativePath(settings.customDirectory)
                } else {
                    nativeLocation.relativePath
                }
                val targetDirectoryName = if (settings.customDirectory.isNotBlank()) {
                    getCustomDirectoryDisplayName(context, settings.customDirectory)
                } else {
                    nativeLocation.directoryName
                }

                val savedUri = if (settings.customDirectory.startsWith("content://")) {
                    saveFrameToDocumentUri(
                        context = context,
                        bitmap = bitmap,
                        fileName = "$destFileName.${settings.format.name.lowercase()}",
                        format = settings.format,
                        quality = settings.jpegQuality,
                        treeUriString = settings.customDirectory,
                        overrideFramesSameName = settings.overrideFramesSameName
                    )
                } else {
                    saveFrameToMediaStore(
                        context = context,
                        bitmap = bitmap,
                        fileName = destFileName,
                        format = settings.format,
                        quality = settings.jpegQuality,
                        relativePath = videoRelativePath,
                        volumeName = volumeName,
                        overrideFramesSameName = settings.overrideFramesSameName
                    )
                }

                if (savedUri == Uri.parse("content://skipped")) {
                    addLogInternal("Skipped: '${destFileName}.${settings.format.name.lowercase()}' already exists (Skip setting is active).")
                } else if (savedUri != null) {
                    val photoSize = getUriSize(context, savedUri)
                    val savedFrame = SavedFrame(
                        uri = savedUri,
                        name = "$destFileName.${settings.format.name.lowercase()}",
                        timestampMs = timeMs,
                        videoName = video.name
                    )
                    withContext(Dispatchers.Main) {
                        _extractedFrames.add(0, savedFrame)
                    }
                    val savedPhotoEntry = SavedPhotoEntry(
                        uriString = savedUri.toString(),
                        fileName = "$destFileName.${settings.format.name.lowercase()}",
                        timestampMs = timeMs,
                        videoName = video.name,
                        sizeBytes = photoSize,
                        directoryName = targetDirectoryName.ifBlank { extractDirectoryName(context, savedUri) }
                    )
                    recordSavedPhoto(context, savedPhotoEntry)
                    addLogInternal("✅ Screenshot saved: ${savedFrame.name} (${formatSize(photoSize)})")
                } else {
                    addLogInternal("❌ Failed to save screenshot to MediaStore.")
                }
            } else {
                addLogInternal("❌ Failed to extract frame bitmap.")
            }
        }
    }

    fun startExtraction(context: Context, settings: ExtractionSettings) {
        if (!SettingsPersistence.isAdFree(context)) {
            AdManager.loadInterstitialAd(context.applicationContext)
        }
        if (selectedVideos.isEmpty()) {
            _logs.add("❌ FATAL ERROR: No videos selected to process.")
            return
        }

        isExtractionPaused = false
        extractionJob = viewModelScope.launch(Dispatchers.IO) {
            extractionStatus = ExtractionStatus.Processing(
                videoIndex = 1,
                totalVideos = selectedVideos.size,
                currentFrame = 0,
                expectedFrames = null,
                currentVideoName = selectedVideos[0].name,
                progress = 0f
            )

            addLogInternal("🚀 Starting frame extraction from ${selectedVideos.size} ${if (selectedVideos.size == 1) "video" else "videos"}...")
            addLogInternal("Interval: ${if (settings.rateType == RateType.Interval) "${settings.intervalAmount} ${settings.intervalUnit.name.lowercase()}" else "${settings.fps} FPS"}")
            addLogInternal("Format: ${settings.format.name.uppercase()} (Quality: ${settings.jpegQuality}%)")
            addLogInternal("Precision: ${if (settings.isPrecise) "Precise (OPTION_CLOSEST)" else "Fast (OPTION_CLOSEST_SYNC)"}")

            val totalExtractedCount = AtomicInteger(0)
            val startTime = System.currentTimeMillis()
            val totalPhotosSizeBytes = java.util.concurrent.atomic.AtomicLong(0L)
            val sourceVideosTotalSize = selectedVideos.sumOf { it.sizeBytes }

            for ((idx, video) in selectedVideos.withIndex()) {
                if (!isActive) break
                val currentVideoIdx = idx + 1
                addLogInternal("[$currentVideoIdx/${selectedVideos.size}] Extracting from '${video.name}'...")

                val nativeLocation = getVideoNativeLocation(context, video.uri)
                val volumeName = if (settings.customDirectory.isNotBlank()) {
                    getVolumeNameForPath(context, settings.customDirectory)
                } else {
                    nativeLocation.volumeName
                }

                val videoRelativePath = if (settings.customDirectory.isNotBlank()) {
                    getCustomRelativePath(settings.customDirectory)
                } else {
                    nativeLocation.relativePath
                }
                val targetDirectoryName = if (settings.customDirectory.isNotBlank()) {
                    getCustomDirectoryDisplayName(context, settings.customDirectory)
                } else {
                    nativeLocation.directoryName
                }
                addLogInternal("Target folder: $videoRelativePath on volume: $volumeName (Directory: $targetDirectoryName)")

                // Calculate interval in milliseconds
                val intervalMs = when (settings.rateType) {
                    RateType.Interval -> {
                        when (settings.intervalUnit) {
                            IntervalUnit.Ms -> settings.intervalAmount
                            IntervalUnit.S -> settings.intervalAmount * 1000.0
                            IntervalUnit.M -> settings.intervalAmount * 1000.0 * 60.0
                        }
                    }
                    RateType.Fps -> 1000.0 / settings.fps
                }.toLong()

                if (intervalMs <= 0L) {
                    addLogInternal("Error: Interval evaluated to <= 0ms. Skipping video.")
                    continue
                }

                val durationMs = video.durationMs
                val activeStartMs = if (selectedVideos.size == 1 && settings.startMs >= 0) settings.startMs else 0L
                val activeEndMs = if (selectedVideos.size == 1 && settings.endMs > 0) settings.endMs else durationMs
                val activeDurationMs = (activeEndMs - activeStartMs).coerceAtLeast(0L)
                val expectedFramesCount = if (activeDurationMs > 0) (activeDurationMs / intervalMs).toInt() + 1 else null

                val rangeLabel = if (selectedVideos.size == 1) {
                    " [Range: ${formatDuration(activeStartMs)} - ${formatDuration(activeEndMs)}]"
                } else ""
                addLogInternal("Duration: ${formatDuration(durationMs)}$rangeLabel | Expected: ${expectedFramesCount ?: "Dynamic"}")

                try {
                    val targetOffsets = mutableListOf<Long>()
                    var attemptIndex = 0
                    while (true) {
                        val currentOffsetMs = if (settings.rateType == RateType.Fps) {
                            activeStartMs + (attemptIndex * 1000.0 / settings.fps).toLong()
                        } else {
                            activeStartMs + attemptIndex * intervalMs
                        }
                        if (currentOffsetMs > activeEndMs) break
                        targetOffsets.add(currentOffsetMs)
                        attemptIndex++
                    }

                    val totalExpected = targetOffsets.size
                    val frameIndex = AtomicInteger(0)
                    val baseCreationTimeMs = System.currentTimeMillis()

                    coroutineScope {
                        if (settings.forceSequentialRendering) {
                            addLogInternal("⚡ Pipeline mode: Strict Sequential Rendering active (Gallery Order Preserved)")
                            for ((idx, currentOffsetMs) in targetOffsets.withIndex()) {
                                while (isExtractionPaused && isActive) {
                                    delay(200)
                                }
                                if (!isActive) break

                                val r = MediaMetadataRetriever()
                                var bitmap: Bitmap? = null
                                try {
                                    r.setDataSource(context, video.uri)
                                    val timeUs = currentOffsetMs * 1000L
                                    val option = if (settings.isPrecise) {
                                        MediaMetadataRetriever.OPTION_CLOSEST
                                    } else {
                                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                                    }
                                    bitmap = r.getFrameAtTime(timeUs, option)
                                } catch (e: Exception) {
                                    // Ignore frame extraction error
                                } finally {
                                    try { r.release() } catch (ex: Exception) {}
                                }

                                if (bitmap != null) {
                                    val originalName = video.name.substringBeforeLast(".")
                                    
                                    val totalVideoFrames = ((video.durationMs * video.fps / 1000.0) + 0.5).toLong().coerceAtLeast(1L)
                                    val padLength = totalVideoFrames.toString().length.coerceAtLeast(5)
                                    val absoluteFrame = ((currentOffsetMs * video.fps / 1000.0) + 0.5).toLong().coerceAtLeast(1L)
                                    val frameStr = String.format("%0${padLength}d", absoluteFrame)
                                    
                                    val destFileName = if (settings.prefix.isNotEmpty()) {
                                        "${settings.prefix}_$frameStr"
                                    } else {
                                        var processedName = originalName
                                        val namePattern = Regex("^([^-]+)-(post|reels|tagged|igtv|stories|highlights|clip|video)(\\\\s+\\\\[[^\\\\]]+\\\\])?-(.*)$", RegexOption.IGNORE_CASE)
                                        val matchResult = namePattern.matchEntire(originalName)
                                        if (matchResult != null) {
                                            val dirName = matchResult.groupValues[1]
                                            val bracketInfo = matchResult.groupValues[3]
                                            val fileNamePart = matchResult.groupValues[4]
                                            processedName = "$dirName-screenshot$bracketInfo-$fileNamePart"
                                        } else {
                                            processedName = originalName.replace(" ", "_")
                                        }
                                        "${processedName}_$frameStr"
                                    }

                                    val sequentialTimestamp = baseCreationTimeMs + (idx * 100L)

                                    val savedUri = if (settings.customDirectory.startsWith("content://")) {
                                        saveFrameToDocumentUri(
                                            context = context,
                                            bitmap = bitmap,
                                            fileName = "$destFileName.${settings.format.name.lowercase()}",
                                            format = settings.format,
                                            quality = settings.jpegQuality,
                                            treeUriString = settings.customDirectory,
                                            overrideFramesSameName = settings.overrideFramesSameName,
                                            creationTimestampMs = sequentialTimestamp
                                        )
                                    } else {
                                        saveFrameToMediaStore(
                                            context = context,
                                            bitmap = bitmap,
                                            fileName = destFileName,
                                            format = settings.format,
                                            quality = settings.jpegQuality,
                                            relativePath = videoRelativePath,
                                            volumeName = volumeName,
                                            overrideFramesSameName = settings.overrideFramesSameName,
                                            creationTimestampMs = sequentialTimestamp
                                        )
                                    }

                                    if (savedUri == Uri.parse("content://skipped")) {
                                        val currentFrameCount = frameIndex.incrementAndGet()
                                        if (currentFrameCount % 10 == 0 || currentFrameCount == 1 || currentOffsetMs + intervalMs > activeEndMs) {
                                            addLogInternal("Skipped: '${destFileName}.${settings.format.name.lowercase()}' already exists.")
                                        }
                                    } else if (savedUri != null) {
                                        val photoSize = getUriSize(context, savedUri)
                                        totalPhotosSizeBytes.addAndGet(photoSize)
                                        val currentFrameCount = frameIndex.incrementAndGet()
                                        totalExtractedCount.incrementAndGet()

                                        val savedFrame = SavedFrame(
                                            uri = savedUri,
                                            name = "$destFileName.${settings.format.name.lowercase()}",
                                            timestampMs = currentOffsetMs,
                                            videoName = video.name
                                        )

                                        withContext(Dispatchers.Main) {
                                            _extractedFrames.add(0, savedFrame)
                                        }

                                        val savedPhotoEntry = SavedPhotoEntry(
                                            uriString = savedUri.toString(),
                                            fileName = "$destFileName.${settings.format.name.lowercase()}",
                                            timestampMs = currentOffsetMs,
                                            videoName = video.name,
                                            sizeBytes = photoSize,
                                            directoryName = targetDirectoryName.ifBlank { extractDirectoryName(context, savedUri) }
                                        )
                                        recordSavedPhoto(context, savedPhotoEntry)

                                        if (currentFrameCount % 5 == 0 || currentFrameCount == 1 || currentOffsetMs + intervalMs > activeEndMs) {
                                            addLogInternal("Saved [Sequential]: ${savedFrame.name}")
                                        }
                                    }
                                }

                                val currentFrameCount = frameIndex.get()
                                val currentVideoProgress = if (activeDurationMs > 0) {
                                    ((currentOffsetMs - activeStartMs).toFloat() / activeDurationMs.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                                val overallProgress = (currentVideoIdx - 1).toFloat() / selectedVideos.size +
                                        (currentVideoProgress / selectedVideos.size)

                                extractionStatus = ExtractionStatus.Processing(
                                    videoIndex = currentVideoIdx,
                                    totalVideos = selectedVideos.size,
                                    currentFrame = currentFrameCount,
                                    expectedFrames = totalExpected,
                                    currentVideoName = video.name,
                                    progress = overallProgress.coerceIn(0f, 1f)
                                )
                            }
                        } else {
                            val semaphore = Semaphore(settings.maxThreads.coerceAtLeast(1))
                            val jobs = targetOffsets.map { currentOffsetMs ->
                                async(Dispatchers.IO) {
                                    semaphore.withPermit {
                                        while (isExtractionPaused && isActive) {
                                            delay(200)
                                        }
                                        if (!isActive) return@async
                                        
                                        val r = MediaMetadataRetriever()
                                        var bitmap: Bitmap? = null
                                        try {
                                            r.setDataSource(context, video.uri)
                                            val timeUs = currentOffsetMs * 1000L
                                            val option = if (settings.isPrecise) {
                                                MediaMetadataRetriever.OPTION_CLOSEST
                                            } else {
                                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                                            }
                                            bitmap = r.getFrameAtTime(timeUs, option)
                                        } catch (e: Exception) {
                                            // Ignore frame extraction error
                                        } finally {
                                            try { r.release() } catch (ex: Exception) {}
                                        }

                                        if (bitmap != null) {
                                            val originalName = video.name.substringBeforeLast(".")
                                            
                                            val totalVideoFrames = ((video.durationMs * video.fps / 1000.0) + 0.5).toLong().coerceAtLeast(1L)
                                            val padLength = totalVideoFrames.toString().length.coerceAtLeast(5)
                                            val absoluteFrame = ((currentOffsetMs * video.fps / 1000.0) + 0.5).toLong().coerceAtLeast(1L)
                                            val frameStr = String.format("%0${padLength}d", absoluteFrame)
                                            
                                            val destFileName = if (settings.prefix.isNotEmpty()) {
                                                "${settings.prefix}_$frameStr"
                                            } else {
                                                var processedName = originalName
                                                val namePattern = Regex("^([^-]+)-(post|reels|tagged|igtv|stories|highlights|clip|video)(\\\\s+\\\\[[^\\\\]]+\\\\])?-(.*)$", RegexOption.IGNORE_CASE)
                                                val matchResult = namePattern.matchEntire(originalName)
                                                if (matchResult != null) {
                                                    val dirName = matchResult.groupValues[1]
                                                    val bracketInfo = matchResult.groupValues[3]
                                                    val fileNamePart = matchResult.groupValues[4]
                                                    processedName = "$dirName-screenshot$bracketInfo-$fileNamePart"
                                                } else {
                                                    processedName = originalName.replace(" ", "_")
                                                }
                                                "${processedName}_$frameStr"
                                            }

                                            val savedUri = if (settings.customDirectory.startsWith("content://")) {
                                                saveFrameToDocumentUri(
                                                    context = context,
                                                    bitmap = bitmap,
                                                    fileName = "$destFileName.${settings.format.name.lowercase()}",
                                                    format = settings.format,
                                                    quality = settings.jpegQuality,
                                                    treeUriString = settings.customDirectory,
                                                    overrideFramesSameName = settings.overrideFramesSameName
                                                )
                                            } else {
                                                saveFrameToMediaStore(
                                                    context = context,
                                                    bitmap = bitmap,
                                                    fileName = destFileName,
                                                    format = settings.format,
                                                    quality = settings.jpegQuality,
                                                    relativePath = videoRelativePath,
                                                    volumeName = volumeName,
                                                    overrideFramesSameName = settings.overrideFramesSameName
                                                )
                                            }

                                            if (savedUri == Uri.parse("content://skipped")) {
                                                val currentFrameCount = frameIndex.incrementAndGet()
                                                if (currentFrameCount % 10 == 0 || currentFrameCount == 1 || currentOffsetMs + intervalMs > activeEndMs) {
                                                    addLogInternal("Skipped: '${destFileName}.${settings.format.name.lowercase()}' already exists.")
                                                }
                                            } else if (savedUri != null) {
                                                val photoSize = getUriSize(context, savedUri)
                                                totalPhotosSizeBytes.addAndGet(photoSize)
                                                val currentFrameCount = frameIndex.incrementAndGet()
                                                totalExtractedCount.incrementAndGet()

                                                val savedFrame = SavedFrame(
                                                    uri = savedUri,
                                                    name = "$destFileName.${settings.format.name.lowercase()}",
                                                    timestampMs = currentOffsetMs,
                                                    videoName = video.name
                                                )

                                                withContext(Dispatchers.Main) {
                                                    _extractedFrames.add(0, savedFrame)
                                                }

                                                val savedPhotoEntry = SavedPhotoEntry(
                                                    uriString = savedUri.toString(),
                                                    fileName = "$destFileName.${settings.format.name.lowercase()}",
                                                    timestampMs = currentOffsetMs,
                                                    videoName = video.name,
                                                    sizeBytes = photoSize,
                                                    directoryName = targetDirectoryName.ifBlank { extractDirectoryName(context, savedUri) }
                                                )
                                                recordSavedPhoto(context, savedPhotoEntry)

                                                if (currentFrameCount % 5 == 0 || currentFrameCount == 1 || currentOffsetMs + intervalMs > activeEndMs) {
                                                    addLogInternal("Saved: ${savedFrame.name}")
                                                }
                                            }
                                        }

                                        val currentFrameCount = frameIndex.get()
                                        val currentVideoProgress = if (activeDurationMs > 0) {
                                            ((currentOffsetMs - activeStartMs).toFloat() / activeDurationMs.toFloat()).coerceIn(0f, 1f)
                                        } else {
                                            0f
                                        }
                                        val overallProgress = (currentVideoIdx - 1).toFloat() / selectedVideos.size +
                                                (currentVideoProgress / selectedVideos.size)

                                        extractionStatus = ExtractionStatus.Processing(
                                            videoIndex = currentVideoIdx,
                                            totalVideos = selectedVideos.size,
                                            currentFrame = currentFrameCount,
                                            expectedFrames = totalExpected,
                                            currentVideoName = video.name,
                                            progress = overallProgress.coerceIn(0f, 1f)
                                        )
                                    }
                                }
                            }
                            jobs.awaitAll()
                        }
                    }
                } catch (e: Exception) {
                    addLogInternal("❌ FATAL ERROR: Failed extracting frames from ${video.name}: ${e.localizedMessage}")
                }
            }

            val totalTimeSec = (System.currentTimeMillis() - startTime) / 1000.0
            if (isActive) {
                extractionStatus = ExtractionStatus.Success(totalExtractedCount.get(), if (settings.customDirectory.isNotBlank()) "Pictures/${settings.customDirectory}" else "respective folders")
                addLogInternal("✅ EXTRACTION COMPLETED SUCCESSFULLY!")
                addLogInternal("Captured ${totalExtractedCount.get()} ${if (totalExtractedCount.get() == 1) "frame" else "frames"} in ${String.format("%.1f", totalTimeSec)}s.")
                addLogInternal("Outputs saved directly to their respective directories (or custom directory override).")
                addLogInternal("These photos are instantly discoverable by Gallery, Google Photos, or CX File Explorer.")

                addLogInternal("📊 Storage Impact Report:")
                val formattedPhotosSize = formatSize(totalPhotosSizeBytes.get())
                addLogInternal("📸 Added from Photos: $formattedPhotosSize")

                if (settings.deleteAfterSuccess) {
                    val formattedVideosSize = formatSize(sourceVideosTotalSize)
                    val diffBytes = sourceVideosTotalSize - totalPhotosSizeBytes.get()
                    val formattedDiff = formatSize(Math.abs(diffBytes))
                    val diffSign = if (diffBytes >= 0) "freed" else "increase"
                    addLogInternal("🧹 ${if (selectedVideos.size == 1) "Video" else "Videos"} Deleted: $formattedVideosSize")
                    addLogInternal("💾 Net Storage Saved: $formattedDiff $diffSign")
                } else {
                    val formattedVideosSize = formatSize(sourceVideosTotalSize)
                    addLogInternal("ℹ️ Source ${if (selectedVideos.size == 1) "Video" else "Videos"} Size: $formattedVideosSize (kept on device)")
                    val potentialSavedBytes = sourceVideosTotalSize - totalPhotosSizeBytes.get()
                    val formattedPotential = formatSize(Math.abs(potentialSavedBytes))
                    if (potentialSavedBytes >= 0) {
                        addLogInternal("💡 Enabling 'Delete Source after Extraction' would have freed $formattedPotential of space.")
                    } else {
                        addLogInternal("💡 Extracted ${if (totalExtractedCount.get() == 1) "frame takes" else "frames take"} up $formattedPotential more space than the source ${if (selectedVideos.size == 1) "video" else "videos"}.")
                    }
                }

                val urisToDelete = selectedVideos.map { resolveToMediaStoreVideoUri(context, it.uri) }
                withContext(Dispatchers.Main) {
                    selectedVideos = emptyList()
                    appContext?.let { ctx ->
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                PipelineRepository.getInstance(ctx).clearPipeline()
                            } catch (e: Exception) {}
                        }
                    }
                    if (settings.deleteAfterSuccess) {
                        addLogInternal("🧹 Post-processing: Requesting permission to delete source video files...")
                        pendingDeletes = urisToDelete
                    }
                }
            } else {
                extractionStatus = ExtractionStatus.Idle
            }
        }
    }

    private fun getVideoDetails(context: Context, uri: Uri): VideoItem? {
        val contentResolver = context.contentResolver
        var name = "Unknown_Video.mp4"
        var size = 0L

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex) ?: name
                    }
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val retriever = MediaMetadataRetriever()
        var durationMs = 0L
        var thumbnail: Bitmap? = null
        var parsedFps = 30.0

        try {
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durationStr?.toLongOrNull() ?: 0L

            val thumbTimeUs = if (durationMs > 1000L) 1000L * 1000L else (durationMs / 2) * 1000L

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                thumbnail = retriever.getScaledFrameAtTime(thumbTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 128, 128)
            } else {
                val raw = retriever.getFrameAtTime(thumbTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (raw != null) {
                    thumbnail = Bitmap.createScaledBitmap(raw, 128, 128, false)
                }
            }

            // Extract FPS
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val frameCountStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                    val frameCount = frameCountStr?.toIntOrNull() ?: 0
                    if (frameCount > 0 && durationMs > 0L) {
                        parsedFps = (frameCount.toDouble() / (durationMs / 1000.0)).coerceIn(1.0, 120.0)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val captureFpsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                    val captureFps = captureFpsStr?.toDoubleOrNull()
                    if (captureFps != null && captureFps > 0.0) {
                        parsedFps = captureFps
                    }
                }
            } catch (e: Exception) {}
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {}
        }

        return VideoItem(
            id = uri.toString(),
            uri = uri,
            name = name,
            durationMs = durationMs,
            sizeBytes = size,
            thumbnail = thumbnail,
            fps = parsedFps
        )
    }

    private fun getCustomRelativePath(customDir: String): String {
        val trimmed = customDir.trim().trim('/')
        if (trimmed.isEmpty()) {
            return "Pictures/v2p"
        }
        val firstSegment = trimmed.split('/').firstOrNull()?.lowercase()
        if (firstSegment != "pictures" && firstSegment != "dcim" && firstSegment != "download") {
            return "Pictures/$trimmed"
        }
        return trimmed
    }

    private fun getVideoDataPath(context: Context, videoUri: Uri): String? {
        var dataPath: String? = null
        try {
            val projection = arrayOf(MediaStore.Video.Media.DATA)
            context.contentResolver.query(videoUri, projection, null, null, null)?.use { cursor ->
                val dataIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                if (cursor.moveToFirst() && dataIndex != -1) {
                    dataPath = cursor.getString(dataIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dataPath
    }

    private fun resolveToMediaStoreVideoUri(context: Context, uri: Uri): Uri {
        if (uri.authority == "media") {
            return uri
        }
        
        // Try checking DocumentProvider first
        try {
            if (android.provider.DocumentsContract.isDocumentUri(context, uri)) {
                val docId = android.provider.DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                if (split.size == 2) {
                    val type = split[0]
                    val id = split[1].toLongOrNull()
                    if (id != null && (type == "video" || type == "movie")) {
                        return android.content.ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Try querying the MediaStore table for a matching file size or file path or display name
        try {
            var size = 0L
            var name = ""
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.SIZE, android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                    if (nameIdx != -1) name = cursor.getString(nameIdx) ?: ""
                }
            }
            if (size > 0 && name.isNotEmpty()) {
                val projection = arrayOf(MediaStore.Video.Media._ID)
                val selection = "${MediaStore.Video.Media.SIZE} = ? AND ${MediaStore.Video.Media.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(size.toString(), name)
                context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                        if (idIndex != -1) {
                            val id = cursor.getLong(idIndex)
                            return android.content.ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // fallback to query _id directly on the Uri
        try {
            val projection = arrayOf(MediaStore.Video.Media._ID)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                    if (idIndex != -1) {
                        val id = cursor.getLong(idIndex)
                        return android.content.ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return uri
    }

    private fun findExistingMediaStoreUri(context: Context, displayNames: List<String>, relativePath: String): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection: String
        val selectionArgs: Array<String>
        
        if (Build.VERSION.SDK_INT >= 29) {
            selection = "${MediaStore.Images.Media.DISPLAY_NAME} IN (${displayNames.joinToString { "?" }}) AND ${MediaStore.Images.Media.RELATIVE_PATH} = ?"
            selectionArgs = (displayNames + relativePath).toTypedArray()
        } else {
            selection = "${MediaStore.Images.Media.DISPLAY_NAME} IN (${displayNames.joinToString { "?" }})"
            selectionArgs = displayNames.toTypedArray()
        }
        
        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIdx = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    if (idIdx != -1) {
                        val id = cursor.getLong(idIdx)
                        return android.content.ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun saveFrameToMediaStore(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        format: OutputFormat,
        quality: Int,
        relativePath: String,
        volumeName: String = MediaStore.VOLUME_EXTERNAL_PRIMARY,
        overrideFramesSameName: Boolean = true,
        creationTimestampMs: Long? = null
    ): Uri? {
        val contentResolver = context.contentResolver
        val mimeType = if (format == OutputFormat.Jpeg) "image/jpeg" else "image/png"
        val compressFormat = if (format == OutputFormat.Jpeg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG

        val ext = if (format == OutputFormat.Jpeg) "jpg" else "png"
        val displayNames = if (format == OutputFormat.Jpeg) {
            listOf("$fileName.jpg", "$fileName.jpeg", fileName)
        } else {
            listOf("$fileName.png", fileName)
        }

        val existingUri = findExistingMediaStoreUri(context, displayNames, relativePath)
        if (existingUri != null) {
            if (!overrideFramesSameName) {
                return Uri.parse("content://skipped")
            }
            // Try to delete existing so we get a clean insert and update MediaStore indexing
            try {
                contentResolver.delete(existingUri, null, null)
            } catch (e: Exception) {
                // Fallback: directly write to existingUri
                try {
                    contentResolver.openOutputStream(existingUri, "rwt")?.use { out ->
                        bitmap.compress(compressFormat, quality, out)
                    }
                    return existingUri
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            if (creationTimestampMs != null) {
                put(MediaStore.Images.Media.DATE_TAKEN, creationTimestampMs)
                put(MediaStore.Images.Media.DATE_ADDED, creationTimestampMs / 1000L)
                put(MediaStore.Images.Media.DATE_MODIFIED, creationTimestampMs / 1000L)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        var imageUri: Uri? = null
        try {
            val insertUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && volumeName.isNotBlank() && volumeName != MediaStore.VOLUME_EXTERNAL_PRIMARY) {
                try {
                    MediaStore.Images.Media.getContentUri(volumeName)
                } catch (e: Exception) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            imageUri = contentResolver.insert(insertUri, contentValues)
            if (imageUri == null && insertUri != MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            }

            if (imageUri != null) {
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(compressFormat, quality, outputStream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    if (creationTimestampMs != null) {
                        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, creationTimestampMs)
                        contentValues.put(MediaStore.Images.Media.DATE_ADDED, creationTimestampMs / 1000L)
                        contentValues.put(MediaStore.Images.Media.DATE_MODIFIED, creationTimestampMs / 1000L)
                    }
                    contentResolver.update(imageUri, contentValues, null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (imageUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    if (creationTimestampMs != null) {
                        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, creationTimestampMs)
                        contentValues.put(MediaStore.Images.Media.DATE_ADDED, creationTimestampMs / 1000L)
                        contentValues.put(MediaStore.Images.Media.DATE_MODIFIED, creationTimestampMs / 1000L)
                    }
                    contentResolver.update(imageUri, contentValues, null, null)
                } catch (ex: Exception) {}
            }
            return null
        }
        return imageUri
    }

    private fun saveFrameToDocumentUri(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        format: OutputFormat,
        quality: Int,
        treeUriString: String,
        overrideFramesSameName: Boolean = true,
        creationTimestampMs: Long? = null
    ): Uri? {
        try {
            val treeUri = Uri.parse(treeUriString)
            val pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri) ?: return null
            val mimeType = if (format == OutputFormat.Jpeg) "image/jpeg" else "image/png"
            val compressFormat = if (format == OutputFormat.Jpeg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG
            
            var file = pickedDir.findFile(fileName)
            if (file != null) {
                if (!overrideFramesSameName) {
                    return Uri.parse("content://skipped")
                }
            } else {
                file = pickedDir.createFile(mimeType, fileName) ?: return null
            }
            
            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                bitmap.compress(compressFormat, quality, out)
            }
            return file.uri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

fun roundIntervalAmount(amount: Double, unit: IntervalUnit): Double {
    return when (unit) {
        IntervalUnit.Ms -> {
            val rounded = (Math.round(amount / 5.0) * 5.0).coerceIn(10.0, 1000.0)
            (Math.round(rounded * 10.0) / 10.0)
        }
        IntervalUnit.S, IntervalUnit.M -> {
            (Math.round(amount * 10.0) / 10.0).coerceIn(0.1, 60.0)
        }
    }
}

fun roundFps(fps: Double, maxFps: Double? = null): Double {
    val upperLimit = (maxFps ?: 150.0).coerceAtLeast(0.1)
    return (Math.round(fps * 10.0) / 10.0).coerceIn(0.1, upperLimit)
}

object SettingsPersistence {
    private const val PREFS_NAME = "v2p_settings"
    
    fun getPersistentEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("persistent_config_enabled", true)
    }
    
    fun setPersistentEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("persistent_config_enabled", enabled).apply()
    }
    
    fun getAutoSaveEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_save_defaults_enabled", true)
    }
    
    fun setAutoSaveEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_save_defaults_enabled", enabled).apply()
    }
    
    fun getConsecutiveNoAd(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("consecutive_no_ad", 0)
    }
    
    fun setConsecutiveNoAd(context: Context, count: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("consecutive_no_ad", count).apply()
    }

    fun getSingleVideoExtractionCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("single_video_extractions_count", prefs.getInt("consecutive_no_ad", 0))
    }

    fun setSingleVideoExtractionCount(context: Context, count: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("single_video_extractions_count", count).apply()
    }

    fun incrementSingleVideoExtractionCount(context: Context) {
        val current = getSingleVideoExtractionCount(context)
        setSingleVideoExtractionCount(context, current + 1)
    }

    fun resetSingleVideoExtractionCount(context: Context) {
        setSingleVideoExtractionCount(context, 0)
    }

    private val CONSENT_USER_ID_REGEX = Regex("^[0-9]{4}-[a-zA-Z0-9]{8}-[0-9]{2}-[a-zA-Z0-9]{8}-[0-9]{3}$")

    fun generateConsentUserId(): String {
        val random = java.security.SecureRandom()
        val digits = "0123456789"
        val alphanum = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        fun randomChars(source: String, length: Int): String {
            val sb = java.lang.StringBuilder(length)
            for (i in 0 until length) {
                sb.append(source[random.nextInt(source.length)])
            }
            return sb.toString()
        }

        val part1 = randomChars(digits, 4)       // 4 digits
        val part2 = randomChars(alphanum, 8)     // 8 alphanumeric
        val part3 = randomChars(digits, 2)       // 2 digits
        val part4 = randomChars(alphanum, 8)     // 8 alphanumeric
        val part5 = randomChars(digits, 3)       // 3 digits

        return "$part1-$part2-$part3-$part4-$part5"
    }

    fun isValidConsentUserId(id: String?): Boolean {
        if (id.isNullOrEmpty()) return false
        return CONSENT_USER_ID_REGEX.matches(id)
    }

    fun getUserId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var userId = prefs.getString("custom_user_id", null)
        if (userId.isNullOrEmpty() || !isValidConsentUserId(userId)) {
            userId = generateConsentUserId()
            prefs.edit().putString("custom_user_id", userId).apply()
        }
        return userId
    }

    fun isAdFree(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("is_ad_free", false)
    }

    fun setAdFree(context: Context, adFree: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_ad_free", adFree).apply()
        FirebaseUserDatabase.syncUserData(context)
    }

    fun isAccountLinked(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("is_account_linked", false)
    }

    fun getLinkedAccountEmail(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("linked_account_email", "") ?: ""
    }

    fun linkAccount(context: Context, email: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_account_linked", true)
            .putString("linked_account_email", email)
            .putBoolean("is_ad_free", true)
            .apply()
        FirebaseUserDatabase.syncUserData(context)
    }

    fun unlinkAccount(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_account_linked", false)
            .putString("linked_account_email", "")
            .putBoolean("is_ad_free", false)
            .apply()
        FirebaseUserDatabase.syncUserData(context)
    }

    fun loadSettings(context: Context): ExtractionSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val persistent = prefs.getBoolean("persistent_config_enabled", true)
        if (!persistent) {
            return ExtractionSettings()
        }
        
        val unit = IntervalUnit.valueOf(prefs.getString("interval_unit", IntervalUnit.Ms.name) ?: IntervalUnit.Ms.name)
        val amount = prefs.getFloat("interval_amount", 250f).toDouble()
        val fpsVal = prefs.getFloat("fps", 5f).toDouble()
        
        return ExtractionSettings(
            rateType = RateType.valueOf(prefs.getString("rate_type", RateType.Interval.name) ?: RateType.Interval.name),
            intervalAmount = roundIntervalAmount(amount, unit),
            intervalUnit = unit,
            fps = roundFps(fpsVal),
            isPrecise = prefs.getBoolean("is_precise", true),
            format = OutputFormat.valueOf(prefs.getString("format", OutputFormat.Jpeg.name) ?: OutputFormat.Jpeg.name),
            jpegQuality = prefs.getInt("jpeg_quality", 90),
            prefix = prefs.getString("prefix", "") ?: "",
            deleteAfterSuccess = prefs.getBoolean("delete_after_success", false),
            customDirectory = prefs.getString("custom_directory", "") ?: "",
            startMs = prefs.getLong("start_ms", 0L),
            endMs = prefs.getLong("end_ms", -1L),
            playAudioByDefault = prefs.getBoolean("play_audio_by_default", false),
            maxThreads = prefs.getInt("max_threads", 4),
            overrideFramesSameName = prefs.getBoolean("override_frames_same_name", true),
            forceSequentialRendering = prefs.getBoolean("force_sequential_rendering", false),
            galleryIsGridView = prefs.getBoolean("gallery_is_grid_list_view", false),
            galleryGroupMode = try {
                GalleryGroupMode.valueOf(prefs.getString("gallery_group_mode", GalleryGroupMode.DIRECTORY.name) ?: GalleryGroupMode.DIRECTORY.name)
            } catch (_: Exception) {
                GalleryGroupMode.DIRECTORY
            },
            gallerySortOption = try {
                GallerySortOption.valueOf(prefs.getString("gallery_sort_option", GallerySortOption.DATE_EXTRACTED.name) ?: GallerySortOption.DATE_EXTRACTED.name)
            } catch (_: Exception) {
                GallerySortOption.DATE_EXTRACTED
            },
            gallerySortDirection = try {
                GallerySortDirection.valueOf(prefs.getString("gallery_sort_direction", GallerySortDirection.DESCENDING.name) ?: GallerySortDirection.DESCENDING.name)
            } catch (_: Exception) {
                GallerySortDirection.DESCENDING
            }
        )
    }
    
    fun saveSettings(context: Context, settings: ExtractionSettings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val autoSave = prefs.getBoolean("auto_save_defaults_enabled", true)
        if (!autoSave) return
        
        prefs.edit().apply {
            putString("rate_type", settings.rateType.name)
            putFloat("interval_amount", roundIntervalAmount(settings.intervalAmount, settings.intervalUnit).toFloat())
            putString("interval_unit", settings.intervalUnit.name)
            putFloat("fps", roundFps(settings.fps).toFloat())
            putBoolean("is_precise", settings.isPrecise)
            putString("format", settings.format.name)
            putInt("jpeg_quality", settings.jpegQuality)
            putString("prefix", settings.prefix)
            putBoolean("delete_after_success", settings.deleteAfterSuccess)
            putString("custom_directory", settings.customDirectory)
            putLong("start_ms", settings.startMs)
            putLong("end_ms", settings.endMs)
            putBoolean("play_audio_by_default", settings.playAudioByDefault)
            putInt("max_threads", settings.maxThreads)
            putBoolean("override_frames_same_name", settings.overrideFramesSameName)
            putBoolean("force_sequential_rendering", settings.forceSequentialRendering)
            putBoolean("gallery_is_grid_list_view", settings.galleryIsGridView)
            putString("gallery_group_mode", settings.galleryGroupMode.name)
            putString("gallery_sort_option", settings.gallerySortOption.name)
            putString("gallery_sort_direction", settings.gallerySortDirection.name)
            apply()
        }
        PhotoCacheManager.saveSettingsCache(context, settings)
    }

    fun getGalleryIsGridListView(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val persistent = prefs.getBoolean("persistent_config_enabled", true)
        if (!persistent) return false
        return prefs.getBoolean("gallery_is_grid_list_view", false)
    }

    fun setGalleryIsGridListView(context: Context, isGrid: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val autoSave = prefs.getBoolean("auto_save_defaults_enabled", true)
        if (!autoSave) return
        prefs.edit().putBoolean("gallery_is_grid_list_view", isGrid).apply()
    }

    fun getGalleryGroupMode(context: Context): GalleryGroupMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val persistent = prefs.getBoolean("persistent_config_enabled", true)
        if (!persistent) return GalleryGroupMode.DIRECTORY
        val name = prefs.getString("gallery_group_mode", GalleryGroupMode.DIRECTORY.name) ?: GalleryGroupMode.DIRECTORY.name
        return try {
            GalleryGroupMode.valueOf(name)
        } catch (_: Exception) {
            GalleryGroupMode.DIRECTORY
        }
    }

    fun setGalleryGroupMode(context: Context, mode: GalleryGroupMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val autoSave = prefs.getBoolean("auto_save_defaults_enabled", true)
        if (!autoSave) return
        prefs.edit().putString("gallery_group_mode", mode.name).apply()
    }

    fun getGallerySortOption(context: Context): GallerySortOption {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val persistent = prefs.getBoolean("persistent_config_enabled", true)
        if (!persistent) return GallerySortOption.DATE_EXTRACTED
        val name = prefs.getString("gallery_sort_option", GallerySortOption.DATE_EXTRACTED.name) ?: GallerySortOption.DATE_EXTRACTED.name
        return try {
            GallerySortOption.valueOf(name)
        } catch (_: Exception) {
            GallerySortOption.DATE_EXTRACTED
        }
    }

    fun setGallerySortOption(context: Context, option: GallerySortOption) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val autoSave = prefs.getBoolean("auto_save_defaults_enabled", true)
        if (!autoSave) return
        prefs.edit().putString("gallery_sort_option", option.name).apply()
    }

    fun getGallerySortDirection(context: Context): GallerySortDirection {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val persistent = prefs.getBoolean("persistent_config_enabled", true)
        if (!persistent) return GallerySortDirection.DESCENDING
        val name = prefs.getString("gallery_sort_direction", GallerySortDirection.DESCENDING.name) ?: GallerySortDirection.DESCENDING.name
        return try {
            GallerySortDirection.valueOf(name)
        } catch (_: Exception) {
            GallerySortDirection.DESCENDING
        }
    }

    fun setGallerySortDirection(context: Context, direction: GallerySortDirection) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val autoSave = prefs.getBoolean("auto_save_defaults_enabled", true)
        if (!autoSave) return
        prefs.edit().putString("gallery_sort_direction", direction.name).apply()
    }

    fun getLastStateGalleryCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("last_state_gallery_count", 0).coerceAtLeast(0)
    }

    fun setLastStateGalleryCount(context: Context, count: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("last_state_gallery_count", count.coerceAtLeast(0)).apply()
    }

    fun getHasSeenGalleryTutorial(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("has_seen_gallery_tutorial", false)
    }

    fun setHasSeenGalleryTutorial(context: Context, seen: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("has_seen_gallery_tutorial", seen).apply()
    }

    fun getDeletePhotosPermanently(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("delete_photos_permanently", false)
    }

    fun setDeletePhotosPermanently(context: Context, permanently: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("delete_photos_permanently", permanently).apply()
    }

    fun saveConsentStatus(
        context: Context,
        consentStatus: String,
        canRequestAds: Boolean,
        isPrivacyRequired: Boolean
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("consent_status", consentStatus)
            .putBoolean("can_request_ads", canRequestAds)
            .putBoolean("is_privacy_options_required", isPrivacyRequired)
            .putLong("consent_recorded_timestamp", System.currentTimeMillis())
            .apply()
    }

    fun getConsentStatus(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("consent_status", "UNKNOWN") ?: "UNKNOWN"
    }

    fun getCanRequestAds(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("can_request_ads", false)
    }

    fun shouldShowRatingDialog(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val permanentlyIgnored = prefs.getBoolean("rating_permanently_ignored", false)
        if (permanentlyIgnored) return false

        val hasRated = prefs.getBoolean("rating_completed", false)
        if (hasRated) return false

        val maybeLaterTime = prefs.getLong("rating_maybe_later_timestamp", 0L)
        if (maybeLaterTime > 0) {
            val elapsed = System.currentTimeMillis() - maybeLaterTime
            val twentyFourHoursMs = 24 * 60 * 60 * 1000L
            if (elapsed < twentyFourHoursMs) {
                return false
            }
        }
        return true
    }

    fun setRatingPermanentlyIgnored(context: Context, ignored: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("rating_permanently_ignored", ignored).apply()
    }

    fun setRatingCompleted(context: Context, completed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("rating_completed", completed).apply()
    }

    fun setRatingMaybeLater(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong("rating_maybe_later_timestamp", System.currentTimeMillis()).apply()
    }

    fun getRatingMaybeLaterTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("rating_maybe_later_timestamp", 0L)
    }

    fun isRatingPermanentlyIgnored(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("rating_permanently_ignored", false)
    }

    fun isRatingCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("rating_completed", false)
    }

    fun resetRatingStatus(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove("rating_permanently_ignored")
            .remove("rating_completed")
            .remove("rating_maybe_later_timestamp")
            .apply()
    }
}

object AppVersionInfo {
    val versionDisplayString: String
        get() = "ver ${AppConfig.versionNumber}"

    data class VersionLog(
        val version: String,
        val releaseDate: String,
        val summary: String,
        val changes: List<String>
    )

    val changelog: List<VersionLog> = listOf(
        VersionLog(
            version = "ver 1.1.5 (Build 8)",
            releaseDate = "August 2026",
            summary = "Upgraded to Google Play Billing Library version 9.0.0, updated versionCode to 8 (versionName 1.1.5), and ensured compliance with Google Play requirements.",
            changes = listOf(
                "Upgraded Google Play Billing Library to version 9.0.0 (meets the August 2026 requirement for Play Billing 8.0.0+).",
                "Updated build configuration to Version 1.1.5 (versionCode 8).",
                "Verified asynchronous in-app purchase verification and license query pipeline compatibility.",
                "Maintained full native debug symbol generation and Gradle 9.3.1 toolchain build optimization."
            )
        ),
        VersionLog(
            version = "ver 1.1.4 (Build 7)",
            releaseDate = "August 2026",
            summary = "Google Play Console release build with Native Debug Symbols, embedded RSA Licensing public key, and Gradle 9.3.1 toolchain upgrade.",
            changes = listOf(
                "Updated build configuration to Version 1.1.4 (versionCode 7).",
                "Enabled full native debug symbol generation (ndk.debugSymbolLevel = FULL) for enhanced Play Console crash and ANR analytics.",
                "Embedded Google Play Licensing RSA Public Key in AppConfig for purchase and license verification.",
                "Upgraded Gradle wrapper and build configuration to Gradle 9.3.1.",
                "Resolved Play Billing KTX dependency references and optimized release signing fallback."
            )
        ),
        VersionLog(
            version = "ver 1.1.3",
            releaseDate = "August 2026",
            summary = "Internal testing build with AdMob Application ID setup, Google Account licensing state controls, and modular code architecture.",
            changes = listOf(
                "Updated build configuration to version 1.1.3 (versionCode 4) for internal testing readiness.",
                "Configured Google Advertising AD_ID permission and standard AdMob sample Application ID in AndroidManifest.xml.",
                "Implemented Link/Unlink Google Account licensing controls with ad-free status toggling and purchase revocation simulation.",
                "Added active linked device status display (1 / 5 Devices) backed by Google Play Store verification.",
                "Modularized architecture by extracting Workspace dashboard and video processing views into dedicated Workspace.kt component.",
                "Optimized app initialization performance, memory usage, and UI layout responsiveness."
            )
        ),
        VersionLog(
            version = "ver 1.1",
            releaseDate = "August 2026",
            summary = "Major feature & UX overhaul introducing Samsung Gallery gestures, toolbar minimization, collapsible search, directory grid view, and modular Settings.",
            changes = listOf(
                "Added Samsung Gallery-style multi-selection mode with long-press and swipe gesture support.",
                "Added persistent bottom action bar displaying total selected items and cumulative file size.",
                "Added minimizable Pipeline Extractor bottom toolbar with rotatable arrow toggle button.",
                "Added animated collapsible Gallery search bar in header with instant clear button.",
                "Added Directory and Video Grid / Details List view toggle for folder browsing.",
                "Updated Gallery tab header with formatted compact count notation (e.g. 12.3k, 1.2M).",
                "Dedicated Force Sequential Frame Rendering subcontainer in Settings with clean divider styling.",
                "Moved Version Footer to bottom of screen above extraction toolbar.",
                "Added 'Delete Photos Permanently?' toggle in Settings supporting automatic 3-day App Trash auto-deletion.",
                "Added persistent state tracking for gallery item count across sessions.",
                "Added scrollable Update Log / Changelog popup dialog with version history dropdown selector.",
                "Patches & Bug Fixes: Enhanced pinch-to-zoom grid columns, memory cleanup, and coroutine lifecycle safety."
            )
        ),
        VersionLog(
            version = "ver 1.0",
            releaseDate = "July 2026",
            summary = "Initial release of Video2Photos frame extraction suite.",
            changes = listOf(
                "High-speed video frame extraction supporting MP4, MOV, MKV, and WEBM formats.",
                "Configurable extraction rates (interval in ms/sec/frames and FPS mode) with precise seeking.",
                "Local photo gallery with directory, video, and image grouping.",
                "Ad-supported pipeline with optional ad-free upgrade."
            )
        )
    )
}

object OfflineAdCache {
    private val cachedBannerTitles = listOf(
        "⚡ Ultra-Fast Local GPU Extractor Pro",
        "🚀 4K 120FPS Frame Capture Engine",
        "🔒 100% On-Device Offline Privacy Guard",
        "🎞️ Precision Millisecond Frame Seeker"
    )
    private val cachedBannerSubtitles = listOf(
        "Offline Cached Ad • Zero Data Usage",
        "Preloaded Creative • Local Storage Active",
        "Airplane Mode Ad Cache • Instant Load",
        "Local Banner Cache • Smooth Offline Experience"
    )

    fun initialize(context: Context) {}

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    fun getCachedBanner(isLarge: Boolean): Pair<String, String> {
        return if (isLarge) {
            Pair("⚡ Ultra-Fast Local GPU Extractor Pro", "Preloaded Creative • Airplane Mode Ad Cache Active")
        } else {
            Pair("Video To Pics Pro", "Offline Cached Creative • Zero Data Usage")
        }
    }
}

object AdBlockerVpnDetector {
    private val knownAdFilterPackages = listOf(
        "dk.p2p.dnsfilter",
        "org.adguard.android",
        "org.blokada.alarm",
        "com.free.adblocker.browser",
        "com.hsv.freeadblockerbrowser",
        "net.openvpn.openvpn",
        "com.wireguard.android"
    )

    fun isAdBlockerOrVpnActive(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            if (activeNetwork != null) {
                val caps = cm.getNetworkCapabilities(activeNetwork)
                if (caps != null && caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN)) {
                    return true
                }
            }

            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces != null && interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                if (ni.isUp) {
                    val name = ni.name.lowercase()
                    if (name.contains("tun") || name.contains("p2p") || name.contains("ppp") || name.contains("tap")) {
                        return true
                    }
                }
            }

            val pm = context.packageManager
            for (pkg in knownAdFilterPackages) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    return true
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}

enum class AppScreen {
    Main, Settings
}

fun formatTabCount(count: Int): String {
    if (count < 0) return "0"
    return when {
        count < 10_000 -> java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(count)
        count < 100_000 -> {
            val valK = count / 1000.0
            val formatted = String.format(java.util.Locale.US, "%.2f", valK).replace(Regex("\\.?0+$"), "")
            "${formatted}k"
        }
        count < 1_000_000 -> {
            val valK = count / 1000.0
            val formatted = String.format(java.util.Locale.US, "%.1f", valK).replace(Regex("\\.?0+$"), "")
            "${formatted}k"
        }
        count < 10_000_000 -> {
            val valM = count / 1_000_000.0
            val formatted = String.format(java.util.Locale.US, "%.2f", valM).replace(Regex("\\.?0+$"), "")
            "${formatted}M"
        }
        count < 100_000_000 -> {
            val valM = count / 1_000_000.0
            val formatted = String.format(java.util.Locale.US, "%.1f", valM).replace(Regex("\\.?0+$"), "")
            "${formatted}M"
        }
        else -> {
            val valM = count / 1_000_000.0
            val formatted = String.format(java.util.Locale.US, "%.0f", valM)
            "${formatted}M"
        }
    }
}

// Composables UI
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun V2pAppScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val mainListState = rememberLazyListState()
    
    var showFallbackAdDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.extractionStatus) {
        if (viewModel.extractionStatus is ExtractionStatus.Success) {
            if (SettingsPersistence.shouldShowRatingDialog(context)) {
                showRatingDialog = true
            }
        }
    }
    var currentScreen by remember { mutableStateOf(AppScreen.Main) }
    
    var hasPhotosVideosPermission by remember { mutableStateOf(false) }
    var hasWriteStoragePermission by remember { mutableStateOf(false) }

    val updatePermissionStates: () -> Unit = {
        hasPhotosVideosPermission = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        hasWriteStoragePermission = if (Build.VERSION.SDK_INT >= 29) {
            true
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initPipelineQueue(context)
        updatePermissionStates()
    }

    var persistentEnabled by remember { mutableStateOf(SettingsPersistence.getPersistentEnabled(context)) }
    var autoSaveEnabled by remember { mutableStateOf(SettingsPersistence.getAutoSaveEnabled(context)) }
    
    var isAdFree by remember { mutableStateOf(SettingsPersistence.isAdFree(context)) }
    var userId by remember { mutableStateOf(SettingsPersistence.getUserId(context)) }
    var isAccountLinked by remember { mutableStateOf(SettingsPersistence.isAccountLinked(context)) }
    var linkedAccountEmail by remember { mutableStateOf(SettingsPersistence.getLinkedAccountEmail(context)) }
    var showAdBlockerVpnDialog by remember { mutableStateOf(false) }
    var showGoogleAccountLinkDialog by remember { mutableStateOf(false) }

    var settings by remember { mutableStateOf(SettingsPersistence.loadSettings(context)) }
    var pendingExtractionAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showAbortConfirmDialog by remember { mutableStateOf(false) }
    var showAdFreeUpgradeDialog by remember { mutableStateOf(false) }
    var isTopAdUpgradeDismissed by remember { mutableStateOf(false) }
    var isBottomAdUpgradeDismissed by remember { mutableStateOf(false) }
    var mainTab by remember { mutableIntStateOf(0) } // 0 = Workspace, 1 = Gallery
    var isToolbarMinimized by remember { mutableStateOf(false) }
    
    val updateSettings: (ExtractionSettings) -> Unit = { newSettings ->
        settings = newSettings
        if (autoSaveEnabled) {
            SettingsPersistence.saveSettings(context, newSettings)
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        updatePermissionStates()
        val allGranted = results.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "Storage and media permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Some permissions were denied. You can select a Custom Folder for SD card saving.", Toast.LENGTH_LONG).show()
        }
    }

    val sdCardTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                
                updateSettings(settings.copy(customDirectory = uri.toString()))
                Toast.makeText(context, "SD Card/Custom Directory successfully registered!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to persist folder permissions: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // Intercept system back press when on Settings screen to navigate back to main screen
    androidx.activity.compose.BackHandler(enabled = currentScreen == AppScreen.Settings) {
        currentScreen = AppScreen.Main
    }

    if (showAbortConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showAbortConfirmDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Abort Frame Extraction?",
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F),
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = { showAbortConfirmDialog = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = CoolGrey, modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Text(
                    "Are you sure you want to end the pipeline extraction process? Saved photo frames will remain intact.",
                    color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAbortConfirmDialog = false
                        viewModel.cancelExtraction()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
                ) {
                    Text("Yes, End Pipeline", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAbortConfirmDialog = false }) {
                    Text("No, Continue", color = CoolGrey)
                }
            },
            containerColor = if (ThemeConfig.isDarkTheme) TerminalBg else Color(0xFFF4F4F4)
        )
    }

    if (showAdFreeUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showAdFreeUpgradeDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (AppConfig.adFreeUpgradeTestMode) "V2P Ad-Free Pro (Test Mode)" else "V2P Ad-Free Pro",
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F),
                            fontSize = 18.sp
                        )
                    }
                    IconButton(
                        onClick = { showAdFreeUpgradeDialog = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = CoolGrey, modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Column {
                    if (AppConfig.adFreeUpgradeTestMode) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = WarningAmber.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "🧪 TEST MODE ACTIVE: Simulate purchase instantly without real charges.",
                                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "Unlock the ultimate frame extraction experience with a single lifetime purchase:",
                        fontSize = 13.sp,
                        color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val features = listOf(
                        "🚫 100% Banner Ad Free",
                        "⚡ No Full-Screen Interstitial Video Ads",
                        "🚀 Maximum Extraction & Processing Speed",
                        "♾️ Unlimited Batch Processing & Lifetime Updates",
                        "🛡️ Tied to Google Play Account (Protects against device format/loss)"
                    )
                    features.forEach { feature ->
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeConfig.isDarkTheme) TextLight else Color(0xFF1C1B1F),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            SettingsPersistence.setAdFree(context, true)
                            isAdFree = true
                            showAdFreeUpgradeDialog = false
                            val msg = if (AppConfig.adFreeUpgradeTestMode) {
                                "🎉 Welcome to V2P Ad-Free Pro! (Test Mode Purchase Activated)"
                            } else {
                                "🎉 Welcome to V2P Ad-Free Pro! Ads & upgrade banners removed."
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (AppConfig.adFreeUpgradeTestMode) "SIMULATE PURCHASE (TEST MODE) — $2.99" else "GET AD-FREE FOR $2.99",
                            color = Color(0xFF12181F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            showGoogleAccountLinkDialog = true
                        },
                        border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAccountLinked) "LINKED: $linkedAccountEmail" else "🔗 LINK ACTIVE GOOGLE ACCOUNT",
                            color = TechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Verifying Google Play purchase...", Toast.LENGTH_SHORT).show()
                            V2pBillingManager.queryPurchasesAsync(context) { verified ->
                                isAdFree = verified
                                if (verified) {
                                    showAdFreeUpgradeDialog = false
                                    Toast.makeText(context, "✅ Purchase verified via Google Play Store! Ad-Free Pro active.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "No active Ad-Free purchase found on this Google Play account.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VERIFY / RESTORE PURCHASE (GOOGLE PLAY)",
                            color = TechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = null,
            containerColor = if (ThemeConfig.isDarkTheme) TerminalBg else Color(0xFFF4F4F4)
        )
    }

    if (showGoogleAccountLinkDialog) {
        val userEmail = if (linkedAccountEmail.isNotEmpty()) linkedAccountEmail else "ExperienceIt12@gmail.com"
        AlertDialog(
            onDismissRequest = { showGoogleAccountLinkDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Link Google Account",
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F),
                            fontSize = 18.sp
                        )
                    }
                    IconButton(
                        onClick = { showGoogleAccountLinkDialog = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = CoolGrey, modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Column {
                    Text(
                        text = "Link your V2P license to your active Google Account so your ad-free status is never lost during phone upgrades or resets.",
                        fontSize = 13.sp,
                        color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = TechCyan.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Active Google Play Account:",
                                fontSize = 11.sp,
                                color = CoolGrey
                            )
                            Text(
                                text = userEmail,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Custom Encrypted User ID: $userId",
                                fontSize = 10.sp,
                                color = TechCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🛡️ License Protection & Anti-Piracy Guard:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val securityInfos = listOf(
                        "• Entitlement Token signed by Google Play Licensing API.",
                        "• Valid for simultaneous personal activation on up to 5 devices.",
                        "• Session Guard automatically flags and revokes license keys shared publicly across multiple external networks."
                    )
                    securityInfos.forEach { info ->
                        Text(
                            text = info,
                            fontSize = 10.sp,
                            color = CoolGrey,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    var showPublicKeyDetail by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPublicKeyDetail = !showPublicKeyDetail }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Play Console Public Key (RSA)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (showPublicKeyDetail) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (showPublicKeyDetail) {
                        Text(
                            text = V2pBillingManager.GOOGLE_PLAY_LICENSING_KEY,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CoolGrey.copy(alpha = 0.8f),
                            lineHeight = 11.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isAccountLinked) {
                        Button(
                            onClick = {
                                SettingsPersistence.unlinkAccount(context)
                                isAccountLinked = false
                                linkedAccountEmail = ""
                                isAdFree = false
                                showGoogleAccountLinkDialog = false
                                Toast.makeText(context, "⚠️ License Unlinked! Ad-Free Pro purchase revoked.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeleteRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("UNLINK GOOGLE ACCOUNT (REVOKE PURCHASE)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                SettingsPersistence.linkAccount(context, userEmail)
                                isAccountLinked = true
                                linkedAccountEmail = userEmail
                                isAdFree = true
                                showGoogleAccountLinkDialog = false
                                Toast.makeText(context, "✅ License Linked to $userEmail! Ad-Free Pro active.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LINK TO $userEmail", color = Color(0xFF12181F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (userEmail.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isAccountLinked) "Active Linked Devices for $userEmail: 1 / 5 Devices (Verified via Google Play)" else "Linked Devices for $userEmail: 0 / 5 Devices",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TechCyan,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedButton(
                        onClick = { showGoogleAccountLinkDialog = false },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Cancel", color = CoolGrey, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = null,
            containerColor = if (ThemeConfig.isDarkTheme) TerminalBg else Color(0xFFF4F4F4)
        )
    }

    if (showAdBlockerVpnDialog) {
        AlertDialog(
            onDismissRequest = { showAdBlockerVpnDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ad-Blocker / VPN Active",
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F),
                            fontSize = 17.sp
                        )
                    }
                    IconButton(
                        onClick = { showAdBlockerVpnDialog = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = CoolGrey, modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Column {
                    Text(
                        text = "An active ad-blocking VPN or DNS filter (such as personalDNSfilter or AdGuard) was detected on your device.",
                        fontSize = 13.sp,
                        color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "To keep Video to Pics free, the ad-tier relies on sponsor ads. Please disable your VPN/ad-filter or upgrade to V2P Ad-Free Pro ($2.99) to support development and remove all ads.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            showAdBlockerVpnDialog = false
                            showAdFreeUpgradeDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("UPGRADE TO AD-FREE ($2.99)", color = Color(0xFF12181F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            if (AdBlockerVpnDetector.isAdBlockerOrVpnActive(context)) {
                                Toast.makeText(context, "⚠️ Ad-blocker/VPN is still active. Please disable it or upgrade to Pro.", Toast.LENGTH_LONG).show()
                            } else {
                                showAdBlockerVpnDialog = false
                                Toast.makeText(context, "✅ Ad-blocker check passed! You can proceed.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Re-check Connection", color = CoolGrey, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = null,
            containerColor = if (ThemeConfig.isDarkTheme) TerminalBg else Color(0xFFF4F4F4)
        )
    }

    if (showRatingDialog) {
        RateAndReviewDialog(
            onDismiss = { showRatingDialog = false }
        )
    }

    val totalExpectedFrames = remember(viewModel.selectedVideos, settings) {
        if (viewModel.selectedVideos.isEmpty()) {
            0L
        } else {
            val intervalMs = when (settings.rateType) {
                RateType.Interval -> {
                    when (settings.intervalUnit) {
                        IntervalUnit.Ms -> settings.intervalAmount
                        IntervalUnit.S -> settings.intervalAmount * 1000.0
                        IntervalUnit.M -> settings.intervalAmount * 1000.0 * 60.0
                    }
                }
                RateType.Fps -> 1000.0 / settings.fps
            }.toLong()

            if (intervalMs <= 0L) {
                0L
            } else {
                if (viewModel.selectedVideos.size == 1) {
                    val video = viewModel.selectedVideos.first()
                    val activeStartMs = if (settings.startMs >= 0) settings.startMs else 0L
                    val activeEndMs = if (settings.endMs > 0) settings.endMs else video.durationMs
                    val activeDurationMs = (activeEndMs - activeStartMs).coerceAtLeast(0L)
                    if (activeDurationMs > 0) (activeDurationMs / intervalMs) + 1 else 0L
                } else {
                    var sum = 0L
                    for (video in viewModel.selectedVideos) {
                        val durationMs = video.durationMs
                        if (durationMs > 0L) {
                            sum += (durationMs / intervalMs) + 1
                        }
                    }
                    sum
                }
            }
        }
    }

    if (viewModel.showProcessingDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissProcessingDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info Icon",
                        tint = TechCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Processing in Progress",
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F),
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Text(
                    text = viewModel.showProcessingDialogMessage ?: "",
                    color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissProcessingDialog() }) {
                    Text("OK", color = TechCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (ThemeConfig.isDarkTheme) TerminalBg else Color(0xFFF4F4F4),
            textContentColor = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F),
            titleContentColor = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F)
        )
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val intentData = result.data
            val uris = mutableListOf<Uri>()
            
            // Try clipData first for multiple selections
            val clipData = intentData?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i).uri?.let { uris.add(it) }
                }
            } else {
                intentData?.data?.let { uris.add(it) }
            }
            
            if (uris.isNotEmpty()) {
                viewModel.selectVideos(context, uris)
            }
        }
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.pendingDeletes?.let { deletedUris ->
                viewModel.onVideosDeleted(deletedUris)
            }
        } else {
            viewModel.onVideosDeletionFailed()
        }
    }

    val pendingDeletes = viewModel.pendingDeletes
    LaunchedEffect(pendingDeletes) {
        if (pendingDeletes != null && pendingDeletes.isNotEmpty()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    val pendingIntent = android.provider.MediaStore.createDeleteRequest(
                        context.contentResolver, 
                        pendingDeletes
                    )
                    val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(
                        pendingIntent.intentSender
                    ).build()
                    deleteLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    viewModel.addLog("Error creating delete request: ${e.localizedMessage}")
                    var deletedCount = 0
                    for (uri in pendingDeletes) {
                        try {
                            val deleted = context.contentResolver.delete(uri, null, null)
                            if (deleted > 0) deletedCount++
                        } catch (ex: Exception) {}
                    }
                    if (deletedCount > 0) {
                        viewModel.onVideosDeleted(pendingDeletes)
                    } else {
                        viewModel.onVideosDeletionFailed()
                    }
                }
            } else {
                var deletedCount = 0
                val successfullyDeleted = mutableListOf<Uri>()
                var handledWithException = false
                
                for (uri in pendingDeletes) {
                    try {
                        val deleted = context.contentResolver.delete(uri, null, null)
                        if (deleted > 0) {
                            deletedCount++
                            successfullyDeleted.add(uri)
                        }
                    } catch (securityException: SecurityException) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            val recoverableSecurityException = securityException as? android.app.RecoverableSecurityException
                            if (recoverableSecurityException != null) {
                                try {
                                    val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(
                                        recoverableSecurityException.userAction.actionIntent.intentSender
                                    ).build()
                                    deleteLauncher.launch(intentSenderRequest)
                                    handledWithException = true
                                    break
                                } catch (ex: Exception) {}
                            }
                        }
                    } catch (e: Exception) {}
                }
                
                if (!handledWithException) {
                    if (deletedCount > 0) {
                        viewModel.onVideosDeleted(successfullyDeleted)
                    } else {
                        viewModel.onVideosDeletionFailed()
                    }
                }
            }
        }
    }

    val selectedVideos = viewModel.selectedVideos
    LaunchedEffect(selectedVideos) {
        if (selectedVideos.size == 1) {
            val duration = selectedVideos.first().durationMs
            settings = settings.copy(startMs = 0L, endMs = duration)
        } else {
            settings = settings.copy(startMs = 0L, endMs = -1L)
        }
    }

    Column(
        modifier = modifier
            .background(DarkSlateBg)
            .fillMaxSize()
    ) {
        // App Header Toolbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp)),
            shape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon_yellow_1783684646197),
                    contentDescription = "V2P Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSlate, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "V2P Frame Grabber",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "High-fidelity media frame pipeline",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = CoolGrey
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1.0f))

                // Settings button replacing Local Directory with overlayed Test Mode badge
                Box(contentAlignment = Alignment.TopCenter) {
                    Row(
                        modifier = Modifier
                            .background(TechCyan.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .border(1.dp, TechCyan.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                            .clickable { currentScreen = if (currentScreen == AppScreen.Settings) AppScreen.Main else AppScreen.Settings }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = TechCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SETTINGS",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechCyan,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    if (TestSettings.isTestMode) {
                        Text(
                            text = "Test Mode",
                            style = TextStyle(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700),
                                letterSpacing = 0.3.sp,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.8f),
                                    offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 12.dp)
                        )
                    }
                }
            }
        }

        if (currentScreen == AppScreen.Settings) {
            SettingsScreen(
                settings = settings,
                onSettingsChanged = updateSettings,
                persistentEnabled = persistentEnabled,
                onPersistentEnabledChanged = { isChecked ->
                    persistentEnabled = isChecked
                    SettingsPersistence.setPersistentEnabled(context, isChecked)
                },
                autoSaveEnabled = autoSaveEnabled,
                onAutoSaveEnabledChanged = { isChecked ->
                    autoSaveEnabled = isChecked
                    SettingsPersistence.setAutoSaveEnabled(context, isChecked)
                    if (isChecked) {
                        SettingsPersistence.saveSettings(context, settings)
                    }
                },
                hasPhotosVideosPermission = hasPhotosVideosPermission,
                hasWriteStoragePermission = hasWriteStoragePermission,
                onRequestPermissions = {
                    val permissionsToRequest = if (android.os.Build.VERSION.SDK_INT >= 33) {
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO
                        )
                    } else if (android.os.Build.VERSION.SDK_INT >= 29) {
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    } else {
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                    permissionsLauncher.launch(permissionsToRequest)
                },
                onSelectCustomDirectory = {
                    try {
                        sdCardTreeLauncher.launch(null)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Storage Access Framework is not supported.", Toast.LENGTH_SHORT).show()
                    }
                },
                onUpgradeAdFree = { showAdFreeUpgradeDialog = true },
                onLinkAccountClick = { showGoogleAccountLinkDialog = true },
                isAdFree = isAdFree,
                userId = userId,
                isAccountLinked = isAccountLinked,
                linkedAccountEmail = linkedAccountEmail,
                onBack = { currentScreen = AppScreen.Main }
            )
        } else {
            // Workspace / Gallery Mode Selector Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlateCard,
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mainTab = 0 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Dashboard,
                                contentDescription = null,
                                tint = if (mainTab == 0) TechCyan else CoolGrey,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WORKSPACE",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mainTab == 0) TechCyan else CoolGrey,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mainTab = 1 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                tint = if (mainTab == 1) TechCyan else CoolGrey,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GALLERY (${formatTabCount(viewModel.savedPhotos.size)})",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mainTab == 1) TechCyan else CoolGrey,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount < -40) {
                                mainTab = 1
                            } else if (dragAmount > 40) {
                                mainTab = 0
                            }
                        }
                    }
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = mainTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(durationMillis = 120)) { width -> width } + fadeIn(animationSpec = tween(durationMillis = 120)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(durationMillis = 120)) { width -> -width } + fadeOut(animationSpec = tween(durationMillis = 120)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(durationMillis = 120)) { width -> -width } + fadeIn(animationSpec = tween(durationMillis = 120)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(durationMillis = 120)) { width -> width } + fadeOut(animationSpec = tween(durationMillis = 120)))
                        }
                    },
                    label = "WorkspaceGalleryTabTransition"
                ) { targetTab ->
                    if (targetTab == 1) {
                        GalleryScreen(
                            viewModel = viewModel,
                            onBack = { mainTab = 0 }
                        )
                    } else {
                        // Scrollable content dashboard
                        LazyColumn(
                            state = mainListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                if (!isAdFree) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if ((AppConfig.testMode || TestSettings.isTestMode) && !isTopAdUpgradeDismissed) {
                                AdFreeUpgradeBanner(
                                    onUpgradeClick = { showAdFreeUpgradeDialog = true },
                                    onDismissClick = { isTopAdUpgradeDismissed = true },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            MockAdBanner(isLarge = false)
                        }
                    }
                }

            if (!hasPhotosVideosPermission || !hasWriteStoragePermission) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("permission_alert_banner"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Permissions Required",
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Permissions Required",
                                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                )
                                Text(
                                    text = "Grant media and storage access to extract and save screenshots without issues.",
                                    style = TextStyle(fontSize = 11.sp, color = CoolGrey, lineHeight = 15.sp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val permissionsToRequest = if (android.os.Build.VERSION.SDK_INT >= 33) {
                                        arrayOf(
                                            android.Manifest.permission.READ_MEDIA_IMAGES,
                                            android.Manifest.permission.READ_MEDIA_VIDEO
                                        )
                                    } else if (android.os.Build.VERSION.SDK_INT >= 29) {
                                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                    } else {
                                        arrayOf(
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )
                                    }
                                    permissionsLauncher.launch(permissionsToRequest)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                            ) {
                                Text("GRANT", color = TerminalBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            // Source videos card
            item {
                VideoPickerCard(
                    selectedVideos = viewModel.selectedVideos,
                    onPickVideos = {
                        val getContentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "video/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        
                        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
                            type = "video/*"
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        
                        val chooserIntent = Intent.createChooser(getContentIntent, "Select Video File(s)").apply {
                            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
                        }
                        
                        pickerLauncher.launch(chooserIntent)
                    },
                    onClearVideos = { viewModel.clearVideos() },
                    onRemoveVideo = { viewModel.removeVideo(it) }
                )
            }

            // Advanced Extraction settings
            item {
                val maxFps = if (viewModel.selectedVideos.size == 1) {
                    viewModel.selectedVideos.first().fps
                } else {
                    null
                }
                ExtractionSettingsCard(
                    settings = settings,
                    onSettingsChanged = updateSettings,
                    isEnabled = viewModel.extractionStatus !is ExtractionStatus.Processing,
                    maxFps = maxFps
                )
            }

            if (viewModel.selectedVideos.size == 1) {
                item {
                    SingleVideoRangeCard(
                        video = viewModel.selectedVideos.first(),
                        settings = settings,
                        onSettingsChanged = updateSettings,
                        isEnabled = viewModel.extractionStatus !is ExtractionStatus.Processing,
                        onCaptureSingleFrame = { timeMs ->
                            viewModel.captureSingleFrame(context, viewModel.selectedVideos.first(), timeMs, settings)
                        }
                    )
                }
            }

            if (!isAdFree) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if ((AppConfig.testMode || TestSettings.isTestMode) && !isBottomAdUpgradeDismissed) {
                            AdFreeUpgradeBanner(
                                onUpgradeClick = { showAdFreeUpgradeDialog = true },
                                onDismissClick = { isBottomAdUpgradeDismissed = true },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        MockAdBanner(isLarge = true)
                    }
                }
            }

            // Real-time terminal log viewer
            item {
                TerminalLogCard(
                    logs = viewModel.logs,
                    onClearLogs = { viewModel.clearLogs() },
                    mainListState = mainListState
                )
            }

            // Processing and Status Monitor Card
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = viewModel.extractionStatus !is ExtractionStatus.Idle,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    StatusMonitorCard(
                        status = viewModel.extractionStatus,
                        isPaused = viewModel.isExtractionPaused,
                        onPause = { viewModel.pauseExtraction() },
                        onResume = { viewModel.resumeExtraction() },
                        onCancel = { showAbortConfirmDialog = true },
                        onDismiss = { viewModel.resetExtractionStatus() }
                    )
                }
            }

            // Extracted frames preview gallery
            if (viewModel.extractedFrames.isNotEmpty()) {
                item {
                    ExtractedFramesCard(
                        frames = if (viewModel.recentExtractedFrames.isNotEmpty()) viewModel.recentExtractedFrames else viewModel.extractedFrames,
                        onShareFrame = { shareImage(context, it.uri) },
                        onViewFrame = { viewImage(context, it.uri) }
                    )
                }
            }
        }
    }
}
}

        if (mainTab == 0) {
            Text(
                text = AppVersionInfo.versionDisplayString,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CoolGrey.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 4.dp)
            )

            // Footer action bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)),
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                color = SlateCard,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(horizontal = 12.dp, vertical = if (isToolbarMinimized) 6.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val arrowRotation by animateFloatAsState(
                        targetValue = if (isToolbarMinimized) 180f else 0f,
                        label = "arrowRotation"
                    )

                    IconButton(
                        onClick = { isToolbarMinimized = !isToolbarMinimized },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isToolbarMinimized) "Expand Toolbar" else "Minimize Toolbar",
                            tint = TechCyan,
                            modifier = Modifier.graphicsLayer(rotationZ = arrowRotation)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (val size = viewModel.selectedVideos.size) {
                                0 -> "0 Video(s) Selected"
                                1 -> "1 Video Selected"
                                else -> "$size Videos Selected"
                            },
                            style = TextStyle(
                                fontSize = if (isToolbarMinimized) 12.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                        )
                        if (!isToolbarMinimized) {
                            Text(
                                text = "Ready for process pipeline",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = CoolGrey
                                )
                            )
                        }
                    }

                    if (viewModel.extractionStatus !is ExtractionStatus.Processing) {
                        val formattedFrames = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalExpectedFrames)
                        val buttonText = if (viewModel.selectedVideos.isEmpty()) {
                            "EXTRACT FRAMES"
                        } else {
                            if (totalExpectedFrames == 1L) {
                                "EXTRACT 1 FRAME"
                            } else {
                                if (isToolbarMinimized) "EXTRACT $formattedFrames" else "EXTRACT $formattedFrames FRAMES"
                            }
                        }

                        Button(
                            onClick = {
                                if (!isAdFree && AdBlockerVpnDetector.isAdBlockerOrVpnActive(context)) {
                                    showAdBlockerVpnDialog = true
                                    return@Button
                                }

                                val isMultiVideo = viewModel.selectedVideos.size > 1
                                val isSingleVideo = viewModel.selectedVideos.size == 1
                                
                                val singleVideoCount = SettingsPersistence.getSingleVideoExtractionCount(context)
                                val isThirdSingleExtraction = isSingleVideo && singleVideoCount >= 2
                                
                                val triggerAd = !isAdFree && (isMultiVideo || isThirdSingleExtraction)
                                
                                val runExtraction = {
                                    viewModel.startExtraction(context, settings)
                                    if (isSingleVideo) {
                                        if (isThirdSingleExtraction) {
                                            SettingsPersistence.resetSingleVideoExtractionCount(context)
                                        } else {
                                            SettingsPersistence.incrementSingleVideoExtractionCount(context)
                                        }
                                    }
                                }
                                
                                if (triggerAd) {
                                    AdManager.showInterstitialAd(context) { wasAdShown ->
                                        if (wasAdShown) {
                                            runExtraction()
                                        } else {
                                            pendingExtractionAction = runExtraction
                                        }
                                    }
                                } else {
                                    runExtraction()
                                }
                            },
                            modifier = Modifier
                                .testTag("start_extraction_button")
                                .height(if (isToolbarMinimized) 36.dp else 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TechCyan,
                                contentColor = Color(0xFF22282A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = viewModel.selectedVideos.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = if (isToolbarMinimized) 10.dp else 16.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Run", modifier = Modifier.size(if (isToolbarMinimized) 18.dp else 24.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buttonText,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isToolbarMinimized) 10.sp else 11.sp,
                                    letterSpacing = 0.3.sp
                                )
                            )
                        }
                    } else {
                        Button(
                            onClick = { showAbortConfirmDialog = true },
                            modifier = Modifier
                                .testTag("cancel_extraction_button")
                                .height(if (isToolbarMinimized) 36.dp else 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningAmber,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = if (isToolbarMinimized) 10.dp else 16.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = "Cancel", modifier = Modifier.size(if (isToolbarMinimized) 18.dp else 24.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isToolbarMinimized) "ABORT" else "ABORT PIPELINE",
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = if (isToolbarMinimized) 10.sp else 12.sp, letterSpacing = 0.5.sp)
                            )
                        }
                    }
                }
            }
        }
        }
    }

    if (pendingExtractionAction != null) {
        InterstitialAdDialog(
            onAdClosed = {
                val action = pendingExtractionAction
                pendingExtractionAction = null
                action?.invoke()
            }
        )
    }

    if (showFallbackAdDialog) {
        InterstitialAdDialog(
            onAdClosed = {
                showFallbackAdDialog = false
            }
        )
    }
}

