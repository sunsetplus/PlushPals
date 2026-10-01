package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AlgorithmInsightsDialog
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.feed.CommentsBottomSheet
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.feed.StoryViewerDialog
import com.example.ui.screens.messages.MessagesScreen
import com.example.ui.screens.nightai.NightAiScreen
import com.example.ui.screens.photoeditor.CreatePostScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaOnContainer
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.flow.collectLatest

enum class AppNavigationTab {
    HOME,
    EXPLORE,
    MESSAGES,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlushieApp(viewModel: PlushieViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(AppNavigationTab.HOME) }
    var isDarkMode by remember { mutableStateOf(false) } // Default to pristine Light Mode
    var showCreatePost by remember { mutableStateOf(false) }
    var showNightAi by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val feedPosts by viewModel.feedPosts.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val reels by viewModel.reels.collectAsStateWithLifecycle()
    val chatThreads by viewModel.chatThreads.collectAsStateWithLifecycle()
    val likedPosts by viewModel.likedPosts.collectAsStateWithLifecycle()
    val feedFilter by viewModel.feedFilter.collectAsStateWithLifecycle()
    val breedFilter by viewModel.breedFilter.collectAsStateWithLifecycle()
    val isRefreshingFeed by viewModel.isRefreshingFeed.collectAsStateWithLifecycle()

    val activeStory by viewModel.activeStory.collectAsStateWithLifecycle()
    val activeCommentsPost by viewModel.activeCommentsPost.collectAsStateWithLifecycle()
    val currentPostComments by viewModel.currentPostComments.collectAsStateWithLifecycle()
    val algorithmBreakdown by viewModel.algorithmBreakdown.collectAsStateWithLifecycle()

    val activeThread by viewModel.activeThread.collectAsStateWithLifecycle()
    val threadMessages by viewModel.activeThreadMessages.collectAsStateWithLifecycle()

    val createDraft by viewModel.createDraft.collectAsStateWithLifecycle()

    // Night AI state
    val nightAiMessages by viewModel.nightAiMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val currentPlayingPreset by viewModel.currentPlayingPreset.collectAsStateWithLifecycle()

    // Artist MP3 state
    val allArtistTracks by viewModel.allArtistTracks.collectAsStateWithLifecycle()
    val allArtistAlbums by viewModel.allArtistAlbums.collectAsStateWithLifecycle()
    val currentlyPlayingMp3Id by viewModel.currentPlayingMp3Id.collectAsStateWithLifecycle()
    val isMp3Playing by viewModel.isMp3Playing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val totalUnreadMessages = chatThreads.sumOf { it.unreadCount }

    MyApplicationTheme(darkTheme = isDarkMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (!showNightAi && !showCreatePost && activeStory == null && activeThread == null) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PlushPaws",
                                    fontWeight = FontWeight.Black,
                                    color = TerracottaPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = TerracottaContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🐾 Companions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TerracottaPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        actions = {
                            // Night AI Bedtime Chatbot & Music Button
                            FilledTonalButton(
                                onClick = { showNightAi = true },
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = TerracottaContainer,
                                    contentColor = TerracottaPrimary
                                ),
                                modifier = Modifier.testTag("top_bar_night_ai_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = "Night AI",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentPlayingPreset != null || isMp3Playing) "Playing 🎵" else "Night AI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Light / Dark Mode Toggle button
                            IconButton(
                                onClick = { isDarkMode = !isDarkMode },
                                modifier = Modifier.testTag("theme_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                    tint = TerracottaPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                } else if (showNightAi || showCreatePost) {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (showNightAi) "Night AI Companion 🌙" else "Share Companion Moment 📸",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    showNightAi = false
                                    showCreatePost = false
                                },
                                modifier = Modifier.testTag("subscreen_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            floatingActionButton = {
                // Sleek, uncluttered FAB on Home and Explore to create a post/story/reel
                if (!showNightAi && !showCreatePost && (selectedTab == AppNavigationTab.HOME || selectedTab == AppNavigationTab.EXPLORE)) {
                    ExtendedFloatingActionButton(
                        onClick = { showCreatePost = true },
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .testTag("create_post_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Share Moment"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Moment", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            bottomBar = {
                // Simplified, Intuitive 4-Tab Navigation: Home, Explore, Messages, Profile
                if (!showNightAi && !showCreatePost && activeStory == null && activeThread == null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.testTag("main_bottom_nav_bar")
                    ) {
                        // 1. Home
                        NavigationBarItem(
                            selected = selectedTab == AppNavigationTab.HOME,
                            onClick = { selectedTab = AppNavigationTab.HOME },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == AppNavigationTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home", fontSize = 11.sp, fontWeight = if (selectedTab == AppNavigationTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_home_item")
                        )

                        // 2. Explore
                        NavigationBarItem(
                            selected = selectedTab == AppNavigationTab.EXPLORE,
                            onClick = { selectedTab = AppNavigationTab.EXPLORE },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == AppNavigationTab.EXPLORE) Icons.Default.Explore else Icons.Outlined.Explore,
                                    contentDescription = "Explore"
                                )
                            },
                            label = { Text("Explore", fontSize = 11.sp, fontWeight = if (selectedTab == AppNavigationTab.EXPLORE) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_explore_item")
                        )

                        // 3. Messages
                        NavigationBarItem(
                            selected = selectedTab == AppNavigationTab.MESSAGES,
                            onClick = { selectedTab = AppNavigationTab.MESSAGES },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalUnreadMessages > 0) {
                                            Badge(containerColor = TerracottaPrimary) {
                                                Text("$totalUnreadMessages")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == AppNavigationTab.MESSAGES) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Messages"
                                    )
                                }
                            },
                            label = { Text("Messages", fontSize = 11.sp, fontWeight = if (selectedTab == AppNavigationTab.MESSAGES) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_messages_item")
                        )

                        // 4. Profile
                        NavigationBarItem(
                            selected = selectedTab == AppNavigationTab.PROFILE,
                            onClick = { selectedTab = AppNavigationTab.PROFILE },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == AppNavigationTab.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                                    contentDescription = "Profile"
                                )
                            },
                            label = { Text("Profile", fontSize = 11.sp, fontWeight = if (selectedTab == AppNavigationTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_profile_item")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showNightAi) {
                    BackHandler { showNightAi = false }
                    NightAiScreen(
                        messages = nightAiMessages,
                        isThinking = isAiThinking,
                        currentPlayingPreset = currentPlayingPreset,
                        companionName = currentUser?.plushieName ?: "Barnaby",
                        companionBreed = currentUser?.plushieBreed ?: "Golden Retriever",
                        isDarkMode = isDarkMode,
                        onSendMessage = { viewModel.sendNightAiMessage(it) },
                        onGenerateMusic = { preset, title -> viewModel.generateNightAiMusic(preset, title) },
                        onTogglePlayTrack = { viewModel.togglePlayTrack(it) },
                        onClearHistory = { viewModel.clearNightAiHistory() }
                    )
                } else if (showCreatePost) {
                    BackHandler { showCreatePost = false }
                    CreatePostScreen(
                        draft = createDraft,
                        availableAlbums = allArtistAlbums,
                        onPhotoSelected = { viewModel.updateDraftPhoto(it) },
                        onFilterSelected = { viewModel.updateDraftFilter(it) },
                        onBrightnessChanged = { viewModel.updateDraftBrightness(it) },
                        onContrastChanged = { viewModel.updateDraftContrast(it) },
                        onCaptionChanged = { viewModel.updateDraftCaption(it) },
                        onToggleTag = { viewModel.toggleDraftTag(it) },
                        onMoodChanged = { viewModel.updateDraftMood(it) },
                        onSelectSoundAlbum = { viewModel.selectPostSoundAlbum(it) },
                        onPublishPost = {
                            viewModel.publishPost {
                                showCreatePost = false
                                selectedTab = AppNavigationTab.HOME
                            }
                        },
                        onPublishStory = { caption, uri ->
                            viewModel.publishStory(caption, uri) {
                                showCreatePost = false
                                selectedTab = AppNavigationTab.HOME
                            }
                        },
                        onPublishReel = { caption, uri, track, tags ->
                            viewModel.publishReel(caption, uri, track, tags) {
                                showCreatePost = false
                                selectedTab = AppNavigationTab.EXPLORE
                            }
                        }
                    )
                } else {
                    when (selectedTab) {
                        AppNavigationTab.HOME -> {
                            FeedScreen(
                                posts = feedPosts,
                                stories = stories,
                                selectedFilter = feedFilter,
                                selectedBreed = breedFilter,
                                isRefreshing = isRefreshingFeed,
                                onRefresh = { viewModel.refreshHomeFeed() },
                                onFilterChanged = { viewModel.setFeedFilter(it) },
                                onBreedChanged = { viewModel.setBreedFilter(it) },
                                onStoryClicked = { viewModel.openStory(it) },
                                onAddStoryClicked = { showCreatePost = true },
                                onLikeClicked = { viewModel.toggleLikePost(it) },
                                onCommentClicked = { viewModel.openComments(it) },
                                onAlgorithmInsightsClicked = { viewModel.showAlgorithmInsights(it) },
                                onAuthorClicked = { selectedTab = AppNavigationTab.PROFILE }
                            )
                        }

                        AppNavigationTab.EXPLORE -> {
                            ExploreScreen(
                                reels = reels,
                                discoveryPosts = feedPosts,
                                artistTracks = allArtistTracks,
                                currentlyPlayingMp3Id = currentlyPlayingMp3Id,
                                isMp3Playing = isMp3Playing,
                                onLikeReel = { viewModel.toggleLikeReel(it) },
                                onPostClicked = { viewModel.showAlgorithmInsights(it) },
                                onAuthorClicked = { selectedTab = AppNavigationTab.PROFILE },
                                onPlayMp3 = { viewModel.playArtistMp3Track(context, it) },
                                onPauseMp3 = { viewModel.pauseArtistMp3() }
                            )
                        }

                        AppNavigationTab.MESSAGES -> {
                            MessagesScreen(
                                threads = chatThreads,
                                activeThread = activeThread,
                                messages = threadMessages,
                                currentUserId = currentUser?.userId ?: "user_me",
                                onThreadSelected = { viewModel.openChat(it) },
                                onCloseChat = { viewModel.closeChat() },
                                onSendMessage = { text, sticker -> viewModel.sendMessage(text, sticker) }
                            )
                        }

                        AppNavigationTab.PROFILE -> {
                            val myPosts = feedPosts.filter { it.authorId == (currentUser?.userId ?: "user_me") }
                            val myArtistTracks = allArtistTracks.filter { it.artistId == (currentUser?.userId ?: "user_me") }
                            val myArtistAlbums = allArtistAlbums.filter { it.artistId == (currentUser?.userId ?: "user_me") }
                            ProfileScreen(
                                user = currentUser,
                                userPosts = myPosts,
                                likedPosts = likedPosts,
                                artistTracks = myArtistTracks,
                                artistAlbums = myArtistAlbums,
                                currentlyPlayingMp3Id = currentlyPlayingMp3Id,
                                isMp3Playing = isMp3Playing,
                                onPlayMp3 = { viewModel.playArtistMp3Track(context, it) },
                                onPauseMp3 = { viewModel.pauseArtistMp3() },
                                onUploadMp3 = { title, uri, category, duration ->
                                    viewModel.uploadArtistTrack(title, uri, category, duration) {}
                                },
                                onUploadAlbum = { title, desc, genre, audio, cover ->
                                    viewModel.uploadArtistAlbum(title, desc, genre, audio, cover) {}
                                },
                                onDeleteTrack = { viewModel.deleteArtistTrack(it) },
                                onDeleteAlbum = { viewModel.deleteArtistAlbum(it) },
                                onToggleArtistMode = { isArtist, genre, bio ->
                                    viewModel.updateArtistProfile(isArtist, genre, bio)
                                },
                                onUpdateProfile = { name, plushie, breed, bio, role, avatar ->
                                    viewModel.updateProfile(name, plushie, breed, bio, role, avatar)
                                },
                                onApplyVerification = { badgeType ->
                                    viewModel.applyVerificationBadge(badgeType)
                                },
                                onGoogleSignIn = { name, email, plushie, breed ->
                                    viewModel.switchGoogleAccount(name, email, plushie, breed)
                                }
                            )
                        }
                    }
                }

                // Story Viewer Dialog
                activeStory?.let { story ->
                    StoryViewerDialog(
                        story = story,
                        onDismiss = { viewModel.closeStory() },
                        onReply = { reply ->
                            viewModel.addComment(reply)
                            viewModel.closeStory()
                        }
                    )
                }

                // Comments Bottom Sheet
                activeCommentsPost?.let { post ->
                    CommentsBottomSheet(
                        post = post,
                        comments = currentPostComments,
                        onDismiss = { viewModel.closeComments() },
                        onAddComment = { viewModel.addComment(it) }
                    )
                }

                // Algorithm Transparency Dialog
                algorithmBreakdown?.let { (post, breakdown) ->
                    AlgorithmInsightsDialog(
                        post = post,
                        breakdown = breakdown,
                        onDismiss = { viewModel.dismissAlgorithmInsights() }
                    )
                }
            }
        }
    }
}
