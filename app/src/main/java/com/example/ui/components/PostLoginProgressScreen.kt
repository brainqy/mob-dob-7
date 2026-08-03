package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ApiLevelProgressDto

@Composable
fun PostLoginProgressScreen(
    userFullName: String,
    progress: ApiLevelProgressDto?,
    isLoadingProgress: Boolean,
    progressError: String?,
    isContinuing: Boolean,
    interviewsError: String?,
    onContinue: () -> Unit,
    onSkipContinue: () -> Unit,
    onRetryProgress: () -> Unit = {}
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1220),
            Color(0xFF0F1724),
            Color(0xFF111A2B)
        )
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F1724)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 32.dp)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + scaleIn(tween(400))
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Welcome back,",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = userFullName,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF0E2A33).copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.border(
                                    width = 1.dp,
                                    color = Color(0xFF22D3EE).copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Rank",
                                        tint = Color(0xFF22D3EE),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Rank #${progress?.rank ?: "—"}",
                                        color = Color(0xFF22D3EE),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = "YOUR PROGRESS",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PERFORMANCE STATS",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        when {
                            isLoadingProgress && progress == null -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF22D3EE),
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Loading your performance stats...",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                            progressError != null && progress == null -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF3B0D0D).copy(alpha = 0.45f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color(0xFFEF4444).copy(alpha = 0.35f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Couldn't load progress",
                                            color = Color(0xFFFCA5A5),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = progressError,
                                            color = Color(0xFFFECACA),
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row {
                                            OutlinedButton(
                                                onClick = onRetryProgress,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color(0xFF22D3EE)
                                                )
                                            ) {
                                                Text(text = "Retry", fontSize = 12.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            OutlinedButton(
                                                onClick = onSkipContinue,
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(text = "Skip & Continue", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        progress?.let { data ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(500)) + scaleIn(tween(500))
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Text(
                                                text = "LEVEL ${data.level}",
                                                color = Color(0xFF22D3EE),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = "%,d".format(data.currentXp),
                                                    color = Color.White,
                                                    fontSize = 52.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "/ %,d XP".format(data.xpForNextLevel),
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                            }
                                        }
                                        val percentSafe = data.percentToNext.coerceIn(0f, 100f).toInt()
                                        Text(
                                            text = "$percentSafe%",
                                            color = Color(0xFF22D3EE),
                                            fontSize = 34.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(22.dp))

                                    val animatedProgress by animateFloatAsState(
                                        targetValue = (data.percentToNext.coerceIn(0f, 100f) / 100f),
                                        animationSpec = tween(durationMillis = 900, delayMillis = 150),
                                        label = "xpProgress"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                    ) {
                                        LinearProgressIndicator(
                                            progress = { animatedProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(18.dp),
                                            color = Color(0xFF22D3EE),
                                            trackColor = Color(0xFF1E293B)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(22.dp))

                                    Text(
                                        text = "%,d XP UNITS TO LEVEL %d".format(
                                            data.remainingXp.coerceAtLeast(0),
                                            data.level + 1
                                        ),
                                        color = Color(0xFF22D3EE),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        letterSpacing = 0.8.sp
                                    )

                                    Spacer(modifier = Modifier.height(36.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        StatCard(
                                            modifier = Modifier.weight(1f),
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.LocalFireDepartment,
                                                    contentDescription = "Day Streak",
                                                    tint = Color(0xFFFB923C),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            },
                                            iconBg = Color(0xFF1A1033),
                                            value = data.dayStreak.toString(),
                                            label = "DAY STREAK"
                                        )

                                        StatCard(
                                            modifier = Modifier.weight(1f),
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.WorkspacePremium,
                                                    contentDescription = "Badges Earned",
                                                    tint = Color(0xFFFACC15),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            },
                                            iconBg = Color(0xFF1A1A0A),
                                            value = data.badgesEarned.toString(),
                                            label = "BADGES\nEARNED"
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AnimatedVisibility(visible = interviewsError != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF3B0D0D).copy(alpha = 0.45f)
                                    )
                                ) {
                                    Text(
                                        text = interviewsError ?: "",
                                        color = Color(0xFFFECACA),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = onContinue,
                                enabled = !isContinuing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF22D3EE),
                                    contentColor = Color(0xFF042F36),
                                    disabledContainerColor = Color(0xFF0E7481),
                                    disabledContentColor = Color(0xFF072A2F)
                                )
                            ) {
                                if (isContinuing) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF042F36),
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Text(
                                    text = if (isContinuing) "Loading interviews…" else "Continue →",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    iconBg: Color,
    value: String,
    label: String
) {
    Card(
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18212F)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
