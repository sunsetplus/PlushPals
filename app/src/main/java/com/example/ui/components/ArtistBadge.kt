package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.TerracottaPrimary

@Composable
fun ArtistBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    showLabel: Boolean = false
) {
    if (showLabel) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFFECE5),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.5f)),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(TerracottaPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Verified Artist",
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Artist",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            TerracottaPrimary,
                            AmberAccent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Verified Artist Badge",
                tint = Color.White,
                modifier = Modifier.size(size * 0.65f)
            )
        }
    }
}
