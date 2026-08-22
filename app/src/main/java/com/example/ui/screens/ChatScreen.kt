package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppSettings
import com.example.data.Chat
import com.example.data.Message
import com.example.data.PhantomRepository
import com.example.data.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {}
) {
    val repository = remember { PhantomRepository() }
    val currentUserId = PhantomRepository.currentUserId ?: ""
    val messages by repository.getMessagesForChat(chatId).collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    var chat by remember { mutableStateOf<Chat?>(null) }
    var text by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var showInfo by remember { mutableStateOf(false) }

    val otherUser = remember(chat, allUsers, currentUserId) {
        if (chat?.type == "PRIVATE") {
            val otherUid = chat?.participantIds?.find { it != currentUserId }
            allUsers.find { it.uid == otherUid }
        } else null
    }

    val chatDisplayName = remember(chat, otherUser) {
        if (chat?.type == "PRIVATE" && otherUser != null) {
            otherUser.name
        } else {
            chat?.name ?: "Chat"
        }
    }

    LaunchedEffect(chatId) {
        chat = repository.getChat(chatId)
        repository.markMessagesAsRead(chatId, currentUserId)
    }

    // Auto mark as read when new messages arrive
    LaunchedEffect(messages.size) {
        repository.markMessagesAsRead(chatId, currentUserId)
    }

    if (showInfo && chat != null) {
        ChatInfoScreen(
            chat = chat!!,
            onBack = { showInfo = false },
            onNavigateToUserProfile = onNavigateToUserProfile
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    chatDisplayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                if (chat?.isOfficial == true || otherUser?.isOfficial == true || otherUser?.isDeveloper == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Official",
                                        tint = Color(0xFF5A9BEC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (chat?.type == "PRIVATE") {
                                if (otherUser?.uid == "bot_phantom") {
                                    Text(
                                        text = "bot",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (otherUser?.isOnline == true) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF00E676),
                                            modifier = Modifier.size(7.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "online",
                                            fontSize = 12.sp,
                                            color = Color(0xFF00E676),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "last seen recently",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "${chat?.participantIds?.size ?: 0} members",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showInfo = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Info")
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    if (errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { errorMessage = null }) {
                                    Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Message...") },
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (text.isNotBlank()) {
                                    scope.launch {
                                        try {
                                            errorMessage = null
                                            repository.sendMessage(chatId, text.trim())
                                            text = ""
                                        } catch (e: Exception) {
                                            errorMessage = e.localizedMessage ?: "Failed to send message"
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 10.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    MessageBubble(
                        message = message,
                        isOwnMessage = message.senderId == currentUserId
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorSubtext() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "typing",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "•••",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alpha(dotAlpha)
        )
    }
}

@Composable
fun MessageBubble(message: Message, isOwnMessage: Boolean) {
    val bubbleAccent by AppSettings.customBubbleAccentColor.collectAsState()
    val fontSize by AppSettings.messageFontSize.collectAsState()
    val cornerRadius by AppSettings.messageCornerRadius.collectAsState()

    val backgroundColor = if (isOwnMessage) bubbleAccent else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start

    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = cornerRadius.dp,
                topEnd = cornerRadius.dp,
                bottomStart = if (isOwnMessage) cornerRadius.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else cornerRadius.dp
            ),
            color = backgroundColor,
            modifier = Modifier.widthIn(min = 100.dp, max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 6.dp)) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = fontSize.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time in lower left corner of the message bubble
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.65f)
                    )

                    // Read status in lower right corner of own message
                    if (isOwnMessage) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            if (message.isRead) {
                                // Double checkmarks for read
                                Text(
                                    text = "✓✓",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81D4FA)
                                )
                            } else {
                                // Single gray checkmark for sent / unread
                                Text(
                                    text = "✓",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInfoScreen(
    chat: Chat,
    onBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {}
) {
    val repository = remember { PhantomRepository() }
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val participants = remember(chat.participantIds, allUsers) {
        allUsers.filter { chat.participantIds.contains(it.uid) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (chat.type == "PRIVATE") "Profile Info" else "Group Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Centered Group / Chat Header with Avatar in Middle
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Center Avatar
                    Surface(
                        shape = CircleShape,
                        color = when {
                            chat.isOfficial -> Color(0xFF1E88E5)
                            chat.type == "GROUP" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier.size(96.dp),
                        shadowElevation = 4.dp
                    ) {
                        if (chat.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = chat.photoUrl,
                                contentDescription = chat.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = chat.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Center Group Name with Verification Checkmark
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = chat.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (chat.isOfficial) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Official Group",
                                tint = Color(0xFF5A9BEC),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (chat.type == "GROUP" || chat.type == "CHANNEL") {
                        Text(
                            text = "${participants.size} participants",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (chat.description.isNotEmpty() || chat.id == "phantom_group_official") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (chat.description.isNotEmpty()) {
                                    Text(
                                        text = "About",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = chat.description, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (chat.id == "phantom_group_official") {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text(
                                        text = "Link: p.ha/PhantomGroupOfficial",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Participants Section
            if (chat.type == "GROUP" || chat.type == "CHANNEL") {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Participants (${participants.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Нажмите на любого участника, чтобы открыть профиль, начать общение или заглушить его",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }

                items(participants) { user ->
                    ListItem(
                        headlineContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Medium)
                                if (user.isDeveloper || user.isOfficial) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Official",
                                        tint = Color(0xFF5A9BEC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (user.uid == chat.ownerId) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            "Owner",
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        },
                        supportingContent = { Text("@${user.username}") },
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    user.isDeveloper -> Color(0xFF1E88E5)
                                    user.isOfficial -> Color(0xFF5A9BEC)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = if (user.isDeveloper || user.isOfficial) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "View Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.clickable {
                            onNavigateToUserProfile(user.uid)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                }
            } else {
                // For Private Chat: option to view the other participant's profile
                val otherUser = participants.find { it.uid != PhantomRepository.currentUserId }
                if (otherUser != null) {
                    item {
                        Button(
                            onClick = { onNavigateToUserProfile(otherUser.uid) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Посмотреть профиль @${otherUser.username}")
                        }
                    }
                }
            }
        }
    }
}
