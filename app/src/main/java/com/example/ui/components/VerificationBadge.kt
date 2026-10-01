package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VerifiedBadgeBlue
import com.example.ui.theme.VerifiedGold

@Composable
fun VerificationBadge(
    badgeType: String = "CERTIFIED_COMPANION",
    size: Dp = 18.dp,
    showLabel: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val (badgeColor, title, description, icon) = when (badgeType) {
        "SENIOR_SNUGGLE_PRO" -> Quadruple(
            Color(0xFF8E24AA),
            "Senior Snuggle Pro",
            "This emotional support dog plushie has served as a dedicated comfort provider for over 2 years.",
            Icons.Default.WorkspacePremium
        )
        "TOP_COMFORT_ANCHOR" -> Quadruple(
            VerifiedGold,
            "Top Comfort Anchor",
            "Verified peer-recognized emotional support companion for sensory grounding & anxiety management.",
            Icons.Default.Pets
        )
        else -> Quadruple(
            VerifiedBadgeBlue,
            "Verified Support Companion",
            "Official authenticity badge confirming a registered, cherished emotional support dog plushie.",
            Icons.Default.Verified
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { showDialog = true }
            .testTag("verification_badge_${badgeType.lowercase()}")
            .padding(horizontal = if (showLabel) 6.dp else 2.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = badgeColor,
            modifier = Modifier.size(size)
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Authenticity & Support Verified",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.testTag("verification_dialog_close")
                ) {
                    Text("Understood")
                }
            }
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
