package com.example.shared.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.CustomAvatar
import com.example.UserStats
import com.example.features.profile.AvatarView
import com.example.features.profile.ProfileOptionItem
import com.example.HexagonXpBadge
import com.example.AboutSubDialog

@Composable
fun MiniProfileBottomSheet(
    userStats: UserStats,
    onDismiss: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateAvatar: (Int) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onResetStats: () -> Unit
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangeAvatarDialog by remember { mutableStateOf(false) }
    var showTitlesDialog by remember { mutableStateOf(false) }
    var showBadgesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    var soundEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks */ }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        if (dragAmount.y > 15f) {
                            onDismiss()
                        }
                    }
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0D14)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, Color(0xFF1D1F30))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(Color(0xFF374151), CircleShape)
                        .clickable { onDismiss() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Avatar and Profile Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarView(
                        avatarId = userStats.avatarId,
                        size = 64.dp,
                        borderColor = Color(0xFF8D6EFD),
                        borderWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = userStats.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showEditNameDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = Color(0xFF8D6EFD),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF8D6EFD),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = userStats.selectedTitle.ifBlank { "No titles yet" },
                                color = Color(0xFF8D6EFD),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Level indicator: Level 12, progress bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level ${userStats.level}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${userStats.xp} / ${userStats.maxXp} XP",
                            color = Color(0xFF8D6EFD),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }

                    // Progress bar
                    val progressRatio = (userStats.xp.toFloat() / userStats.maxXp.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFF161726), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio)
                                .fillMaxHeight()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                                    ),
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats row: Day Streak, Coins, Total XP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Day Streak
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Day Streak",
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${userStats.streak}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Day Streak",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Coins
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${userStats.coins}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Coins",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Total XP
                    Card(
                        modifier = Modifier.weight(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HexagonXpBadge(modifier = Modifier.size(24.dp))
                            Text(
                                text = String.format("%,d", userStats.totalXp),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total XP",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // List of options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileOptionItem(
                        icon = Icons.Default.Person,
                        label = "Edit Profile",
                        onClick = { showEditNameDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Photo,
                        label = "Change Avatar",
                        onClick = { showChangeAvatarDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.EmojiEvents,
                        label = "Titles",
                        onClick = { showTitlesDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.MilitaryTech,
                        label = "Badges",
                        onClick = { showBadgesDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = { showSettingsDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Info,
                        label = "About",
                        onClick = { showAboutDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.ExitToApp,
                        label = "Log Out",
                        textColor = Color(0xFFEF4444),
                        onClick = { showLogoutConfirm = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Sub-dialogs
    if (showEditNameDialog) {
        EditProfileNameDialog(
            currentName = userStats.name,
            onDismiss = { showEditNameDialog = false },
            onConfirm = {
                onUpdateName(it)
                showEditNameDialog = false
            }
        )
    }

    if (showChangeAvatarDialog) {
        ChangeAvatarDialog(
            selectedAvatarId = userStats.avatarId,
            onDismiss = { showChangeAvatarDialog = false },
            onSelect = {
                onUpdateAvatar(it)
                showChangeAvatarDialog = false
            }
        )
    }

    if (showTitlesDialog) {
        TitlesDialog(
            selectedTitle = userStats.selectedTitle,
            userLevel = userStats.level,
            onDismiss = { showTitlesDialog = false },
            onEquip = {
                onUpdateTitle(it)
                showTitlesDialog = false
            }
        )
    }

    if (showBadgesDialog) {
        BadgesDialog(
            unlockedBadges = userStats.unlockedBadges,
            onDismiss = { showBadgesDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsSubDialog(
            soundEnabled = soundEnabled,
            onSoundToggle = { soundEnabled = it },
            notificationsEnabled = notificationsEnabled,
            onNotificationsToggle = { notificationsEnabled = it },
            hapticFeedback = hapticFeedback,
            onHapticToggle = { hapticFeedback = it },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutSubDialog(onDismiss = { showAboutDialog = false })
    }

    if (showLogoutConfirm) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutConfirm = false },
            onConfirm = {
                onResetStats()
                showLogoutConfirm = false
            }
        )
    }
}

@Composable
fun EditProfileNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Edit Profile Name",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF1D1F30)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onConfirm(nameInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChangeAvatarDialog(
    selectedAvatarId: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose Your Avatar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val rows = listOf(0..3, 4..7)
                    rows.forEach { range ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            range.forEach { id ->
                                val isSelected = id == selectedAvatarId
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            onSelect(id)
                                        }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AvatarView(
                                        avatarId = id,
                                        size = 56.dp,
                                        borderColor = if (isSelected) Color(0xFF00F0FF) else Color.Transparent,
                                        borderWidth = if (isSelected) 3.dp else 0.dp
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .background(Color(0xFF00F0FF), CircleShape)
                                                .align(Alignment.BottomEnd),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TitlesDialog(
    selectedTitle: String,
    userLevel: Int,
    onDismiss: () -> Unit,
    onEquip: (String) -> Unit
) {
    val titlesList = listOf(
        Pair("Shadow Runner", 1),
        Pair("Novice Tasker", 1),
        Pair("Bug Hunter", 1),
        Pair("Code Ninja", 5),
        Pair("Quest Master", 8),
        Pair("Legendary Warrior", 12),
        Pair("Godlike Slayer", 15),
        Pair("Zen Master", 20)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select Title",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    items(titlesList) { (titleName, requiredLevel) ->
                        val isLocked = userLevel < requiredLevel
                        val isEquipped = titleName == selectedTitle

                        Surface(
                            onClick = {
                                if (!isLocked && !isEquipped) {
                                    onEquip(titleName)
                                }
                            },
                            enabled = !isLocked,
                            color = if (isEquipped) Color(0xFF8D6EFD).copy(alpha = 0.15f) else Color(0xFF161726).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isEquipped) Color(0xFF8D6EFD) else Color(0xFF1D1F30)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = titleName,
                                        color = if (isLocked) Color.White.copy(alpha = 0.4f) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (isLocked) {
                                        Text(
                                            text = "Requires Level $requiredLevel",
                                            color = Color(0xFFEF4444),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Equipped", color = Color(0xFF8D6EFD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (isLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Equip",
                                        color = Color(0xFF00F0FF),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgesDialog(
    unlockedBadges: List<String>,
    onDismiss: () -> Unit
) {
    val badgesList = listOf(
        Triple("First Step", "Completed your first task", Icons.Default.CheckCircle),
        Triple("7-Day Warrior", "Maintained a 7-day streak", Icons.Default.Whatshot),
        Triple("Coin Collector", "Earned 100+ coins", Icons.Default.MonetizationOn),
        Triple("Main Quest Hero", "Complete a Main Quest", Icons.Default.EmojiEvents),
        Triple("Task Slayer", "Complete 10+ tasks", Icons.Default.FitnessCenter),
        Triple("Elite Explorer", "Complete all category quests", Icons.Default.Explore)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Your Badges",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    items(badgesList) { (badgeName, badgeDesc, icon) ->
                        val isUnlocked = unlockedBadges.contains(badgeName)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF161726).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isUnlocked) Color(0xFF8D6EFD).copy(alpha = 0.2f) else Color(0xFF374151).copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = badgeName,
                                    tint = if (isUnlocked) Color(0xFF8D6EFD) else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badgeName,
                                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = badgeDesc,
                                    color = if (isUnlocked) Color(0xFF9CA3AF) else Color.White.copy(alpha = 0.25f),
                                    fontSize = 11.sp
                                )
                            }

                            if (isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Unlocked", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSubDialog(
    soundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    hapticFeedback: Boolean,
    onHapticToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "App Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF8D6EFD))
                        Text("Sound Effects", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8D6EFD)
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFF1D1F30))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF8D6EFD))
                        Text("Reminders & Notifications", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8D6EFD)
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFF1D1F30))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFF8D6EFD))
                        Text("Haptic Feedback", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = onHapticToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8D6EFD)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Are you sure you want to log out?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Logging out will reset all your temporary local progress and game achievements.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Yes, Reset & Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
