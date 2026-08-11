package com.example

import com.example.ui.theme.*

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

enum class GalleryGroupMode(val title: String) {
    DIRECTORY("Directory > Video > Frames"),
    VIDEO("Group by Video"),
    IMAGES("Group by Images")
}

enum class GallerySortOption(val title: String) {
    TITLE("Title"),
    DATE_EXTRACTED("Date Extracted"),
    DATE_CREATED("Date Video Created"),
    SIZE("Size")
}

enum class GallerySortDirection {
    ASCENDING, DESCENDING
}

data class VideoGroup(
    val videoName: String,
    val directoryName: String,
    val photos: List<SavedPhotoEntry>,
    val totalSize: Long,
    val photoCount: Int,
    val bestThumbnailUri: String,
    val latestExtractedMs: Long,
    val minTimestampMs: Long
)

data class DirectoryGroup(
    val directoryName: String,
    val videoGroups: List<VideoGroup>,
    val totalPhotos: Int,
    val totalSize: Long,
    val bestThumbnailUri: String,
    val latestExtractedMs: Long,
    val minTimestampMs: Long
)

fun buildVideoGroups(photos: List<SavedPhotoEntry>): List<VideoGroup> {
    return photos.groupBy { it.videoName }.map { (videoName, videoPhotos) ->
        val totalSize = videoPhotos.sumOf { it.sizeBytes }
        val photoCount = videoPhotos.size
        val bestThumb = videoPhotos.maxByOrNull { it.sizeBytes }?.uriString ?: videoPhotos.first().uriString
        val latestExtracted = videoPhotos.maxOfOrNull { it.dateSavedMs } ?: 0L
        val minTimestamp = videoPhotos.minOfOrNull { it.timestampMs } ?: 0L
        val dirName = videoPhotos.firstOrNull()?.directoryName?.ifEmpty { "FrameGrabber" } ?: "FrameGrabber"
        VideoGroup(
            videoName = videoName,
            directoryName = dirName,
            photos = videoPhotos,
            totalSize = totalSize,
            photoCount = photoCount,
            bestThumbnailUri = bestThumb,
            latestExtractedMs = latestExtracted,
            minTimestampMs = minTimestamp
        )
    }
}

fun buildDirectoryGroups(photos: List<SavedPhotoEntry>): List<DirectoryGroup> {
    val videoGroups = buildVideoGroups(photos)
    return videoGroups.groupBy { it.directoryName }.map { (dirName, dirVideoGroups) ->
        val totalPhotos = dirVideoGroups.sumOf { it.photoCount }
        val totalSize = dirVideoGroups.sumOf { it.totalSize }
        val allPhotos = dirVideoGroups.flatMap { it.photos }
        val bestThumb = allPhotos.maxByOrNull { it.sizeBytes }?.uriString ?: ""
        val latestExtracted = dirVideoGroups.maxOfOrNull { it.latestExtractedMs } ?: 0L
        val minTimestamp = dirVideoGroups.minOfOrNull { it.minTimestampMs } ?: 0L
        DirectoryGroup(
            directoryName = dirName,
            videoGroups = dirVideoGroups,
            totalPhotos = totalPhotos,
            totalSize = totalSize,
            bestThumbnailUri = bestThumb,
            latestExtractedMs = latestExtracted,
            minTimestampMs = minTimestamp
        )
    }
}

fun sortPhotos(photos: List<SavedPhotoEntry>, option: GallerySortOption, direction: GallerySortDirection): List<SavedPhotoEntry> {
    val sorted = when (option) {
        GallerySortOption.TITLE -> photos.sortedBy { it.fileName.lowercase() }
        GallerySortOption.DATE_EXTRACTED -> photos.sortedBy { it.dateSavedMs }
        GallerySortOption.DATE_CREATED -> photos.sortedBy { it.timestampMs }
        GallerySortOption.SIZE -> photos.sortedBy { it.sizeBytes }
    }
    return if (direction == GallerySortDirection.DESCENDING) sorted.reversed() else sorted
}

fun sortVideoGroups(groups: List<VideoGroup>, option: GallerySortOption, direction: GallerySortDirection): List<VideoGroup> {
    val sorted = when (option) {
        GallerySortOption.TITLE -> groups.sortedBy { it.videoName.lowercase() }
        GallerySortOption.DATE_EXTRACTED -> groups.sortedBy { it.latestExtractedMs }
        GallerySortOption.DATE_CREATED -> groups.sortedBy { it.minTimestampMs }
        GallerySortOption.SIZE -> groups.sortedBy { it.totalSize }
    }
    return if (direction == GallerySortDirection.DESCENDING) sorted.reversed() else sorted
}

fun sortDirectoryGroups(groups: List<DirectoryGroup>, option: GallerySortOption, direction: GallerySortDirection): List<DirectoryGroup> {
    val sorted = when (option) {
        GallerySortOption.TITLE -> groups.sortedBy { it.directoryName.lowercase() }
        GallerySortOption.DATE_EXTRACTED -> groups.sortedBy { it.latestExtractedMs }
        GallerySortOption.DATE_CREATED -> groups.sortedBy { it.minTimestampMs }
        GallerySortOption.SIZE -> groups.sortedBy { it.totalSize }
    }
    return if (direction == GallerySortDirection.DESCENDING) sorted.reversed() else sorted
}

@Composable
fun GroupByDropdown(
    selectedMode: GalleryGroupMode,
    onModeSelected: (GalleryGroupMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(34.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateCard, contentColor = TechCyan),
            border = BorderStroke(1.dp, BorderSlate),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Icon(
                imageVector = when(selectedMode) {
                    GalleryGroupMode.DIRECTORY -> Icons.Filled.Folder
                    GalleryGroupMode.VIDEO -> Icons.Filled.VideoLibrary
                    GalleryGroupMode.IMAGES -> Icons.Filled.GridView
                },
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = TechCyan
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = selectedMode.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = CoolGrey, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().background(SlateCard)
        ) {
            GalleryGroupMode.values().forEach { mode ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when(mode) {
                                    GalleryGroupMode.DIRECTORY -> Icons.Filled.Folder
                                    GalleryGroupMode.VIDEO -> Icons.Filled.VideoLibrary
                                    GalleryGroupMode.IMAGES -> Icons.Filled.GridView
                                },
                                contentDescription = null,
                                tint = if (mode == selectedMode) TechCyan else CoolGrey,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mode.title,
                                color = if (mode == selectedMode) TechCyan else TextLight,
                                fontWeight = if (mode == selectedMode) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SortByDropdown(
    selectedSort: GallerySortOption,
    onSortSelected: (GallerySortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(34.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateCard, contentColor = TechCyan),
            border = BorderStroke(1.dp, BorderSlate),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(15.dp), tint = TechCyan)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = selectedSort.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextLight,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = CoolGrey, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().background(SlateCard)
        ) {
            GallerySortOption.values().forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = {
                        Text(
                            text = option.title,
                            color = if (option == selectedSort) TechCyan else TextLight,
                            fontWeight = if (option == selectedSort) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    onClick = {
                        onSortSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SortDirectionButton(
    sortDirection: GallerySortDirection,
    onDirectionToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onDirectionToggle,
        modifier = modifier.size(34.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateCard, contentColor = TechCyan),
        border = BorderStroke(1.dp, BorderSlate),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = if (sortDirection == GallerySortDirection.ASCENDING) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
            contentDescription = if (sortDirection == GallerySortDirection.ASCENDING) "Ascending" else "Descending",
            tint = TechCyan,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun GridDetailButton(
    isGridListView: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.size(34.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateCard, contentColor = TechCyan),
        border = BorderStroke(1.dp, BorderSlate),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = if (isGridListView) Icons.Filled.GridView else Icons.Filled.ViewList,
            contentDescription = if (isGridListView) "Grid Shape View" else "Details List View",
            tint = TechCyan,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoItemCard(
    videoGroup: VideoGroup,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TechCyan.copy(alpha = 0.2f) else SlateCard
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) TechCyan else BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = videoGroup.bestThumbnailUri,
                    contentDescription = videoGroup.videoName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = TechCyan, modifier = Modifier.size(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val displayTitle = videoGroup.videoName.take(40)
                Text(
                    text = displayTitle,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "(${videoGroup.photoCount} ",
                        style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Photo,
                        contentDescription = "Photos",
                        tint = TechCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = ") ${formatFileSize(videoGroup.totalSize)}",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CoolGrey)
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open Video",
                tint = CoolGrey,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderGridCard(
    directoryGroup: DirectoryGroup,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDragSelect: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TechCyan.copy(alpha = 0.2f) else SlateCard
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) TechCyan else BorderSlate)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (directoryGroup.bestThumbnailUri.isNotEmpty()) {
                AsyncImage(
                    model = directoryGroup.bestThumbnailUri,
                    contentDescription = directoryGroup.directoryName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(TerminalBg))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 60f
                        )
                    )
            )

            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                tint = TechCyan,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(22.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = directoryGroup.directoryName,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${directoryGroup.videoGroups.size} Videos • ${directoryGroup.totalPhotos} Photos",
                    style = TextStyle(fontSize = 10.sp, color = CoolGrey),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoGridCard(
    videoGroup: VideoGroup,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDragSelect: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TechCyan.copy(alpha = 0.2f) else SlateCard
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) TechCyan else BorderSlate)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (videoGroup.bestThumbnailUri.isNotEmpty()) {
                AsyncImage(
                    model = videoGroup.bestThumbnailUri,
                    contentDescription = videoGroup.videoName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(TerminalBg))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 60f
                        )
                    )
            )

            Icon(
                imageVector = Icons.Filled.VideoLibrary,
                contentDescription = null,
                tint = TechCyan,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(20.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = videoGroup.videoName,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${videoGroup.photoCount} Photos • ${formatFileSize(videoGroup.totalSize)}",
                    style = TextStyle(fontSize = 10.sp, color = CoolGrey),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItemCard(
    directoryGroup: DirectoryGroup,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDragSelect: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TechCyan.copy(alpha = 0.2f) else SlateCard
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) TechCyan else BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalBg)
            ) {
                if (directoryGroup.bestThumbnailUri.isNotEmpty()) {
                    AsyncImage(
                        model = directoryGroup.bestThumbnailUri,
                        contentDescription = directoryGroup.directoryName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = TechCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = directoryGroup.directoryName,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${directoryGroup.videoGroups.size} Videos",
                    style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "(${directoryGroup.totalPhotos} ",
                        style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Photo,
                        contentDescription = "Photos",
                        tint = TechCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = ") ${formatFileSize(directoryGroup.totalSize)}",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CoolGrey)
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open Folder",
                tint = CoolGrey,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun Modifier.gridDragSelect(
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    enabled: Boolean = true,
    getItemUris: (key: Any) -> Set<String>,
    selectedUris: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
): Modifier {
    val currentSelectedUris by rememberUpdatedState(selectedUris)
    val currentOnSelectionChange by rememberUpdatedState(onSelectionChange)
    val currentGetItemUris by rememberUpdatedState(getItemUris)

    return this.pointerInput(enabled) {
        if (enabled) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val downPointer = event.changes.firstOrNull { it.pressed }
                    if (downPointer != null) {
                        val startPos = downPointer.position
                        val initialHitItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                            startPos.x >= item.offset.x && startPos.x <= item.offset.x + item.size.width &&
                            startPos.y >= item.offset.y && startPos.y <= item.offset.y + item.size.height
                        }
                        val initialKey = initialHitItem?.key
                        val selectedAtDown = currentSelectedUris

                        var isDragging = false
                        var isAddingMode = true
                        var passStartSelection = emptySet<String>()
                        val thisPassAdded = mutableSetOf<String>()
                        val thisPassRemoved = mutableSetOf<String>()
                        var currentSelected = currentSelectedUris
                        var lastHitKey: Any? = null

                        while (true) {
                            val moveEvent = awaitPointerEvent(PointerEventPass.Initial)
                            val activePointer = moveEvent.changes.firstOrNull { it.pressed }
                            if (activePointer == null) break

                            val currentPos = activePointer.position
                            val dist = (currentPos - startPos).getDistance()

                            if (dist > 10f) {
                                val latestSelected = currentSelectedUris
                                if (latestSelected.isNotEmpty()) {
                                    if (!isDragging) {
                                        isDragging = true
                                        val initialUris = if (initialKey != null) currentGetItemUris(initialKey) else emptySet()

                                        if (selectedAtDown.isEmpty()) {
                                            // Activated by long press during this press
                                            isAddingMode = true
                                            passStartSelection = latestSelected
                                            thisPassAdded.addAll(initialUris)
                                            currentSelected = latestSelected
                                        } else {
                                            val initialWasSelected = initialUris.isNotEmpty() && initialUris.all { it in selectedAtDown }
                                            isAddingMode = !initialWasSelected
                                            passStartSelection = selectedAtDown

                                            if (isAddingMode) {
                                                thisPassAdded.addAll(initialUris)
                                                currentSelected = passStartSelection + initialUris
                                            } else {
                                                thisPassRemoved.addAll(initialUris)
                                                currentSelected = passStartSelection - initialUris
                                            }
                                        }

                                        lastHitKey = initialKey
                                        if (currentSelected != latestSelected) {
                                            currentOnSelectionChange(currentSelected)
                                        }
                                    }
                                }
                            }

                            if (isDragging) {
                                activePointer.consume()

                                val viewportHeight = gridState.layoutInfo.viewportSize.height
                                if (currentPos.y > viewportHeight - 80) {
                                    gridState.dispatchRawDelta(15f)
                                } else if (currentPos.y < 80) {
                                    gridState.dispatchRawDelta(-15f)
                                }

                                val hitItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                                    currentPos.x >= item.offset.x && currentPos.x <= item.offset.x + item.size.width &&
                                    currentPos.y >= item.offset.y && currentPos.y <= item.offset.y + item.size.height
                                }
                                val currentKey = hitItem?.key
                                if (currentKey != null && currentKey != lastHitKey) {
                                    lastHitKey = currentKey
                                    val uris = currentGetItemUris(currentKey)
                                    if (uris.isNotEmpty()) {
                                        var updated = false
                                        val nextSelected = currentSelected.toMutableSet()

                                        if (isAddingMode) {
                                            for (uri in uris) {
                                                if (uri in thisPassAdded) {
                                                    // Selected in this go around -> keep selected
                                                    if (nextSelected.add(uri)) updated = true
                                                } else if (uri in passStartSelection) {
                                                    // Selected in a previous pass -> can deselect
                                                    if (nextSelected.remove(uri)) {
                                                        thisPassRemoved.add(uri)
                                                        updated = true
                                                    }
                                                } else {
                                                    // Unselected -> select it in this go around
                                                    if (nextSelected.add(uri)) {
                                                        thisPassAdded.add(uri)
                                                        updated = true
                                                    }
                                                }
                                            }
                                        } else {
                                            for (uri in uris) {
                                                if (uri in thisPassRemoved) {
                                                    // Removed in this go around -> keep removed
                                                    if (nextSelected.remove(uri)) updated = true
                                                } else if (uri !in passStartSelection) {
                                                    // Unselected in previous pass -> select it
                                                    if (nextSelected.add(uri)) {
                                                        thisPassAdded.add(uri)
                                                        updated = true
                                                    }
                                                } else {
                                                    // Selected in previous pass -> deselect it
                                                    if (nextSelected.remove(uri)) {
                                                        thisPassRemoved.add(uri)
                                                        updated = true
                                                    }
                                                }
                                            }
                                        }

                                        if (updated) {
                                            currentSelected = nextSelected
                                            currentOnSelectionChange(currentSelected)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectablePhotoCard(
    photo: SavedPhotoEntry,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDragSelect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TechCyan.copy(alpha = 0.2f) else SlateCard
        ),
        border = BorderStroke(
            if (isSelected) 2.5.dp else 1.dp,
            if (isSelected) TechCyan else BorderSlate
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.uriString,
                contentDescription = photo.fileName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .background(
                            if (isSelected) TechCyan else Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .border(1.5.dp, if (isSelected) TechCyan else Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = TerminalBg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = photo.fileName,
                    fontSize = 10.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GalleryTutorialOverlay(
    onDismiss: () -> Unit
) {
    androidx.activity.compose.BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1000f)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom > 1.1f || zoom < 0.9f) {
                        onDismiss()
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    onDismiss()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            val holeRadius = minOf(canvasWidth, canvasHeight) * 0.28f

            val radialBrush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.5f),
                    Color.Black.copy(alpha = 0.7f)
                ),
                center = Offset(centerX, centerY),
                radius = holeRadius * 1.35f
            )

            drawRect(
                brush = radialBrush,
                size = size
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.OpenInFull,
                contentDescription = null,
                tint = TechCyan,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pinch with fingers to Zoom",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pinch in or out with two fingers to adjust gallery columns.\n1 column (full width in portrait) up to 4 columns.",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    shadow = Shadow(Color.Black, Offset(1f, 1f), 3f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Filled.ArrowDownward,
                contentDescription = "Arrow pointing to hole",
                tint = TechCyan,
                modifier = Modifier.size(36.dp)
            )
        }

        // Okay button at lower right of screen, above all other elements
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 28.dp, end = 20.dp)
                .shadow(12.dp, RoundedCornerShape(10.dp))
                .zIndex(1001f),
            colors = ButtonDefaults.buttonColors(
                containerColor = TechCyan,
                contentColor = Color(0xFF121212)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "Okay",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var isGridListView by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<SavedPhotoEntry?>(null) }
    var photoToDelete by remember { mutableStateOf<SavedPhotoEntry?>(null) }

    var groupMode by remember { mutableStateOf(GalleryGroupMode.DIRECTORY) }
    var sortOption by remember { mutableStateOf(GallerySortOption.DATE_EXTRACTED) }
    var sortDirection by remember { mutableStateOf(GallerySortDirection.DESCENDING) }

    var selectedDirectory by remember { mutableStateOf<String?>(null) }
    var selectedVideo by remember { mutableStateOf<String?>(null) }

    var selectedPhotoUris by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    var columnCount by remember { mutableIntStateOf(3) } // 3 is default in portrait mode
    var totalZoom by remember { mutableFloatStateOf(1f) }

    var showTutorial by remember { mutableStateOf(!SettingsPersistence.getHasSeenGalleryTutorial(context)) }

    val dismissTutorial = {
        if (showTutorial) {
            showTutorial = false
            SettingsPersistence.setHasSeenGalleryTutorial(context, true)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val hasBackFolder = selectedVideo != null || selectedDirectory != null || selectedPhotoUris.isNotEmpty()
    androidx.activity.compose.BackHandler(enabled = hasBackFolder) {
        if (selectedPhotoUris.isNotEmpty()) {
            selectedPhotoUris = emptySet()
        } else if (selectedVideo != null && selectedDirectory != null && groupMode == GalleryGroupMode.DIRECTORY) {
            selectedVideo = null
        } else {
            selectedDirectory = null
            selectedVideo = null
        }
    }

    // Refresh rate for photos in gallery itself: (1/5)Hz = once every 5 seconds (5000ms)
    LaunchedEffect(Unit) {
        viewModel.initLastStateGalleryCount(context)
        viewModel.loadAndValidateSavedPhotos(context)
        while (coroutineContext.isActive) {
            kotlinx.coroutines.delay(5000L)
            viewModel.loadAndValidateSavedPhotos(context)
        }
    }

    val savedPhotos = viewModel.savedPhotos
    val filteredPhotos = remember(savedPhotos, searchQuery) {
        if (searchQuery.isBlank()) savedPhotos
        else savedPhotos.filter {
            it.fileName.contains(searchQuery, ignoreCase = true) ||
            it.videoName.contains(searchQuery, ignoreCase = true) ||
            it.directoryName.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showBatchDeleteConfirm) {
        val selectedPhotosList = savedPhotos.filter { it.uriString in selectedPhotoUris }
        val totalBytes = selectedPhotosList.sumOf { it.sizeBytes }
        val deletePermanently = SettingsPersistence.getDeletePhotosPermanently(context)

        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = {
                Text(
                    "Delete ${selectedPhotosList.size} Item(s)?",
                    fontWeight = FontWeight.Bold,
                    color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F)
                )
            },
            text = {
                Text(
                    if (deletePermanently)
                        "Are you sure you want to PERMANENTLY delete ${selectedPhotosList.size} photo(s) (${formatFileSize(totalBytes)})? This action cannot be undone."
                    else
                        "Move ${selectedPhotosList.size} photo(s) (${formatFileSize(totalBytes)}) to App Trash? Trashed files auto delete after 3 days.",
                    color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSavedPhotosBatch(context, selectedPhotosList)
                        selectedPhotoUris = emptySet()
                        showBatchDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("Cancel", color = CoolGrey)
                }
            },
            containerColor = if (ThemeConfig.isDarkTheme) TerminalBg else Color(0xFFF4F4F4)
        )
    }

    if (photoToDelete != null) {
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = {
                Text(
                    "Delete Saved Photo?",
                    fontWeight = FontWeight.Bold,
                    color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F)
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete '${photoToDelete?.fileName}' from your device gallery? This action cannot be undone.",
                    color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        photoToDelete?.let { viewModel.deleteSavedPhoto(context, it) }
                        photoToDelete = null
                        selectedPhoto = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { photoToDelete = null }) {
                    Text("Cancel", color = CoolGrey)
                }
            },
            containerColor = if (ThemeConfig.isDarkTheme) TerminalBg else Color(0xFFF4F4F4)
        )
    }

    if (selectedPhoto != null) {
        val photo = selectedPhoto!!
        val activePhotosList = if (filteredPhotos.isNotEmpty()) filteredPhotos else savedPhotos
        val photosForViewer = if (activePhotosList.any { it.uriString == photo.uriString }) activePhotosList else listOf(photo)
        FocusedPhotoViewer(
            photo = photo,
            photos = photosForViewer,
            onDismiss = { selectedPhoto = null },
            onDelete = { photoToDelete = it },
            onShare = { sharePhotoEntry ->
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(sharePhotoEntry.uriString))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Extracted Frame"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to share: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SAVED PHOTOS GALLERY",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight, letterSpacing = 0.5.sp),
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty() && !isSearchVisible) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear Search Text",
                                    tint = DeleteRed
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        val searchArrowRotation by animateFloatAsState(
                            targetValue = if (isSearchVisible) 180f else 0f,
                            label = "searchArrowRotation"
                        )

                        IconButton(
                            onClick = { isSearchVisible = !isSearchVisible },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Filled.KeyboardArrowUp else Icons.Filled.Search,
                                contentDescription = "Toggle Search Bar",
                                tint = TechCyan,
                                modifier = Modifier.graphicsLayer(rotationZ = if (isSearchVisible) 0f else searchArrowRotation)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isSearchVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search photo or video title...", color = CoolGrey, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = CoolGrey) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Clear", tint = DeleteRed)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechCyan,
                                unfocusedBorderColor = BorderSlate,
                                focusedContainerColor = SlateCard,
                                unfocusedContainerColor = SlateCard,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (savedPhotos.isNotEmpty()) {
            // Control panel for Group By, Grid/List view toggle, and Sort By
            if (isLandscape) {
                // Landscape Mode: [Grid/Detail][Group by][Sort By][Asc/Desc] in transparent container (max-width 2000px)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 2000.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 2000.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GridDetailButton(
                            isGridListView = isGridListView,
                            onClick = { isGridListView = !isGridListView },
                            modifier = Modifier.size(34.dp)
                        )

                        GroupByDropdown(
                            selectedMode = groupMode,
                            onModeSelected = { mode ->
                                groupMode = mode
                                selectedDirectory = null
                                selectedVideo = null
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SortByDropdown(
                            selectedSort = sortOption,
                            onSortSelected = { sortOption = it },
                            modifier = Modifier.weight(1f)
                        )

                        SortDirectionButton(
                            sortDirection = sortDirection,
                            onDirectionToggle = {
                                sortDirection = if (sortDirection == GallerySortDirection.ASCENDING) GallerySortDirection.DESCENDING else GallerySortDirection.ASCENDING
                            },
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            } else {
                // Portrait Mode: Line 1 [GROUP BY] [GRID/DETAIL], Line 2 [SORT BY] [ASC/DESC]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GroupByDropdown(
                            selectedMode = groupMode,
                            onModeSelected = { mode ->
                                groupMode = mode
                                selectedDirectory = null
                                selectedVideo = null
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        GridDetailButton(
                            isGridListView = isGridListView,
                            onClick = { isGridListView = !isGridListView },
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(0.55f)) {
                            SortByDropdown(
                                selectedSort = sortOption,
                                onSortSelected = { sortOption = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        SortDirectionButton(
                            sortDirection = sortDirection,
                            onDirectionToggle = {
                                sortDirection = if (sortDirection == GallerySortDirection.ASCENDING) GallerySortDirection.DESCENDING else GallerySortDirection.ASCENDING
                            },
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Drill-down Breadcrumb Navigation bar if inside a Folder or Video
        if (selectedDirectory != null || selectedVideo != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TechCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        if (selectedVideo != null && selectedDirectory != null && groupMode == GalleryGroupMode.DIRECTORY) {
                            selectedVideo = null
                        } else {
                            selectedDirectory = null
                            selectedVideo = null
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "Gallery",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable {
                            selectedDirectory = null
                            selectedVideo = null
                        }
                    )

                    if (selectedDirectory != null) {
                        Text(
                            text = " > ",
                            style = TextStyle(fontSize = 13.sp, color = CoolGrey, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "📂 $selectedDirectory",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                selectedVideo = null
                            }
                        )
                    }

                    if (selectedVideo != null) {
                        Text(
                            text = " > ",
                            style = TextStyle(fontSize = 13.sp, color = CoolGrey, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "🎥 $selectedVideo",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (filteredPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = "Empty Gallery",
                        tint = CoolGrey,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching photos found" else "No Extracted Photos Yet",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try typing a different search phrase" else "Saved frames from video extraction will appear here automatically.",
                        style = TextStyle(fontSize = 12.sp, color = CoolGrey)
                    )
                    if (savedPhotos.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onBack() },
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan)
                        ) {
                            Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Go to Frame Grabber", color = Color(0xFF22282A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            val gridStateDir = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
            val gridStateVid = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
            val gridStateImg = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(isLandscape) {
                        awaitPointerEventScope {
                            var zoomAccumulator = 1f
                            while (true) {
                                val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                val pointers = event.changes.filter { it.pressed }
                                if (pointers.size >= 2) {
                                    val p1 = pointers[0].position
                                    val p2 = pointers[1].position
                                    val prevP1 = pointers[0].previousPosition
                                    val prevP2 = pointers[1].previousPosition

                                    val currentDistance = (p1 - p2).getDistance()
                                    val previousDistance = (prevP1 - prevP2).getDistance()

                                    if (previousDistance > 0f && currentDistance > 0f) {
                                        val scale = currentDistance / previousDistance
                                        if (scale > 0.5f && scale < 2.0f) {
                                            zoomAccumulator *= scale
                                            if (zoomAccumulator > 1.15f) {
                                                if (columnCount > 1) {
                                                    columnCount--
                                                }
                                                zoomAccumulator = 1f
                                                dismissTutorial()
                                                pointers.forEach { it.consume() }
                                            } else if (zoomAccumulator < 0.85f) {
                                                val maxCols = 4
                                                if (columnCount < maxCols) {
                                                    columnCount++
                                                }
                                                zoomAccumulator = 1f
                                                dismissTutorial()
                                                pointers.forEach { it.consume() }
                                            }
                                        }
                                    }
                                } else {
                                    zoomAccumulator = 1f
                                }
                            }
                        }
                    }
            ) {
                when (groupMode) {
                    GalleryGroupMode.DIRECTORY -> {
                        if (selectedDirectory == null) {
                            // Level 1: Show Directory Folders
                            val dirGroups = sortDirectoryGroups(buildDirectoryGroups(filteredPhotos), sortOption, sortDirection)
                            val dirGroupsMap = remember(dirGroups) {
                                dirGroups.associate { group ->
                                    group.directoryName to group.videoGroups.flatMap { it.photos }.map { it.uriString }.toSet()
                                }
                            }
                            if (isGridListView) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columnCount),
                                    state = gridStateDir,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .gridDragSelect(
                                            gridState = gridStateDir,
                                            getItemUris = { key -> dirGroupsMap[key.toString()] ?: emptySet() },
                                            selectedUris = selectedPhotoUris,
                                            onSelectionChange = { selectedPhotoUris = it }
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(dirGroups, key = { it.directoryName }) { dirGroup ->
                                        val folderUris = dirGroup.videoGroups.flatMap { it.photos }.map { it.uriString }.toSet()
                                        val isFolderSelected = folderUris.isNotEmpty() && folderUris.all { it in selectedPhotoUris }
                                        FolderGridCard(
                                            directoryGroup = dirGroup,
                                            isSelected = isFolderSelected,
                                            onClick = {
                                                if (selectedPhotoUris.isNotEmpty()) {
                                                    selectedPhotoUris = if (isFolderSelected) selectedPhotoUris - folderUris else selectedPhotoUris + folderUris
                                                } else {
                                                    selectedDirectory = dirGroup.directoryName
                                                }
                                            },
                                            onLongClick = {
                                                selectedPhotoUris = if (isFolderSelected) selectedPhotoUris - folderUris else selectedPhotoUris + folderUris
                                            }
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(dirGroups, key = { it.directoryName }) { dirGroup ->
                                        val folderUris = dirGroup.videoGroups.flatMap { it.photos }.map { it.uriString }.toSet()
                                        val isFolderSelected = folderUris.isNotEmpty() && folderUris.all { it in selectedPhotoUris }
                                        FolderItemCard(
                                            directoryGroup = dirGroup,
                                            isSelected = isFolderSelected,
                                            onClick = {
                                                if (selectedPhotoUris.isNotEmpty()) {
                                                    selectedPhotoUris = if (isFolderSelected) selectedPhotoUris - folderUris else selectedPhotoUris + folderUris
                                                } else {
                                                    selectedDirectory = dirGroup.directoryName
                                                }
                                            },
                                            onLongClick = {
                                                selectedPhotoUris = if (isFolderSelected) selectedPhotoUris - folderUris else selectedPhotoUris + folderUris
                                            }
                                        )
                                    }
                                }
                            }
                        } else if (selectedVideo == null) {
                            // Level 2: Show Videos in Directory
                            val photosInDir = filteredPhotos.filter { it.directoryName == selectedDirectory }
                            val vidGroups = sortVideoGroups(buildVideoGroups(photosInDir), sortOption, sortDirection)
                            val vidGroupsMap = remember(vidGroups) {
                                vidGroups.associate { group ->
                                    group.videoName to group.photos.map { it.uriString }.toSet()
                                }
                            }
                            if (isGridListView) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columnCount),
                                    state = gridStateVid,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .gridDragSelect(
                                            gridState = gridStateVid,
                                            getItemUris = { key -> vidGroupsMap[key.toString()] ?: emptySet() },
                                            selectedUris = selectedPhotoUris,
                                            onSelectionChange = { selectedPhotoUris = it }
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(vidGroups, key = { it.videoName }) { vidGroup ->
                                        val vidUris = vidGroup.photos.map { it.uriString }.toSet()
                                        val isVidSelected = vidUris.isNotEmpty() && vidUris.all { it in selectedPhotoUris }
                                        VideoGridCard(
                                            videoGroup = vidGroup,
                                            isSelected = isVidSelected,
                                            onClick = {
                                                if (selectedPhotoUris.isNotEmpty()) {
                                                    selectedPhotoUris = if (isVidSelected) selectedPhotoUris - vidUris else selectedPhotoUris + vidUris
                                                } else {
                                                    selectedVideo = vidGroup.videoName
                                                }
                                            },
                                            onLongClick = {
                                                selectedPhotoUris = if (isVidSelected) selectedPhotoUris - vidUris else selectedPhotoUris + vidUris
                                            }
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(vidGroups, key = { it.videoName }) { vidGroup ->
                                        val vidUris = vidGroup.photos.map { it.uriString }.toSet()
                                        val isVidSelected = vidUris.isNotEmpty() && vidUris.all { it in selectedPhotoUris }
                                        VideoItemCard(
                                            videoGroup = vidGroup,
                                            isSelected = isVidSelected,
                                            onClick = {
                                                if (selectedPhotoUris.isNotEmpty()) {
                                                    selectedPhotoUris = if (isVidSelected) selectedPhotoUris - vidUris else selectedPhotoUris + vidUris
                                                } else {
                                                    selectedVideo = vidGroup.videoName
                                                }
                                            },
                                            onLongClick = {
                                                selectedPhotoUris = if (isVidSelected) selectedPhotoUris - vidUris else selectedPhotoUris + vidUris
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Level 3: Show Frames in Video (Grid columns zoomable)
                            val frames = sortPhotos(
                                filteredPhotos.filter { it.directoryName == selectedDirectory && it.videoName == selectedVideo },
                                sortOption,
                                sortDirection
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columnCount),
                                state = gridStateDir,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .gridDragSelect(
                                        gridState = gridStateDir,
                                        getItemUris = { key -> setOf(key.toString()) },
                                        selectedUris = selectedPhotoUris,
                                        onSelectionChange = { selectedPhotoUris = it }
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(frames, key = { it.uriString }) { photo ->
                                    val isSelected = photo.uriString in selectedPhotoUris
                                    SelectablePhotoCard(
                                        photo = photo,
                                        isSelected = isSelected,
                                        isSelectionMode = selectedPhotoUris.isNotEmpty(),
                                        onClick = {
                                            if (selectedPhotoUris.isNotEmpty()) {
                                                selectedPhotoUris = if (isSelected) selectedPhotoUris - photo.uriString else selectedPhotoUris + photo.uriString
                                            } else {
                                                selectedPhoto = photo
                                            }
                                        },
                                        onLongClick = {
                                            selectedPhotoUris = if (isSelected) selectedPhotoUris - photo.uriString else selectedPhotoUris + photo.uriString
                                        },
                                        onDragSelect = {
                                            if (selectedPhotoUris.isNotEmpty() && photo.uriString !in selectedPhotoUris) {
                                                selectedPhotoUris = selectedPhotoUris + photo.uriString
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    GalleryGroupMode.VIDEO -> {
                        if (selectedVideo == null) {
                            // Level 1: Show Videos
                            val vidGroups = sortVideoGroups(buildVideoGroups(filteredPhotos), sortOption, sortDirection)
                            val vidGroupsMap = remember(vidGroups) {
                                vidGroups.associate { group ->
                                    group.videoName to group.photos.map { it.uriString }.toSet()
                                }
                            }
                            if (isGridListView) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columnCount),
                                    state = gridStateVid,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .gridDragSelect(
                                            gridState = gridStateVid,
                                            getItemUris = { key -> vidGroupsMap[key.toString()] ?: emptySet() },
                                            selectedUris = selectedPhotoUris,
                                            onSelectionChange = { selectedPhotoUris = it }
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(vidGroups, key = { it.videoName }) { vidGroup ->
                                        val vidUris = vidGroup.photos.map { it.uriString }.toSet()
                                        val isVidSelected = vidUris.isNotEmpty() && vidUris.all { it in selectedPhotoUris }
                                        VideoGridCard(
                                            videoGroup = vidGroup,
                                            isSelected = isVidSelected,
                                            onClick = {
                                                if (selectedPhotoUris.isNotEmpty()) {
                                                    selectedPhotoUris = if (isVidSelected) selectedPhotoUris - vidUris else selectedPhotoUris + vidUris
                                                } else {
                                                    selectedVideo = vidGroup.videoName
                                                }
                                            },
                                            onLongClick = {
                                                selectedPhotoUris = if (isVidSelected) selectedPhotoUris - vidUris else selectedPhotoUris + vidUris
                                            }
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(vidGroups, key = { it.videoName }) { vidGroup ->
                                        val vidUris = vidGroup.photos.map { it.uriString }.toSet()
                                        val isVidSelected = vidUris.isNotEmpty() && vidUris.all { it in selectedPhotoUris }
                                        VideoItemCard(
                                            videoGroup = vidGroup,
                                            isSelected = isVidSelected,
                                            onClick = {
                                                if (selectedPhotoUris.isNotEmpty()) {
                                                    selectedPhotoUris = if (isVidSelected) selectedPhotoUris - vidUris else selectedPhotoUris + vidUris
                                                } else {
                                                    selectedVideo = vidGroup.videoName
                                                }
                                            },
                                            onLongClick = {
                                                selectedPhotoUris = if (isVidSelected) selectedPhotoUris - vidUris else selectedPhotoUris + vidUris
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Level 2: Show Frames in Video (Grid columns zoomable)
                            val frames = sortPhotos(
                                filteredPhotos.filter { it.videoName == selectedVideo },
                                sortOption,
                                sortDirection
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columnCount),
                                state = gridStateVid,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .gridDragSelect(
                                        gridState = gridStateVid,
                                        getItemUris = { key -> setOf(key.toString()) },
                                        selectedUris = selectedPhotoUris,
                                        onSelectionChange = { selectedPhotoUris = it }
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(frames, key = { it.uriString }) { photo ->
                                    val isSelected = photo.uriString in selectedPhotoUris
                                    SelectablePhotoCard(
                                        photo = photo,
                                        isSelected = isSelected,
                                        isSelectionMode = selectedPhotoUris.isNotEmpty(),
                                        onClick = {
                                            if (selectedPhotoUris.isNotEmpty()) {
                                                selectedPhotoUris = if (isSelected) selectedPhotoUris - photo.uriString else selectedPhotoUris + photo.uriString
                                            } else {
                                                selectedPhoto = photo
                                            }
                                        },
                                        onLongClick = {
                                            selectedPhotoUris = if (isSelected) selectedPhotoUris - photo.uriString else selectedPhotoUris + photo.uriString
                                        },
                                        onDragSelect = {
                                            if (selectedPhotoUris.isNotEmpty() && photo.uriString !in selectedPhotoUris) {
                                                selectedPhotoUris = selectedPhotoUris + photo.uriString
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    GalleryGroupMode.IMAGES -> {
                        val sortedPhotos = sortPhotos(filteredPhotos, sortOption, sortDirection)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columnCount),
                            state = gridStateImg,
                            modifier = Modifier
                                .fillMaxSize()
                                .gridDragSelect(
                                    gridState = gridStateImg,
                                    getItemUris = { key -> setOf(key.toString()) },
                                    selectedUris = selectedPhotoUris,
                                    onSelectionChange = { selectedPhotoUris = it }
                                ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sortedPhotos, key = { it.uriString }) { photo ->
                                val isSelected = photo.uriString in selectedPhotoUris
                                SelectablePhotoCard(
                                    photo = photo,
                                    isSelected = isSelected,
                                    isSelectionMode = selectedPhotoUris.isNotEmpty(),
                                    onClick = {
                                        if (selectedPhotoUris.isNotEmpty()) {
                                            selectedPhotoUris = if (isSelected) selectedPhotoUris - photo.uriString else selectedPhotoUris + photo.uriString
                                        } else {
                                            selectedPhoto = photo
                                        }
                                    },
                                    onLongClick = {
                                        selectedPhotoUris = if (isSelected) selectedPhotoUris - photo.uriString else selectedPhotoUris + photo.uriString
                                    },
                                    onDragSelect = {
                                        if (selectedPhotoUris.isNotEmpty() && photo.uriString !in selectedPhotoUris) {
                                            selectedPhotoUris = selectedPhotoUris + photo.uriString
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom selection bar (Samsung Gallery Style)
        if (selectedPhotoUris.isNotEmpty()) {
            val selectedList = savedPhotos.filter { it.uriString in selectedPhotoUris }
            val totalSizeBytes = selectedList.sumOf { it.sizeBytes }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = TerminalBg,
                tonalElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedList.size} Item${if (selectedList.size == 1) "" else "s"} Selected",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                        )
                        Text(
                            text = "Total Size: ${formatFileSize(totalSizeBytes)}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = TechCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentVisibleUris = filteredPhotos.map { it.uriString }.toSet()
                        TextButton(
                            onClick = {
                                selectedPhotoUris = if (selectedPhotoUris.containsAll(currentVisibleUris)) {
                                    emptySet()
                                } else {
                                    selectedPhotoUris + currentVisibleUris
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedPhotoUris.containsAll(currentVisibleUris)) "Deselect All" else "Select All",
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { showBatchDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DeleteRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DELETE",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Version Footer
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

        // Show silhouette tutorial on first usage
        if (showTutorial) {
            GalleryTutorialOverlay(
                onDismiss = dismissTutorial
            )
        }
    }
}
