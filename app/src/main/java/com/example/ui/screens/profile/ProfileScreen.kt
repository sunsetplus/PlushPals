package com.example.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.ArtistAlbumEntity
import com.example.data.local.model.ArtistTrackEntity
import com.example.data.local.model.PostEntity
import com.example.data.local.model.UserProfileEntity
import com.example.ui.components.ArtistBadge
import com.example.ui.components.GoogleAuthDialog
import com.example.ui.components.PlushieImage
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserProfileEntity?,
    userPosts: List<PostEntity>,
    likedPosts: List<PostEntity>,
    artistTracks: List<ArtistTrackEntity>,
    artistAlbums: List<ArtistAlbumEntity> = emptyList(),
    currentlyPlayingMp3Id: Long?,
    isMp3Playing: Boolean,
    onPlayMp3: (ArtistTrackEntity) -> Unit,
    onPauseMp3: () -> Unit,
    onUploadMp3: (title: String, uri: String, category: String, duration: String) -> Unit,
    onUploadAlbum: (title: String, description: String, genre: String, audioUri: String, coverUri: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteTrack: (Long) -> Unit,
    onDeleteAlbum: (Long) -> Unit = {},
    onToggleArtistMode: (isArtist: Boolean, genre: String, bio: String) -> Unit,
    onUpdateProfile: (name: String, plushie: String, breed: String, bio: String, role: String, avatar: String) -> Unit,
    onApplyVerification: (badgeType: String) -> Unit,
    onGoogleSignIn: (name: String, email: String, plushie: String, breed: String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var showGoogleAuthDialog by remember { mutableStateOf(false) }
    var showArtistSetupDialog by remember { mutableStateOf(false) }
    var showUploadMp3Dialog by remember { mutableStateOf(false) }
    var showUploadAlbumDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Grid Posts, 1 = Music Sounds/Albums, 2 = Liked

    val activeUser = user ?: UserProfileEntity(
        userId = "user_me",
        displayName = "Alex & Barnaby",
        plushieName = "Barnaby",
        plushieBreed = "Golden Retriever",
        bio = "Proud parent of Barnaby, my emotional support dog plushie.",
        comfortRole = "Sensory Grounding & Anxiety Relief",
        profileAvatarUri = "drawable://plushie_hero_dog",
        isVerified = true,
        verificationBadgeType = "CERTIFIED_COMPANION",
        isArtist = false
    )

    val totalLikesCount = remember(userPosts) {
        userPosts.sumOf { it.likeCount }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .testTag("profile_screen")
    ) {
        // --- Top App Bar: Clean TikTok-style Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "@${activeUser.displayName.lowercase().replace(" ", "_").replace("&", "and")}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Google Account Switcher
            OutlinedButton(
                onClick = { showGoogleAuthDialog = true },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("google_account_btn")
            ) {
                Text(
                    text = "G",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4285F4),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Account",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // --- TikTok-style Hero Profile Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large circular avatar with TikTok-style glowing gradient ring
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(TerracottaPrimary, AmberAccent)
                        )
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                PlushieImage(
                    uri = activeUser.profileAvatarUri,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display Name with Verified Badge and Artist Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = activeUser.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                if (activeUser.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerificationBadge(
                        badgeType = activeUser.verificationBadgeType,
                        size = 18.dp
                    )
                }
                // Visual Indicator Badge for Verified Artists!
                if (activeUser.isArtist) {
                    Spacer(modifier = Modifier.width(4.dp))
                    ArtistBadge(size = 18.dp, showLabel = true)
                }
            }

            // Companion Breed Tag
            Surface(
                color = TerracottaContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "🐾 ${activeUser.plushieName} • ${activeUser.plushieBreed}",
                    color = TerracottaOnContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TikTok Metric Counter Row: Following | Followers | Likes
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${activeUser.followingCount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Following",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${activeUser.followerCount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Followers",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalLikesCount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Likes",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TikTok Action Buttons Row: Edit Profile | Artist Studio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.weight(1f).height(40.dp).testTag("edit_profile_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Text("Edit profile", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { showArtistSetupDialog = true },
                    modifier = Modifier.weight(1f).height(40.dp).testTag("artist_profile_setup_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeUser.isArtist) AmberAccent else TerracottaPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (activeUser.isArtist) "Artist Studio 🎵" else "Become Artist",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bio & Comfort Role Card
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (activeUser.isArtist && activeUser.artistBio.isNotBlank()) activeUser.artistBio else activeUser.bio,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = TextSecondary,
                maxLines = 3,
                fontSize = 12.sp
            )

            // If user is verified artist: TikTok Music Album & MP3 Studio Bar
            if (activeUser.isArtist) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = AmberContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TikTok Sound Studio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AmberOnContainer
                            )
                            Text(
                                text = "${artistAlbums.size} Albums • ${artistTracks.size} MP3 Sounds",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        // Create Album button
                        Button(
                            onClick = { showUploadAlbumDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("create_music_album_btn")
                        ) {
                            Icon(Icons.Default.Album, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Album", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Upload MP3 button
                        OutlinedButton(
                            onClick = { showUploadMp3Dialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("artist_upload_mp3_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("MP3", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- TikTok-style Iconic Sliding Tab Icons ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = TerracottaPrimary
        ) {
            // Tab 0: Grid Posts
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "My Posts",
                        tint = if (selectedTab == 0) TerracottaPrimary else TextSecondary
                    )
                },
                modifier = Modifier.testTag("tab_posts_grid")
            )

            // Tab 1: Music Albums & Sound Tracks (TikTok Sound Library)
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = {
                    BadgedBox(
                        badge = {
                            if (artistAlbums.isNotEmpty()) {
                                Badge(containerColor = AmberAccent) {
                                    Text("${artistAlbums.size}")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Sound Albums",
                            tint = if (selectedTab == 1) AmberAccent else TextSecondary
                        )
                    }
                },
                modifier = Modifier.testTag("tab_artist_sounds")
            )

            // Tab 2: Liked Posts (TikTok Lock / Heart)
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Liked Posts",
                        tint = if (selectedTab == 2) TerracottaPrimary else TextSecondary
                    )
                },
                modifier = Modifier.testTag("tab_liked_posts")
            )
        }

        // --- Tab Contents ---
        when (selectedTab) {
            0 -> {
                // TikTok 3-column Grid of Posts
                if (userPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TerracottaPrimary.copy(alpha = 0.5f), modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No videos or photos yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Capture and share your first plushie moment!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(userPosts, key = { it.id }) { post ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(0.85f)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                PlushieImage(
                                    uri = post.photoUri,
                                    filterName = post.filterName,
                                    brightness = post.brightness,
                                    contrast = post.contrast,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // TikTok-style bottom-left play/like count
                                Surface(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("${post.likeCount}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TikTok Sound Albums & MP3 Tracks Catalog
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Albums Header
                    item {
                        Text(
                            text = "Music Sound Albums",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }

                    if (artistAlbums.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (activeUser.isArtist) "No music albums uploaded yet. Tap 'New Album' above to create one!" else "This user has not released any music sound albums.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    } else {
                        items(artistAlbums, key = { it.id }) { album ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Spinning vinyl preview
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF191919)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Album, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(24.dp))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(album.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("By ${album.artistName} • ${album.genre}", fontSize = 10.sp, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("📌 Used on ${album.useCount} community posts", fontSize = 10.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
                                    }

                                    if (activeUser.isArtist) {
                                        IconButton(onClick = { onDeleteAlbum(album.id) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Album", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // MP3 Audio Tracks section
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Individual MP3 Sound Tracks",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }

                    if (artistTracks.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (activeUser.isArtist) "No MP3 tracks uploaded yet. Tap 'MP3' above to upload one!" else "No sound tracks available.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    } else {
                        items(artistTracks, key = { it.id }) { track ->
                            val isPlaying = currentlyPlayingMp3Id == track.id && isMp3Playing

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (isPlaying) onPauseMp3() else onPlayMp3(track) },
                                        modifier = Modifier.size(38.dp).clip(CircleShape).background(TerracottaPrimary)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${track.category} • 🎧 ${track.playCount} plays", fontSize = 10.sp, color = TextSecondary)
                                    }

                                    if (activeUser.isArtist) {
                                        IconButton(onClick = { onDeleteTrack(track.id) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Liked / Saved Posts
                if (likedPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No liked companion posts yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Heart posts on your feed to save them here!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(likedPosts, key = { it.id }) { post ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(0.85f)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                PlushieImage(
                                    uri = post.photoUri,
                                    filterName = post.filterName,
                                    brightness = post.brightness,
                                    contrast = post.contrast,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Upload Music Album Dialog (TikTok-style sound album upload) ---
    if (showUploadAlbumDialog) {
        UploadAlbumDialog(
            onDismiss = { showUploadAlbumDialog = false },
            onUpload = { title, desc, genre, audioUri, coverUri ->
                onUploadAlbum(title, desc, genre, audioUri, coverUri)
                showUploadAlbumDialog = false
            }
        )
    }

    // --- Upload MP3 Dialog ---
    if (showUploadMp3Dialog) {
        UploadMp3Dialog(
            onDismiss = { showUploadMp3Dialog = false },
            onUpload = { title, uri, category, duration ->
                onUploadMp3(title, uri, category, duration)
                showUploadMp3Dialog = false
            }
        )
    }

    // --- Artist Setup Dialog ---
    if (showArtistSetupDialog) {
        ArtistSetupDialog(
            currentArtist = activeUser.isArtist,
            currentGenre = activeUser.artistGenre,
            currentBio = activeUser.artistBio,
            onDismiss = { showArtistSetupDialog = false },
            onSave = { isArtist, genre, bio ->
                onToggleArtistMode(isArtist, genre, bio)
                showArtistSetupDialog = false
            }
        )
    }

    // --- Edit Profile Dialog ---
    if (showEditDialog) {
        EditProfileDialog(
            current = activeUser,
            onDismiss = { showEditDialog = false },
            onSave = { name, plushie, breed, bio, role, avatar ->
                onUpdateProfile(name, plushie, breed, bio, role, avatar)
                showEditDialog = false
            }
        )
    }

    // --- Google Sign-In Dialog ---
    if (showGoogleAuthDialog) {
        GoogleAuthDialog(
            onDismiss = { showGoogleAuthDialog = false },
            onAccountSelected = { name, email, plushie, breed ->
                onGoogleSignIn(name, email, plushie, breed)
                showGoogleAuthDialog = false
            }
        )
    }
}

// Dialog for Artists to upload a Music Sound Album (TikTok-style)
@Composable
fun UploadAlbumDialog(
    onDismiss: () -> Unit,
    onUpload: (title: String, desc: String, genre: String, audioUri: String, coverUri: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Bedtime Lullabies & 432Hz Calm") }
    var audioUri by remember { mutableStateOf<String?>(null) }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) audioUri = uri.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Album, contentDescription = null, tint = AmberAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish Music Album", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Release an official Sound Album. Other users will be able to select your album sounds for their posts, just like TikTok sounds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Audio file selector
                Surface(
                    color = AmberContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { audioPicker.launch("audio/*") }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (audioUri != null) Icons.Default.CheckCircle else Icons.Default.AudioFile,
                            contentDescription = null,
                            tint = AmberOnContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (audioUri != null) "Album Audio Attached ✅" else "Choose Album MP3 Track",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AmberOnContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Album Title (e.g. Starry Paws EP)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre (e.g. 432Hz Ambient Calm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Album Description & Mood") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalUri = audioUri ?: "android.resource://com.aistudio.plushiepaws.dgvqtr/raw/lullaby"
                    onUpload(title.ifBlank { "Cozy Paws Night Album" }, description, genre, finalUri, "")
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                modifier = Modifier.testTag("confirm_upload_album_btn")
            ) {
                Text("Publish Album")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Dialog exclusively for Artists to upload real MP3 files
@Composable
fun UploadMp3Dialog(
    onDismiss: () -> Unit,
    onUpload: (title: String, audioUri: String, category: String, duration: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedAudioUri by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("Bedtime Lullaby") }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAudioUri = uri.toString()
        }
    }

    val categories = listOf("Bedtime Lullaby", "432Hz Calm", "Rain & Nature", "Plushie Heartbeat")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = TerracottaPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload MP3 Track", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "As a verified sound artist, you can upload MP3 audio files to soothe plushie companions to sleep.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Surface(
                    color = TerracottaContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { audioPicker.launch("audio/*") }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selectedAudioUri != null) Icons.Default.CheckCircle else Icons.Default.AudioFile,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedAudioUri != null) "MP3 File Selected ✅" else "Tap to Choose MP3 File from Device",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaOnContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Track Title (e.g. Moonlit Paw Lullaby)") },
                    modifier = Modifier.fillMaxWidth().testTag("mp3_title_input"),
                    singleLine = true
                )

                Text("Category:", style = MaterialTheme.typography.labelSmall)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat },
                            color = if (selectedCategory == cat) TerracottaContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategory == cat) TerracottaOnContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uri = selectedAudioUri ?: "android.resource://com.aistudio.plushiepaws.dgvqtr/raw/lullaby"
                    onUpload(title.ifBlank { "Serene Night Companion Track" }, uri, selectedCategory, "3:24")
                },
                modifier = Modifier.testTag("confirm_upload_mp3_btn")
            ) {
                Text("Publish Track")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ArtistSetupDialog(
    currentArtist: Boolean,
    currentGenre: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (isArtist: Boolean, genre: String, bio: String) -> Unit
) {
    var isArtist by remember { mutableStateOf(currentArtist) }
    var genre by remember { mutableStateOf(currentGenre) }
    var bio by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Artist Profile Settings 🎵", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Plushie Sound Artists compose and upload soothing MP3 audio tracks, bedtime frequencies, and TikTok-style music albums for the community.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Artist Mode Enabled", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isArtist,
                        onCheckedChange = { isArtist = it },
                        modifier = Modifier.testTag("artist_mode_switch")
                    )
                }

                if (isArtist) {
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Sound Style (e.g. 432Hz Lullabies)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Artist Statement / Music Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(isArtist, genre, bio) },
                modifier = Modifier.testTag("save_artist_settings_btn")
            ) {
                Text("Save Artist Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditProfileDialog(
    current: UserProfileEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, plushie: String, breed: String, bio: String, role: String, avatar: String) -> Unit
) {
    var displayName by remember { mutableStateOf(current.displayName) }
    var plushieName by remember { mutableStateOf(current.plushieName) }
    var plushieBreed by remember { mutableStateOf(current.plushieBreed) }
    var bio by remember { mutableStateOf(current.bio) }
    var comfortRole by remember { mutableStateOf(current.comfortRole) }
    var avatarUri by remember { mutableStateOf(current.profileAvatarUri) }

    val avatarOptions = listOf(
        Pair("drawable://plushie_hero_dog", "Golden"),
        Pair("drawable://plushie_corgi_friend", "Corgi"),
        Pair("drawable://ic_plush_dog_icon", "Icon")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Companion Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Companion Avatar:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    avatarOptions.forEach { (uri, _) ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (avatarUri == uri) 2.dp else 0.dp,
                                    color = if (avatarUri == uri) TerracottaPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { avatarUri = uri }
                        ) {
                            PlushieImage(uri = uri, modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = plushieName,
                    onValueChange = { plushieName = it },
                    label = { Text("Plushie Dog Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = plushieBreed,
                    onValueChange = { plushieBreed = it },
                    label = { Text("Plushie Breed") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = comfortRole,
                    onValueChange = { comfortRole = it },
                    label = { Text("Support Specialization") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Companion Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(displayName, plushieName, plushieBreed, bio, comfortRole, avatarUri) },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
