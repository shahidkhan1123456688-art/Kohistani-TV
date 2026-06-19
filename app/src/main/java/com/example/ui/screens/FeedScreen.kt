package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Comment
import com.example.data.model.VideoItem
import com.example.ui.KohistaniTvViewModel
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: KohistaniTvViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val activeIndex by viewModel.activeVideoIndex.collectAsStateWithLifecycle()
    val activeComments by viewModel.activeComments.collectAsStateWithLifecycle()
    val activeCommentsVideoId by viewModel.activeVideoIdForComments.collectAsStateWithLifecycle()
    
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categories = listOf("سب", "ثقافت", "لوک موسیقی", "منظر خوبصورت", "ڈرامہ و مزاح", "کھیل و روایات")

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // TikTok vertical swiping feed using VerticalPager
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { videos.size })

            // Keep ViewModel selection in sync with pager state
            LaunchedEffect(pagerState.currentPage) {
                viewModel.setActiveVideoIndex(pagerState.currentPage)
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val video = videos.getOrNull(page)
                if (video != null) {
                    FeedVideoItem(
                        video = video,
                        viewModel = viewModel,
                        isActive = (page == activeIndex),
                        onOpenComments = { viewModel.openCommentsFor(video.id) }
                    )
                }
            }
        }

        // Top Category Navigation Overlay (TikTok Style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { category ->
                            val isSelected = category == selectedCategory
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .clickable { viewModel.setCategory(category) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.65f),
                                    fontSize = if (isSelected) 17.sp else 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(2.dp)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Comments Bottom Sheet Dialog
        if (activeCommentsVideoId != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeComments() },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                CommentSheetContent(
                    comments = activeComments,
                    onAddComment = { text -> viewModel.addComment(activeCommentsVideoId!!, text) },
                    onClose = { viewModel.closeComments() }
                )
            }
        }
    }
}

@Composable
fun FeedVideoItem(
    video: VideoItem,
    viewModel: KohistaniTvViewModel,
    isActive: Boolean,
    onOpenComments: () -> Unit
) {
    var doubleTapHeartTrigger by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                    }
                )
            }
            .clickable {
                // Dual action simulation: double tap to like
                viewModel.likeVideo(video)
                doubleTapHeartTrigger = true
            }
    ) {
        // Background Video Player Simulation (gorgeous native abstract fluid art)
        VideoSimulationCanvas(
            videoColorHex = video.videoColorHex,
            isActive = isActive
        )

        // Bottom and right shadow scrim for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
            )

        // Live status indicator (floating badges)
        if (video.isLive) {
            Row(
                modifier = Modifier
                    .padding(top = 110.dp, start = 16.dp)
                    .background(Color.Red, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "لائیو",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right side floating control panel (TikTok profile, like, comments, shares, audio disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & Follow Action
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clickable {
                        viewModel.selectCreator(video.username)
                        viewModel.setTab("profile")
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "R",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Small follow plus button
                if (!video.isFollowed) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 1.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.followCreator(video) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Follow",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Like Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val likeScale by animateFloatAsState(
                    targetValue = if (video.isLiked) 1.25f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                )
                Icon(
                    imageVector = if (video.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (video.isLiked) MaterialTheme.colorScheme.tertiary else Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .scale(likeScale)
                        .clickable { viewModel.likeVideo(video) }
                        .testTag("like_button")
                )
                Text(
                    text = video.likesCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comments Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Comment,
                    contentDescription = "Comments",
                    tint = Color.White,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { onOpenComments() }
                        .testTag("comments_button")
                )
                Text(
                    text = video.commentsCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Share Action
            var isShareSheetOpened by remember { mutableStateOf(false) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            isShareSheetOpened = true
                            viewModel.shareVideo(video)
                        }
                        .testTag("share_button")
                )
                Text(
                    text = video.sharesCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Spinning music record overlay
            MusicRecordDisc(soundName = video.soundName, isActive = isActive)
        }

        // Left Side metadata and description overlays
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.72f)
                .padding(start = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "@${video.username}",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    viewModel.selectCreator(video.username)
                    viewModel.setTab("profile")
                }
            )

            Text(
                text = video.videoTitle,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = video.description,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Category tag
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "# ${video.category}",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Scrolling Audio track name marquee representation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = video.soundName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Live Chat box floating simulation if video is a live streaming channel
        if (video.isLive) {
            LiveStreamingPanel(viewModel = viewModel)
        }
    }
}

// Simulated active motion graphics representing video content
@Composable
fun VideoSimulationCanvas(
    videoColorHex: String,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedPhase = if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * java.lang.Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val waveHeight = if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = 40f,
            targetValue = 180f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(80f) }
    }

    val baseColor = Color(android.graphics.Color.parseColor(videoColorHex))

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Fill background with subtle gradient base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.85f),
                    Color(0xFF0F0F12),
                    Color.Black
                )
            )
        )

        // Draw animated Kohistani dynamic mountain peaks
        val path1 = Path()
        val path2 = Path()

        path1.moveTo(0f, height)
        path2.moveTo(0f, height)

        val steps = 80
        for (i in 0..steps) {
            val x = (width / steps) * i
            
            // Peak 1: culture theme waves representing Kohistani rugged ridges
            val y1 = (height * 0.55f) + 
                    sin((i.toFloat() / steps * 4f) * java.lang.Math.PI.toFloat() + animatedPhase.value) * waveHeight.value * 0.4f +
                    sin((i.toFloat() / steps * 8f) * java.lang.Math.PI.toFloat()) * 20f

            // Peak 2: higher ridges
            val y2 = (height * 0.65f) + 
                    sin((i.toFloat() / steps * 3f) * java.lang.Math.PI.toFloat() - animatedPhase.value) * waveHeight.value * 0.6f +
                    sin((i.toFloat() / steps * 6f) * java.lang.Math.PI.toFloat()) * 30f

            path1.lineTo(x, y1)
            path2.lineTo(x, y2)
        }

        path1.lineTo(width, height)
        path1.close()
        path2.lineTo(width, height)
        path2.close()

        // Draw Peak 2 (Back mountain)
        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.25f),
                    Color(0xFF1E1E24).copy(alpha = 0.4f)
                )
            )
        )

        // Draw Peak 1 (Front mountain) with bright highlight
        drawPath(
            path = path1,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF00F5D4).copy(alpha = 0.15f),
                    Color.Black.copy(alpha = 0.8f)
                )
            )
        )

        // Draw sound frequencies or falling starry nodes in background
        val particleCount = 12
        for (i in 1..particleCount) {
            val particlePhase = animatedPhase.value + (i * 0.5f)
            val px = (width / particleCount) * (i - 0.5f)
            val py = (height * 0.35f) + sin(particlePhase) * 120f
            
            drawCircle(
                color = Color(0xFFFF9F1C).copy(alpha = 0.3f),
                radius = 6f + sin(particlePhase) * 3f,
                center = Offset(px, py)
            )
        }
    }
}

@Composable
fun MusicRecordDisc(soundName: String, isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .rotate(rotationAngle)
            .clip(CircleShape)
            .background(Color(0xFF111115))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner vinyl decoration
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Color.DarkGray,
                            Color.Black,
                            Color.DarkGray,
                            Color.Black
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

// Comments Sheet List
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CommentSheetContent(
    comments: List<Comment>,
    onAddComment: (String) -> Unit,
    onClose: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .heightIn(max = 500.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
            Text(
                text = "${comments.size} کمنٹس",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // Equalizer spacing
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Comments List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(comments) { comment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Profile Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.username.take(1),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = comment.username,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = comment.commentText,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input field for writing comments
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("comment_input"),
                placeholder = { Text("کمنٹ لکھیں...") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
                maxLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (commentText.isNotBlank()) {
                        onAddComment(commentText)
                        commentText = ""
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onAddComment(commentText)
                        commentText = ""
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .size(40.dp)
                    .testTag("send_comment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Live Streaming chat and heart actions overlay
@Composable
fun LiveStreamingPanel(
    viewModel: KohistaniTvViewModel
) {
    val chatMessages by viewModel.liveChatMessages.collectAsStateWithLifecycle()
    val heartsCount by viewModel.floatingHeartsCount.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 140.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Live Chat Roll (left)
            LazyColumn(
                modifier = Modifier
                    .weight(0.72f)
                    .height(180.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                reverseLayout = false
            ) {
                items(chatMessages) { message ->
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${message.first}: ",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.second,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Float heart trigger button (right)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Display floating hearts counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "H",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = heartsCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.emitLiveHeart() },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        .size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Burst Heart",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// Extension extension function for scale property inside Modifier
private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.pointerInput(scale) {
        // Just empty to consume or can be completely styled
    }
)
