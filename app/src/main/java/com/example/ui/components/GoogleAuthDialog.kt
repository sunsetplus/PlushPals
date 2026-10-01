package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WarmCaramel

@Composable
fun GoogleAuthDialog(
    currentEmail: String = "alex.plushie.companion@gmail.com",
    onDismiss: () -> Unit,
    onAccountSelected: (name: String, email: String, plushieName: String, breed: String) -> Unit
) {
    var selectedOption by remember { mutableStateOf(0) }
    var customName by remember { mutableStateOf("") }
    var customPlushie by remember { mutableStateOf("") }
    var customBreed by remember { mutableStateOf("Golden Retriever") }
    var showCustomInput by remember { mutableStateOf(false) }

    val googleAccounts = listOf(
        Triple("Alex Rivera", "alex.plushie.companion@gmail.com", "Barnaby (Golden Retriever)"),
        Triple("Taylor Swift & Otis", "taylor.otis.support@gmail.com", "Otis (Bernese Mountain Dog)"),
        Triple("Jordan Lee & Biscuit", "jordan.biscuit@gmail.com", "Biscuit (Corgi)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F0FE)),
                contentAlignment = Alignment.Center
            ) {
                // Google "G" style icon representation
                Text(
                    text = "G",
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color(0xFF4285F4)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Google Account Sign-In",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Sign in to synchronize your plushie companions securely",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!showCustomInput) {
                    Text(
                        text = "Choose a Google Account:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    googleAccounts.forEachIndexed { index, account ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedOption = index }
                                .testTag("google_account_option_$index"),
                            color = if (selectedOption == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedOption == index) BorderStroke(1.5.dp, WarmCaramel) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(WarmCaramel.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = WarmCaramel,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = account.first,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = account.second,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Companion: ${account.third}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarmCaramel
                                    )
                                }
                                if (selectedOption == index) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = WarmCaramel
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally).testTag("custom_google_account_btn")
                    ) {
                        Text("+ Add another Google Account")
                    }
                } else {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customPlushie,
                        onValueChange = { customPlushie = it },
                        label = { Text("Dog Plushie Name") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_plushie_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customBreed,
                        onValueChange = { customBreed = it },
                        label = { Text("Plushie Breed (e.g. Golden Retriever, Corgi)") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_breed_input"),
                        singleLine = true
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Encrypted & locally stored in secure Room DB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (showCustomInput) {
                        val name = customName.ifBlank { "Support Parent" }
                        val plushie = customPlushie.ifBlank { "Buddy" }
                        val breed = customBreed.ifBlank { "Golden Retriever" }
                        val email = "${name.lowercase().replace(" ", "")}@gmail.com"
                        onAccountSelected(name, email, plushie, breed)
                    } else {
                        val account = googleAccounts[selectedOption]
                        val parts = account.third.split(" (")
                        val plushieName = parts[0]
                        val breed = parts.getOrNull(1)?.removeSuffix(")") ?: "Golden Retriever"
                        onAccountSelected(account.first, account.second, plushieName, breed)
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("google_auth_confirm_btn")
            ) {
                Text("Continue with Google")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("google_auth_cancel_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}
