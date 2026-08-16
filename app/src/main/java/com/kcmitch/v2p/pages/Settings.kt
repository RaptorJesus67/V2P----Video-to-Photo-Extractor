package com.kcmitch.v2p.pages

import com.kcmitch.v2p.*
import com.kcmitch.v2p.config.*
import com.kcmitch.v2p.settings.*
import com.kcmitch.v2p.thirdParty.ads.*
import com.kcmitch.v2p.thirdParty.db.*
import com.kcmitch.v2p.pages.workspace.*
import com.kcmitch.v2p.pages.gallery.*
import com.kcmitch.v2p.applets.settings.*
import com.kcmitch.v2p.applets.ads.*
import com.kcmitch.v2p.applets.rating.*
import com.kcmitch.v2p.ui.theme.*

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class SystemStats(
    val cpuModel: String = "",
    val cpuSpeed: String = "",
    val gpuInfo: String = "",
    val coreTemp: String = "",
    val activeThreads: Int = 0,
    val maxCores: Int = 0,
    val ramInfo: String = "",
    val storageInfo: String = "",
    val deviceModel: String = "",
    val osVersion: String = ""
)

fun getDeviceModel(): String {
    val manufacturer = android.os.Build.MANUFACTURER ?: ""
    val model = android.os.Build.MODEL ?: "Generic Android"
    return if (model.startsWith(manufacturer, ignoreCase = true)) {
        model.replaceFirstChar { it.uppercase() }
    } else {
        "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
    }
}

fun getOsVersion(): String {
    val release = android.os.Build.VERSION.RELEASE ?: "Unknown"
    val sdk = android.os.Build.VERSION.SDK_INT
    return "Android $release (API $sdk)"
}

fun getCacheSizeBytes(context: Context): Long {
    var size = 0L
    try {
        context.cacheDir?.walkTopDown()?.forEach { file ->
            if (file.isFile) size += file.length()
        }
        context.externalCacheDir?.walkTopDown()?.forEach { file ->
            if (file.isFile) size += file.length()
        }
    } catch (e: Exception) {}
    return size
}

fun clearAppCache(context: Context) {
    try {
        context.cacheDir?.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
    } catch (e: Exception) {}
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@Composable
fun SettingsScreen(
    settings: ExtractionSettings,
    onSettingsChanged: (ExtractionSettings) -> Unit,
    persistentEnabled: Boolean,
    onPersistentEnabledChanged: (Boolean) -> Unit,
    autoSaveEnabled: Boolean,
    onAutoSaveEnabledChanged: (Boolean) -> Unit,
    hasPhotosVideosPermission: Boolean,
    hasWriteStoragePermission: Boolean,
    onRequestPermissions: () -> Unit,
    onSelectCustomDirectory: () -> Unit,
    onUpgradeAdFree: () -> Unit,
    onLinkAccountClick: () -> Unit,
    isAdFree: Boolean,
    userId: String,
    isAccountLinked: Boolean,
    linkedAccountEmail: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isLinked = isAccountLinked
    val linkedEmail = linkedAccountEmail

    var stats by remember { mutableStateOf(SystemStats()) }
    var deletePhotosPermanently by remember { mutableStateOf(SettingsPersistence.getDeletePhotosPermanently(context)) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    var simulatedRegion by remember { mutableStateOf(TestSettings.selectedRegion) }
    var simulatedLocation by remember { mutableStateOf(TestSettings.currentLocationName) }
    var isRegionDropdownExpanded by remember { mutableStateOf(false) }

    if (showChangelogDialog) {
        ChangelogDialog(onDismiss = { showChangelogDialog = false })
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val cpuModel = getCpuModel()
            val cpuSpeed = getCpuFreq()
            val gpuInfo = getGpuInfo()
            val coreTemp = getCoreTemp(context)
            val activeThreads = Thread.activeCount()
            val maxCores = Runtime.getRuntime().availableProcessors()
            val ramInfo = getRamInfo(context)
            val storageInfo = getStorageInfo()
            val deviceModel = getDeviceModel()
            val osVersion = getOsVersion()

            stats = SystemStats(
                cpuModel = cpuModel,
                cpuSpeed = cpuSpeed,
                gpuInfo = gpuInfo,
                coreTemp = coreTemp,
                activeThreads = activeThreads,
                maxCores = maxCores,
                ramInfo = ramInfo,
                storageInfo = storageInfo,
                deviceModel = deviceModel,
                osVersion = osVersion
            )
            delay(250)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Row: Back Button on Left, Account Tier & User ID on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TechCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Back to Workspace",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TechCyan)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isAdFree) TechCyan.copy(alpha = 0.18f) else WarningAmber.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, if (isAdFree) TechCyan.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isAdFree) "Pro-tier Account" else "Ad-tier Account",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdFree) TechCyan else WarningAmber
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "User ID: $userId",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                )
                if (isLinked && linkedEmail.isNotEmpty()) {
                    Text(
                        text = "Linked: $linkedEmail",
                        style = TextStyle(fontSize = 9.sp, color = CoolGrey)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fixed Option to Upgrade to Ad-Free or Manage Pro (Visible only in Test Mode)
        if (AppConfig.testMode || TestSettings.isTestMode) {
            FixedAdFreeUpgradeCard(
                onUpgradeClick = onUpgradeAdFree,
                onLinkAccountClick = onLinkAccountClick,
                isAdFree = isAdFree,
                isLinked = isLinked,
                linkedEmail = linkedEmail
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!isAdFree) {
            NativeAdvancedAdCard()

            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderSlate)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = TechCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settings",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                        )
                        Text(
                            text = "Automatically Save Settings as the Default",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = CoolGrey
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Beautiful divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSlate)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Feature toggles representation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Persistent Configuration",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Remember custom extraction ranges & intervals",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Switch(
                        checked = persistentEnabled,
                        onCheckedChange = onPersistentEnabledChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalBg,
                            checkedTrackColor = TechCyan,
                            uncheckedThumbColor = CoolGrey,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto-save Defaults",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Changes are saved immediately on modification",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Switch(
                        checked = autoSaveEnabled,
                        onCheckedChange = onAutoSaveEnabledChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalBg,
                            checkedTrackColor = TechCyan,
                            uncheckedThumbColor = CoolGrey,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Play Audio by Default",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Configure whether the video preview starts with audio playing (defaulted to on)",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = settings.playAudioByDefault,
                        onCheckedChange = { isEnabled ->
                            onSettingsChanged(settings.copy(playAudioByDefault = isEnabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalBg,
                            checkedTrackColor = TechCyan,
                            uncheckedThumbColor = CoolGrey,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Mode Theme",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Switch between professional Obsidian Dark and clean Slate Light mode",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = ThemeConfig.isDarkTheme,
                        onCheckedChange = { isDark ->
                            ThemeConfig.setTheme(context, isDark)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalBg,
                            checkedTrackColor = TechCyan,
                            uncheckedThumbColor = CoolGrey,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Photos Permanently Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Delete Photos Permanently?",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Choose whether deleted photos are placed in App Trash (auto-deleted after 3 days) or deleted forever.",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = deletePhotosPermanently,
                        onCheckedChange = { isChecked ->
                            deletePhotosPermanently = isChecked
                            SettingsPersistence.setDeletePhotosPermanently(context, isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalBg,
                            checkedTrackColor = TechCyan,
                            uncheckedThumbColor = CoolGrey,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Max Parallel Threads Configuration Row and Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Max Extraction Threads",
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            )
                            Text(
                                text = "Set maximum parallel workers to accelerate extraction",
                                style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                            )
                        }
                        Text(
                            text = "${settings.maxThreads} Threads",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TechCyan)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.maxThreads.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(maxThreads = it.toInt().coerceIn(1, 16))) },
                        valueRange = 1f..16f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = TechCyan,
                            activeTrackColor = TechCyan,
                            inactiveTrackColor = BorderSlate
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Override Captured Frames with same name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Override Captured Frames with Same Name",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "If enabled, replaces existing screenshots of the same name. If disabled, skips saving without throwing errors.",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = settings.overrideFramesSameName,
                        onCheckedChange = { isEnabled ->
                            onSettingsChanged(settings.copy(overrideFramesSameName = isEnabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalBg,
                            checkedTrackColor = TechCyan,
                            uncheckedThumbColor = CoolGrey,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gallery Default View (Details vs Grid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gallery Grid / Details View",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = if (settings.galleryIsGridView) "Defaulting to Grid View for folder and video browsing" else "Defaulting to Details List View for folder and video browsing",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = settings.galleryIsGridView,
                        onCheckedChange = { isGrid ->
                            onSettingsChanged(settings.copy(galleryIsGridView = isGrid))
                            SettingsPersistence.setGalleryIsGridListView(context, isGrid)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalBg,
                            checkedTrackColor = TechCyan,
                            uncheckedThumbColor = CoolGrey,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gallery Default Group By
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gallery Default Group By",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Choose the initial hierarchy view mode (Folder, Video, or All Images)",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.widthIn(min = 150.dp, max = 210.dp)) {
                        GroupByDropdown(
                            selectedMode = settings.galleryGroupMode,
                            onModeSelected = { mode ->
                                onSettingsChanged(settings.copy(galleryGroupMode = mode))
                                SettingsPersistence.setGalleryGroupMode(context, mode)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gallery Default Sort By
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gallery Default Sort By",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Choose the default sort criterion for gallery media",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.widthIn(min = 150.dp, max = 210.dp)) {
                        SortByDropdown(
                            selectedSort = settings.gallerySortOption,
                            onSortSelected = { option ->
                                onSettingsChanged(settings.copy(gallerySortOption = option))
                                SettingsPersistence.setGallerySortOption(context, option)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderSlate.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Force Sequential Frame Rendering Subcontainer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderSlate)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Force Sequential Frame Rendering",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TechCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "GALLERY SORT ORDER",
                                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TechCyan),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Forces pipeline to create and render images strictly one by one in chronological order. Dedicates full resources to finishing each frame before starting the next, ensuring Date Created and Date Modified in Android photo gallery stay perfectly ordered for carousel swiping.\n\n⚠️ Note: This will slow down overall processing time drastically compared to multi-threaded extraction.",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = settings.forceSequentialRendering,
                        onCheckedChange = { isEnabled ->
                            onSettingsChanged(settings.copy(forceSequentialRendering = isEnabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalBg,
                            checkedTrackColor = TechCyan,
                            uncheckedThumbColor = CoolGrey,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderSlate.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(16.dp))

        // App Cache & Storage Management
        var cacheSizeBytes by remember { mutableLongStateOf(getCacheSizeBytes(context)) }
        var showClearCacheConfirm by remember { mutableStateOf(false) }

        if (showClearCacheConfirm) {
            AlertDialog(
                onDismissRequest = { showClearCacheConfirm = false },
                title = {
                    Text(
                        "Clear Application Cache?",
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F)
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to clear temporary thumbnails and cached data? This will free up storage space without removing any of your extracted photo files.",
                        color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showClearCacheConfirm = false
                            clearAppCache(context)
                            cacheSizeBytes = getCacheSizeBytes(context)
                            Toast.makeText(context, "App cache cleared successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                    ) {
                        Text("Clear Cache", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearCacheConfirm = false }) {
                        Text("Cancel", color = CoolGrey)
                    }
                },
                containerColor = if (ThemeConfig.isDarkTheme) TerminalBg else Color(0xFFF4F4F4)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderSlate)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CleaningServices,
                        contentDescription = "Cache",
                        tint = TechCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "App Storage & Cache",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Manage cached image thumbnails and temporary disk memory",
                            style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CURRENT CACHE DATA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(
                            text = formatSize(cacheSizeBytes),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { showClearCacheConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CLEAR CACHE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Noticeable Line Break with Stats Section
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(TechCyan.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Telemetry Dashboard Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Terminal,
                contentDescription = "Diagnostics",
                tint = TechCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "System Diagnostics & Telemetry",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Real-time hardware metrics (Updating 4x/sec)",
            style = TextStyle(fontSize = 11.sp, color = CoolGrey)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalBg),
            border = BorderStroke(1.dp, BorderSlate.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text("DEVICE MODEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(stats.deviceModel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("CPU CHIPSET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(stats.cpuModel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("CORE TEMP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(stats.coreTemp, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningAmber, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("SYSTEM RAM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(stats.ramInfo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TechCyan, fontFamily = FontFamily.Monospace)
                    }
                    Column(modifier = Modifier.weight(0.9f)) {
                        Text("OS VERSION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(stats.osVersion, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("CPU SPEED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(stats.cpuSpeed, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TechCyan, fontFamily = FontFamily.Monospace, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("GPU BOARD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(stats.gpuInfo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("STORAGE SPACE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text(stats.storageInfo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TechCyan, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSlate.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("JVM ACTIVE THREADS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text("${stats.activeThreads} Threads", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TechCyan, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PHYSICAL CORES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        Text("${stats.maxCores} Cores Available", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().testTag("permissions_management_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderSlate)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FolderShared,
                        contentDescription = "Storage Permissions",
                        tint = TechCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Storage & Permissions",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                        )
                        Text(
                            text = "Manage storage directories, photos/videos permissions, and SD card pipelines",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = CoolGrey
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSlate)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Photos and Videos Permission Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasPhotosVideosPermission) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = if (hasPhotosVideosPermission) "Granted" else "Restricted",
                                tint = if (hasPhotosVideosPermission) TerminalGreen else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Photos & Videos Access",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            )
                        }
                        Text(
                            text = "Required to discover and read video files on your device.",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey),
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                        )
                    }

                    if (!hasPhotosVideosPermission) {
                        Button(
                            onClick = onRequestPermissions,
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                        ) {
                            Text("Grant", color = TechCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("GRANTED", color = TerminalGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Write Storage Permission Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasWriteStoragePermission) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = if (hasWriteStoragePermission) "Granted" else "Restricted",
                                tint = if (hasWriteStoragePermission) TerminalGreen else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Media Storage Write Access",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            )
                        }
                        Text(
                            text = "Allows writing extracted screenshots to system media folders.",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey),
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                        )
                    }

                    if (!hasWriteStoragePermission) {
                        Button(
                            onClick = onRequestPermissions,
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                        ) {
                            Text("Grant", color = TechCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("GRANTED", color = TerminalGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSlate.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Custom SD Card / SAF Tree Folder Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (settings.customDirectory.startsWith("content://")) Icons.Filled.CheckCircle else Icons.Filled.Info,
                            contentDescription = "Custom Storage Status",
                            tint = if (settings.customDirectory.startsWith("content://")) TerminalGreen else CoolGrey,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Direct SD Card / Custom Directory Pipeline",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                    }

                    Text(
                        text = "Highly recommended for external SD cards or when saving screenshots fails due to MediaStore permission limitations. Grants full persistable write access to any folder.",
                        style = TextStyle(fontSize = 11.sp, color = CoolGrey, lineHeight = 15.sp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onSelectCustomDirectory,
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Select SD Card / Folder", color = TerminalBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (settings.customDirectory.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onSettingsChanged(settings.copy(customDirectory = "")) },
                                border = BorderStroke(1.dp, DeleteRed.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeleteRed),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Clear Override", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (settings.customDirectory.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TerminalBg),
                            border = BorderStroke(1.dp, BorderSlate.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "REGISTERED PATH:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TechCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = settings.customDirectory,
                                    fontSize = 10.sp,
                                    color = TerminalGreen,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TEST MODE REGULATORY & LOCATION SIMULATOR (Active only during testMode)
        if (TestSettings.isTestMode || AppConfig.testMode) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = "Test Mode Location",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TEST MODE: LOCATION & REGULATION",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700),
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (simulatedRegion.isGdpr) "GDPR REGIME" else "CCPA / US REGIME",
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Select a simulated location to switch between European GDPR consent and US State Privacy (CCPA) opt-out policies in real time.",
                        style = TextStyle(fontSize = 12.sp, color = CoolGrey, lineHeight = 16.sp),
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                    )

                    // Dropdown Selector Box
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isRegionDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = TerminalBg.copy(alpha = 0.6f),
                                contentColor = TextLight
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = TechCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = simulatedRegion.displayName,
                                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Expand Location Menu",
                                    tint = TechCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isRegionDropdownExpanded,
                            onDismissRequest = { isRegionDropdownExpanded = false },
                            modifier = Modifier
                                .background(SlateCard)
                                .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            TestSettings.TestRegion.values().forEach { region ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = region.displayName,
                                                style = TextStyle(
                                                    fontSize = 13.sp,
                                                    fontWeight = if (region == simulatedRegion) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (region == simulatedRegion) TechCyan else TextLight
                                                )
                                            )
                                            Text(
                                                text = if (region.isGdpr) "European GDPR Consent Flow" else "US CCPA / Global Privacy Flow",
                                                style = TextStyle(fontSize = 10.sp, color = CoolGrey)
                                            )
                                        }
                                    },
                                    onClick = {
                                        isRegionDropdownExpanded = false
                                        val newLocation = TestSettings.selectRegion(region, randomizeLocation = true)
                                        simulatedRegion = region
                                        simulatedLocation = newLocation

                                        // Re-trigger / update UMP consent with new simulated debug geography
                                        val activity = AdManager.findActivity(context)
                                        if (activity != null) {
                                            AdManager.initialize(activity)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (region.isGdpr) Icons.Filled.Security else Icons.Filled.Gavel,
                                            contentDescription = null,
                                            tint = if (region == simulatedRegion) TechCyan else CoolGrey,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location detail badge and re-roll button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TerminalBg.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, BorderSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SIMULATED COUNTRY / SUBREGION:",
                                    style = TextStyle(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TechCyan,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = simulatedLocation,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerminalGreen,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    val randomized = TestSettings.randomizeCurrentLocation()
                                    simulatedLocation = randomized
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = TechCyan.copy(alpha = 0.15f),
                                    contentColor = TechCyan
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Casino,
                                    contentDescription = "Reroll Location",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "REROLL",
                                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Device's own Test Device Hash ID badge & copy action
                    val deviceHashId = remember { TestSettings.getDeviceTestHashedId(context) }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TerminalBg.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, BorderSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "THIS DEVICE'S TEST HASH ID (AUTO-REGISTERED):",
                                        style = TextStyle(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TechCyan,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Text(
                                        text = if (deviceHashId.isNotBlank()) deviceHashId else "EMULATOR / UNKNOWN",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700),
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (deviceHashId.isNotBlank()) {
                                    FilledTonalButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Test Device Hashed ID", deviceHashId)
                                            clipboard?.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied Test Device Hash ID!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFFFD700).copy(alpha = 0.15f),
                                            contentColor = Color(0xFFFFD700)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy Test Device ID",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "COPY",
                                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "This ID is automatically registered with Google UMP ConsentDebugSettings and MobileAds RequestConfiguration.",
                                style = TextStyle(fontSize = 10.sp, color = CoolGrey),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reset consent button to test first-launch popup
                    OutlinedButton(
                        onClick = {
                            val activity = AdManager.findActivity(context)
                            if (activity != null) {
                                val consentManager = GoogleMobileAdsConsentManager.getInstance(activity)
                                consentManager.resetConsent()
                                AdManager.initialize(activity)
                                Toast.makeText(context, "Consent state reset! Re-evaluating consent dialog...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFF6B6B).copy(alpha = 0.1f),
                            contentColor = Color(0xFFFF6B6B)
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset Consent",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset Consent State & Re-Prompt",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset Rating dialog state for testing
                    OutlinedButton(
                        onClick = {
                            SettingsPersistence.resetRatingStatus(context)
                            Toast.makeText(context, "Rate & Review status reset! Will prompt after next extraction.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = TechCyan.copy(alpha = 0.1f),
                            contentColor = TechCyan
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Reset Rating Cooldown",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset Rate & Review Cooldown / Ignored Status",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Footer Row: Version on Left, Update Log links on Right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppVersionInfo.versionDisplayString,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoolGrey
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rate App",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable { openPlayStore(context) }
                    )

                    Text(
                        text = "Update Log",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable { showChangelogDialog = true }
                    )
                }
            }
        }
    }
}
