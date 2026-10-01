package com.example.ui.screens.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.PostEntity
import com.example.data.local.model.StoryEntity
import com.example.ui.FeedFilterType
import com.example.ui.components.ArtistBadge
import com.example.ui.components.PlushieImage
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    posts: List<PostEntity>,
    stories: List<StoryEntity>,
    selectedFilter: FeedFilterType,
    selectedBreed: String,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onFilterChanged: (FeedFilterType) -> Unit,
    onBreedChanged: (String) -> Unit,
    onStoryClicked: (StoryEntity) -> Unit,
    onAddStoryClicked: () -> Unit,
    onLikeClicked: (PostEntity) -> Unit,
    onCommentClicked: (PostEntity) -> Unit,
    onAlgorithmInsightsClicked: (PostEntity) -> Unit,
    onAuthorClicked: (String) -> Unit
) {
    val breeds = listOf("All", "Golden Retriever", "Corgi", "Samoyed", "Dachshund")

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_pull_to_refresh")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("feed_screen_list"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
        // --- Clean Stories Row ---
        item {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add story button
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onAddStoryClicked() }
                                .testTag("add_story_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Share Story",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your Story",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }

                    items(stories, key = { it.id }) { story ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onStoryClicked(story) }
                                .testTag("story_item_${story.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (story.isViewed) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        else TerracottaPrimary
                                    )
                                    .padding(2.dp)
                            ) {
                                PlushieImage(
                                    uri = story.mediaUri,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = story.plushieName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Refined Filter Chips Bar (Uncluttered) ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == FeedFilterType.FOR_YOU,
                            onClick = { onFilterChanged(FeedFilterType.FOR_YOU) },
                            label = { Text("✨ For You", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("filter_for_you")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == FeedFilterType.RECENT,
                            onClick = { onFilterChanged(FeedFilterType.RECENT) },
                            label = { Text("🐾 Latest", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("filter_recent")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == FeedFilterType.POPULAR,
                            onClick = { onFilterChanged(FeedFilterType.POPULAR) },
                            label = { Text("💛 Most Loved", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("filter_popular")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == FeedFilterType.VERIFIED_ONLY,
                            onClick = { onFilterChanged(FeedFilterType.VERIFIED_ONLY) },
                            label = { Text("⭐ Verified", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("filter_verified")
                        )
                    }
                    // Breed filter pills inline
                    items(breeds) { breed ->
                        val isSelected = selectedBreed == breed
                        FilterChip(
                            selected = isSelected,
                            onClick = { onBreedChanged(if (isSelected && breed != "All") "All" else breed) },
                            label = { Text(if (breed == "All") "All Breeds" else "🐶 $breed", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }

        // --- Posts Feed List ---
        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = TerracottaPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No companion posts here yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Be the first to share your emotional support dog plushie!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                CleanPostCard(
                    post = post,
                    onLikeClicked = { onLikeClicked(post) },
                    onCommentClicked = { onCommentClicked(post) },
                    onAlgorithmInsightsClicked = { onAlgorithmInsightsClicked(post) },
                    onAuthorClicked = { onAuthorClicked(post.authorId) }
                )
            }
        }
    }
}
}

@Composable
fun CleanPostCard(
    post: PostEntity,
    onLikeClicked: () -> Unit,
    onCommentClicked: () -> Unit,
    onAlgorithmInsightsClicked: () -> Unit,
    onAuthorClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column {
            // Post Header: Clean, modern, spacious
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlushieImage(
                    uri = post.authorAvatarUri,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onAuthorClicked() }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f).clickable { onAuthorClicked() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (post.isAuthorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerificationBadge(size = 15.dp)
                        }
                        if (post.isAuthorArtist) {
                            Spacer(modifier = Modifier.width(4.dp))
                            ArtistBadge(size = 15.dp)
                        }
                    }
                    Text(
                        text = "🐾 ${post.plushieName} • ${post.plushieBreed}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Algorithm Insights Button
                IconButton(
                    onClick = onAlgorithmInsightsClicked,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("algorithm_insights_btn_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Why recommended?",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Photo with 16.dp rounded corners inside card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                PlushieImage(
                    uri = post.photoUri,
                    filterName = post.filterName,
                    brightness = post.brightness,
                    contrast = post.contrast,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Mood chip overlay (Minimalist glassmorphism style)
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = post.plushieMood,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Actions row: Clean, prominent, easy to tap
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val heartTint by animateColorAsState(
                    targetValue = if (post.isLiked) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface,
                    label = "heart_tint"
                )

                IconButton(
                    onClick = onLikeClicked,
                    modifier = Modifier.testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = heartTint
                    )
                }
                Text(
                    text = "${post.likeCount}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(14.dp))

                IconButton(
                    onClick = onCommentClicked,
                    modifier = Modifier.testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${post.commentCount}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = TextSecondary
                    )
                }
            }

            // Caption & Tags: Clean typography
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).padding(bottom = 12.dp)) {
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )

                if (post.tags.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.tags,
                        style = MaterialTheme.typography.labelSmall,
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // TikTok-style Music Album Sound Banner
                if (post.soundAlbumTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_disc_rot")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "disc_angle"
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("post_sound_album_banner_${post.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rotating vinyl record disc
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1E1E))
                                    .rotate(rotation),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(TerracottaPrimary)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎵 ${post.soundAlbumTitle}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Original Sound • ${post.soundArtistName.ifBlank { post.authorName }}",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
