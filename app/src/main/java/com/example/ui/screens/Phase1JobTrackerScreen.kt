package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.data.AuthDatabase
import com.example.data.DailyGoalEntity
import com.example.data.DailyGoalRepository
import com.example.ui.components.DailyGoalsComponent
import com.example.ui.components.InterviewDashboardComponent
import com.example.ui.components.RecentQuizzesSection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppEnvironment
import com.example.data.I18nHelper
import com.example.data.JobEntity
import com.example.data.QuizResult
import com.example.data.OfferComparisonEntity
import com.example.data.SessionManager
import com.example.data.StreakData
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Phase1JobTrackerScreen(
    jobs: List<JobEntity>,
    offers: List<OfferComparisonEntity> = emptyList(),
    currentTenant: String,
    currentLanguage: String,
    baseUrl: String = AppEnvironment.DEV.defaultBaseUrl,
    isDummyDataAllowed: Boolean = AppEnvironment.DEV.isDummyDataAllowed,
    sessionManager: SessionManager? = null,
    recentQuizResults: List<QuizResult> = emptyList(),
    streakData: StreakData = StreakData(),
    onAddJob: (JobEntity) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteJob: (String) -> Unit,
    onScheduleReminder: (String) -> Unit,
    onAddOffer: (OfferComparisonEntity) -> Unit = {},
    onDeleteOffer: (String) -> Unit = {}
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Interview Dashboard, 1: Daily Goals, 2: Pipeline Board, 3: Offer Evaluator
    var isKanbanMode by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isAddJobDialogOpen by remember { mutableStateOf(false) }
    var isAddOfferDialogOpen by remember { mutableStateOf(false) }
    var interviewRefreshTick by remember { mutableStateOf(0L) }
    var isInterviewRefreshing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember(context) { AuthDatabase.getDatabase(context) }
    val dailyGoalRepo = remember(db) { DailyGoalRepository(db.dailyGoalDao()) }
    val todayGoals by dailyGoalRepo.getTodayGoals().collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        dailyGoalRepo.seedDefaultGoalsIfEmpty()
    }

    val statusCategories = listOf("All", "Saved", "Applied", "Incoming Interview", "Offered", "Cancelled", "Rejected")

    val tenantJobs = remember(jobs, currentTenant) {
        jobs.filter { currentTenant == "platform" || it.tenantId == currentTenant || it.tenantId == "platform" }
    }

    val filteredJobs = remember(tenantJobs, selectedStatusFilter, searchQuery) {
        tenantJobs.filter { job ->
            val matchesStatus = selectedStatusFilter == "All" ||
                    job.status.equals(selectedStatusFilter, ignoreCase = true) ||
                    (selectedStatusFilter == "Incoming Interview" && job.status.equals("Interviewing", ignoreCase = true))
            val matchesSearch = job.companyName.contains(searchQuery, ignoreCase = true) ||
                    job.jobTitle.contains(searchQuery, ignoreCase = true) ||
                    job.notes.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    // Dashboard Statistics
    val totalApps = tenantJobs.size
    val incomingInterviews = tenantJobs.filter { it.status.equals("Incoming Interview", ignoreCase = true) || it.status.equals("Interviewing", ignoreCase = true) }
    val offeredCount = tenantJobs.count { it.status.equals("Offered", ignoreCase = true) }
    val cancelledCount = tenantJobs.count { it.status.equals("Cancelled", ignoreCase = true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedSubTab == 3) {
                    Button(
                        onClick = { isAddOfferDialogOpen = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.testTag("add_offer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Offer",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Offer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Daily Login Streak Banner Card (DataStore Persisted)
            DailyStreakBannerCard(
                streakData = streakData,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Sub-Tab Switcher
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val tabs = listOf("Interview Hub", "Daily Goals", "Pipeline Board", "Offer Evaluator")
                    tabs.forEachIndexed { idx, label ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedSubTab = idx }
                                .testTag("subtab_$idx"),
                            color = if (selectedSubTab == idx) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = if (selectedSubTab == idx) 2.dp else 0.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selectedSubTab == idx) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSubTab == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedSubTab == 0) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = isInterviewRefreshing,
                    onRefresh = { interviewRefreshTick++ }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pullRefresh(pullRefreshState)
                ) {
                    InterviewDashboardComponent(
                        baseUrl = baseUrl,
                        isDummyDataAllowed = isDummyDataAllowed,
                        sessionManager = sessionManager,
                        recentQuizResults = recentQuizResults,
                        onShowToast = { msg -> onScheduleReminder(msg) },
                        externalRefreshTick = interviewRefreshTick,
                        onIsRefreshingChange = { isInterviewRefreshing = it }
                    )
                    PullRefreshIndicator(
                        refreshing = isInterviewRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (selectedSubTab == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    DailyGoalsComponent(
                        goals = todayGoals,
                        onToggleGoal = { goal ->
                            scope.launch { dailyGoalRepo.toggleGoalCompleted(goal) }
                        },
                        onIncrement = { goal ->
                            scope.launch { dailyGoalRepo.incrementProgress(goal) }
                        },
                        onDecrement = { goal ->
                            scope.launch { dailyGoalRepo.decrementProgress(goal) }
                        },
                        onDeleteGoal = { id ->
                            scope.launch { dailyGoalRepo.deleteGoal(id) }
                        },
                        onAddCustomGoal = { title, category, target ->
                            scope.launch {
                                dailyGoalRepo.addGoal(
                                    DailyGoalEntity(
                                        title = title,
                                        category = category,
                                        targetCount = target,
                                        completedCount = 0,
                                        isCompleted = false,
                                        date = DailyGoalRepository.getTodayDateString()
                                    )
                                )
                            }
                        },
                        onSeedPresets = {
                            scope.launch { dailyGoalRepo.seedDefaultGoalsIfEmpty() }
                        }
                    )
                }
            } else if (selectedSubTab == 3) {
                OfferEvaluatorSection(
                    offers = offers,
                    onDeleteOffer = onDeleteOffer,
                    onOpenAddOffer = { isAddOfferDialogOpen = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pipeline Sub-header: Stats + View Mode Toggle
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pipeline Overview",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    TextButton(
                                        onClick = { isKanbanMode = false },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (!isKanbanMode) MaterialTheme.colorScheme.surface else Color.Transparent
                                        )
                                    ) {
                                        Text("List", fontSize = 11.sp, fontWeight = if (!isKanbanMode) FontWeight.Bold else FontWeight.Normal)
                                    }
                                    TextButton(
                                        onClick = { isKanbanMode = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (isKanbanMode) MaterialTheme.colorScheme.surface else Color.Transparent
                                        )
                                    ) {
                                        Text("Kanban", fontSize = 11.sp, fontWeight = if (isKanbanMode) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }

                    // Dashboard Stat Cards Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Total Apps",
                                value = "$totalApps",
                                icon = Icons.Default.Work,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                                isSelected = selectedStatusFilter == "All",
                                onClick = { selectedStatusFilter = "All" },
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                title = "Incoming",
                                value = "${incomingInterviews.size}",
                                icon = Icons.Default.CalendarToday,
                                containerColor = Color(0xFFFFEDD5),
                                contentColor = Color(0xFFC2410C),
                                isSelected = selectedStatusFilter == "Incoming Interview",
                                onClick = { selectedStatusFilter = "Incoming Interview" },
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                title = "Offers",
                                value = "$offeredCount",
                                icon = Icons.Default.CheckCircle,
                                containerColor = Color(0xFFDCFCE7),
                                contentColor = Color(0xFF15803D),
                                isSelected = selectedStatusFilter == "Offered",
                                onClick = { selectedStatusFilter = "Offered" },
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                title = "Cancelled",
                                value = "$cancelledCount",
                                icon = Icons.Default.EventBusy,
                                containerColor = Color(0xFFF3E8FF),
                                contentColor = Color(0xFF7E22CE),
                                isSelected = selectedStatusFilter == "Cancelled",
                                onClick = { selectedStatusFilter = "Cancelled" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (isKanbanMode) {
                        item {
                            KanbanBoardView(
                                jobs = tenantJobs,
                                onUpdateStatus = onUpdateStatus,
                                onDeleteJob = onDeleteJob,
                                onScheduleReminder = onScheduleReminder
                            )
                        }
                    } else {
                        // Search Bar
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("job_search_input"),
                                placeholder = { Text("Search by company, title or keywords...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        // Status Filter Chips Row
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(statusCategories) { status ->
                                    val isSelected = selectedStatusFilter == status
                                    val badgeColor = when (status) {
                                        "Saved" -> Color(0xFF6B7280)
                                        "Applied" -> Color(0xFF2563EB)
                                        "Incoming Interview" -> Color(0xFFEA580C)
                                        "Offered" -> Color(0xFF059669)
                                        "Cancelled" -> Color(0xFF7E22CE)
                                        "Rejected" -> Color(0xFFDC2626)
                                        else -> MaterialTheme.colorScheme.primary
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable { selectedStatusFilter = status }
                                            .testTag("filter_chip_$status"),
                                        color = if (isSelected) badgeColor else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = status,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Job Applications List
                        if (filteredJobs.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Work,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No job applications found",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Tap 'Add Job' to create a new application entry.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredJobs, key = { it.id }) { job ->
                                JobCardItem(
                                    job = job,
                                    onUpdateStatus = onUpdateStatus,
                                    onDeleteJob = onDeleteJob,
                                    onScheduleReminder = onScheduleReminder
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Floating Action Button for Add Job on Pipeline Board
        if (selectedSubTab == 1) {
            FloatingActionButton(
                onClick = { isAddJobDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_job_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Job"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Job", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Add Job Dialog Modal
        if (isAddJobDialogOpen) {
            AddJobDialog(
                currentTenant = currentTenant,
                onDismiss = { isAddJobDialogOpen = false },
                onSave = { newJob ->
                    onAddJob(newJob)
                    isAddJobDialogOpen = false
                }
            )
        }

        // Add Offer Dialog Modal
        if (isAddOfferDialogOpen) {
            AddOfferDialog(
                onDismiss = { isAddOfferDialogOpen = false },
                onSave = { offer ->
                    onAddOffer(offer)
                    isAddOfferDialogOpen = false
                }
            )
        }
    }
}


@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("stat_card_${title.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) containerColor else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun JobCardItem(
    job: JobEntity,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteJob: (String) -> Unit,
    onScheduleReminder: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val statusColor = when (job.status) {
        "Saved" -> Color(0xFF6B7280)
        "Applied" -> Color(0xFF2563EB)
        "Incoming Interview", "Interviewing" -> Color(0xFFEA580C)
        "Offered" -> Color(0xFF059669)
        "Cancelled" -> Color(0xFF7E22CE)
        "Rejected" -> Color(0xFFDC2626)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("job_card_${job.id}"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.jobTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = job.companyName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Status Change Dropdown Badge
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { menuExpanded = true }
                            .testTag("job_status_chip_${job.id}"),
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${job.status} ▾",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        listOf("Saved", "Applied", "Incoming Interview", "Offered", "Cancelled", "Rejected").forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption) },
                                onClick = {
                                    onUpdateStatus(job.id, statusOption)
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Salary Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (job.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.location,
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                if (job.salary.isNotBlank()) {
                    Text(
                        text = job.salary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            if (job.interviewDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val isCancelled = job.status.equals("Cancelled", ignoreCase = true)
                val isIncoming = job.status.equals("Incoming Interview", ignoreCase = true) || job.status.equals("Interviewing", ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCancelled) Color(0xFFF3E8FF) else if (isIncoming) Color(0xFFFFF7ED) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCancelled) Icons.Default.EventBusy else Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (isCancelled) Color(0xFF7E22CE) else if (isIncoming) Color(0xFFEA580C) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCancelled) "Cancelled: ${job.interviewDate}" else "Interview: ${job.interviewDate}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCancelled) Color(0xFF6B21A8) else if (isIncoming) Color(0xFFC2410C) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (!isCancelled) {
                            IconButton(
                                onClick = { onScheduleReminder(job.companyName) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Schedule Reminder",
                                    tint = if (isIncoming) Color(0xFFEA580C) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (job.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = job.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            if (job.hrName.isNotBlank() || job.hrNumber.isNotBlank() || job.hrEmail.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HR Contact: " + (if (job.hrName.isNotBlank()) job.hrName else "Recruiter"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        if (job.hrNumber.isNotBlank() || job.hrEmail.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (job.hrNumber.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "HR Phone",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = job.hrNumber,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }

                                if (job.hrEmail.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "HR Email",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = job.hrEmail,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { onDeleteJob(job.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Job",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddJobDialog(
    currentTenant: String,
    onDismiss: () -> Unit,
    onSave: (JobEntity) -> Unit
) {
    var company by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Applied") }
    var salary by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var interviewDate by remember { mutableStateOf("") }
    var hrName by remember { mutableStateOf("") }
    var hrNumber by remember { mutableStateOf("") }
    var hrEmail by remember { mutableStateOf("") }

    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_job_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Job Application",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_company_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Dropdown
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { statusDropdownExpanded = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status: $status", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("▼", fontSize = 12.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        listOf("Saved", "Applied", "Incoming Interview", "Offered", "Cancelled", "Rejected").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    status = s
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary / Compensation") },
                    placeholder = { Text("$120,000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("Remote / New York") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = interviewDate,
                    onValueChange = { interviewDate = it },
                    label = { Text("Interview Schedule / Notes") },
                    placeholder = { Text("e.g., Fri, July 25 at 2:00 PM") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Next Steps") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "HR / RECRUITER CONTACT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = hrName,
                    onValueChange = { hrName = it },
                    label = { Text("HR / Recruiter Name") },
                    placeholder = { Text("e.g. Jane Doe") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_hr_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = hrNumber,
                    onValueChange = { hrNumber = it },
                    label = { Text("HR Contact Number") },
                    placeholder = { Text("e.g. +1 (555) 019-2834") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_hr_number_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = hrEmail,
                    onValueChange = { hrEmail = it },
                    label = { Text("HR Email Address") },
                    placeholder = { Text("e.g. hr@company.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_hr_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (company.isNotBlank() && title.isNotBlank()) {
                                onSave(
                                    JobEntity(
                                        id = "job-${System.currentTimeMillis()}",
                                        companyName = company,
                                        jobTitle = title,
                                        status = status,
                                        salary = salary,
                                        location = location,
                                        notes = notes,
                                        interviewDate = interviewDate,
                                        hrName = hrName,
                                        hrNumber = hrNumber,
                                        hrEmail = hrEmail,
                                        tenantId = currentTenant
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_job_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Application")
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanBoardView(
    jobs: List<JobEntity>,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteJob: (String) -> Unit,
    onScheduleReminder: (String) -> Unit
) {
    val columns = listOf(
        "Saved" to Color(0xFF6B7280),
        "Applied" to Color(0xFF2563EB),
        "Incoming Interview" to Color(0xFFEA580C),
        "Offered" to Color(0xFF059669),
        "Rejected" to Color(0xFFDC2626)
    )

    val statusOrder = listOf("Saved", "Applied", "Incoming Interview", "Offered", "Rejected")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(columns) { (status, color) ->
            val colJobs = jobs.filter {
                it.status.equals(status, ignoreCase = true) ||
                (status == "Incoming Interview" && it.status.equals("Interviewing", ignoreCase = true))
            }

            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Column Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = color.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = color
                        ) {
                            Text(
                                text = "${colJobs.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (colJobs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No applications",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(450.dp)
                        ) {
                            items(colJobs, key = { it.id }) { job ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = job.jobTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = job.companyName,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        if (job.salary.isNotBlank()) {
                                            Text(
                                                text = job.salary,
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Move Column Controls
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val currentIdx = statusOrder.indexOfFirst { it.equals(status, ignoreCase = true) }

                                            if (currentIdx > 0) {
                                                TextButton(
                                                    onClick = { onUpdateStatus(job.id, statusOrder[currentIdx - 1]) },
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("← ${statusOrder[currentIdx - 1]}", fontSize = 10.sp)
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }

                                            if (currentIdx in 0 until statusOrder.size - 1) {
                                                TextButton(
                                                    onClick = { onUpdateStatus(job.id, statusOrder[currentIdx + 1]) },
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("${statusOrder[currentIdx + 1]} →", fontSize = 10.sp)
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
        }
    }
}

@Composable
private fun OfferEvaluatorSection(
    offers: List<OfferComparisonEntity>,
    onDeleteOffer: (String) -> Unit,
    onOpenAddOffer: () -> Unit
) {
    val maxTC = remember(offers) { offers.maxOfOrNull { it.totalAnnualComp } ?: 1.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💰 Offer Comparison & Compensation Evaluator",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = "Compare base salary, equity stock options, annual bonuses, and relocation benefits side-by-side.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Collapsible points to consider card
        var isPointsExpanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isPointsExpanded = !isPointsExpanded }
                .testTag("points_to_consider_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Points to consider during offer acceptance",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (isPointsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isPointsExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isPointsExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val points = listOf(
                        "CTC is fixed or performance based",
                        "Bench period (whether they pay during full bench period)",
                        "Work life balance",
                        "How many Holidays including sick + mandatory + public holidays",
                        "Location (is Client location is too far)",
                        "Remote work or not or how many days per week or per month",
                        "Any yearly increment provided",
                        "Any bonus like joining or yearly bonus",
                        "Check if hire n fire going on"
                    )
                    points.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (offers.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No offers saved yet.", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenAddOffer, shape = RoundedCornerShape(16.dp)) {
                        Text("Add First Job Offer")
                    }
                }
            }
        } else {
            offers.forEach { offer ->
                val progress = (offer.totalAnnualComp / maxTC).toFloat().coerceIn(0.1f, 1.0f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("offer_card_${offer.id}"),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = offer.companyName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = offer.roleTitle,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format("%,.0f", offer.totalAnnualComp)}/yr",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669)
                                    )
                                )
                                Text(
                                    text = "Total Annual Compensation",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual TC Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TC Weight", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(progress * 100).toInt()}% of top offer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF059669),
                                trackColor = Color(0xFFDCFCE7)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Detailed breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CompensationItem("Base Salary", "$${String.format("%,.0f", offer.baseSalary)}")
                            CompensationItem("Annual Bonus", "$${String.format("%,.0f", offer.bonusAmount)}")
                            CompensationItem("Equity / Stock", "$${String.format("%,.0f", offer.equityValueAnnual)}/yr")
                            if (offer.signingBonus > 0) {
                                CompensationItem("Sign-on Bonus", "$${String.format("%,.0f", offer.signingBonus)}")
                            }
                        }

                        if (offer.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = offer.notes,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { onDeleteOffer(offer.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Offer",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun CompensationItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
        Text(text = value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun AddOfferDialog(
    onDismiss: () -> Unit,
    onSave: (OfferComparisonEntity) -> Unit
) {
    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var base by remember { mutableStateOf("") }
    var bonus by remember { mutableStateOf("") }
    var equity by remember { mutableStateOf("") }
    var signing by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Offer Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = base,
                    onValueChange = { base = it },
                    label = { Text("Base Salary ($)") },
                    placeholder = { Text("140000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bonus,
                    onValueChange = { bonus = it },
                    label = { Text("Annual Bonus ($)") },
                    placeholder = { Text("15000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = equity,
                    onValueChange = { equity = it },
                    label = { Text("Annual Equity / RSU ($/yr)") },
                    placeholder = { Text("25000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = signing,
                    onValueChange = { signing = it },
                    label = { Text("Sign-on Bonus ($)") },
                    placeholder = { Text("10000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Benefits") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (company.isNotBlank() && role.isNotBlank()) {
                                onSave(
                                    OfferComparisonEntity(
                                        id = "off-${System.currentTimeMillis()}",
                                        companyName = company,
                                        roleTitle = role,
                                        baseSalary = base.toDoubleOrNull() ?: 120000.0,
                                        bonusAmount = bonus.toDoubleOrNull() ?: 10000.0,
                                        equityValueAnnual = equity.toDoubleOrNull() ?: 15000.0,
                                        signingBonus = signing.toDoubleOrNull() ?: 0.0,
                                        notes = notes
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Offer")
                    }
                }
            }
        }
    }
}

private data class StreakParticle(
    val startAngle: Float,
    val distance: Float,
    val color: Color,
    val sizePx: Float,
    val isCircle: Boolean
)

@Composable
fun DailyStreakBannerCard(
    streakData: StreakData,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val confettiAnim = remember { Animatable(0f) }

    // Continuous flame pulse
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flamePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    // Generate random particle data
    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFEF4444), // Crimson
            Color(0xFF10B981), // Emerald
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6), // Purple
            Color(0xFFF59E0B)  // Amber
        )
        List(32) {
            StreakParticle(
                startAngle = Random.nextFloat() * 360f,
                distance = Random.nextFloat() * 180f + 60f,
                color = colors[Random.nextInt(colors.size)],
                sizePx = Random.nextFloat() * 12f + 8f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    val triggerCelebration = {
        coroutineScope.launch {
            scaleAnim.snapTo(0.65f)
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        coroutineScope.launch {
            confettiAnim.snapTo(0f)
            confettiAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, easing = LinearEasing)
            )
        }
    }

    androidx.compose.runtime.LaunchedEffect(streakData.streakDays, streakData.totalLogins) {
        triggerCelebration()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_streak_main_card")
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
            .clickable { triggerCelebration() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF2F2))
                                .graphicsLayer {
                                    scaleX = flamePulseScale
                                    scaleY = flamePulseScale
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak Flame",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${streakData.streakDays}-Day Login Streak!",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.testTag("daily_streak_count_text")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "DataStore",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = streakData.milestoneMessage ?: "Log in daily to keep your momentum going strong!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BEST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${streakData.bestStreak}d",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Stats Row (Total Logins, Streak Freezes, Today's Status)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (streakData.isLoginRecordedToday) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (streakData.isLoginRecordedToday) "Active Today" else "Pending Today",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${streakData.streakFreezes} Freeze",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Total Logins: ${streakData.totalLogins}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Confetti Overlay Canvas
            if (confettiAnim.value > 0f && confettiAnim.value < 1f) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val progress = confettiAnim.value
                    val alpha = (1f - progress).coerceIn(0f, 1f)
                    val centerX = size.width * 0.15f
                    val centerY = size.height * 0.35f

                    particles.forEach { p ->
                        val rad = Math.toRadians(p.startAngle.toDouble())
                        val curDist = p.distance * progress
                        val px = centerX + (Math.cos(rad) * curDist).toFloat()
                        val py = centerY + (Math.sin(rad) * curDist).toFloat() + (progress * progress * 90f)

                        rotate(degrees = p.startAngle + progress * 360f, pivot = Offset(px, py)) {
                            if (p.isCircle) {
                                drawCircle(
                                    color = p.color.copy(alpha = alpha),
                                    radius = p.sizePx / 2f,
                                    center = Offset(px, py)
                                )
                            } else {
                                drawRect(
                                    color = p.color.copy(alpha = alpha),
                                    topLeft = Offset(px - p.sizePx / 2f, py - p.sizePx / 2f),
                                    size = Size(p.sizePx, p.sizePx)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


