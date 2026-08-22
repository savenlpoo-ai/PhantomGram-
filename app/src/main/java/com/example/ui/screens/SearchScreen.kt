package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Chat
import com.example.data.PhantomRepository
import com.example.data.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    val repository = remember { PhantomRepository() }
    val scope = rememberCoroutineScope()
    val currentUserId by repository.getCurrentUserIdFlow().collectAsState(initial = PhantomRepository.currentUserId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { 
                    query = it 
                    scope.launch {
                        if (query.isNotBlank()) {
                            users = repository.searchUsers(query).filter { it.uid != currentUserId }
                            chats = repository.searchChats(query)
                        } else {
                            users = emptyList()
                            chats = emptyList()
                        }
                    }
                },
                placeholder = { Text("Search users (@username) or groups...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Distinct Users Section
                if (users.isNotEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "PEOPLE / USERS",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    items(users) { user ->
                        ListItem(
                            headlineContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.name, fontWeight = FontWeight.SemiBold)
                                    if (user.isDeveloper || user.isOfficial) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Official",
                                            tint = Color(0xFF5A9BEC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            supportingContent = { Text("@${user.username} • ${user.bio.ifBlank { "User" }}", maxLines = 1) },
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            },
                            modifier = Modifier.clickable {
                                scope.launch {
                                    val chatId = repository.getOrCreatePrivateChat(user.uid)
                                    onNavigateToChat(chatId)
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                    }
                }

                // Distinct Groups & Channels Section
                if (chats.isNotEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "GROUPS & CHANNELS",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    items(chats) { chat ->
                        ListItem(
                            headlineContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(chat.name, fontWeight = FontWeight.SemiBold)
                                    if (chat.isOfficial) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Official",
                                            tint = Color(0xFF5A9BEC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            supportingContent = { Text("${chat.type} • ${chat.description}", maxLines = 1) },
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = if (chat.isOfficial) Color(0xFF1E88E5) else MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = if (chat.isOfficial) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.clickable { onNavigateToChat(chat.id) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                    }
                }

                if (query.isNotEmpty() && users.isEmpty() && chats.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No users or groups found for \"$query\"",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
