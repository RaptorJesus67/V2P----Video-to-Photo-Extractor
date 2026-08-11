package com.example

import com.example.ui.theme.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoPickerCard(
    selectedVideos: List<VideoItem>,
    onPickVideos: () -> Unit,
    onClearVideos: () -> Unit,
    onRemoveVideo: (VideoItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.VideoLibrary,
                        contentDescription = "Source Videos",
                        tint = TechCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Choose video source(s)...",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    )
                }

                if (selectedVideos.isNotEmpty()) {
                    TextButton(
                        onClick = onClearVideos,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CLEAR ALL", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSlateBg)
                        .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                        .clickable { onPickVideos() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileUpload,
                            contentDescription = "Select Videos",
                            tint = TechCyan.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TAP TO CHOOSE VIDEO FILES",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechCyan,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Supports Gallery, CX File Explorer, and system drive files",
                            style = TextStyle(fontSize = 11.sp, color = CoolGrey),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedVideos.forEach { video ->
                        VideoItemRow(video = video, onRemove = { onRemoveVideo(video) })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onPickVideos,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateCardHeader),
                        border = BorderStroke(1.dp, BorderSlate),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add More", tint = TechCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("APPEND VIDEO FILES", style = TextStyle(fontWeight = FontWeight.Bold, color = TechCyan, fontSize = 12.sp))
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItemRow(
    video: VideoItem,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSlateBg)
            .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail or static video icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (video.thumbnail != null) {
                androidx.compose.foundation.Image(
                    bitmap = video.thumbnail.asImageBitmap(),
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Movie,
                    contentDescription = "Video",
                    tint = TechCyan.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.name,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Duration: ${formatDuration(video.durationMs)}",
                    style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                )
                Text(
                    text = "Size: ${formatSize(video.sizeBytes)}",
                    style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove",
                tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ExtractionSettingsCard(
    settings: ExtractionSettings,
    onSettingsChanged: (ExtractionSettings) -> Unit,
    isEnabled: Boolean,
    maxFps: Double? = null
) {
    var expandedAdvanced by remember { mutableStateOf(false) }
    var isCollapsed by remember { mutableStateOf(false) }
    var showIntervalEditDialog by remember { mutableStateOf(false) }
    var showFpsEditDialog by remember { mutableStateOf(false) }

    // Automatically collapse when extraction starts (isEnabled becomes false)
    LaunchedEffect(isEnabled) {
        if (!isEnabled) {
            isCollapsed = true
        } else {
            isCollapsed = false
        }
    }

    // Coerce settings.fps if maxFps changes and is lower than current fps
    LaunchedEffect(maxFps) {
        if (maxFps != null && settings.fps > maxFps) {
            onSettingsChanged(settings.copy(fps = maxFps))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCollapsed = !isCollapsed }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = TechCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Extraction Configurations",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    )
                }
                Icon(
                    imageVector = if (isCollapsed) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                    contentDescription = "Toggle Collapse",
                    tint = CoolGrey,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = !isCollapsed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))

            // Extraction Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSlateBg)
                    .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (settings.rateType == RateType.Interval) TechCyan else Color.Transparent)
                        .clickable(enabled = isEnabled) { onSettingsChanged(settings.copy(rateType = RateType.Interval)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TIME INTERVAL",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.rateType == RateType.Interval) Color(0xFF22282A) else CoolGrey
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (settings.rateType == RateType.Fps) TechCyan else Color.Transparent)
                        .clickable(enabled = isEnabled) { onSettingsChanged(settings.copy(rateType = RateType.Fps)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FRAMES PER SECOND",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.rateType == RateType.Fps) Color(0xFF22282A) else CoolGrey
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Inputs based on selected mode
            if (settings.rateType == RateType.Interval) {
                Column {
                    val sliderRange = when (settings.intervalUnit) {
                        IntervalUnit.Ms -> 10f..1000f
                        IntervalUnit.S -> 0.1f..60f
                        IntervalUnit.M -> 0.1f..60f
                    }
                    val presets = when (settings.intervalUnit) {
                        IntervalUnit.Ms -> listOf(25.0, 50.0, 100.0, 250.0, 500.0)
                        IntervalUnit.S -> listOf(0.5, 1.0, 2.0, 5.0, 10.0, 15.0)
                        IntervalUnit.M -> listOf(0.5, 1.0, 2.5, 5.0, 10.0, 15.0)
                    }
                    val displayValue = if (settings.intervalUnit == IntervalUnit.Ms) {
                        "${settings.intervalAmount.toInt()} ms"
                    } else {
                        "${settings.intervalAmount} ${settings.intervalUnit.name.lowercase()}"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Interval Rate:",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = displayValue,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TechCyan, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
                            modifier = Modifier
                                .clickable(enabled = isEnabled) { showIntervalEditDialog = true }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = settings.intervalAmount.toFloat().coerceIn(sliderRange),
                        onValueChange = { newVal ->
                            val rounded = roundIntervalAmount(newVal.toDouble(), settings.intervalUnit)
                            onSettingsChanged(settings.copy(intervalAmount = rounded))
                        },
                        valueRange = sliderRange,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            activeTrackColor = TechCyan,
                            thumbColor = TechCyan,
                            inactiveTrackColor = Color(0xFF293134)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Unit selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IntervalUnit.values().forEach { unit ->
                            val active = settings.intervalUnit == unit
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) TechCyan.copy(alpha = 0.15f) else DarkSlateBg)
                                    .border(1.dp, if (active) TechCyan else BorderSlate, RoundedCornerShape(6.dp))
                                    .clickable(enabled = isEnabled) {
                                        val newAmount = when (unit) {
                                            IntervalUnit.Ms -> if (settings.intervalUnit == IntervalUnit.S || settings.intervalUnit == IntervalUnit.M) {
                                                (settings.intervalAmount * 1000.0).coerceIn(10.0, 1000.0)
                                            } else {
                                                settings.intervalAmount.coerceIn(10.0, 1000.0)
                                            }
                                            IntervalUnit.S -> if (settings.intervalUnit == IntervalUnit.Ms) {
                                                (settings.intervalAmount / 1000.0).coerceIn(0.1, 60.0)
                                            } else if (settings.intervalUnit == IntervalUnit.M) {
                                                (settings.intervalAmount * 60.0).coerceIn(0.1, 60.0)
                                            } else {
                                                settings.intervalAmount.coerceIn(0.1, 60.0)
                                            }
                                            IntervalUnit.M -> if (settings.intervalUnit == IntervalUnit.S) {
                                                (settings.intervalAmount / 60.0).coerceIn(0.1, 60.0)
                                            } else if (settings.intervalUnit == IntervalUnit.Ms) {
                                                (settings.intervalAmount / 60000.0).coerceIn(0.1, 60.0)
                                            } else {
                                                settings.intervalAmount.coerceIn(0.1, 60.0)
                                            }
                                        }
                                        val finalAmount = if (unit == IntervalUnit.Ms) {
                                            Math.round(newAmount).toDouble()
                                        } else {
                                            Math.round(newAmount * 10.0) / 10.0
                                        }
                                        onSettingsChanged(settings.copy(intervalUnit = unit, intervalAmount = finalAmount))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unit.name.uppercase(),
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) TechCyan else CoolGrey
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interval Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Interval presets:", style = TextStyle(fontSize = 11.sp, color = CoolGrey))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.forEach { value ->
                                val label = if (settings.intervalUnit == IntervalUnit.Ms) {
                                    "${value.toInt()}ms"
                                } else {
                                    "${value}${settings.intervalUnit.name.lowercase()}"
                                }
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TechCyan
                                    ),
                                    modifier = Modifier
                                        .background(TechCyan.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .border(1.dp, TechCyan.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                        .clickable(enabled = isEnabled) {
                                            onSettingsChanged(settings.copy(intervalAmount = value, intervalUnit = settings.intervalUnit))
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                val upperLimit = (maxFps ?: 15.0).toFloat().coerceAtLeast(0.1f)
                val sliderValue = settings.fps.toFloat().coerceIn(0.1f, upperLimit)

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Frames Per Second (FPS):",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "${settings.fps} fps",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TechCyan, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
                            modifier = Modifier
                                .clickable(enabled = isEnabled) { showFpsEditDialog = true }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { onSettingsChanged(settings.copy(fps = roundFps(it.toDouble(), upperLimit.toDouble()))) },
                        valueRange = 0.1f..upperLimit,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            activeTrackColor = TechCyan,
                            thumbColor = TechCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // FPS Presets
                    val fpsPresets = if (maxFps != null) {
                        val baseList = listOf(1.0, 2.0, 5.0, 8.0, 10.0, 12.0, 15.0, 24.0, 30.0, 60.0)
                        val filtered = baseList.filter { it <= maxFps }.toMutableList()
                        val roundedMax = Math.round(maxFps * 10.0) / 10.0
                        if (!filtered.any { Math.abs(it - roundedMax) < 0.1 }) {
                            filtered.add(roundedMax)
                        }
                        filtered.sorted()
                    } else {
                        listOf(1.0, 2.0, 5.0, 8.0, 10.0)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FPS presets:", style = TextStyle(fontSize = 11.sp, color = CoolGrey))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            fpsPresets.forEach { value ->
                                Text(
                                    text = "$value fps",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TechCyan
                                    ),
                                    modifier = Modifier
                                        .background(TechCyan.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .border(1.dp, TechCyan.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                        .clickable(enabled = isEnabled) {
                                            onSettingsChanged(settings.copy(fps = value.coerceIn(0.1, upperLimit.toDouble())))
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Advanced Config Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedAdvanced = !expandedAdvanced }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Advanced Settings",
                        tint = CoolGrey,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Advanced Storage & Decoder Options",
                        style = TextStyle(fontSize = 12.sp, color = CoolGrey, fontWeight = FontWeight.Bold)
                    )
                }
                Icon(
                    imageVector = if (expandedAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Toggle",
                    tint = CoolGrey,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (expandedAdvanced) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Precision Select
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "High Precision (Slower)",
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            )
                            Text(
                                text = "Retrieve exact frames instead of sync-keyframes",
                                style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                            )
                        }
                        Switch(
                            checked = settings.isPrecise,
                            onCheckedChange = { onSettingsChanged(settings.copy(isPrecise = it)) },
                            enabled = isEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TechCyan,
                                checkedTrackColor = TechCyan.copy(alpha = 0.4f)
                            )
                        )
                    }

                    HorizontalDivider(color = BorderSlate, thickness = 0.8.dp)

                    // Output Image Format Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Image Format",
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            )
                            Text(
                                text = "Choose export compress type",
                                style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .width(120.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSlateBg)
                                .border(1.dp, BorderSlate, RoundedCornerShape(6.dp))
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (settings.format == OutputFormat.Jpeg) TechCyan else Color.Transparent)
                                    .clickable(enabled = isEnabled) { onSettingsChanged(settings.copy(format = OutputFormat.Jpeg)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "JPG",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (settings.format == OutputFormat.Jpeg) Color(0xFF22282A) else CoolGrey
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (settings.format == OutputFormat.Png) TechCyan else Color.Transparent)
                                    .clickable(enabled = isEnabled) { onSettingsChanged(settings.copy(format = OutputFormat.Png)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "PNG",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (settings.format == OutputFormat.Png) Color(0xFF22282A) else CoolGrey
                                    )
                                )
                            }
                        }
                    }

                    // JPEG quality slide (only if Jpeg)
                    if (settings.format == OutputFormat.Jpeg) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "JPEG Compression Quality:",
                                    style = TextStyle(fontSize = 12.sp, color = TextLight)
                                )
                                Text(
                                    "${settings.jpegQuality}%",
                                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TechCyan)
                                )
                            }
                            Slider(
                                value = settings.jpegQuality.toFloat(),
                                onValueChange = { onSettingsChanged(settings.copy(jpegQuality = it.toInt())) },
                                valueRange = 30f..100f,
                                enabled = isEnabled,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = TechCyan,
                                    thumbColor = TechCyan
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = BorderSlate, thickness = 0.8.dp)

                    // Filename Custom Prefix
                    Column {
                        Text(
                            text = "Custom File Name Prefix:",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Defaults to: [video_title_without_spaces]_frame_",
                            style = TextStyle(fontSize = 11.sp, color = CoolGrey),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = settings.prefix,
                            onValueChange = { onSettingsChanged(settings.copy(prefix = it)) },
                            placeholder = { Text("e.g. vacation_shot_", fontSize = 12.sp, color = CoolGrey) },
                            textStyle = TextStyle(fontSize = 13.sp, color = TextLight, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            enabled = isEnabled,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechCyan,
                                unfocusedBorderColor = BorderSlate,
                                focusedContainerColor = DarkSlateBg,
                                unfocusedContainerColor = DarkSlateBg
                            ),
                            modifier = Modifier
                                .testTag("filename_prefix_input")
                                .fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = BorderSlate, thickness = 0.8.dp)

                    // Custom Directory Override
                    Column {
                        Text(
                            text = "Manual Save Folder Override:",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        )
                        Text(
                            text = "Defaults to video's parent folder. Enter a folder name/path to override.",
                            style = TextStyle(fontSize = 11.sp, color = CoolGrey),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = settings.customDirectory,
                            onValueChange = { onSettingsChanged(settings.copy(customDirectory = it)) },
                            placeholder = { Text("e.g. MyScreenshots (optional)", fontSize = 12.sp, color = CoolGrey) },
                            textStyle = TextStyle(fontSize = 13.sp, color = TextLight, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            enabled = isEnabled,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechCyan,
                                unfocusedBorderColor = BorderSlate,
                                focusedContainerColor = DarkSlateBg,
                                unfocusedContainerColor = DarkSlateBg
                            ),
                            modifier = Modifier
                                .testTag("custom_directory_input")
                                .fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = BorderSlate, thickness = 0.8.dp)

                    // Delete video after success Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isEnabled) { 
                                onSettingsChanged(settings.copy(deleteAfterSuccess = !settings.deleteAfterSuccess)) 
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Dumpster Icon",
                                tint = DeleteRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Delete source video(s) on success",
                                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeleteRed)
                                )
                                Text(
                                    text = "Deletes source video files after they are successfully processed",
                                    style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                                )
                            }
                        }
                        Checkbox(
                            checked = settings.deleteAfterSuccess,
                            onCheckedChange = { onSettingsChanged(settings.copy(deleteAfterSuccess = it)) },
                            enabled = isEnabled,
                            colors = CheckboxDefaults.colors(
                                checkedColor = if (ThemeConfig.isDarkTheme) DeleteRed.copy(alpha = 0.25f) else DeleteRed.copy(alpha = 0.15f),
                                uncheckedColor = CoolGrey,
                                checkmarkColor = DeleteRed
                            )
                        )
                    }
                }
            }
                }
            }
        }
    }

    if (showIntervalEditDialog) {
        var textVal by remember { mutableStateOf(settings.intervalAmount.toString()) }
        var isError by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showIntervalEditDialog = false },
            title = { Text("Edit Interval", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        text = "Enter custom interval in ${settings.intervalUnit.name.lowercase()}:",
                        style = TextStyle(fontSize = 13.sp, color = CoolGrey),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = textVal,
                        onValueChange = {
                            textVal = it
                            val d = it.toDoubleOrNull()
                            isError = d == null || d <= 0.0
                        },
                        isError = isError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedContainerColor = DarkSlateBg,
                            unfocusedContainerColor = DarkSlateBg,
                            errorBorderColor = Color(0xFFFF5252)
                        ),
                        textStyle = TextStyle(color = TextLight, fontFamily = FontFamily.Monospace)
                    )
                    if (isError) {
                        Text("Please enter a valid positive number", color = Color(0xFFFF5252), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    } else {
                        val parsed = textVal.toDoubleOrNull() ?: 0.0
                        val rounded = roundIntervalAmount(parsed, settings.intervalUnit)
                        val unitStr = if (settings.intervalUnit == IntervalUnit.Ms) "ms" else settings.intervalUnit.name.lowercase()
                        Text(
                            text = "Value will be rounded to: $rounded $unitStr",
                            color = TechCyan,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val d = textVal.toDoubleOrNull()
                        if (d != null && d > 0.0) {
                            val rounded = roundIntervalAmount(d, settings.intervalUnit)
                            onSettingsChanged(settings.copy(intervalAmount = rounded))
                            showIntervalEditDialog = false
                        }
                    },
                    enabled = !isError && textVal.isNotEmpty()
                ) {
                    Text("SAVE", color = TechCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIntervalEditDialog = false }) {
                    Text("CANCEL", color = CoolGrey)
                }
            },
            containerColor = SlateCard,
            textContentColor = TextLight,
            titleContentColor = TextLight
        )
    }

    if (showFpsEditDialog) {
        val upperLimit = (maxFps ?: 15.0).toFloat().coerceAtLeast(0.1f)
        var textVal by remember { mutableStateOf(settings.fps.toString()) }
        var isError by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showFpsEditDialog = false },
            title = { Text("Edit FPS", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        text = "Enter custom FPS (0.1 to ${upperLimit}):",
                        style = TextStyle(fontSize = 13.sp, color = CoolGrey),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = textVal,
                        onValueChange = {
                            textVal = it
                            val d = it.toDoubleOrNull()
                            isError = d == null || d < 0.1 || d > upperLimit.toDouble()
                        },
                        isError = isError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedContainerColor = DarkSlateBg,
                            unfocusedContainerColor = DarkSlateBg,
                            errorBorderColor = Color(0xFFFF5252)
                        ),
                        textStyle = TextStyle(color = TextLight, fontFamily = FontFamily.Monospace)
                    )
                    if (isError) {
                        Text("Please enter a number between 0.1 and $upperLimit", color = Color(0xFFFF5252), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    } else {
                        val parsed = textVal.toDoubleOrNull() ?: 0.0
                        val rounded = roundFps(parsed, upperLimit.toDouble())
                        Text(
                            text = "Value will be rounded to: $rounded fps",
                            color = TechCyan,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val d = textVal.toDoubleOrNull()
                        if (d != null && d >= 0.1 && d <= upperLimit.toDouble()) {
                            val rounded = roundFps(d, upperLimit.toDouble())
                            onSettingsChanged(settings.copy(fps = rounded))
                            showFpsEditDialog = false
                        }
                    },
                    enabled = !isError && textVal.isNotEmpty()
                ) {
                    Text("SAVE", color = TechCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFpsEditDialog = false }) {
                    Text("CANCEL", color = CoolGrey)
                }
            },
            containerColor = SlateCard,
            textContentColor = TextLight,
            titleContentColor = TextLight
        )
    }
}

@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SingleVideoRangeCard(
    video: VideoItem,
    settings: ExtractionSettings,
    onSettingsChanged: (ExtractionSettings) -> Unit,
    isEnabled: Boolean,
    onCaptureSingleFrame: (Long) -> Unit
) {
    val context = LocalContext.current
    val duration = video.durationMs.coerceAtLeast(1L)
    val currentStart = settings.startMs.toFloat().coerceIn(0f, duration.toFloat())
    val currentEnd = if (settings.endMs <= 0L || settings.endMs > duration) {
        duration.toFloat()
    } else {
        settings.endMs.toFloat().coerceIn(0f, duration.toFloat())
    }
    
    var sliderValue by remember(video.id, settings.startMs, settings.endMs) {
        mutableStateOf(currentStart..currentEnd)
    }

    var lastStart by remember(video.id) { mutableStateOf(settings.startMs) }
    var lastEnd by remember(video.id) { mutableStateOf(settings.endMs) }

    var activePlayheadMs by remember(video.id) {
        mutableStateOf(settings.startMs)
    }
    
    var triggerFlash by remember { mutableStateOf(false) }
    LaunchedEffect(triggerFlash) {
        if (triggerFlash) {
            kotlinx.coroutines.delay(80)
            triggerFlash = false
        }
    }
    
    val flashAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (triggerFlash) 0.85f else 0.0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = if (triggerFlash) 50 else 400,
            easing = androidx.compose.animation.core.LinearOutSlowInEasing
        ),
        label = "FlashAlpha"
    )
    
    // Coerce active playhead within selection bounds whenever bounds or video changes
    LaunchedEffect(video.id, settings.startMs, settings.endMs) {
        val endMs = if (settings.endMs > 0) settings.endMs else duration
        activePlayheadMs = activePlayheadMs.coerceIn(settings.startMs, endMs)
    }
    
    var isPlaying by remember { mutableStateOf(false) }
    var wasPlayingBeforeDrag by remember { mutableStateOf(false) }

    val totalVideoFrames = Math.round(duration * video.fps / 1000.0).coerceAtLeast(1L)
    val currentFrameNum = (Math.round(activePlayheadMs * video.fps / 1000.0) + 1L).coerceIn(1L, totalVideoFrames)
    var lastActivePlayheadMs by remember(video.id) { mutableStateOf(activePlayheadMs) }
    var frameInputString by remember(video.id) { mutableStateOf(currentFrameNum.toString()) }

    LaunchedEffect(activePlayheadMs) {
        if (activePlayheadMs != lastActivePlayheadMs) {
            lastActivePlayheadMs = activePlayheadMs
            frameInputString = currentFrameNum.toString()
        }
    }

    // Filmstrip thumbnails loaded in background
    var videoThumbnails by remember(video.id) { mutableStateOf<List<Bitmap>>(emptyList()) }
    
    LaunchedEffect(video.id) {
        withContext(Dispatchers.IO) {
            val count = 8
            val list = mutableListOf<Bitmap>()
            for (i in 0 until count) {
                val fraction = i.toFloat() / (count - 1).coerceAtLeast(1)
                val timeMs = (duration * fraction).toLong()
                val bitmap = getFrameAtTime(context, video.uri, timeMs)
                if (bitmap != null) {
                    val scaled = Bitmap.createScaledBitmap(bitmap, 120, 80, false)
                    list.add(scaled)
                }
            }
            withContext(Dispatchers.Main) {
                videoThumbnails = list
            }
        }
    }

    var videoViewRef by remember(video.id) { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember(video.id) { mutableStateOf<android.media.MediaPlayer?>(null) }
    var isMuted by remember(video.id, settings.playAudioByDefault) { mutableStateOf(!settings.playAudioByDefault) }

    var isPauseButtonVisible by remember { mutableStateOf(true) }
    var isPlayButtonVisible by remember { mutableStateOf(true) }

    val playButtonAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (!isPlaying && isPlayButtonVisible) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "PlayButtonAlpha"
    )

    val pauseButtonAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPlaying && isPauseButtonVisible) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(400),
        label = "PauseButtonAlpha"
    )

    // Handle fading out of the pause button when playing starts, or when user toggles visibility
    LaunchedEffect(isPlaying, isPauseButtonVisible) {
        if (isPlaying && isPauseButtonVisible) {
            delay(600)
            isPauseButtonVisible = false
        }
    }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            isPlayButtonVisible = true
        }
    }

    // Sync volume level whenever isMuted or mediaPlayerRef changes
    LaunchedEffect(isMuted, mediaPlayerRef) {
        val volume = if (isMuted) 0f else 1f
        try {
            mediaPlayerRef?.setVolume(volume, volume)
        } catch (e: Exception) {}
    }

    // Sync playback states with the real video view
    LaunchedEffect(isPlaying, video.id, videoViewRef) {
        if (isPlaying) {
            val player = videoViewRef ?: return@LaunchedEffect
            val endMs = if (settings.endMs > 0) settings.endMs else duration
            // Force seek to activePlayheadMs before starting, to ensure we start exactly where the playhead is!
            player.seekTo(activePlayheadMs.toInt())
            // Give a tiny delay for seek to initiate/register
            delay(50)
            player.start()
            while (isActive && isPlaying) {
                val currentPos = player.currentPosition.toLong()
                if (currentPos >= endMs) {
                    activePlayheadMs = endMs
                    isPlaying = false
                    player.pause()
                    player.seekTo(settings.startMs.toInt())
                    break
                } else {
                    // Only update the playhead if it's within the startMs and endMs range
                    if (currentPos >= settings.startMs) {
                        activePlayheadMs = currentPos
                    }
                }
                delay(16) // Smooth 60fps playhead tracing!
            }
        } else {
            videoViewRef?.pause()
        }
    }

    val seekRequests = remember(video.id) { kotlinx.coroutines.channels.Channel<Long>(kotlinx.coroutines.channels.Channel.CONFLATED) }

    LaunchedEffect(activePlayheadMs) {
        if (!isPlaying) {
            seekRequests.trySend(activePlayheadMs)
        }
    }

    LaunchedEffect(video.id, videoViewRef) {
        val player = videoViewRef ?: return@LaunchedEffect
        for (timeMs in seekRequests) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && mediaPlayerRef != null) {
                    mediaPlayerRef?.seekTo(timeMs, android.media.MediaPlayer.SEEK_CLOSEST)
                } else {
                    player.seekTo(timeMs.toInt())
                }
            } catch (e: Exception) {
                try {
                    player.seekTo(timeMs.toInt())
                } catch (ex: Exception) {}
            }
            delay(30) // ~33fps seek updates for smooth and non-blocking realtime dragging!
        }
    }

    // Load active frame for activePlayheadMs using a conflated channel to avoid cancellation of retriever tasks
    var activeFrameBitmap by remember(video.id) { mutableStateOf<Bitmap?>(null) }
    val frameRequests = remember(video.id) { kotlinx.coroutines.channels.Channel<Long>(kotlinx.coroutines.channels.Channel.CONFLATED) }
    
    // Push requests into the channel when activePlayheadMs changes
    LaunchedEffect(video.id, activePlayheadMs) {
        frameRequests.trySend(activePlayheadMs)
    }
    
    // Process the requests sequentially, reusing MediaMetadataRetriever for maximum speed and zero cancelled frame retrievals!
    LaunchedEffect(video.id) {
        withContext(Dispatchers.IO) {
            var retriever: MediaMetadataRetriever? = null
            try {
                retriever = MediaMetadataRetriever().apply {
                    setDataSource(context, video.uri)
                }
            } catch (e: Exception) {
                // Ignore, we will try standard getFrameAtTime fallback on demand
            }
            
            try {
                while (isActive) {
                    val timeMs = frameRequests.receive()
                    try {
                        var bitmap: Bitmap? = null
                        if (retriever != null) {
                            try {
                                bitmap = retriever.getFrameAtTime(timeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
                            } catch (e: Exception) {
                                try {
                                    bitmap = retriever.getFrameAtTime(timeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                } catch (ex: Exception) {}
                            }
                        }
                        
                        if (bitmap == null) {
                            bitmap = getFrameAtTime(context, video.uri, timeMs)
                        }
                        
                        if (bitmap != null) {
                            withContext(Dispatchers.Main) {
                                activeFrameBitmap = bitmap
                            }
                        }
                    } catch (e: Exception) {
                        // Suppress single-frame errors to keep loop alive!
                    }
                }
            } catch (e: Exception) {
                // Loop cancelled or channel closed
            } finally {
                try {
                    retriever?.release()
                } catch (e: Exception) {}
            }
        }
    }

    val onPlayPauseClick = {
        if (isPlaying) {
            isPlaying = false
            isPlayButtonVisible = true
        } else {
            val endMs = if (settings.endMs > 0) settings.endMs else duration
            if (activePlayheadMs >= endMs || activePlayheadMs < settings.startMs) {
                activePlayheadMs = settings.startMs
            }
            isPlaying = true
            isPauseButtonVisible = true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCut,
                    contentDescription = "Trim Video",
                    tint = TechCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Extraction Range",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Select custom start and end time for frame extraction:",
                style = TextStyle(fontSize = 12.sp, color = CoolGrey)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            val isPortrait = activeFrameBitmap?.let { it.height > it.width } ?: false

            // Active frame preview with Play/Pause button
            Box(
                modifier = (if (isPortrait) {
                    Modifier
                        .fillMaxWidth(0.75f)
                        .aspectRatio(3f / 4f)
                        .align(Alignment.CenterHorizontally)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                })
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                    .background(TerminalBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isPlaying) {
                            isPauseButtonVisible = !isPauseButtonVisible
                        } else {
                            isPlayButtonVisible = !isPlayButtonVisible
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (activeFrameBitmap != null) {
                    androidx.compose.runtime.key(video.id) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setVideoURI(video.uri)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = false
                                        mediaPlayerRef = mp
                                        val volume = if (isMuted) 0f else 1f
                                        mp.setVolume(volume, volume)
                                        seekTo(activePlayheadMs.toInt())
                                        if (isPlaying) {
                                            start()
                                        } else {
                                            pause()
                                        }
                                    }
                                    videoViewRef = this
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { _ -> }
                        )
                    }
                } else {
                    CircularProgressIndicator(color = TechCyan, modifier = Modifier.size(36.dp))
                }

                // Smooth camera flash overlay
                if (flashAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = flashAlpha))
                    )
                }

                // Mute/Unmute Button (top-right corner of the video container)
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, TechCyan.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = if (isMuted) Color.White.copy(alpha = 0.6f) else TechCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Play/Pause Button
                val currentButtonAlpha = if (isPlaying) pauseButtonAlpha else playButtonAlpha
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(54.dp)
                        .alpha(currentButtonAlpha)
                        .background(Color.Black.copy(alpha = 0.5f * currentButtonAlpha), CircleShape)
                        .border(1.dp, TechCyan.copy(alpha = 0.5f * currentButtonAlpha), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = TechCyan.copy(alpha = currentButtonAlpha),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Timecode
                Text(
                    text = formatDuration(activePlayheadMs),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Snap Controls & Editable Frame Box Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left "Snap" Button (Snap ->)
                OutlinedButton(
                    onClick = {
                        val end = if (settings.endMs <= 0L || settings.endMs > duration) duration else settings.endMs
                        val newStart = activePlayheadMs.coerceAtMost(end)
                        onSettingsChanged(settings.copy(startMs = newStart))
                        sliderValue = newStart.toFloat()..sliderValue.endInclusive
                    },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TechCyan
                    ),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(6.dp),
                    enabled = isEnabled
                ) {
                    Text("Snap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }

                // Center Frame decrement, Frame box, Frame increment
                val backInteractionSource = remember { MutableInteractionSource() }
                val isBackPressed by backInteractionSource.collectIsPressedAsState()
                
                val stepBack = {
                    val newFrameNum = (currentFrameNum - 1).coerceIn(1L, totalVideoFrames)
                    val targetMs = ((newFrameNum - 1) * 1000.0 / video.fps).toLong().coerceIn(0L, duration)
                    
                    var currentStart = sliderValue.start.toLong()
                    if (targetMs < currentStart) {
                        currentStart = targetMs
                        sliderValue = currentStart.toFloat()..sliderValue.endInclusive
                        onSettingsChanged(settings.copy(startMs = currentStart))
                    }
                    
                    lastActivePlayheadMs = targetMs
                    activePlayheadMs = targetMs
                    frameInputString = newFrameNum.toString()
                }
                
                LaunchedEffect(isBackPressed) {
                    if (isBackPressed) {
                        kotlinx.coroutines.delay(400)
                        while (isActive) {
                            stepBack()
                            kotlinx.coroutines.delay(80)
                        }
                    }
                }

                val forwardInteractionSource = remember { MutableInteractionSource() }
                val isForwardPressed by forwardInteractionSource.collectIsPressedAsState()
                
                val stepForward = {
                    val newFrameNum = (currentFrameNum + 1).coerceIn(1L, totalVideoFrames)
                    val targetMs = ((newFrameNum - 1) * 1000.0 / video.fps).toLong().coerceIn(0L, duration)
                    
                    var currentEnd = sliderValue.endInclusive.toLong()
                    if (targetMs > currentEnd) {
                        currentEnd = targetMs
                        sliderValue = sliderValue.start..currentEnd.toFloat()
                        onSettingsChanged(settings.copy(endMs = currentEnd))
                    }
                    
                    lastActivePlayheadMs = targetMs
                    activePlayheadMs = targetMs
                    frameInputString = newFrameNum.toString()
                }
                
                LaunchedEffect(isForwardPressed) {
                    if (isForwardPressed) {
                        kotlinx.coroutines.delay(400)
                        while (isActive) {
                            stepForward()
                            kotlinx.coroutines.delay(80)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Back one frame (<)
                    IconButton(
                        onClick = { stepBack() },
                        modifier = Modifier.size(32.dp),
                        enabled = isEnabled,
                        interactionSource = backInteractionSource
                    ) {
                        Text(
                            text = "<",
                            color = if (isEnabled) TechCyan else CoolGrey,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Center Editable Frame Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(TerminalBg, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderSlate, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Frame ",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey, fontWeight = FontWeight.Bold)
                        )
                        
                        androidx.compose.foundation.text.BasicTextField(
                            value = frameInputString,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                frameInputString = filtered
                                if (filtered.isNotEmpty()) {
                                    val targetFrame = filtered.toLongOrNull()
                                    if (targetFrame != null) {
                                        val clampedFrame = if (targetFrame > totalVideoFrames) totalVideoFrames else targetFrame.coerceAtLeast(1L)
                                        val targetMs = ((clampedFrame - 1) * 1000.0 / video.fps).toLong().coerceIn(0L, duration)
                                        lastActivePlayheadMs = targetMs
                                        activePlayheadMs = targetMs
                                    }
                                }
                            },
                            textStyle = TextStyle(
                                color = TechCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(54.dp)
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(TechCyan),
                            enabled = isEnabled
                        )
                        
                        Text(
                            text = " / $totalVideoFrames",
                            style = TextStyle(fontSize = 12.sp, color = CoolGrey, fontWeight = FontWeight.Medium)
                        )
                    }

                    // Forward one frame (>)
                    IconButton(
                        onClick = { stepForward() },
                        modifier = Modifier.size(32.dp),
                        enabled = isEnabled,
                        interactionSource = forwardInteractionSource
                    ) {
                        Text(
                            text = ">",
                            color = if (isEnabled) TechCyan else CoolGrey,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right "Snap" Button (<- Snap)
                OutlinedButton(
                    onClick = {
                        val start = settings.startMs
                        val newEnd = activePlayheadMs.coerceAtLeast(start)
                        onSettingsChanged(settings.copy(endMs = newEnd))
                        sliderValue = sliderValue.start..newEnd.toFloat()
                    },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TechCyan
                    ),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(6.dp),
                    enabled = isEnabled
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Slider container with filmstrip behind it and isolated draggable playhead
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                val widthPx = with(LocalDensity.current) { maxWidth.toPx() }

                // 1. Filmstrip container (bottom 60dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                ) {
                    // Filmstrip backgrounds
                    if (videoThumbnails.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            videoThumbnails.forEach { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentScale = ContentScale.Crop,
                                    alpha = 1.0f
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF1E2528))
                        )
                    }
                    
                    // RangeSlider superimposed inside the bottom 60dp filmstrip box
                    RangeSlider(
                        value = sliderValue,
                        onValueChange = { range ->
                            if (isEnabled) {
                                sliderValue = range
                                val newStart = range.start.toLong()
                                val newEnd = range.endInclusive.toLong()
                                
                                // Push playhead if boundaries push past it
                                if (activePlayheadMs < newStart) {
                                    activePlayheadMs = newStart
                                } else if (activePlayheadMs > newEnd) {
                                    activePlayheadMs = newEnd
                                }
                                lastStart = newStart
                                lastEnd = newEnd
                            }
                        },
                        onValueChangeFinished = {
                            onSettingsChanged(
                                settings.copy(
                                    startMs = sliderValue.start.toLong(),
                                    endMs = sliderValue.endInclusive.toLong()
                                )
                            )
                        },
                        valueRange = 0f..duration.toFloat(),
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Black.copy(alpha = 0.6f),
                            thumbColor = TechCyan
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // 2. Playhead line indicator (running through full height)
                val playheadFraction = (activePlayheadMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                val playheadOffset = maxWidth * playheadFraction
                
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .offset(x = playheadOffset)
                        .background(TechCyan)
                )

                // 3. Isolated top track (24dp) for playhead seeking
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.TopCenter)
                        .pointerInput(duration, isEnabled, sliderValue) {
                            if (!isEnabled) return@pointerInput
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val wasPlaying = isPlaying
                                    isPlaying = false
                                    
                                    var fraction = (down.position.x / widthPx).coerceIn(0f, 1f)
                                    var rawMs = (duration * fraction).toLong()
                                    
                                    // Push boundaries if dragging cursor past them
                                    var currentStart = sliderValue.start.toLong()
                                    var currentEnd = sliderValue.endInclusive.toLong()
                                    if (rawMs < currentStart) {
                                        currentStart = rawMs
                                    } else if (rawMs > currentEnd) {
                                        currentEnd = rawMs
                                    }
                                    sliderValue = currentStart.toFloat()..currentEnd.toFloat()
                                    activePlayheadMs = rawMs.coerceIn(0L, duration)
                                    
                                    onSettingsChanged(
                                        settings.copy(
                                            startMs = currentStart,
                                            endMs = currentEnd
                                        )
                                    )
                                    
                                    down.consume()
                                    
                                    val dragPointerId = down.id
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == dragPointerId } ?: break
                                        if (change.pressed) {
                                            fraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                                            rawMs = (duration * fraction).toLong()
                                            
                                            currentStart = sliderValue.start.toLong()
                                            currentEnd = sliderValue.endInclusive.toLong()
                                            if (rawMs < currentStart) {
                                                currentStart = rawMs
                                            } else if (rawMs > currentEnd) {
                                                currentEnd = rawMs
                                            }
                                            sliderValue = currentStart.toFloat()..currentEnd.toFloat()
                                            activePlayheadMs = rawMs.coerceIn(0L, duration)
                                            
                                            onSettingsChanged(
                                                settings.copy(
                                                    startMs = currentStart,
                                                    endMs = currentEnd
                                                )
                                            )
                                            
                                            change.consume()
                                        } else {
                                            break
                                        }
                                    }
                                    
                                    if (wasPlaying) {
                                        isPlaying = true
                                    }
                                }
                            }
                        }
                ) {
                    // Sleek handle with glowing border to indicate touch target
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = playheadOffset - 8.dp, y = 4.dp)
                            .background(TechCyan, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "START TIME",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                    )
                    Text(
                        text = formatDuration(sliderValue.start.toLong()),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TechCyan)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "END TIME",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                    )
                    Text(
                        text = formatDuration(sliderValue.endInclusive.toLong()),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TechCyan)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val rangeDuration = (sliderValue.endInclusive - sliderValue.start).toLong()
            val expectedFrames = if (settings.rateType == RateType.Interval) {
                val intervalMs = when (settings.intervalUnit) {
                    IntervalUnit.Ms -> settings.intervalAmount
                    IntervalUnit.S -> settings.intervalAmount * 1000.0
                    IntervalUnit.M -> settings.intervalAmount * 60000.0
                }
                if (intervalMs > 0) {
                    (rangeDuration / intervalMs).toLong() + 1
                } else {
                    0L
                }
            } else {
                val seconds = rangeDuration / 1000.0
                (seconds * settings.fps).toLong() + 1
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalBg, RoundedCornerShape(6.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Segment Duration:",
                        style = TextStyle(fontSize = 11.sp, color = TextLight.copy(alpha = 0.7f))
                    )
                    Text(
                        text = formatDuration(rangeDuration),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expected Segment Frames:",
                        style = TextStyle(fontSize = 11.sp, color = TextLight.copy(alpha = 0.7f))
                    )
                    Text(
                        text = "$expectedFrames frames",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerminalGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            Button(
                onClick = {
                    triggerFlash = true
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onCaptureSingleFrame(activePlayheadMs)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("capture_screenshot_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerminalGreen,
                    contentColor = Color(0xFF152023)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = isEnabled && !isPlaying
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Capture Screenshot",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CAPTURE CURRENT FRAME",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun StatusMonitorCard(
    status: ExtractionStatus,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(
            1.dp,
            when (status) {
                is ExtractionStatus.Success -> TerminalGreen.copy(alpha = 0.3f)
                is ExtractionStatus.Error -> Color.Red.copy(alpha = 0.3f)
                else -> if (isPaused) WarningAmber.copy(alpha = 0.5f) else TechCyan.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (status) {
                                    is ExtractionStatus.Success -> TerminalGreen
                                    is ExtractionStatus.Error -> Color.Red
                                    else -> if (isPaused) WarningAmber else TechCyan
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (status) {
                            is ExtractionStatus.Success -> "Pipeline Complete"
                            is ExtractionStatus.Error -> "Pipeline Failed"
                            else -> if (isPaused) "Pipeline Paused" else "Extracting Frames"
                        },
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = when (status) {
                            is ExtractionStatus.Success -> "SUCCESS"
                            is ExtractionStatus.Error -> "FAILED"
                            else -> if (isPaused) "PAUSED" else "EXTRACTING"
                        },
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (status) {
                                is ExtractionStatus.Success -> TerminalGreen
                                is ExtractionStatus.Error -> Color.Red
                                else -> if (isPaused) WarningAmber else TechCyan
                            },
                            letterSpacing = 0.5.sp
                        )
                    )

                    if (status is ExtractionStatus.Processing) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (isPaused) onResume() else onPause()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (isPaused) TechCyan.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                contentDescription = if (isPaused) "Resume Extraction" else "Pause Extraction",
                                tint = if (isPaused) TechCyan else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = CoolGrey,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (status) {
                is ExtractionStatus.Processing -> {
                    Column {
                        // Title of current video
                        Text(
                            text = status.currentVideoName,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Video ${status.videoIndex} of ${status.totalVideos}",
                                style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                            )
                            Text(
                                text = "Captured: ${status.currentFrame} frame(s) ${status.expectedFrames?.let { " / $it expected" } ?: ""}",
                                style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { status.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isPaused) WarningAmber else TechCyan,
                            trackColor = BorderSlate
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isPaused) {
                                Text(
                                    text = "PAUSED (Tap ▶ to resume)",
                                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                            Text(
                                text = "${Math.round(status.progress * 100)}%",
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isPaused) WarningAmber else TechCyan)
                            )
                        }
                    }
                }
                is ExtractionStatus.Success -> {
                    Text(
                        "Success! Captured ${status.totalFrames} frames in ${status.savedFolder}",
                        color = TerminalGreen,
                        fontSize = 13.sp
                    )
                }
                is ExtractionStatus.Error -> {
                    Text(
                        "Error: ${status.message}",
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun TerminalLogCard(
    logs: List<String>,
    onClearLogs: () -> Unit,
    mainListState: LazyListState? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var isInitialComposition by remember { mutableStateOf(true) }
    val density = LocalDensity.current
    LaunchedEffect(isExpanded) {
        if (isInitialComposition) {
            isInitialComposition = false
            return@LaunchedEffect
        }
        if (mainListState != null) {
            val heightDiff = (screenHeight * 0.75f) - 160.dp
            val heightDiffPx = with(density) { heightDiff.toPx() }
            if (isExpanded) {
                mainListState.animateScrollBy(heightDiffPx)
            } else {
                mainListState.animateScrollBy(-heightDiffPx)
            }
        }
    }

    // Scroll-lock to bottom on new logs
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column {
            // Console Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateCardHeader)
                    .border(BorderStroke(1.dp, BorderSlate), RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Terminal,
                        contentDescription = "Console Log",
                        tint = TerminalGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DATA TRANSCRIPT:",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalGreen,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TerminalGreen.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val fullText = logs.joinToString("\n")
                            clipboard.setText(AnnotatedString(fullText))
                            Toast.makeText(context, "Copied transcript to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy Transcript",
                            tint = CoolGrey,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Clear Console",
                            tint = CoolGrey,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Console Box
            val boxHeight by animateDpAsState(
                targetValue = if (isExpanded) screenHeight * 0.75f else 160.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "ConsoleBoxHeight"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxHeight)
                    .background(TerminalBg)
                    .padding(12.dp)
            ) {
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            Text(
                                text = buildAnnotatedString {
                                    if (log.startsWith("Saved:") || log.contains("COMPLETED") || log.contains("DONE")) {
                                        withStyle(SpanStyle(color = TerminalGreen, fontWeight = FontWeight.Bold)) {
                                            append("$ ")
                                        }
                                        withStyle(SpanStyle(color = TerminalGreen)) {
                                            append(log)
                                        }
                                    } else if (log.contains("⚠️ Warning:")) {
                                        withStyle(SpanStyle(color = WarningAmber, fontWeight = FontWeight.Bold)) {
                                            append("⚠️ ")
                                        }
                                        withStyle(SpanStyle(color = WarningAmber)) {
                                            append(log.substringAfter("⚠️ ").trim())
                                        }
                                    } else if (log.contains("❌ FATAL ERROR:")) {
                                        withStyle(SpanStyle(color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)) {
                                            append("❌ ")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFFFF5252))) {
                                            append(log.substringAfter("❌ ").trim())
                                        }
                                    } else if (log.contains("Error") || log.contains("⚠️") || log.contains("❌")) {
                                        withStyle(SpanStyle(color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)) {
                                            append("![ERROR] ")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFFFF5252))) {
                                            append(log)
                                        }
                                    } else if (log.contains("Analyzing") || log.contains("🚀") || log.contains("Selecting") || log.contains("Skipping")) {
                                        withStyle(SpanStyle(color = TechCyan, fontWeight = FontWeight.Bold)) {
                                            append("> ")
                                        }
                                        withStyle(SpanStyle(color = TextLight)) {
                                            append(log)
                                        }
                                    } else {
                                        withStyle(SpanStyle(color = CoolGrey)) {
                                            append("  $log")
                                        }
                                    }
                                },
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExtractedFramesCard(
    frames: List<SavedFrame>,
    onShareFrame: (SavedFrame) -> Unit,
    onViewFrame: (SavedFrame) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoLibrary,
                    contentDescription = "Extracted Photos",
                    tint = TechCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Recent Extracted Frames",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    )
                    Text(
                        text = "Click to view full photo | Saved to Pictures/v2p",
                        style = TextStyle(fontSize = 11.sp, color = CoolGrey)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(frames) { frame ->
                    FramePreviewItem(
                        frame = frame,
                        onView = { onViewFrame(frame) },
                        onShare = { onShareFrame(frame) }
                    )
                }
            }
        }
    }
}

@Composable
fun FramePreviewItem(
    frame: SavedFrame,
    onView: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSlateBg)
            .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
            .clickable { onView() }
    ) {
        // Thumbnail Image via Coil
        AsyncImage(
            model = frame.uri,
            contentDescription = frame.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient cover for status details overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 50f
                    )
                )
        )

        // Title and Share overlay actions
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Share icon on top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable { onShare() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = TechCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Timestamp details
            Column {
                Text(
                    text = "${frame.timestampMs} ms",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechCyan
                    )
                )
                Text(
                    text = frame.videoName,
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = TextLight
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Global Intent operations
fun shareImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Extracted Frame"))
}

fun viewImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No gallery viewer app available to display this image", Toast.LENGTH_SHORT).show()
    }
}

