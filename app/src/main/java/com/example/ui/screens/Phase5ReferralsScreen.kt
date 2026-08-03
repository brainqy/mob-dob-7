package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReferralActivityLog
import com.example.data.ReferralHistoryEntity
import com.example.data.ReferralLeaderboardUser
import com.example.data.WalletState

@Composable
fun Phase5ReferralsScreen(
    referrals: List<ReferralHistoryEntity>,
    leaderboard: List<ReferralLeaderboardUser>,
    activityLogs: List<ReferralActivityLog>,
    walletState: WalletState,
    onCreateReferral: (String, String, String) -> Unit,
    onActivateReferral: (String) -> String,
    onMarkReferralHired: (String, String) -> String,
    onNudgeReferralFriend: (String) -> String,
    onGiftStreakFreeze: (String) -> Pair<Boolean, String>,
    onShowToast: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Friends List, 1: Leaderboard, 2: Wallet & Logs
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var isInviteDialogOpen by remember { mutableStateOf(false) }

    val userReferralCode = walletState.referralCode

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
            Spacer(modifier = Modifier.height(8.dp))

            if (onBack != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("referrals_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Referrals & Rewards",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Header Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("referral_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Invite Friends & Earn Rewards 🎁",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                text = "Earn +50 XP on signup & up to 700 coins when your candidate is hired!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Referral",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Referral Code Box with Copy & Share Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "YOUR REFERRAL CODE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = userReferralCode,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Referral Code", userReferralCode)
                                    clipboard.setPrimaryClip(clip)
                                    onShowToast("Referral code '$userReferralCode' copied to clipboard!")
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("copy_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    onShowToast("Shareable link: https://jobtraq.app/signup?ref=$userReferralCode")
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("share_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Wallet Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatMiniBadge(label = "Wallet Coins", value = "🪙 ${walletState.coins}")
                        StatMiniBadge(label = "Streak Protects", value = "🛡️ ${walletState.streakFreezes}")
                        StatMiniBadge(label = "Total Referrals", value = "👥 ${referrals.size}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "My Friends (${referrals.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_referrals_list")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Leaderboard 🏆",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_referrals_leaderboard")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Wallet & Logs 📜",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_referrals_logs")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> ReferralsListTabContent(
                    referrals = referrals,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    onSearchQueryChange = { searchQuery = it },
                    onStatusFilterChange = { statusFilter = it },
                    onOpenInviteDialog = { isInviteDialogOpen = true },
                    onActivateReferral = { id ->
                        val msg = onActivateReferral(id)
                        onShowToast(msg)
                    },
                    onMarkReferralHired = { id, dept ->
                        val msg = onMarkReferralHired(id, dept)
                        onShowToast(msg)
                    },
                    onNudgeFriend = { id ->
                        val msg = onNudgeReferralFriend(id)
                        onShowToast(msg)
                    },
                    onGiftStreakFreeze = { id ->
                        val (success, msg) = onGiftStreakFreeze(id)
                        onShowToast(msg)
                    }
                )

                1 -> ReferralsLeaderboardTabContent(leaderboard = leaderboard)

                2 -> WalletAndLogsTabContent(
                    walletState = walletState,
                    activityLogs = activityLogs
                )
            }
        }

        // New Invite Referral Dialog
        if (isInviteDialogOpen) {
            NewReferralInviteDialog(
                onDismiss = { isInviteDialogOpen = false },
                onSendInvite = { email, dept, title ->
                    onCreateReferral(email, userReferralCode, dept)
                    onShowToast("Invite code '$userReferralCode' sent to $email!")
                    isInviteDialogOpen = false
                }
            )
        }
    }
}

@Composable
private fun StatMiniBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ReferralsListTabContent(
    referrals: List<ReferralHistoryEntity>,
    searchQuery: String,
    statusFilter: String,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onOpenInviteDialog: () -> Unit,
    onActivateReferral: (String) -> Unit,
    onMarkReferralHired: (String, String) -> Unit,
    onNudgeFriend: (String) -> Unit,
    onGiftStreakFreeze: (String) -> Unit
) {
    val statusOptions = listOf("All", "Pending", "Signed Up", "Reward Earned")

    val filteredReferrals = remember(referrals, searchQuery, statusFilter) {
        referrals.filter { ref ->
            val matchesSearch = ref.referredEmailOrName.contains(searchQuery, ignoreCase = true) ||
                    ref.department.contains(searchQuery, ignoreCase = true) ||
                    ref.jobTitle.contains(searchQuery, ignoreCase = true)
            val matchesFilter = statusFilter == "All" || ref.status.equals(statusFilter, ignoreCase = true)
            matchesSearch && matchesFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("referrals_search_input"),
                placeholder = { Text("Search friends...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onOpenInviteDialog,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("invite_friend_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Invite",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Invite", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statusOptions.forEach { status ->
                val isSelected = statusFilter.equals(status, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onStatusFilterChange(status) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("status_chip_$status")
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredReferrals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No referral invites found",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "Tap 'Invite' above to send your code to friends!",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredReferrals) { ref ->
                    ReferralFriendCard(
                        referral = ref,
                        onActivate = { onActivateReferral(ref.id) },
                        onMarkHired = { onMarkReferralHired(ref.id, ref.department) },
                        onNudge = { onNudgeFriend(ref.id) },
                        onGiftShield = { onGiftStreakFreeze(ref.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferralFriendCard(
    referral: ReferralHistoryEntity,
    onActivate: () -> Unit,
    onMarkHired: () -> Unit,
    onNudge: () -> Unit,
    onGiftShield: () -> Unit
) {
    val statusColor = when (referral.status) {
        "Reward Earned" -> Color(0xFF10B981)
        "Signed Up" -> Color(0xFF3B82F6)
        else -> Color(0xFFF59E0B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_card_${referral.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = referral.referredEmailOrName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = referral.referredEmailOrName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "${referral.jobTitle} • ${referral.department}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Status Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (referral.status == "Reward Earned") "Hired 🏆 (+${referral.rewardAmount ?: 700}🪙)" else referral.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Workflow Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (referral.status == "Pending") {
                    TextButton(
                        onClick = onActivate,
                        modifier = Modifier.testTag("activate_signup_btn_${referral.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activate (+50 XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (referral.status == "Signed Up") {
                    Button(
                        onClick = onMarkHired,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("mark_hired_btn_${referral.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Hired (+700 🪙)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reward Disbursed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Friend Nudge Action
                    OutlinedButton(
                        onClick = onNudge,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("nudge_btn_${referral.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Nudge",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Nudge 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Gift Streak Freeze Action (200 Coins)
                    OutlinedButton(
                        onClick = onGiftShield,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("gift_shield_btn_${referral.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Gift Shield",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Gift Shield 🎁", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferralsLeaderboardTabContent(leaderboard: List<ReferralLeaderboardUser>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(leaderboard) { user ->
            val rankBadgeColor = when (user.rank) {
                1 -> Color(0xFFFFD700) // Gold
                2 -> Color(0xFFC0C0C0) // Silver
                3 -> Color(0xFFCD7F32) // Bronze
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leaderboard_user_${user.rank}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(rankBadgeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${user.rank}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = if (user.rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${user.successfulReferrals} Successful Referrals",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🪙 ${user.totalEarnedCoins}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletAndLogsTabContent(
    walletState: WalletState,
    activityLogs: List<ReferralActivityLog>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wallet & Gamification Overview 🪙",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Coins Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${walletState.coins} 🪙", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("Bonus XP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${walletState.xp} XP", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("Streak Freezes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${walletState.streakFreezes} 🛡️", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(walletState.transactions) { tx ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (tx.type == "CREDIT") Icons.Default.LocalOffer else Icons.Default.History,
                            contentDescription = null,
                            tint = if (tx.type == "CREDIT") Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tx.description,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = if (tx.type == "CREDIT") "+${tx.amount}" else "-${tx.amount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.type == "CREDIT") Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Referral Activity Log",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(activityLogs) { log ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = log.text,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NewReferralInviteDialog(
    onDismiss: () -> Unit,
    onSendInvite: (String, String, String) -> Unit
) {
    var emailOrName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Engineering") }
    var jobTitle by remember { mutableStateOf("Software Engineer") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Invite Friend to JobTraq 🎁",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter your friend's email or name to send them your unique referral code.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = emailOrName,
                    onValueChange = { emailOrName = it },
                    label = { Text("Friend's Email or Name") },
                    placeholder = { Text("alex@friend.io") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_dialog_email_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Target Department (e.g. Engineering, Product)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailOrName.isNotBlank()) {
                        onSendInvite(emailOrName, department, jobTitle)
                    }
                },
                enabled = emailOrName.isNotBlank(),
                modifier = Modifier.testTag("submit_invite_button")
            ) {
                Text("Send Invite Code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
