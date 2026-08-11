package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.MarqueeSpacing
import com.example.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusedPhotoViewer(
    photo: SavedPhotoEntry,
    photos: List<SavedPhotoEntry> = listOf(photo),
    onDismiss: () -> Unit,
    onDelete: (SavedPhotoEntry) -> Unit,
    onShare: (SavedPhotoEntry) -> Unit
) {
    val initialIndex = remember(photo, photos) {
        val idx = photos.indexOfFirst { it.uriString == photo.uriString }
        if (idx >= 0) idx else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )

    val currentPhoto = photos.getOrNull(pagerState.currentPage) ?: photo

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showBars by remember { mutableStateOf(true) }
    var isInfoSheetExpanded by remember { mutableStateOf(false) }
    var isClosing by remember { mutableStateOf(false) }

    val animScale = remember { Animatable(1f) }
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }

    val coroutineScope = rememberCoroutineScope()
    val carouselListState = rememberLazyListState()

    // Synchronize Carousel Scroll -> Pager State (at static 15Hz ~ 66ms rate)
    LaunchedEffect(carouselListState) {
        snapshotFlow {
            if (carouselListState.isScrollInProgress) {
                val layoutInfo = carouselListState.layoutInfo
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closestItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    Math.abs(itemCenter - viewportCenter)
                }
                closestItem?.index
            } else null
        }
        .filterNotNull()
        .distinctUntilChanged()
        .collect { targetIndex ->
            delay(66) // Static rate limit (~15Hz)
            if (pagerState.currentPage != targetIndex) {
                pagerState.scrollToPage(targetIndex)
            }
        }
    }

    // Reset zoom & scroll thumbnail ribbon on page change
    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        animScale.snapTo(1f)
        animOffsetX.snapTo(0f)
        animOffsetY.snapTo(0f)
        animAlpha.snapTo(1f)

        if (!carouselListState.isScrollInProgress && photos.isNotEmpty()) {
            val layoutInfo = carouselListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val itemWidth = visibleItems.first().size
                val viewportWidth = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                val offsetToCenter = (viewportWidth - itemWidth) / 2
                carouselListState.animateScrollToItem(pagerState.currentPage, -offsetToCenter)
            } else {
                val targetIndex = (pagerState.currentPage - 3).coerceAtLeast(0)
                carouselListState.animateScrollToItem(targetIndex)
            }
        }
    }

    val triggerMinimizeAndDismiss: () -> Unit = {
        if (!isClosing) {
            isClosing = true
            coroutineScope.launch {
                launch { animScale.animateTo(0.35f, tween(180, easing = LinearOutSlowInEasing)) }
                launch { animOffsetY.animateTo(2500f, tween(180, easing = LinearOutSlowInEasing)) }
                launch { animAlpha.animateTo(0f, tween(180, easing = LinearOutSlowInEasing)) }
            }.invokeOnCompletion {
                onDismiss()
            }
        }
    }

    androidx.activity.compose.BackHandler {
        if (isInfoSheetExpanded) {
            isInfoSheetExpanded = false
        } else {
            triggerMinimizeAndDismiss()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            if (isInfoSheetExpanded) {
                isInfoSheetExpanded = false
            } else {
                triggerMinimizeAndDismiss()
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = animAlpha.value.coerceIn(0f, 1f)))
        ) {
            val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val containerHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
            val wiggleThresholdPx = with(LocalDensity.current) { 40.dp.toPx() }
            val dismissThresholdPx = with(LocalDensity.current) { 120.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(isInfoSheetExpanded) {
                        detectTapGestures(
                            onTap = {
                                if (isInfoSheetExpanded) {
                                    isInfoSheetExpanded = false
                                } else {
                                    showBars = !showBars
                                }
                            },
                            onDoubleTap = {
                                if (isClosing) return@detectTapGestures
                                coroutineScope.launch {
                                    if (scale > 1.2f) {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                        launch { animScale.animateTo(1f, tween(200)) }
                                        launch { animOffsetX.animateTo(0f, tween(200)) }
                                        launch { animOffsetY.animateTo(0f, tween(200)) }
                                        launch { animAlpha.animateTo(1f, tween(200)) }
                                    } else {
                                        scale = 2.5f
                                        offsetX = 0f
                                        offsetY = 0f
                                        launch { animScale.animateTo(2.5f, tween(200)) }
                                        launch { animOffsetX.animateTo(0f, tween(200)) }
                                        launch { animOffsetY.animateTo(0f, tween(200)) }
                                        launch { animAlpha.animateTo(1f, tween(200)) }
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(containerHeightPx, containerWidthPx, isInfoSheetExpanded) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            var zoom = 1f
                            var panX = 0f
                            var panY = 0f
                            var pastTouchSlop = false
                            val touchSlop = viewConfiguration.touchSlop
                            var dragOffsetY = 0f
                            var dragOffsetX = 0f
                            var verticalDirection = 0

                            do {
                                val event = awaitPointerEvent()
                                val canceled = event.changes.any { it.isConsumed }
                                if (!canceled) {
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()

                                    if (!pastTouchSlop) {
                                        zoom *= zoomChange
                                        panX += panChange.x
                                        panY += panChange.y
                                        val panMotion = panY * panY + panX * panX
                                        if (zoom != 1f || panMotion > touchSlop * touchSlop) {
                                            pastTouchSlop = true
                                            if (scale <= 1.05f && Math.abs(panY) > Math.abs(panX)) {
                                                if (panY > 0) {
                                                    verticalDirection = 1 // Dragging DOWN
                                                } else if (panY < 0) {
                                                    verticalDirection = -1 // Dragging UP
                                                }
                                            }
                                        }
                                    }

                                    if (pastTouchSlop && !isClosing) {
                                        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                                        scale = newScale

                                        if (scale > 1.05f) {
                                            // Zoomed in mode: smooth pan
                                            offsetX += panChange.x
                                            offsetY += panChange.y
                                            coroutineScope.launch {
                                                animScale.snapTo(scale)
                                                animOffsetX.snapTo(offsetX)
                                                animOffsetY.snapTo(offsetY)
                                                animAlpha.snapTo(1f)
                                            }
                                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                                        } else if (verticalDirection != 0) {
                                            dragOffsetX += panChange.x
                                            dragOffsetY += panChange.y

                                            if (verticalDirection == 1 && !isInfoSheetExpanded) {
                                                // Standard state (1x zoom): pull down to minimize
                                                val effectiveDrag = (dragOffsetY - wiggleThresholdPx).coerceAtLeast(0f)
                                                val alphaVal = (1f - effectiveDrag / (containerHeightPx * 0.5f)).coerceIn(0.2f, 1f)
                                                val scaleVal = (1f - effectiveDrag / (containerHeightPx * 2f)).coerceIn(0.65f, 1f)

                                                coroutineScope.launch {
                                                    animScale.snapTo(scaleVal)
                                                    animOffsetX.snapTo(dragOffsetX * 0.3f)
                                                    animOffsetY.snapTo(effectiveDrag)
                                                    animAlpha.snapTo(alphaVal)
                                                }
                                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                                            } else if (verticalDirection == -1 || isInfoSheetExpanded) {
                                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                                            }
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            // Pointer up / Finger released
                            if (!isClosing && pastTouchSlop) {
                                if (scale > 1.05f) {
                                    val renderedImageHeight = containerHeightPx
                                    val imageTopInScreen = (containerHeightPx - renderedImageHeight * scale) / 2f + offsetY
                                    val exitThreshold = containerHeightPx * 0.20f

                                    if (imageTopInScreen >= exitThreshold) {
                                        triggerMinimizeAndDismiss()
                                    } else if (imageTopInScreen > 0f) {
                                        val snapBackOffsetY = (renderedImageHeight * scale - containerHeightPx) / 2f
                                        val maxOffsetX = ((containerWidthPx * scale - containerWidthPx) / 2f).coerceAtLeast(0f)
                                        val snapBackOffsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)

                                        offsetY = snapBackOffsetY
                                        offsetX = snapBackOffsetX

                                        coroutineScope.launch {
                                            launch { animOffsetY.animateTo(snapBackOffsetY, tween(150, easing = FastOutSlowInEasing)) }
                                            launch { animOffsetX.animateTo(snapBackOffsetX, tween(150, easing = FastOutSlowInEasing)) }
                                            launch { animScale.animateTo(scale, tween(150, easing = FastOutSlowInEasing)) }
                                        }
                                    } else {
                                        val maxOffsetX = ((containerWidthPx * scale - containerWidthPx) / 2f).coerceAtLeast(0f)
                                        val snapBackOffsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                        if (snapBackOffsetX != offsetX) {
                                            offsetX = snapBackOffsetX
                                            coroutineScope.launch {
                                                animOffsetX.animateTo(snapBackOffsetX, tween(150, easing = FastOutSlowInEasing))
                                            }
                                        }
                                    }
                                } else if (verticalDirection == 1) { // Drag DOWN
                                    if (isInfoSheetExpanded) {
                                        // Deliberate swiping down anywhere on screen closes photo info state
                                        isInfoSheetExpanded = false
                                    } else {
                                        if (dragOffsetY >= dismissThresholdPx) {
                                            triggerMinimizeAndDismiss()
                                        } else {
                                            coroutineScope.launch {
                                                launch { animOffsetY.animateTo(0f, tween(150, easing = FastOutSlowInEasing)) }
                                                launch { animOffsetX.animateTo(0f, tween(150, easing = FastOutSlowInEasing)) }
                                                launch { animScale.animateTo(1f, tween(150, easing = FastOutSlowInEasing)) }
                                                launch { animAlpha.animateTo(1f, tween(150, easing = FastOutSlowInEasing)) }
                                            }
                                        }
                                    }
                                } else if (verticalDirection == -1) { // Drag UP
                                    if (!isInfoSheetExpanded && Math.abs(dragOffsetY) >= wiggleThresholdPx) {
                                        // Up-swipe (> 40dp margin) activates Photo Info state!
                                        isInfoSheetExpanded = true
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = (scale <= 1.05f),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (isInfoSheetExpanded) 240.dp else 0.dp)
                ) { page ->
                    val pagePhoto = photos.getOrNull(page) ?: currentPhoto
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = pagePhoto.uriString,
                            contentDescription = pagePhoto.fileName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(
                                    scaleX = if (page == pagerState.currentPage) animScale.value else 1f,
                                    scaleY = if (page == pagerState.currentPage) animScale.value else 1f,
                                    translationX = if (page == pagerState.currentPage) animOffsetX.value else 0f,
                                    translationY = if (page == pagerState.currentPage) animOffsetY.value else 0f
                                ),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }

            // Top Header Bar (visible in normal state when showBars is true)
            AnimatedVisibility(
                visible = showBars && !isInfoSheetExpanded && !isClosing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentPhoto.fileName,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentPhoto.videoName} • ${formatDuration(currentPhoto.timestampMs)}",
                            style = TextStyle(fontSize = 11.sp, color = CoolGrey),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { triggerMinimizeAndDismiss() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Bottom Thumbnail Filmstrip Carousel
            AnimatedVisibility(
                visible = showBars && !isInfoSheetExpanded && !isClosing && photos.size > 1,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200, easing = LinearOutSlowInEasing)) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF101014).copy(alpha = 0.96f))
                        .padding(top = 10.dp, bottom = 28.dp)
                ) {
                    LazyRow(
                        state = carouselListState,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        itemsIndexed(photos, key = { index, item -> item.uriString }) { index, item ->
                            val isSelected = index == pagerState.currentPage
                            val targetScale = if (isSelected) 1.08f else 0.95f
                            val animatedScale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = tween(150),
                                label = "carousel_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                        alpha = if (isSelected) 1f else 0.55f
                                    }
                                    .clip(RoundedCornerShape(5.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(index)
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = item.uriString,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Photo Info Sheet Container (Activated via Swipe Up ~40dp, takes up lower part of screen like Samsung Gallery)
            AnimatedVisibility(
                visible = isInfoSheetExpanded && !isClosing,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(220, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(180, easing = LinearOutSlowInEasing)) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = Color(0xFF1C1C22).copy(alpha = 0.98f),
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Drag Indicator Bar
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.4f), CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )

                        // Title / Header
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = currentPhoto.fileName,
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    initialDelayMillis = 3000,
                                    repeatDelayMillis = 3000,
                                    spacing = MarqueeSpacing(24.dp)
                                )
                            )
                            Text(
                                text = "${currentPhoto.videoName} • ${formatDuration(currentPhoto.timestampMs)}",
                                style = TextStyle(fontSize = 12.sp, color = CoolGrey),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                        // Details Section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Directory", fontSize = 12.sp, color = CoolGrey)
                                Text(currentPhoto.directoryName, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Size", fontSize = 12.sp, color = CoolGrey)
                                Text(formatFileSize(currentPhoto.sizeBytes), fontSize = 12.sp, color = TechCyan, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Action Buttons: Share & Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onShare(currentPhoto) },
                                border = BorderStroke(1.dp, TechCyan),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onDelete(currentPhoto) },
                                colors = ButtonDefaults.buttonColors(containerColor = DeleteRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
