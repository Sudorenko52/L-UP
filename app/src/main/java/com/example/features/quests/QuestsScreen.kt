package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

import com.example.QuestViewModel

@Composable
fun QuestsScreenContent(
    viewModel: QuestViewModel,
    currentSubScreen: String?,
    onSubScreenChanged: (String?) -> Unit,
    selectedQuestId: String?,
    onSelectedQuestIdChanged: (String?) -> Unit,
    onEditMainQuestClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val quests = state.quests
    val userStats = state.userStats

    when (currentSubScreen) {
        "weekly_list" -> {
            QuestListScreen(
                title = "Weekly Quests",
                type = QuestType.WEEKLY,
                quests = quests.filter { it.type == QuestType.WEEKLY },
                onBack = { onSubScreenChanged(null) },
                onAddQuest = { onSubScreenChanged("create_quest") },
                onQuestClick = { id ->
                    onSelectedQuestIdChanged(id)
                    onSubScreenChanged("quest_details")
                }
            )
        }
        "monthly_list" -> {
            QuestListScreen(
                title = "Monthly Quests",
                type = QuestType.MONTHLY,
                quests = quests.filter { it.type == QuestType.MONTHLY },
                onBack = { onSubScreenChanged(null) },
                onAddQuest = { onSubScreenChanged("create_quest") },
                onQuestClick = { id ->
                    onSelectedQuestIdChanged(id)
                    onSubScreenChanged("quest_details")
                }
            )
        }
        "special_list" -> {
            QuestListScreen(
                title = "Special Quests",
                type = QuestType.SPECIAL,
                quests = quests.filter { it.type == QuestType.SPECIAL },
                onBack = { onSubScreenChanged(null) },
                onAddQuest = { onSubScreenChanged("create_quest") },
                onQuestClick = { id ->
                    onSelectedQuestIdChanged(id)
                    onSubScreenChanged("quest_details")
                }
            )
        }
        "quest_details" -> {
            val quest = quests.find { it.id == selectedQuestId }
            if (quest != null) {
                QuestDetailsScreen(
                    quest = quest,
                    onBack = { onSubScreenChanged(quest.type.name.lowercase() + "_list") },
                    onEdit = { onSubScreenChanged("edit_quest") },
                    onDelete = {
                        viewModel.deleteQuest(quest.id)
                        onSubScreenChanged(quest.type.name.lowercase() + "_list")
                    },
                    onComplete = {
                        viewModel.completeManualQuest(quest.id)
                    }
                )
            } else {
                onSubScreenChanged(null)
            }
        }
        "create_quest" -> {
            QuestCreatorScreen(
                quest = null,
                onBack = { onBackTarget ->
                    onSubScreenChanged(null)
                },
                onConfirm = { title, type, desc, target, xp, coins, chest, targetType, tags, duration ->
                    viewModel.addQuest(title, type, desc, target, xp, coins, chest, targetType, tags, duration)
                    if (type == QuestType.MAIN) {
                        onSubScreenChanged(null)
                    } else {
                        onSubScreenChanged(type.name.lowercase() + "_list")
                    }
                }
            )
        }
        "edit_quest" -> {
            val quest = quests.find { it.id == selectedQuestId }
            if (quest != null) {
                QuestCreatorScreen(
                    quest = quest,
                    onBack = { onBackTarget ->
                        onSubScreenChanged("quest_details")
                    },
                    onConfirm = { title, type, desc, target, xp, coins, chest, targetType, tags, duration ->
                        viewModel.updateQuest(quest.id, title, type, desc, target, quest.currentValue, xp, coins, chest, quest.status, targetType, tags, duration)
                        onSubScreenChanged("quest_details")
                    }
                )
            } else {
                onSubScreenChanged(null)
            }
        }
        else -> {
            QuestsOverviewScreen(
                quests = quests,
                streak = userStats.streak,
                onClaimMainQuest = { viewModel.claimMainQuest() },
                onCategoryClick = { category ->
                    onSubScreenChanged(category + "_list")
                },
                onAddQuest = { onSubScreenChanged("create_quest") },
                onEditMainQuestClick = onEditMainQuestClick
            )
        }
    }
}


@Composable
fun QuestsOverviewScreen(
    quests: List<Quest>,
    streak: Int,
    onClaimMainQuest: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onAddQuest: () -> Unit,
    onEditMainQuestClick: () -> Unit
) {
    var selectedTabState by remember { mutableStateOf("Main") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quests",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("quests_screen_title")
            )

            Box(
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Text(
                        text = "$streak Streak",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("Main", "Weekly", "Monthly", "Special")
            tabs.forEach { tab ->
                val isSelected = selectedTabState == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) Color(0xFF8D6EFD) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTabState = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        when (selectedTabState) {
            "Main" -> {
                val mainQuest = quests.find { it.type == QuestType.MAIN }
                if (mainQuest != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditMainQuestClick() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F5E)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Main Quest Icon",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "MAIN QUEST",
                                        color = Color(0xFF00F0FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = onEditMainQuestClick,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Main Quest",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    if (mainQuest.status == QuestStatus.COMPLETED) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Completed",
                                                color = Color(0xFF10B981),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = mainQuest.title,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (mainQuest.description.isNotEmpty()) {
                                Text(
                                    text = mainQuest.description,
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 13.sp
                                )
                            }

                            val progressPercent = (mainQuest.currentValue / mainQuest.targetValue).coerceIn(0f, 1f)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${mainQuest.currentValue.toInt()} / ${mainQuest.targetValue.toInt()} XP",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(progressPercent * 100).toInt()}%",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = Color(0xFF00F0FF),
                                trackColor = Color(0x22FFFFFF)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Reward:",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 11.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "+${mainQuest.xpReward} XP",
                                            color = Color(0xFF8D6EFD),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(Color(0xFFFFD700), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "$", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = "+${mainQuest.coinReward}",
                                                color = Color(0xFFFFB300),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (mainQuest.chest != RewardChest.NONE) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Redeem,
                                                    contentDescription = "Chest",
                                                    tint = when (mainQuest.chest) {
                                                        RewardChest.SILVER -> Color(0xFFC0C0C0)
                                                        RewardChest.GOLD -> Color(0xFFFFD700)
                                                        RewardChest.EPIC -> Color(0xFF9333EA)
                                                        else -> Color.White
                                                    },
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "${mainQuest.chest.name} Chest",
                                                    color = when (mainQuest.chest) {
                                                        RewardChest.SILVER -> Color(0xFFC0C0C0)
                                                        RewardChest.GOLD -> Color(0xFFFFD700)
                                                        RewardChest.EPIC -> Color(0xFFA855F7)
                                                        else -> Color.White
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                if (mainQuest.status == QuestStatus.ACTIVE) {
                                    Button(
                                        onClick = onClaimMainQuest,
                                        enabled = progressPercent >= 1.0f,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8D6EFD),
                                            disabledContainerColor = Color(0xFF2C1E5C)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (progressPercent >= 1.0f) "Завершити квест" else "В процесі",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                            disabledContentColor = Color(0xFF10B981)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Завершено",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Create your Main Quest",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Quests are long-term goals. Set a primary focus to track your growth.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onAddQuest,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                                    Text(text = "New Main Quest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ACTIVE QUESTS",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val weeklyCount = quests.filter { it.type == QuestType.WEEKLY }
                val weeklyCompleted = weeklyCount.count { it.status == QuestStatus.COMPLETED }
                CategoryCard(
                    title = "Weekly Quests",
                    subtitle = "$weeklyCompleted / ${weeklyCount.size} completed",
                    icon = Icons.Default.CalendarToday,
                    iconColor = Color(0xFF00F0FF),
                    onClick = { onCategoryClick("weekly") }
                )

                val monthlyCount = quests.filter { it.type == QuestType.MONTHLY }
                val monthlyCompleted = monthlyCount.count { it.status == QuestStatus.COMPLETED }
                CategoryCard(
                    title = "Monthly Quests",
                    subtitle = "$monthlyCompleted / ${monthlyCount.size} completed",
                    icon = Icons.Default.DateRange,
                    iconColor = Color(0xFFC084FC),
                    onClick = { onCategoryClick("monthly") }
                )

                val specialCount = quests.filter { it.type == QuestType.SPECIAL }
                val specialCompleted = specialCount.count { it.status == QuestStatus.COMPLETED }
                CategoryCard(
                    title = "Special Quests",
                    subtitle = "$specialCompleted / ${specialCount.size} completed",
                    icon = Icons.Default.Diamond,
                    iconColor = Color(0xFFFBBF24),
                    onClick = { onCategoryClick("special") }
                )
            }
            "Weekly" -> {
                QuestListTabContent(
                    title = "Weekly Quests",
                    type = QuestType.WEEKLY,
                    quests = quests.filter { it.type == QuestType.WEEKLY },
                    onCategoryClick = onCategoryClick,
                    onAddQuest = onAddQuest
                )
            }
            "Monthly" -> {
                QuestListTabContent(
                    title = "Monthly Quests",
                    type = QuestType.MONTHLY,
                    quests = quests.filter { it.type == QuestType.MONTHLY },
                    onCategoryClick = onCategoryClick,
                    onAddQuest = onAddQuest
                )
            }
            "Special" -> {
                QuestListTabContent(
                    title = "Special Quests",
                    type = QuestType.SPECIAL,
                    quests = quests.filter { it.type == QuestType.SPECIAL },
                    onCategoryClick = onCategoryClick,
                    onAddQuest = onAddQuest
                )
            }
        }
        
        Spacer(modifier = Modifier.height(76.dp))
    }
}


@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = Color(0xFF9CA3AF)
            )
        }
    }
}


@Composable
fun QuestListTabContent(
    title: String,
    type: QuestType,
    quests: List<Quest>,
    onCategoryClick: (String) -> Unit,
    onAddQuest: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Quests",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddQuest,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD).copy(alpha = 0.2f), contentColor = Color(0xFF8D6EFD)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                    Text(text = "Add Quest", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCategoryClick(type.name.lowercase()) },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF8D6EFD).copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Open $title List",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage quests, view detail trackers, and claim milestones.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = Color(0xFF8D6EFD)
                )
            }
        }

        if (quests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No quests added yet.", color = Color(0xFF9CA3AF), fontSize = 13.sp)
            }
        } else {
            quests.forEach { quest ->
                QuestItemRow(
                    quest = quest,
                    onClick = { onCategoryClick(type.name.lowercase()) }
                )
            }
        }
    }
}


@Composable
fun QuestItemRow(
    quest: Quest,
    onClick: () -> Unit
) {
    val progressPercent = (quest.currentValue / quest.targetValue).coerceIn(0f, 1f)
    val isCompleted = quest.status == QuestStatus.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.05f) else Color(0xFF1E293B).copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF334155).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when (quest.targetType.lowercase()) {
                        "water" -> Icons.Default.Opacity
                        "sleep" -> Icons.Default.Bedtime
                        "reading" -> Icons.Default.Book
                        "habits" -> Icons.Default.FitnessCenter
                        "xp gained" -> Icons.Default.OfflineBolt
                        "level" -> Icons.Default.WorkspacePremium
                        "streak" -> Icons.Default.LocalFireDepartment
                        else -> Icons.Default.Assignment
                    }
                    val iconColor = when (quest.targetType.lowercase()) {
                        "water" -> Color(0xFF0288D1)
                        "sleep" -> Color(0xFF818CF8)
                        "reading" -> Color(0xFFFBBF24)
                        "habits" -> Color(0xFF34D399)
                        "xp gained" -> Color(0xFFA78BFA)
                        "level" -> Color(0xFFFBBF24)
                        "streak" -> Color(0xFFF87171)
                        else -> Color(0xFF38BDF8)
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = quest.targetType,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = quest.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (quest.description.isNotEmpty()) {
                            Text(
                                text = quest.description,
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "+${quest.xpReward} XP",
                        color = Color(0xFF8D6EFD),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "+${quest.coinReward}",
                            color = Color(0xFFFFB300),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .background(Color(0xFF0F172A), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercent)
                            .fillMaxHeight()
                            .background(
                                if (isCompleted) Color(0xFF10B981) else Color(0xFF8D6EFD),
                                CircleShape
                            )
                    )
                }

                Text(
                    text = if (isCompleted) "Completed" else "${quest.currentValue.toInt()} / ${quest.targetValue.toInt()}",
                    color = if (isCompleted) Color(0xFF10B981) else Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun QuestListScreen(
    title: String,
    type: QuestType,
    quests: List<Quest>,
    onBack: () -> Unit,
    onAddQuest: () -> Unit,
    onQuestClick: (String) -> Unit
) {
    val totalCount = quests.size
    val completedCount = quests.count { it.status == QuestStatus.COMPLETED }
    val progressPercent = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onAddQuest) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Quest", tint = Color(0xFF00F0FF))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (type == QuestType.WEEKLY || type == QuestType.MONTHLY) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F5E)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF8D6EFD).copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (type == QuestType.WEEKLY) "WEEKLY REWARD" else "MONTHLY REWARD",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Complete all active quests of this category to claim a Silver Chest of bonus XP & Coins!",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$completedCount / $totalCount completed",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${(progressPercent * 100).toInt()}%",
                                        color = Color(0xFF00F0FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { progressPercent },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFF8D6EFD),
                                    trackColor = Color(0x22FFFFFF)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF8D6EFD).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Redeem,
                                        contentDescription = "Chest",
                                        tint = if (progressPercent >= 1.0f) Color(0xFFFFD700) else Color(0xFFC0C0C0),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = if (progressPercent >= 1.0f) "CLAIMABLE" else "LOCKED",
                                    color = if (progressPercent >= 1.0f) Color(0xFFFFD700) else Color(0xFF9CA3AF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (quests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No quests in this category. Click '+' to add one!", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                    }
                }
            } else {
                items(quests) { quest ->
                    QuestItemRow(
                        quest = quest,
                        onClick = { onQuestClick(quest.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }
    }
}


@Composable
fun QuestDetailsScreen(
    quest: Quest,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    val progressPercent = (quest.currentValue / quest.targetValue).coerceIn(0f, 1f)
    val isCompleted = quest.status == QuestStatus.COMPLETED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Quest Details",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Quest", tint = Color(0xFF00F0FF))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = quest.type.displayName,
                            color = Color(0xFFC084FC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val statusColor = when (quest.status) {
                        QuestStatus.ACTIVE -> Color(0xFF38BDF8)
                        QuestStatus.COMPLETED -> Color(0xFF34D399)
                        QuestStatus.EXPIRED -> Color(0xFFF87171)
                    }
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = quest.status.name,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = quest.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                if (quest.description.isNotEmpty()) {
                    Text(
                        text = quest.description,
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROGRESS",
                            color = Color(0xFF00F0FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${quest.currentValue.toInt()} / ${quest.targetValue.toInt()}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = if (isCompleted) Color(0xFF10B981) else Color(0xFF8D6EFD),
                        trackColor = Color(0xFF0F172A)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "PROGRESS RULE",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quest Engine tracks completed actions of type '${quest.targetType}'. Progress updates automatically without manual check-offs.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TAGS COUNTED",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (quest.tags.isEmpty()) {
                    Text(
                        text = "Matches any ${quest.targetType} completion.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quest.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF8D6EFD).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = Color(0xFFA78BFA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "REWARD",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OfflineBolt, contentDescription = "XP", tint = Color(0xFF8D6EFD), modifier = Modifier.size(16.dp))
                        Text(text = "+${quest.xpReward} XP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "+${quest.coinReward} Coins", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (quest.chest != RewardChest.NONE) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Redeem,
                                contentDescription = "Chest",
                                tint = when (quest.chest) {
                                    RewardChest.SILVER -> Color(0xFFC0C0C0)
                                    RewardChest.GOLD -> Color(0xFFFFD700)
                                    RewardChest.EPIC -> Color(0xFF9333EA)
                                    else -> Color.White
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${quest.chest.name} Chest",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DURATION LIMIT",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = quest.durationText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (quest.type != QuestType.SPECIAL && quest.status == QuestStatus.ACTIVE && quest.currentValue >= quest.targetValue) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("complete_quest_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Завершити квест", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Delete Quest", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}


@Composable
fun QuestCreatorScreen(
    quest: Quest?,
    onBack: (String) -> Unit,
    onConfirm: (
        title: String,
        type: QuestType,
        description: String,
        targetValue: Float,
        xpReward: Int,
        coinReward: Int,
        chest: RewardChest,
        targetType: String,
        tags: List<String>,
        durationText: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(quest?.title ?: "") }
    var type by remember { mutableStateOf(quest?.type ?: QuestType.WEEKLY) }
    var description by remember { mutableStateOf(quest?.description ?: "") }
    var targetValueString by remember { mutableStateOf(quest?.targetValue?.toInt()?.toString() ?: "10") }
    var xpRewardString by remember { mutableStateOf(quest?.xpReward?.toString() ?: "50") }
    var coinRewardString by remember { mutableStateOf(quest?.coinReward?.toString() ?: "15") }
    var chest by remember { mutableStateOf(quest?.chest ?: RewardChest.NONE) }
    var targetType by remember { mutableStateOf(quest?.targetType ?: "Tasks") }
    var durationText by remember { mutableStateOf(quest?.durationText ?: "This week") }

    var currentTagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(quest?.tags ?: emptyList()) }

    val targetTypes = listOf("Tasks", "Habits", "Water", "Sleep", "Reading", "XP Gained", "Level", "Streak")
    var showTargetDropdown by remember { mutableStateOf(false) }

    val chests = listOf(RewardChest.NONE, RewardChest.SILVER, RewardChest.GOLD, RewardChest.EPIC)
    var showChestDropdown by remember { mutableStateOf(false) }

    val durations = listOf("This week", "This month", "No limit")
    var showDurationDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack(if (quest == null) "overview" else "details") }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }

            Text(
                text = if (quest == null) "Create Quest" else "Edit Quest",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    val finalTarget = targetValueString.toFloatOrNull() ?: 10f
                    val finalXp = xpRewardString.toIntOrNull() ?: 50
                    val finalCoins = coinRewardString.toIntOrNull() ?: 15
                    if (title.isNotEmpty()) {
                        onConfirm(title, type, description, finalTarget, finalXp, finalCoins, chest, targetType, tags, durationText)
                    }
                },
                enabled = title.isNotEmpty()
            ) {
                Text(
                    text = "Save",
                    color = if (title.isNotEmpty()) Color(0xFF00F0FF) else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Quest Name", color = Color(0xFF9CA3AF)) },
            placeholder = { Text("e.g. Read 1000 Pages") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF8D6EFD),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "TYPE", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val types = QuestType.values()
                types.forEach { t ->
                    val isSelected = type == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) Color(0xFF8D6EFD) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                type = t
                                durationText = when (t) {
                                    QuestType.MAIN -> "No limit"
                                    QuestType.WEEKLY -> "This week"
                                    QuestType.MONTHLY -> "This month"
                                    QuestType.SPECIAL -> "No limit"
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = t.name,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)", color = Color(0xFF9CA3AF)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF8D6EFD),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "TARGET CONDITIONS", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = targetValueString,
                    onValueChange = { targetValueString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Target Value", color = Color(0xFF9CA3AF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                        .clickable { showTargetDropdown = !showTargetDropdown }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = targetType, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = showTargetDropdown,
                        onDismissRequest = { showTargetDropdown = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        targetTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(text = t, color = Color.White) },
                                onClick = {
                                    targetType = t
                                    showTargetDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "TAGS COUNTED (Match Tasks/Habits tags)", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF8D6EFD).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = tag, color = Color.White, fontSize = 12.sp)
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Tag",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { tags = tags.filter { it != tag } }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentTagInput,
                    onValueChange = { currentTagInput = it },
                    placeholder = { Text("e.g. Fitness") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        if (currentTagInput.trim().isNotEmpty() && !tags.contains(currentTagInput.trim())) {
                            tags = tags + currentTagInput.trim()
                            currentTagInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(text = "Add", fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "REWARD AMOUNTS", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = xpRewardString,
                    onValueChange = { xpRewardString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("XP Reward", color = Color(0xFF9CA3AF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = coinRewardString,
                    onValueChange = { coinRewardString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Coins Reward", color = Color(0xFF9CA3AF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "CHEST REWARD", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .clickable { showChestDropdown = !showChestDropdown }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (chest != RewardChest.NONE) {
                            Icon(
                                imageVector = Icons.Default.Redeem,
                                contentDescription = "Chest",
                                tint = when (chest) {
                                    RewardChest.SILVER -> Color(0xFFC0C0C0)
                                    RewardChest.GOLD -> Color(0xFFFFD700)
                                    RewardChest.EPIC -> Color(0xFF9333EA)
                                    else -> Color.White
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(text = "${chest.name} Chest", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                }

                DropdownMenu(
                    expanded = showChestDropdown,
                    onDismissRequest = { showChestDropdown = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    chests.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(text = "${c.name} Chest", color = Color.White) },
                            onClick = {
                                chest = c
                                showChestDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "DURATION", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .clickable { showDurationDropdown = !showDurationDropdown }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = durationText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                }

                DropdownMenu(
                    expanded = showDurationDropdown,
                    onDismissRequest = { showDurationDropdown = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    durations.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(text = d, color = Color.White) },
                            onClick = {
                                durationText = d
                                showDurationDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}


