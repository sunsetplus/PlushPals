package com.example.ui.screens.photoeditor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ai.GeminiNightAiService
import com.example.data.local.model.ArtistAlbumEntity
import com.example.ui.CreatePostDraft
import com.example.ui.components.PlushieImage
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaOnContainer
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun CreatePostScreen(
    draft: CreatePostDraft,
    availableAlbums: List<ArtistAlbumEntity> = emptyList(),
    onPhotoSelected: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onBrightnessChanged: (Float) -> Unit,
    onContrastChanged: (Float) -> Unit,
    onCaptionChanged: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onMoodChanged: (String) -> Unit,
    onSelectSoundAlbum: (ArtistAlbumEntity?) -> Unit = {},
    onUploadNewMp3: (title: String, uri: String) -> Unit = { _, _ -> },
    onPublishPost: () -> Unit,
    onPublishStory: (caption: String, photoUri: String) -> Unit,
    onPublishReel: (caption: String, photoUri: String, audioTrack: String, tags: String) -> Unit
) {
    var shareMode by remember { mutableStateOf("POST") } // "POST", "STORY", "REEL"
    var selectedAudioTrack by remember { mutableStateOf("Gentle Rain & Soft Breathing 🌧️") }
    var showFineTuning by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }
    var isGeneratingCaption by remember { mutableStateOf(false) }
    var showSoundAlbumPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri.toString())
        }
    }

    val samplePhotos = listOf(
        Pair("drawable://plushie_hero_dog", "Golden Hero"),
        Pair("drawable://plushie_corgi_friend", "Corgi Friend"),
        Pair("drawable://ic_plush_dog_icon", "Icon")
    )

    val filters = listOf(
        "Original",
        "Cozy Warm",
        "Golden Glow",
        "Pastel Fluff",
        "Vintage Soft",
        "Calming Sepia"
    )

    val tagsList = listOf(
        "#FluffyTherapy",
        "#AnxietyRelief",
        "#SensoryGrounding",
        "#BedtimeCuddles",
        "#WeightedPlush",
        "#DogPlushieCommunity"
    )

    val moodsList = listOf(
        "Cozy & Sleepy 💤",
        "Comforting Paw 🐾",
        "Adventuring 🌲",
        "Ready for Snuggles 🧸",
        "Gentle Listener 🎧"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 90.dp)
            .testTag("create_post_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Selector: Clean, modern rounded segmented tabs
        TabRow(
            selectedTabIndex = when (shareMode) {
                "STORY" -> 1
                "REEL" -> 2
                else -> 0
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Tab(
                selected = shareMode == "POST",
                onClick = { shareMode = "POST" },
                text = { Text("Feed Post", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_mode_post")
            )
            Tab(
                selected = shareMode == "STORY",
                onClick = { shareMode = "STORY" },
                text = { Text("24h Story", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_mode_story")
            )
            Tab(
                selected = shareMode == "REEL",
                onClick = { shareMode = "REEL" },
                text = { Text("Plushie Reel", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_mode_reel")
            )
        }

        // TikTok-style "Add Sound / Music Album 🎵" Bar
        Surface(
            color = if (draft.soundAlbumTitle.isNotBlank()) TerracottaContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSoundAlbumPicker = true }
                .testTag("add_sound_album_pill")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (draft.soundAlbumTitle.isNotBlank()) "🎵 ${draft.soundAlbumTitle}" else "Add Music Album / Sound 🎵",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (draft.soundAlbumTitle.isNotBlank()) TerracottaOnContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (draft.soundAlbumTitle.isNotBlank()) "By ${draft.soundArtistName} • Tap to change" else "Choose from verified artist sound albums like TikTok",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                if (draft.soundAlbumTitle.isNotBlank()) {
                    IconButton(
                        onClick = { onSelectSoundAlbum(null) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Sound", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Photo Preview Card: Crisp & Clean
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlushieImage(
                    uri = draft.photoUri,
                    filterName = draft.filterName,
                    brightness = draft.brightness,
                    contrast = draft.contrast,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Pick from Gallery Floating Button
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("pick_from_gallery_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Pick Photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Choose Photo",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Quick Preset avatars row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Presets:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            samplePhotos.forEach { (uri, label) ->
                SuggestionChip(
                    onClick = { onPhotoSelected(uri) },
                    label = { Text(label, fontSize = 11.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Photo Filters Row
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Photo Atmosphere",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showFineTuning = !showFineTuning },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (showFineTuning) "Hide Adjustments" else "Fine-Tune Sliders",
                        fontSize = 11.sp,
                        color = TerracottaPrimary
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    val isSelected = draft.filterName == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TerracottaPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("filter_option_$filter")
                    )
                }
            }

            // Expandable Brightness & Contrast fine-tuning
            AnimatedVisibility(visible = showFineTuning) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Brightness", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(70.dp))
                            Slider(
                                value = draft.brightness,
                                onValueChange = onBrightnessChanged,
                                valueRange = -0.3f..0.3f,
                                modifier = Modifier.weight(1f).testTag("brightness_slider")
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Contrast", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(70.dp))
                            Slider(
                                value = draft.contrast,
                                onValueChange = onContrastChanged,
                                valueRange = 0.7f..1.4f,
                                modifier = Modifier.weight(1f).testTag("contrast_slider")
                            )
                        }
                    }
                }
            }
        }

        // Companion Mood Row
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Companion Mood",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(moodsList) { mood ->
                    FilterChip(
                        selected = draft.mood == mood,
                        onClick = { onMoodChanged(mood) },
                        label = { Text(mood, fontSize = 11.sp) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("mood_chip_$mood")
                    )
                }
            }
        }

        // Caption Input with Audio Transcription (gemini-3.5-transcribe)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Companion Caption",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Plush AI Caption Generator
                    FilledTonalButton(
                        onClick = {
                            isGeneratingCaption = true
                            scope.launch {
                                val generated = com.example.ai.PlushAiService.generateCaptionWithPlushAi("Barnaby", "Golden Retriever", draft.mood)
                                onCaptionChanged(generated)
                                isGeneratingCaption = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TerracottaContainer,
                            contentColor = TerracottaPrimary
                        ),
                        modifier = Modifier.testTag("ask_plush_ai_caption_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isGeneratingCaption) "Plush AI..." else "Plush AI ✨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Transcribe Button
                    FilledTonalButton(
                        onClick = {
                            isTranscribing = true
                            scope.launch {
                                val transcribed = GeminiNightAiService.transcribeAudio("Transcribe companion caption voice note")
                                onCaptionChanged(transcribed)
                                isTranscribing = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isTranscribing) "..." else "Mic",
                            fontSize = 11.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = draft.caption,
                onValueChange = onCaptionChanged,
                placeholder = { Text("What comforting moment did you and your plushie share today?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("caption_input_field"),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Tags Row
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Support Tags",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(tagsList) { tag ->
                    val isSelected = draft.selectedTags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleTag(tag) },
                        label = { Text(tag, fontSize = 11.sp) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("tag_chip_$tag")
                    )
                }
            }
        }

        // Publish Button
        Button(
            onClick = {
                when (shareMode) {
                    "STORY" -> onPublishStory(
                        draft.caption.ifBlank { "Daily plushie snuggle check-in 🐾" },
                        draft.photoUri
                    )
                    "REEL" -> onPublishReel(
                        draft.caption.ifBlank { "Gentle emotional support plushie hug moment 💓" },
                        draft.photoUri,
                        if (draft.soundAlbumTitle.isNotBlank()) "🎵 ${draft.soundAlbumTitle}" else selectedAudioTrack,
                        draft.selectedTags.joinToString(", ")
                    )
                    else -> onPublishPost()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("publish_post_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
        ) {
            Icon(
                imageVector = when (shareMode) {
                    "STORY" -> Icons.Default.AutoAwesome
                    "REEL" -> Icons.Default.VideoCall
                    else -> Icons.Default.Share
                },
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (shareMode) {
                    "STORY" -> "Share 24h Story"
                    "REEL" -> "Publish Plushie Reel"
                    else -> "Publish to Feed"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }

    // TikTok-style Sound Album Selector Dialog
    if (showSoundAlbumPicker) {
        SoundAlbumPickerDialog(
            albums = availableAlbums,
            selectedTitle = draft.soundAlbumTitle,
            onDismiss = { showSoundAlbumPicker = false },
            onSelect = { album ->
                onSelectSoundAlbum(album)
                showSoundAlbumPicker = false
            },
            onUploadNewMp3 = { title, uri ->
                onUploadNewMp3(title, uri)
                showSoundAlbumPicker = false
            }
        )
    }
}

@Composable
fun SoundAlbumPickerDialog(
    albums: List<ArtistAlbumEntity>,
    selectedTitle: String,
    onDismiss: () -> Unit,
    onSelect: (ArtistAlbumEntity?) -> Unit,
    onUploadNewMp3: (title: String, uri: String) -> Unit = { _, _ -> }
) {
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/')?.take(30) ?: "Companion MP3 Sound"
            onUploadNewMp3(fileName, uri.toString())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = TerracottaPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Music Album Sound", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pick an artist-created sound album to accompany your post with a spinning vinyl disc, or upload your own MP3 file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Upload MP3 Button directly from Device
                Button(
                    onClick = { audioPicker.launch("audio/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_upload_mp3_btn")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload MP3 from Device 🎵", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // No Sound option
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTitle.isBlank()) TerracottaContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(null) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VolumeOff, contentDescription = null, tint = TextSecondary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("No Sound (Silent)", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    }
                }

                if (albums.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sound albums uploaded yet.\nVerified Artists can create albums in their Profile studio!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(albums, key = { it.id }) { album ->
                            val isChosen = selectedTitle == album.title
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChosen) TerracottaContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isChosen) BorderStroke(1.dp, TerracottaPrimary) else null,
                                modifier = Modifier.fillMaxWidth().clickable { onSelect(album) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(TerracottaPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Album, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(album.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("By ${album.artistName} • ${album.genre}", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    if (isChosen) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = TerracottaPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
