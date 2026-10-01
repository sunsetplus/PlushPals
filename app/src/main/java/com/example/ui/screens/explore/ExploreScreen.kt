package com.example.ui.screens.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.ArtistTrackEntity
import com.example.data.local.model.PostEntity
import com.example.data.local.model.ReelEntity
import com.example.ui.components.PlushieImage
import com.example.ui.screens.reels.ReelsScreen
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaOnContainer
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExploreScreen(
    reels: List<ReelEntity>,
    discoveryPosts: List<PostEntity>,
    artistTracks: List<ArtistTrackEntity> = emptyList(),
    currentlyPlayingMp3Id: Long? = null,
    isMp3Playing: Boolean = false,
    onLikeReel: (ReelEntity) -> Unit,
    onPostClicked: (PostEntity) -> Unit,
    onAuthorClicked: (String) -> Unit,
    onPlayMp3: (ArtistTrackEntity) -> Unit = {},
    onPauseMp3: () -> Unit = {},
    onUploadMp3: (title: String, uri: String) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf(0) } // 0 = Reels, 1 = Discovery Grid, 2 = Artist Soundscapes

    val audioPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/')?.take(30) ?: "Plushie Audio Track"
            onUploadMp3(fileName, uri.toString())
        }
    }

    val filteredPosts = remember(searchQuery, discoveryPosts) {
        if (searchQuery.isBlank()) discoveryPosts
        else discoveryPosts.filter {
            it.plushieName.contains(searchQuery, ignoreCase = true) ||
            it.plushieBreed.contains(searchQuery, ignoreCase = true) ||
            it.tags.contains(searchQuery, ignoreCase = true) ||
            it.caption.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredTracks = remember(searchQuery, artistTracks) {
        if (searchQuery.isBlank()) artistTracks
        else artistTracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artistName.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("explore_screen")
    ) {
        // Search & Explore Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search companions, #tags, or audio...", fontSize = 13.sp, color = TextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("explore_search_bar"),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TerracottaPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Segmented switch: Reels vs Discovery Grid vs Artist Tracks
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("Reels 🎬", fontSize = 11.sp, fontWeight = if (selectedSection == 0) FontWeight.Bold else FontWeight.Normal)
                }
                SegmentedButton(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("Grid ✨", fontSize = 11.sp, fontWeight = if (selectedSection == 1) FontWeight.Bold else FontWeight.Normal)
                }
                SegmentedButton(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("MP3 Audio 🎵", fontSize = 11.sp, fontWeight = if (selectedSection == 2) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        // Section Content
        when (selectedSection) {
            0 -> {
                ReelsScreen(
                    reels = reels,
                    onLikeClicked = onLikeReel,
                    onCommentClicked = {},
                    onAuthorClicked = onAuthorClicked
                )
            }
            1 -> {
                if (filteredPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Pets, contentDescription = null, tint = TerracottaPrimary.copy(alpha = 0.5f), modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No companion posts found", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Try searching another breed or tag!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(filteredPosts, key = { it.id }) { post ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onPostClicked(post) }
                                    .testTag("discovery_grid_item_${post.id}")
                            ) {
                                PlushieImage(
                                    uri = post.photoUri,
                                    filterName = post.filterName,
                                    brightness = post.brightness,
                                    contrast = post.contrast,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Surface(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                                ) {
                                    Text(
                                        text = post.plushieBreed,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Artist Uploaded MP3 Audio Tracks
                Column(modifier = Modifier.fillMaxSize()) {
                    // Upload MP3 Audio Bar
                    Surface(
                        color = TerracottaContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clickable { audioPicker.launch("audio/*") }
                            .testTag("explore_upload_mp3_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TerracottaPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Upload MP3 Audio 🎵", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TerracottaOnContainer)
                                Text("Add a soothing lullaby or soundscape from your device", fontSize = 10.sp, color = TextSecondary)
                            }
                            FilledTonalButton(
                                onClick = { audioPicker.launch("audio/*") },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (filteredTracks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = TerracottaPrimary.copy(alpha = 0.5f), modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No MP3 tracks uploaded yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Tap 'Upload' above to share the first soundscape!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 90.dp, top = 4.dp)
                        ) {
                            items(filteredTracks, key = { it.id }) { track ->
                                val isPlaying = currentlyPlayingMp3Id == track.id && isMp3Playing

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (isPlaying) onPauseMp3() else onPlayMp3(track)
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(TerracottaPrimary)
                                                .testTag("explore_play_mp3_${track.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Pause" else "Play",
                                                tint = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(track.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text("By ${track.artistName} • ${track.category}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        }

                                        Text("🎧 ${track.playCount}", fontSize = 11.sp, color = TextSecondary)
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
