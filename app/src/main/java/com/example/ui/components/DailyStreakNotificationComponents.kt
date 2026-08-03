package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Share
import com.example.data.DailyStreakNotificationState

@Composable
fun DailyStreakPromptBanner(
    state: DailyStreakNotificationState,
    onCompleteChallenge: (Int) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onShowToast: (String) -> Unit,
    onShareToCommunity: ((String) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var answerInput by remember { mutableStateOf("") }
    var isSubmitted by remember(state.dailyChallengeCompletedToday) { mutableStateOf(state.dailyChallengeCompletedToday) }
    var showShareStreakDialog by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) } // Default collapsed to save space

    if (showShareStreakDialog) {
        ShareMilestoneDialog(
            shareType = ShareType.STREAK,
            title = "Share Streak Milestone",
            subtitle = "Celebrate your ${state.streakDays}-day streak with your network!",
            defaultMessage = "🔥 I'm on a ${state.streakDays}-day interview prep streak on JobTraq! Building consistency every day 💪 #JobTraq #InterviewPrep #Streak",
            streakDays = state.streakDays,
            onShareToCommunity = { text ->
                onShareToCommunity?.invoke(text)
            },
            onDismiss = { showShareStreakDialog = false },
            onShowToast = onShowToast
        )
    }

    val gradientBrush = if (state.dailyChallengeCompletedToday) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF059669), Color(0xFF10B981))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFFDC2626), Color(0xFFEA580C))
        )
    }

    AnimatedVisibility(visible = !isDismissed) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("daily_streak_prompt_banner"),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(gradientBrush)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Header Row: Streak Count & Reminders Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Fire",
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STREAK: ${state.streakDays} DAYS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (state.dailyChallengeCompletedToday) "COMPLETED ✓" else "DAILY REMINDER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showShareStreakDialog = true }
                                .testTag("share_streak_milestone_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Streak",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Share 🔥",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onOpenNotificationSettings() }
                                .testTag("open_notification_settings_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notification Reminders",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.reminderTime,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Expand / Collapse Toggle Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { isExpanded = !isExpanded }
                                .testTag("toggle_expand_daily_challenge_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse Challenge" else "Expand Challenge",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Close / Dismiss Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    isDismissed = true
                                    onDismiss?.invoke()
                                    onShowToast("Daily Interview Challenge card closed")
                                }
                                .testTag("close_daily_challenge_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Daily Challenge",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Body content
                        if (state.dailyChallengeCompletedToday || isSubmitted) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Daily Challenge Complete!",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "Streak maintained: ${state.streakDays} Days 🔥 (+50 XP & +25 FlashCoins awarded)",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            Column {
                                Text(
                                    text = "Q: What is a closure in JavaScript or Kotlin, and how is state captured?",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        lineHeight = 20.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = answerInput,
                                    onValueChange = { answerInput = it },
                                    placeholder = {
                                        Text(
                                            "Type your daily answer to preserve your streak...",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("daily_challenge_answer_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⏰ Reminder set for ${state.reminderTime}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 10.sp
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            isSubmitted = true
                                            isExpanded = false
                                            onCompleteChallenge(50)
                                            onShowToast("🎉 Daily Challenge Completed! Streak maintained: ${state.streakDays + 1} Days 🔥")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFFDC2626)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("submit_daily_streak_answer_button")
                                    ) {
                                        Text(
                                            text = "Submit & Keep Streak 🔥",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
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

@Composable
fun DailyNotificationSettingsDialog(
    state: DailyStreakNotificationState,
    onDismiss: () -> Unit,
    onUpdateSettings: (enabled: Boolean, time: String, frequency: String, sound: Boolean, vibrate: Boolean) -> Unit,
    onTriggerTestNotification: () -> Unit
) {
    var enabled by remember { mutableStateOf(state.dailyReminderEnabled) }
    var selectedTime by remember { mutableStateOf(state.reminderTime) }
    var selectedFrequency by remember { mutableStateOf(state.reminderFrequency) }
    var soundEnabled by remember { mutableStateOf(state.soundEnabled) }
    var vibrateEnabled by remember { mutableStateOf(state.vibrateEnabled) }

    val times = listOf("08:00 AM", "09:00 AM", "12:00 PM", "06:00 PM", "08:00 PM")
    val frequencies = listOf("Daily", "Weekdays", "Weekends")
    val weekDaysLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Streak Notification Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Set daily reminders to complete at least 1 interview question or quiz and preserve your streak.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7-DAY STREAK TRACKER PREVIEW
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Streak History",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${state.streakDays} Day Streak 🔥",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFEA580C),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            weekDaysLabels.forEachIndexed { index, day ->
                                val isDone = state.streakHistoryDays.getOrElse(index) { false }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TOGGLE DAILY NOTIFICATIONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Push Reminders",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Prompt me if daily challenge is uncompleted",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        modifier = Modifier.testTag("toggle_daily_reminder_switch")
                    )
                }

                if (enabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Preferred Reminder Time",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(times) { time ->
                            val isSelected = selectedTime == time
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTime = time },
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Repeat Schedule",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        frequencies.forEach { freq ->
                            val isSelected = selectedFrequency == freq
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedFrequency = freq },
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = freq,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sound & Vibrate options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { soundEnabled = !soundEnabled }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (soundEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sound", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { vibrateEnabled = !vibrateEnabled }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = if (vibrateEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vibrate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TEST NOTIFICATION BUTTON
                OutlinedButton(
                    onClick = {
                        onTriggerTestNotification()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trigger_test_notification_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Test Push Notification", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateSettings(enabled, selectedTime, selectedFrequency, soundEnabled, vibrateEnabled)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_notification_settings_button")
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HeadsUpNotificationBanner(
    text: String,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("headsup_notification_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA580C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "JobTraq • Daily Streak Reminder",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        onDismiss()
                        onActionClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("headsup_solve_now_button")
                ) {
                    Text("Solve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Notification",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
