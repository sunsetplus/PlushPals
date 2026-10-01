package com.example.ui.screens.reels

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.ReelEntity
import com.example.ui.components.PlushieImage
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.WarmCaramel

@Composable
fun ReelsScreen(
    reels: List<ReelEntity>,
    onLikeClicked: (ReelEntity) -> Unit,
    onCommentClicked: (ReelEntity) -> Unit,
    onAuthorClicked: (String) -> Unit
) {
    if (reels.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.VideoCall,
                    contentDescription = null,
                    tint = WarmCaramel,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No plushie reels yet!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Share a short comforting clip of your dog plushie.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("reels_list"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(reels, key = { it.id }) { reel ->
                ReelItem(
                    reel = reel,
                    onLikeClicked = { onLikeClicked(reel) },
                    onCommentClicked = { onCommentClicked(reel) },
                    onAuthorClicked = { onAuthorClicked(reel.authorName) }
                )
            }
        }
    }
}

@Composable
fun ReelItem(
    reel: ReelEntity,
    onLikeClicked: () -> Unit,
    onCommentClicked: () -> Unit,
    onAuthorClicked: () -> Unit
) {
    // Gentle breathing scale animation to simulate comforting rhythmic plushie chest movement
    val infiniteTransition = rememberInfiniteTransition(label = "plushie_breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(580.dp)
            .padding(vertical = 4.dp)
            .testTag("reel_item_${reel.id}")
    ) {
        // Background animated image / video representation
        PlushieImage(
            uri = reel.mediaUri,
            modifier = Modifier
                .fillMaxSize()
                .scale(breathScale),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Right side action bar (Like, Comment, Share, Audio)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLikeClicked,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("reel_like_btn_${reel.id}")
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like Reel",
                        tint = if (reel.isLiked) Color(0xFFE91E63) else Color.White
                    )
                }
                Text(
                    text = "${reel.likeCount}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comments
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentClicked,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("reel_comment_btn_${reel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White
                    )
                }
                Text(
                    text = "${reel.commentCount}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Rotating audio disc indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WarmCaramel),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom left details: Author, Caption, Audio Track
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 16.dp, bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAuthorClicked() }
            ) {
                PlushieImage(
                    uri = reel.authorAvatarUri,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reel.authorName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (reel.isAuthorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerificationBadge(size = 15.dp)
                        }
                    }
                    Text(
                        text = "🐾 ${reel.plushieName} (${reel.plushieBreed})",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reel.caption,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                lineHeight = 18.sp
            )

            if (reel.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reel.tags,
                    color = Color(0xFFFFCC80),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Audio track pill
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reel.audioTrack,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
