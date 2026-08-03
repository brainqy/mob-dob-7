package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ShareType {
    STREAK,
    QUIZ_SCORE
}

@Composable
fun ShareMilestoneDialog(
    shareType: ShareType,
    title: String,
    subtitle: String,
    defaultMessage: String,
    streakDays: Int = 0,
    scorePercentage: Int = 0,
    quizTitle: String = "",
    onShareToCommunity: (String) -> Unit,
    onDismiss: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var customMessage by remember { mutableStateOf(defaultMessage) }
    var isPostedToCommunity by remember { mutableStateOf(false) }

    val gradientBrush = if (shareType == ShareType.STREAK) {
        Brush.horizontalGradient(colors = listOf(Color(0xFFEA580C), Color(0xFFDC2626)))
    } else {
        Brush.horizontalGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED)))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // PREVIEW CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_milestone_preview_card"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(gradientBrush)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (shareType == ShareType.STREAK) Icons.Default.LocalFireDepartment else Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (shareType == ShareType.STREAK) "STREAK MILESTONE 🔥" else "QUIZ SCORE 🎯",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "JobTraq Verified",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (shareType == ShareType.STREAK) {
                            Text(
                                text = "🔥 $streakDays Days Unstoppable Streak!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        } else {
                            Text(
                                text = "🎯 $scorePercentage% Score on '$quizTitle'",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // MESSAGE EDIT FIELD
                Text(
                    text = "Post / Share Caption",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_custom_caption_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // DESTINATION BUTTONS
                Text(
                    text = "Select Share Destination:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option A: Share to JobTraq Community Feed
                Button(
                    onClick = {
                        onShareToCommunity(customMessage)
                        isPostedToCommunity = true
                        onShowToast("🎉 Shared to JobTraq Community Feed!")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_to_community_feed_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPostedToCommunity) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isPostedToCommunity) Icons.Default.Check else Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPostedToCommunity) "Posted to Community Feed ✓" else "Post to JobTraq Community Feed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option B: Native Android External Share (LinkedIn, Twitter, WhatsApp, etc.)
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, customMessage)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share milestone via")
                            context.startActivity(shareIntent)
                            onShowToast("Opening external share menu...")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_external_social_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("External Social", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Option C: Copy to Clipboard
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(customMessage))
                            onShowToast("Copied caption to clipboard! 📋")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_copy_caption_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("close_share_dialog_button")
            ) {
                Text("Done")
            }
        }
    )
}
