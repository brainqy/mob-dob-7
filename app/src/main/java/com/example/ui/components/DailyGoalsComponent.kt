package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.DailyGoalEntity
import kotlinx.coroutines.launch

@Composable
fun DailyGoalsComponent(
    goals: List<DailyGoalEntity>,
    onToggleGoal: (DailyGoalEntity) -> Unit,
    onIncrement: (DailyGoalEntity) -> Unit,
    onDecrement: (DailyGoalEntity) -> Unit,
    onDeleteGoal: (Int) -> Unit,
    onAddCustomGoal: (title: String, category: String, targetCount: Int) -> Unit,
    onSeedPresets: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val completedCount = goals.count { it.isCompleted || (it.targetCount > 0 && it.completedCount >= it.targetCount) }
    val totalGoals = goals.size
    val progressFraction = if (totalGoals > 0) completedCount.toFloat() / totalGoals.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_goals_component")
    ) {
        // TOP SUMMARY CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = "Daily Goals",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Today's Daily Goals",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (totalGoals == 0) "No goals set for today"
                                else if (completedCount == totalGoals) "🎉 All daily goals completed!"
                                else "$completedCount of $totalGoals completed (${(progressFraction * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (completedCount == totalGoals && totalGoals > 0) Color(0xFF10B981)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (completedCount == totalGoals && totalGoals > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("add_daily_goal_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add Goal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Linear Progress Indicator
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (completedCount == totalGoals && totalGoals > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (totalGoals == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSeedPresets,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("load_recommended_goals_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Load Recommended Daily Goals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // GOALS LIST OR EMPTY STATE
        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build job search momentum daily!",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "Set daily targets like 'Apply to 3 companies' or 'Practice 1 interview question'.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                goals.forEach { goal ->
                    DailyGoalCardItem(
                        goal = goal,
                        onToggle = { onToggleGoal(goal) },
                        onIncrement = { onIncrement(goal) },
                        onDecrement = { onDecrement(goal) },
                        onDelete = { onDeleteGoal(goal.id) }
                    )
                }
            }
        }

        // ADD CUSTOM GOAL DIALOG
        if (showAddDialog) {
            AddDailyGoalDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, category, target ->
                    onAddCustomGoal(title, category, target)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun DailyGoalCardItem(
    goal: DailyGoalEntity,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    val isDone = goal.isCompleted || (goal.targetCount > 0 && goal.completedCount >= goal.targetCount)

    val categoryColor = when (goal.category) {
        "Applications" -> Color(0xFF0284C7)
        "Interview Prep" -> Color(0xFF7C3AED)
        "Networking" -> Color(0xFFD97706)
        "Resume & Portfolio" -> Color(0xFF059669)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_goal_item_${goal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDone) Color(0xFF10B981).copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox / Circle Toggle Button
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("toggle_goal_${goal.id}")
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Complete",
                    tint = if (isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Goal Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = goal.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isDone) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✓ Completed",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isDone) FontWeight.Medium else FontWeight.SemiBold,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Stepper controls for multi-count goals (e.g., 2/3)
            if (goal.targetCount > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(24.dp),
                        enabled = goal.completedCount > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrement",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "${goal.completedCount}/${goal.targetCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(24.dp),
                        enabled = goal.completedCount < goal.targetCount
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increment",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Delete Goal Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_goal_${goal.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Goal",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddDailyGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, targetCount: Int) -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Applications") }
    var targetCountText by remember { mutableStateOf("1") }

    val categories = listOf("Applications", "Interview Prep", "Networking", "Resume & Portfolio", "Learning")
    val presetSuggestions = listOf(
        "Apply to 3 companies" to "Applications",
        "Practice 1 interview question" to "Interview Prep",
        "Send 2 networking connection requests" to "Networking",
        "Update resume or review portfolio" to "Resume & Portfolio",
        "Research 2 prospective employers" to "Applications",
        "Follow up on submitted application" to "Applications"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddTask,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Daily Goal", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Presets:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Pick Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickPicks = presetSuggestions.take(3)
                    quickPicks.forEach { (presetTitle, cat) ->
                        SuggestionChip(
                            onClick = {
                                titleText = presetTitle
                                selectedCategory = cat
                                if (presetTitle.contains("3")) targetCountText = "3"
                                else if (presetTitle.contains("2")) targetCountText = "2"
                                else targetCountText = "1"
                            },
                            label = { Text(presetTitle, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Goal Title (e.g. Apply to 3 companies)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Category:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category Selector Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.drop(3).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = targetCountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) targetCountText = it },
                        label = { Text("Target Count") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("goal_target_count_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = targetCountText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    if (titleText.isNotBlank()) {
                        onSave(titleText.trim(), selectedCategory, count)
                    }
                },
                enabled = titleText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_daily_goal_button")
            ) {
                Text("Add Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
