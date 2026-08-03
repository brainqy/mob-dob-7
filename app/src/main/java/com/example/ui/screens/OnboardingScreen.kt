package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingStepData(
    val id: Int,
    val title: String,
    val subtitle: String,
    val tag: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val keyFeatures: List<String>,
    val interactiveTip: String
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    onSkipOnboarding: () -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStepData(
                id = 0,
                title = "Track Your Career Journey",
                subtitle = "Organize job applications, track interview stages, salary offers, and follow-ups in one smart dashboard.",
                tag = "STEP 1 OF 4 • APPLICATION TRACKER",
                icon = Icons.Default.Work,
                primaryColor = Color(0xFF2563EB), // Vibrant Blue
                secondaryColor = Color(0xFF3B82F6),
                keyFeatures = listOf(
                    "Kanban & List views for application stages",
                    "Real-time interview deadline reminders",
                    "Salary expectation & offer comparison"
                ),
                interactiveTip = "💡 Tip: Log applications immediately to track conversion rates over time!"
            ),
            OnboardingStepData(
                id = 1,
                title = "Ace AI Interview Prep",
                subtitle = "Master behavioral & technical questions with AI STAR answer coaching and instant feedback.",
                tag = "STEP 2 OF 4 • INTERVIEW COACHING",
                icon = Icons.Default.Psychology,
                primaryColor = Color(0xFF7C3AED), // Vibrant Purple
                secondaryColor = Color(0xFF8B5CF6),
                keyFeatures = listOf(
                    "AI STAR framework (Situation, Task, Action, Result)",
                    "Custom practice quizzes with detailed explanations",
                    "Peer challenge mode & answer sharing"
                ),
                interactiveTip = "🚀 Tip: Practice 1 question daily to boost interview confidence by 80%!"
            ),
            OnboardingStepData(
                id = 2,
                title = "Build Unstoppable Streaks",
                subtitle = "Stay motivated with daily practice streaks, milestone badges, and peer community support.",
                tag = "STEP 3 OF 4 • DAILY CONSISTENCY",
                icon = Icons.Default.Leaderboard,
                primaryColor = Color(0xFFEA580C), // Vibrant Orange
                secondaryColor = Color(0xFFF97316),
                keyFeatures = listOf(
                    "Daily streak tracking with customizable reminders",
                    "Community feed to share quiz achievements & milestones",
                    "Peer challenge scoreboards"
                ),
                interactiveTip = "🔥 Tip: Keep a 7-day streak to unlock special achievement badges!"
            ),
            OnboardingStepData(
                id = 3,
                title = "Biometric Security & AI Tools",
                subtitle = "Unlock your account instantly with biometrics, export PDF prep reports, and optimize your resume.",
                tag = "STEP 4 OF 4 • SECURITY & UTILITIES",
                icon = Icons.Default.Security,
                primaryColor = Color(0xFF059669), // Vibrant Emerald
                secondaryColor = Color(0xFF10B981),
                keyFeatures = listOf(
                    "Fingerprint & Face biometric quick unlock",
                    "Instant PDF summary generator for offline review",
                    "Salary calculator & AI resume match tool"
                ),
                interactiveTip = "🛡️ Tip: Enable biometric unlock in Settings for fast, single-tap access!"
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    val totalSteps = steps.size

    // Animated progress state for the smooth progress bar
    val targetProgress = (currentStepIndex + 1).toFloat() / totalSteps.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "OnboardingProgressAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ================= TOP HEADER (Progress Bar & Skip) =================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    if (currentStepIndex > 0) {
                        IconButton(
                            onClick = { currentStepIndex-- },
                            modifier = Modifier.testTag("onboarding_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Step",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    // Step Tag Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = currentStep.primaryColor.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = currentStep.tag,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = currentStep.primaryColor,
                                letterSpacing = 0.8.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Skip Button
                    TextButton(
                        onClick = onSkipOnboarding,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // PROMINENT PROGRESS BAR WITH PERCENTAGE INDICATOR
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = currentStep.primaryColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("onboarding_progress_bar"),
                        color = currentStep.primaryColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ================= MAIN CONTENT CAROUSEL WITH ANIMATION =================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState.id > initialState.id) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "OnboardingSlideTransition"
                ) { step ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hero Icon Illustration Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                step.primaryColor,
                                                step.secondaryColor
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Background ambient circles
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .align(Alignment.TopEnd)
                                        .background(
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.BottomStart)
                                        .background(
                                            color = Color.White.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.25f),
                                        modifier = Modifier.size(90.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = step.icon,
                                                contentDescription = step.title,
                                                tint = Color.White,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.Black.copy(alpha = 0.25f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.Yellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "JobTraq Feature Highlights",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Title & Subtitle
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = step.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Key Feature Checklist Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                step.keyFeatures.forEach { feature ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = step.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = feature,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Pro Tip Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = step.primaryColor.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                step.primaryColor.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = step.interactiveTip,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = step.primaryColor,
                                    lineHeight = 18.sp
                                ),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= BOTTOM NAVIGATION CONTROLS & DOT INDICATORS =================
            Column(modifier = Modifier.fillMaxWidth()) {
                // Step Dot Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalSteps) { index ->
                        val isSelected = index == currentStepIndex
                        val width = if (isSelected) 28.dp else 10.dp
                        val color = if (isSelected) currentStep.primaryColor else MaterialTheme.colorScheme.outlineVariant

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(10.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { currentStepIndex = index }
                                .testTag("onboarding_dot_$index")
                        )
                    }
                }

                // Primary Next / Get Started Action Button
                Button(
                    onClick = {
                        if (currentStepIndex < totalSteps - 1) {
                            currentStepIndex++
                        } else {
                            onFinishOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentStep.primaryColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStepIndex == totalSteps - 1) "Get Started Now" else "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (currentStepIndex == totalSteps - 1) Icons.Default.RocketLaunch else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Step",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
