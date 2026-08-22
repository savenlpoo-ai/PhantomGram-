package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppSettings
import com.example.data.LocalizationManager
import com.example.data.PhantomRepository
import com.example.data.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { PhantomRepository() }
    val scope = rememberCoroutineScope()

    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val targetUser = remember(userId, allUsers) {
        allUsers.find { it.uid == userId }
    }
    val currentUserId by repository.getCurrentUserIdFlow().collectAsState(initial = PhantomRepository.currentUserId)
    val currentUser = remember(currentUserId, allUsers) {
        allUsers.find { it.uid == currentUserId }
    }

    val mutedUserIds by AppSettings.mutedUserIds.collectAsState()
    val isMuted = mutedUserIds.contains(userId)
    var isCreatingChat by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(targetUser?.name ?: "Профиль пользователя") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (targetUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Пользователь не найден или был удален", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Centered Avatar
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(110.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            targetUser.isDeveloper -> Color(0xFF1E88E5)
                            targetUser.isOfficial -> Color(0xFF5A9BEC)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(110.dp)
                    ) {
                        if (targetUser.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = targetUser.photoUrl,
                                contentDescription = targetUser.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = targetUser.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Online indicator
                    if (targetUser.isOnline) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E676),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                            modifier = Modifier.size(22.dp)
                        ) {}
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Name & Verified Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = targetUser.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (targetUser.isDeveloper || targetUser.isOfficial) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Official",
                            tint = Color(0xFF5A9BEC),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "@${targetUser.username}",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                if (targetUser.isDeveloper) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E88E5).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Creator & Lead Developer",
                                color = Color(0xFF1E88E5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ACTION BUTTONS: CHAT and MUTE
                val isSelf = targetUser.uid == currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. CHAT BUTTON
                    Button(
                        onClick = {
                            if (isSelf) {
                                Toast.makeText(context, "Это ваш собственный профиль", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    isCreatingChat = true
                                    try {
                                        val chatId = repository.getOrCreatePrivateChat(targetUser.uid)
                                        onNavigateToChat(chatId)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.localizedMessage ?: "Ошибка открытия чата", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isCreatingChat = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !isCreatingChat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isCreatingChat) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(LocalizationManager.tr("chat", "Chat"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // 2. MUTE BUTTON
                    OutlinedButton(
                        onClick = {
                            val newMuted = AppSettings.toggleMuteUser(targetUser.uid)
                            if (newMuted) {
                                Toast.makeText(context, "🔕 @${targetUser.username} muted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "🔔 @${targetUser.username} unmuted", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = if (isMuted) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        }
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isMuted) LocalizationManager.tr("unmute", "Unmute") else LocalizationManager.tr("mute", "Mute"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Information Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (targetUser.bio.isNotEmpty()) {
                            Column {
                                Text("О себе (Bio)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Text(targetUser.bio, fontSize = 15.sp, style = MaterialTheme.typography.bodyMedium)
                            }
                            HorizontalDivider()
                        }

                        Column {
                            Text("Имя пользователя", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text("@${targetUser.username}", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Ссылка: p.ha/${targetUser.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Restriction indicators if applicable
                val now = System.currentTimeMillis()
                if (targetUser.isFrozen || targetUser.bannedUntil > now || targetUser.spamblockUntil != 0L) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text("Ограничения аккаунта", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            if (targetUser.isFrozen) {
                                Text("❄ Аккаунт заморожен. Будет удален через 5 часов после заморозки.", fontSize = 13.sp)
                            }
                            if (targetUser.bannedUntil > now) {
                                val remainingHours = ((targetUser.bannedUntil - now) / 3600000) + 1
                                Text("🚫 Заблокирован еще на $remainingHours ч.", fontSize = 13.sp)
                            }
                            if (targetUser.spamblockUntil == -1L) {
                                Text("🛑 Бессрочный спамблок.", fontSize = 13.sp)
                            } else if (targetUser.spamblockUntil > now) {
                                val remainingHours = ((targetUser.spamblockUntil - now) / 3600000) + 1
                                Text("⏳ Спамблок еще на $remainingHours ч.", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
