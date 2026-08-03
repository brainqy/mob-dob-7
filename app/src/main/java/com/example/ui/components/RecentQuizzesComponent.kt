package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.QuizResult
import com.example.ui.screens.QuizResultView

@Composable
fun RecentQuizzesSection(
    recentQuizzes: List<QuizResult>,
    onReviewQuiz: ((QuizResult) -> Unit)? = null,
    onShareToCommunity: ((String) -> Unit)? = null,
    onShowToast: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedQuizForReview by remember { mutableStateOf<QuizResult?>(null) }
    var selectedQuizForShare by remember { mutableStateOf<QuizResult?>(null) }

    // Dialog for full Review Mode
    selectedQuizForReview?.let { quizResult ->
        Dialog(
            onDismissRequest = { selectedQuizForReview = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    QuizResultView(
                        result = quizResult,
                        onDismiss = { selectedQuizForReview = null },
                        onShareToCommunity = onShareToCommunity,
                        onShowToast = onShowToast
                    )
                }
            }
        }
    }

    // Dialog for Share Score
    selectedQuizForShare?.let { item ->
        ShareMilestoneDialog(
            shareType = ShareType.QUIZ_SCORE,
            title = "Share Quiz Score",
            subtitle = "Showcase your ${item.scorePercentage}% score to peers and social platforms!",
            defaultMessage = "🎯 I scored ${item.scorePercentage}% (${item.correctAnswers}/${item.totalQuestions}) on '${item.quizTitle}' in JobTraq! 🚀 #JobTraq #InterviewPrep",
            scorePercentage = item.scorePercentage,
            quizTitle = item.quizTitle,
            onShareToCommunity = { text ->
                onShareToCommunity?.invoke(text)
            },
            onDismiss = { selectedQuizForShare = null },
            onShowToast = { msg -> onShowToast?.invoke(msg) }
        )
    }

    val displayList = remember(recentQuizzes) { recentQuizzes.take(5) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recent_quizzes_dashboard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recent Quizzes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Last ${displayList.size} quiz attempts & review links",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${displayList.size} Taken",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (displayList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent quizzes taken yet. Take a quiz to track your progress!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                displayList.forEachIndexed { index, item ->
                    val scoreColor = when {
                        item.scorePercentage >= 80 -> Color(0xFF15803D)
                        item.scorePercentage >= 60 -> Color(0xFFB45309)
                        else -> Color(0xFFB91C1C)
                    }
                    val scoreBg = when {
                        item.scorePercentage >= 80 -> Color(0xFFDCFCE7)
                        item.scorePercentage >= 60 -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    }
                    val progressFloat = (item.scorePercentage / 100f).coerceIn(0f, 1f)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (onReviewQuiz != null) {
                                    onReviewQuiz(item)
                                } else {
                                    selectedQuizForReview = item
                                }
                            }
                            .testTag("recent_quiz_item_$index"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Score Badge Ring
                                    Surface(
                                        shape = CircleShape,
                                        color = scoreBg,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${item.scorePercentage}%",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = scoreColor,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.quizTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Challenge / Practice Tag Chip
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (item.isChallengeMode) Color(0xFFFFEDD5) else MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (item.isChallengeMode) {
                                                        Icon(
                                                            imageVector = Icons.Default.EmojiEvents,
                                                            contentDescription = null,
                                                            tint = Color(0xFFC2410C),
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                    }
                                                    Text(
                                                        text = if (item.isChallengeMode) "Challenge" else "Practice",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (item.isChallengeMode) Color(0xFFC2410C) else MaterialTheme.colorScheme.primary
                                                        )
                                                    )
                                                }
                                            }

                                            // Time Tag
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Timer,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${item.durationSeconds / 60}m ${item.durationSeconds % 60}s",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Action Buttons: Share & Review
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { selectedQuizForShare = item },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("share_recent_quiz_button_$index")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share Score",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (onReviewQuiz != null) {
                                                onReviewQuiz(item)
                                            } else {
                                                selectedQuizForReview = item
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("review_quiz_button_$index"),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Review",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Accuracy Progress Bar & Score Breakdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LinearProgressIndicator(
                                    progress = { progressFloat },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = scoreColor,
                                    trackColor = scoreBg
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "${item.correctAnswers}/${item.totalQuestions} Correct",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
