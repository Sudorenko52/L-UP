package com.example.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.UserStats
import com.example.CustomAvatar

@Composable
fun ProfileScreenContent(userStats: UserStats, onResetAll: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Settings",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFF2C1E5C), CircleShape)
                .border(2.dp, Color(0xFF9F75FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = userStats.name,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Level ${userStats.level} Game Master",
            color = Color(0xFF8D6EFD),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Coins Accumulated", color = Color(0xFF9CA3AF))
                    Text("${userStats.coins} Coins", color = Color.White, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = Color(0xFF1D1F30))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active Streak", color = Color(0xFF9CA3AF))
                    Text("${userStats.streak} Days", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onResetAll,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Game & Stats", color = Color.White)
        }
    }
}

@Composable
fun AvatarView(
    avatarId: Int,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 60.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp,
    borderColor: Color = Color(0xFF8D6EFD)
) {
    val avatars = listOf(
        CustomAvatar(0, Icons.Default.Person, "Gamer", Color(0xFF6366F1), Color(0xFF3B82F6)),
        CustomAvatar(1, Icons.Default.Code, "Coder", Color(0xFF06B6D4), Color(0xFF0891B2)),
        CustomAvatar(2, Icons.Default.Whatshot, "Warrior", Color(0xFFF97316), Color(0xFFEA580C)),
        CustomAvatar(3, Icons.Default.FitnessCenter, "Athlete", Color(0xFF10B981), Color(0xFF059669)),
        CustomAvatar(4, Icons.Default.School, "Scholar", Color(0xFFEC4899), Color(0xFFD946EF)),
        CustomAvatar(5, Icons.Default.Star, "Champion", Color(0xFFF59E0B), Color(0xFFD97706)),
        CustomAvatar(6, Icons.Default.Favorite, "Guardian", Color(0xFFEF4444), Color(0xFFDC2626)),
        CustomAvatar(7, Icons.Default.Lightbulb, "Inventor", Color(0xFF14B8A6), Color(0xFF0D9488))
    )
    val avatar = avatars.getOrElse(avatarId) { avatars[0] }

    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(colors = listOf(avatar.startColor, avatar.endColor)),
                shape = CircleShape
            )
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = avatar.icon,
            contentDescription = avatar.name,
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    label: String,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (textColor == Color.White) Color(0xFF8D6EFD) else textColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
