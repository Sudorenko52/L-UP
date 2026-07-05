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

@Composable
fun AboutSubDialog(onDismiss: () -> Unit) {
    var selectedChapterId by remember { mutableStateOf<Int?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedChapterId == null) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Brush.linearGradient(colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "LevelUp Bible",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Версія 1.0 (MVP)",
                                    color = Color(0xFF8D6EFD),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "Game Your Tasks, Rule Your Life!\nLevelUp Bible turns your daily routines and critical goals into real RPG-style quests. Earn experience, gain levels, collect gold, unlock epic chests, equip custom titles, and unleash your ultimate developer power.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = Color(0xFF1D1F30))

                    Text(
                        text = "ЗМІСТ КНИГИ",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    // Scrollable List of Chapters
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Chapter 1
                        ChapterItem(
                            number = 1,
                            title = "Основи LevelUp",
                            subtitle = "RPG концепція у реальному житті",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 1 }
                        )

                        // Chapter 2
                        ChapterItem(
                            number = 2,
                            title = "Звички та Серії",
                            subtitle = "Формування сили волі та серій",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 2 }
                        )

                        // Chapter 3
                        ChapterItem(
                            number = 3,
                            title = "Квести та Нагороди",
                            subtitle = "Як правильно балансувати XP та золото",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 3 }
                        )

                        // Chapter 10 (Featured Flutter Architecture!)
                        ChapterItem(
                            number = 10,
                            title = "Flutter Architecture",
                            subtitle = "Clean Architecture, структури та правила",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 10 }
                        )

                        // Chapter 11 (Featured Design System!)
                        ChapterItem(
                            number = 11,
                            title = "Design System",
                            subtitle = "Стиль, кольори, типографіка та компоненти",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 11 }
                        )

                        // Chapter 12 (Featured Release Plan!)
                        ChapterItem(
                            number = 12,
                            title = "Release Plan",
                            subtitle = "План розробки, тестування та релізу MVP",
                            isUnlocked = true,
                            isFeatured = true,
                            onClick = { selectedChapterId = 12 }
                        )

                        // Lock chapters in between
                        LockedChapterItem(number = 4, title = "Гейм-баланс екранів")
                        LockedChapterItem(number = 5, title = "Економіка та Скрині")
                        LockedChapterItem(number = 6, title = "Магазин та Титули")
                        LockedChapterItem(number = 7, title = "Звуковий супровід")
                        LockedChapterItem(number = 8, title = "Локальне збереження Room")
                        LockedChapterItem(number = 9, title = "Керування станом StateFlow")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Закрити", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // CHAPTER READING VIEW
                    val chapterId = selectedChapterId!!
                    
                    // Header with back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { selectedChapterId = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад до змісту",
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "LevelUp Bible",
                                color = Color(0xFF8D6EFD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (chapterId) {
                                    1 -> "Розділ 1 — Основи LevelUp"
                                    2 -> "Розділ 2 — Звички та Серії"
                                    3 -> "Розділ 3 — Квести та Нагороди"
                                    10 -> "Розділ 10 — Flutter Architecture"
                                    11 -> "Розділ 11 — Design System"
                                    12 -> "Розділ 12 — Release Plan"
                                    else -> "Розділ $chapterId"
                                },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1D1F30))

                    // Scrollable Chapter Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (chapterId) {
                            1 -> ChapterOneContent()
                            2 -> ChapterTwoContent()
                            3 -> ChapterThreeContent()
                            10 -> ChapterTenContent()
                            11 -> ChapterElevenContent()
                            12 -> ChapterTwelveContent()
                        }
                    }

                    Button(
                        onClick = { selectedChapterId = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D1F30)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("До змісту", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun ChapterItem(
    number: Int,
    title: String,
    subtitle: String,
    isUnlocked: Boolean,
    isFeatured: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) Color(0xFF1A153B) else Color(0xFF1A1B2E)
        ),
        border = BorderStroke(
            1.5.dp,
            if (isFeatured) Color(0xFF8D6EFD) else Color(0xFF1D1F30)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isFeatured) Color(0xFF8D6EFD) else Color(0xFF2E3147),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isFeatured) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00F0FF).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "НОВИНКА",
                                color = Color(0xFF00F0FF),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isFeatured) Color(0xFF8D6EFD) else Color(0xFF4B5563),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
fun LockedChapterItem(number: Int, title: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161724).copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, Color(0xFF1D1F30).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1F202E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Розділ $number: $title",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Заблоковано — у розробці",
                    color = Color(0xFF9CA3AF).copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


@Composable
fun ChapterOneContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BibleSectionCard(title = "Призначення гри") {
            Text(
                text = "Перетвори свої щоденні справи у захоплюючу пригоду! Наша місія — допомогти розробникам та ентузіастам підтримувати дисципліну через ігрові механіки RPG.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        BibleSectionCard(title = "Головні показники") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "XP (Досвід) — відображає твій загальний прогрес. Отримуй XP за кожну виконану задачу.")
                BulletPoint(text = "Coins (Золото) — валюта для покупки нагород та відкриття легендарних скринь.")
                BulletPoint(text = "Streak (Серія) — показник регулярності. Чим більше днів поспіль ти виконуєш звички, тим вищий множник XP!")
            }
        }
    }
}


@Composable
fun ChapterTwoContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BibleSectionCard(title = "Сила Звичок") {
            Text(
                text = "Звички — це твої постійні вміння. Регулярне їх виконання гартує волю твого героя.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        BibleSectionCard(title = "Правила серій (Streaks)") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Кожне послідовне виконання звички збільшує лічильник серії на +1.")
                BulletPoint(text = "Якщо пропустити день, серія згасає, а разом з нею і додаткові бонуси.")
                BulletPoint(text = "Утримання серій понад 7 днів відкриває унікальні досягнення та особливі титули!")
            }
        }
    }
}


@Composable
fun ChapterThreeContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BibleSectionCard(title = "Епічні Квести") {
            Text(
                text = "Квести — це твої головні життєві цілі. Вони поділяються на щоденні місії та масштабні епічні квести.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        BibleSectionCard(title = "Баланс нагород") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Легкі квести приносять 10 XP та 5 монет.")
                BulletPoint(text = "Середні квести приносять 25 XP та 12 монет.")
                BulletPoint(text = "Складні / Епічні квести дарують понад 100 XP, 50 монет та шанс знайти ключ від рідкісної скрині!")
            }
        }
    }
}


@Composable
fun ChapterTenContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
            border = BorderStroke(1.dp, Color(0xFF3B2F5C)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Розділ 10 — Flutter Architecture",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Версія 1.0",
                            color = Color(0xFF8D6EFD),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Статус: MVP",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        BibleSectionCard(title = "10.1 Призначення") {
            Text(
                text = "Опис архітектури Flutter-проєкту, структури папок та взаємодії між шарами.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        BibleSectionCard(title = "10.2 Архітектурний підхід") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Clean Architecture (спрощена)")
                BulletPoint(text = "Feature-first")
                BulletPoint(text = "Repository Pattern")
                BulletPoint(text = "Service Layer")
                BulletPoint(text = "Dependency Injection")
            }
        }

        BibleSectionCard(title = "10.3 Структура") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CodePathItem(path = "lib/core", desc = "спільне ядро, константи, базові класи")
                CodePathItem(path = "lib/features", desc = "фічі (функціональні модулі)")
                CodePathItem(path = "lib/shared", desc = "перевикористовувані компоненти та віджети")
                CodePathItem(path = "lib/services", desc = "глобальні сервіси та бізнес-логіка")
                CodePathItem(path = "lib/database", desc = "налаштування локальної БД")
                CodePathItem(path = "lib/models", desc = "глобальні моделі даних")
                CodePathItem(path = "lib/theme", desc = "стилі та теми оформлення")
                CodePathItem(path = "lib/utils", desc = "допоміжні утиліти")
                CodePathItem(path = "lib/widgets", desc = "глобальні UI компоненти")
            }
        }

        BibleSectionCard(title = "10.4 Feature") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CodePathItem(path = "presentation/", desc = "екрани та віджети відображення")
                CodePathItem(path = "domain/", desc = "сутності та бізнес-правила фічі")
                CodePathItem(path = "data/", desc = "джерела даних та репозиторії")
                CodePathItem(path = "widgets/", desc = "локальні віджети фічі")
                CodePathItem(path = "controllers/", desc = "управління станом фічі")
            }
        }

        BibleSectionCard(title = "10.5 State Management") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Riverpod — основне управління станом")
                BulletPoint(text = "Notifier/StateNotifier — реактивна логіка")
                BulletPoint(text = "Один Provider = одна відповідальність")
            }
        }

        BibleSectionCard(title = "10.6 Навігація") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "GoRouter")
                BulletPoint(text = "4 Bottom Tabs")
                BulletPoint(text = "Bottom Sheet для Create і Mini Profile")
            }
        }

        BibleSectionCard(title = "10.7 Repository") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CodePathItem(path = "TaskRepository", desc = "управління задачами")
                CodePathItem(path = "HabitRepository", desc = "управління звичками")
                CodePathItem(path = "QuestRepository", desc = "управління квестами")
                CodePathItem(path = "UserRepository", desc = "профіль користувача")
                CodePathItem(path = "StatisticsRepository", desc = "статистика гравця")
            }
        }

        BibleSectionCard(title = "10.8 Services") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CodePathItem(path = "XPService", desc = "нарахування досвіду та рівні")
                CodePathItem(path = "CoinService", desc = "ігрова валюта та монети")
                CodePathItem(path = "QuestEngine", desc = "обробка та запуск квестів")
                CodePathItem(path = "LevelService", desc = "управління рівнями")
                CodePathItem(path = "AchievementService", desc = "досягнення та нагороди")
                CodePathItem(path = "StreakService", desc = "підрахунок серій та стріків")
            }
        }

        BibleSectionCard(title = "10.9 Бізнес-правила") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedPoint(number = 1, text = "UI не працює напряму з БД.")
                NumberedPoint(number = 2, text = "Логіка знаходиться у Service Layer.")
                NumberedPoint(number = 3, text = "Repository відповідає лише за дані.")
                NumberedPoint(number = 4, text = "Компоненти перевикористовуються.")
            }
        }

        BibleSectionCard(title = "10.10 Definition of Done") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Структура створена.")
                BulletPoint(text = "Riverpod налаштований.")
                BulletPoint(text = "GoRouter налаштований.")
                BulletPoint(text = "Repository і Services створені.")
                BulletPoint(text = "Архітектура готова до MVP.")
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B161A)),
            border = BorderStroke(1.dp, Color(0xFF5C2F34)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Developer Notes:\nНе розміщувати бізнес-логіку у віджетах.",
                    color = Color(0xFFFCA5A5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                )
            }
        }
    }
}


@Composable
fun ChapterElevenContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
            border = BorderStroke(1.dp, Color(0xFF3B2F5C)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Розділ 11 — Design System",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Версія 1.0",
                            color = Color(0xFF8D6EFD),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Статус: MVP",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        BibleSectionCard(title = "11.1 Призначення") {
            Text(
                text = "Design System визначає єдиний стиль застосунку та правила використання всіх UI-компонентів.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        BibleSectionCard(title = "11.2 Кольори") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorPreviewItem("Primary", Color(0xFF8D6EFD))
                ColorPreviewItem("Secondary", Color(0xFF00F0FF))
                ColorPreviewItem("Success", Color(0xFF10B981))
                ColorPreviewItem("Warning", Color(0xFFF59E0B))
                ColorPreviewItem("Danger", Color(0xFFEF4444))
                ColorPreviewItem("Background", Color(0xFF0D0E15))
                ColorPreviewItem("Surface", Color(0xFF1E2030))
                ColorPreviewItem("Card", Color(0xFF1A1B2E))
                ColorPreviewItem("Divider", Color(0xFF1D1F30))
                ColorPreviewItem("Text Primary", Color(0xFFFFFFFF))
                ColorPreviewItem("Text Secondary", Color(0xFF9CA3AF))
            }
        }

        BibleSectionCard(title = "11.3 Типографіка") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TypographyPreviewItem("Display", "Epic Title", 26.sp, FontWeight.ExtraBold)
                TypographyPreviewItem("H1", "Header Level 1", 20.sp, FontWeight.Bold)
                TypographyPreviewItem("H2", "Header Level 2", 17.sp, FontWeight.SemiBold)
                TypographyPreviewItem("H3", "Header Level 3", 14.sp, FontWeight.Medium)
                TypographyPreviewItem("Body", "Standard readable text content.", 13.sp, FontWeight.Normal)
                TypographyPreviewItem("Caption", "Additional small meta information.", 11.sp, FontWeight.Light)
                TypographyPreviewItem("Button", "CLICK ME", 12.sp, FontWeight.Bold)
            }
        }

        BibleSectionCard(title = "11.4 Відступи") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpacingPreviewItem("4 px", 4.dp)
                SpacingPreviewItem("8 px", 8.dp)
                SpacingPreviewItem("12 px", 12.dp)
                SpacingPreviewItem("16 px", 16.dp)
                SpacingPreviewItem("24 px", 24.dp)
                SpacingPreviewItem("32 px", 32.dp)
            }
        }

        BibleSectionCard(title = "11.5 Радіуси") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RadiusPreviewItem("Small — 8 px", 8.dp)
                RadiusPreviewItem("Medium — 12 px", 12.dp)
                RadiusPreviewItem("Large — 16 px", 16.dp)
                RadiusPreviewItem("Extra Large — 24 px", 24.dp)
            }
        }

        BibleSectionCard(title = "11.6 Компоненти") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint("Primary Button — основні CTA кнопки")
                BulletPoint("Secondary Button — додаткові кнопки")
                BulletPoint("Task Card — картка задачі")
                BulletPoint("Habit Card — картка звички")
                BulletPoint("Quest Card — картка квесту")
                BulletPoint("Statistic Card — картка статистики")
                BulletPoint("Progress Bar — смужка прогресу")
                BulletPoint("XP Badge — бейдж досвіду")
                BulletPoint("Coin Badge — бейдж монет")
                BulletPoint("Priority Chip — чіп пріоритету")
                BulletPoint("Bottom Navigation — нижня навігація")
                BulletPoint("Bottom Sheet — нижня шторка")
                BulletPoint("Dialog — діалогове вікно")
                BulletPoint("Snackbar — спливаюче повідомлення")
            }
        }

        BibleSectionCard(title = "11.7 Іконки") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint("Material Symbols — єдине джерело іконок")
                BulletPoint("Єдиний стиль іконок у всьому застосунку")
            }
        }

        BibleSectionCard(title = "11.8 Анімації") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint("Тривалість: 200–300 ms")
                BulletPoint("Крива: Ease In Out (плавні переходи)")
                BulletPoint("Без надлишкових ефектів")
            }
        }

        BibleSectionCard(title = "11.9 Бізнес-правила") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedPoint(number = 1, text = "Усі екрани використовують спільні компоненти.")
                NumberedPoint(number = 2, text = "Не створювати дублікати компонентів.")
                NumberedPoint(number = 3, text = "Нові компоненти додаються лише після затвердження.")
                NumberedPoint(number = 4, text = "Усі кольори беруться лише з Theme.")
            }
        }

        BibleSectionCard(title = "11.10 Definition of Done") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint("Створено Theme.")
                BulletPoint("Створено базові компоненти.")
                BulletPoint("Усі екрани використовують Design System.")
                BulletPoint("Відсутні локальні стилі.")
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B161A)),
            border = BorderStroke(1.dp, Color(0xFF5C2F34)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Developer Notes:\nБудь-які зміни дизайну виконуються через Design System, а не окремо в кожному екрані.",
                    color = Color(0xFFFCA5A5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                )
            }
        }
    }
}


@Composable
fun ColorPreviewItem(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        )
        Text(text = name, color = Color(0xFFD1D5DB), fontSize = 12.sp)
    }
}


@Composable
fun TypographyPreviewItem(name: String, sample: String, size: TextUnit, weight: FontWeight) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$name ($size):",
            color = Color(0xFF00F0FF),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = sample,
            color = Color.White,
            fontSize = size,
            fontWeight = weight,
            maxLines = 1
        )
    }
}


@Composable
fun SpacingPreviewItem(name: String, widthDp: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name,
            color = Color(0xFF8D6EFD),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )
        Box(
            modifier = Modifier
                .height(8.dp)
                .width(widthDp)
                .background(Color(0xFF8D6EFD).copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        )
        Text(
            text = "${widthDp.value.toInt()} dp",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp
        )
    }
}


@Composable
fun RadiusPreviewItem(name: String, radiusDp: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name,
            color = Color(0xFF8D6EFD),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF1E2030), RoundedCornerShape(radiusDp))
                .border(1.5.dp, Color(0xFF00F0FF), RoundedCornerShape(radiusDp))
        )
        Text(
            text = "${radiusDp.value.toInt()} dp",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp
        )
    }
}


@Composable
fun ChapterTwelveContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
            border = BorderStroke(1.dp, Color(0xFF3B2F5C)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Розділ 12 — Release Plan",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Версія 1.0",
                            color = Color(0xFF8D6EFD),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Статус: MVP",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        BibleSectionCard(title = "12.1 Мета") {
            Text(
                text = "План релізу визначає порядок розробки, тестування та публікації MVP у Google Play.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        BibleSectionCard(title = "12.2 Етапи") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedPoint(number = 1, text = "Налаштування проєкту")
                NumberedPoint(number = 2, text = "Реалізація бази даних")
                NumberedPoint(number = 3, text = "Реалізація сервісів")
                NumberedPoint(number = 4, text = "Реалізація UI-компонентів")
                NumberedPoint(number = 5, text = "Реалізація екранів")
                NumberedPoint(number = 6, text = "Тестування")
                NumberedPoint(number = 7, text = "Публікація MVP")
            }
        }

        BibleSectionCard(title = "12.3 Пріоритет реалізації") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "1. Home (Головна панель)")
                BulletPoint(text = "2. Tasks (Задачі та квести)")
                BulletPoint(text = "3. Habits (Корисні звички)")
                BulletPoint(text = "4. Quests (Масштабні пригоди)")
                BulletPoint(text = "5. Progress (Рівень, досвід та золото)")
                BulletPoint(text = "6. Mini Profile (Картка профілю та титули)")
            }
        }

        BibleSectionCard(title = "12.4 MVP Checklist") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CheckedBulletPoint(text = "Усі екрани реалізовані")
                CheckedBulletPoint(text = "База даних працює")
                CheckedBulletPoint(text = "Quest Engine працює")
                CheckedBulletPoint(text = "XP та Coins працюють")
                CheckedBulletPoint(text = "Навігація працює")
                CheckedBulletPoint(text = "Відсутні критичні помилки")
            }
        }

        BibleSectionCard(title = "12.5 Тестування") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Перевірка бізнес-логіки")
                BulletPoint(text = "Перевірка UI")
                BulletPoint(text = "Перевірка продуктивності")
                BulletPoint(text = "Перевірка збереження даних")
                BulletPoint(text = "Регресійне тестування")
            }
        }

        BibleSectionCard(title = "12.6 Критерії готовності") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Стабільна робота без падінь")
                BulletPoint(text = "Усі функції MVP реалізовані")
                BulletPoint(text = "Інтерфейс відповідає Design System")
                BulletPoint(text = "Документація актуальна")
            }
        }
    }
}


@Composable
fun CheckedBulletPoint(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Checked",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
        )
        Text(text = text, color = Color(0xFFD1D5DB), fontSize = 12.sp, lineHeight = 16.sp)
    }
}


@Composable
fun BibleSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2030)),
        border = BorderStroke(1.dp, Color(0xFF2E3147)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = Color(0xFF00F0FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            content()
        }
    }
}


@Composable
fun BulletPoint(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "•", color = Color(0xFF8D6EFD), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = text, color = Color(0xFFD1D5DB), fontSize = 12.sp, lineHeight = 16.sp)
    }
}


@Composable
fun NumberedPoint(number: Int, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "$number.", color = Color(0xFF8D6EFD), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = text, color = Color(0xFFD1D5DB), fontSize = 12.sp, lineHeight = 16.sp)
    }
}


@Composable
fun CodePathItem(path: String, desc: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF0D0E15), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = path,
                color = Color(0xFF00F0FF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Text(text = "— $desc", color = Color(0xFF9CA3AF), fontSize = 11.sp, lineHeight = 14.sp)
    }
}


