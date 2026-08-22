package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Chat
import com.example.data.LocalizationManager
import com.example.data.PhantomRepository
import com.example.data.User
import com.example.ui.theme.FabColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentLang by LocalizationManager.currentLanguage.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var accountsExpanded by remember { mutableStateOf(true) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    var newAccountEmail by remember { mutableStateOf("") }
    var newAccountName by remember { mutableStateOf("") }
    var isAddingAccount by remember { mutableStateOf(false) }

    val repository = remember { PhantomRepository() }
    val currentUserId by repository.getCurrentUserIdFlow().collectAsState(initial = PhantomRepository.currentUserId)
    val savedAccounts by repository.getSavedAccounts().collectAsState(initial = emptyList())
    val chats by repository.getChatsForUser(currentUserId ?: "").collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val currentUser = remember(currentUserId, allUsers) {
        allUsers.find { it.uid == currentUserId }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Header with current account & expandable switcher
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val initial = currentUser?.name?.take(1)?.uppercase() ?: "P"
                                    Text(
                                        text = initial,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentUser?.name ?: "PhantomGram",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (currentUser?.isDeveloper == true || currentUser?.isOfficial == true) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Official",
                                            tint = Color(0xFF5A9BEC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = currentUser?.email ?: (currentUser?.username?.let { "@$it" } ?: ""),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { accountsExpanded = !accountsExpanded },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (accountsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Accounts",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (currentUser?.isDeveloper == true) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF5A9BEC).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Developer & Creator",
                                    color = Color(0xFF5A9BEC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Accounts list accordion
                AnimatedVisibility(visible = accountsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .padding(vertical = 6.dp)
                    ) {
                        val displayAccounts = if (savedAccounts.isNotEmpty()) savedAccounts else (currentUser?.let { listOf(it) } ?: emptyList())
                        displayAccounts.forEach { acc ->
                            val isSelected = acc.uid == currentUserId
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        repository.switchAccount(acc.uid)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = acc.name.take(1).uppercase(),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = acc.email,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Add second / another account button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAddAccountDialog = true
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Добавить аккаунт",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    label = { Text(LocalizationManager.tr("new_group", "New Group")) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCreateGroup()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text(LocalizationManager.tr("search", "Search")) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSearch()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(LocalizationManager.tr("settings", "Settings")) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PhantomGram") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToCreateGroup,
                    containerColor = FabColor,
                    shape = CircleShape,
                    modifier = Modifier.testTag("create_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Create Group or Channel",
                        tint = Color.White
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Restriction Banner if current user has penalties
                if (currentUser != null && (currentUser.isFrozen || currentUser.bannedUntil > System.currentTimeMillis() || currentUser.spamblockUntil != 0L)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { onNavigateToChat("phantom_group_official") }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val penaltyTitle = when {
                                    currentUser.isFrozen -> "Аккаунт заморожен (❄frozen account)"
                                    currentUser.bannedUntil > System.currentTimeMillis() -> "Аккаунт забанен"
                                    else -> "Наложен спамблок"
                                }
                                Text(
                                    text = penaltyTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Нажмите сюда, чтобы написать в официальную группу PhantomGroup для апелляции.",
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                if (chats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You don't have any chats yet.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(chats) { chat ->
                            // Check if other participant is online in private chat
                            val otherUser = if (chat.type == "PRIVATE") {
                                val otherUid = chat.participantIds.find { it != currentUserId }
                                allUsers.find { it.uid == otherUid }
                            } else null

                            val isUserOnline = otherUser?.isOnline == true

                            ListItem(
                                headlineContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (chat.type == "PRIVATE" && otherUser != null) otherUser.name else chat.name,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (chat.isOfficial || otherUser?.isOfficial == true || otherUser?.isDeveloper == true) {
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
                                supportingContent = {
                                    val desc = if (chat.lastMessageText.isNotEmpty()) chat.lastMessageText else chat.description
                                    Text(desc, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                leadingContent = {
                                    // Avatar highlighted with white border / glow if user is online in chat menu
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        val avatarModifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (isUserOnline) Modifier.border(2.5.dp, Color.White, CircleShape)
                                                else Modifier
                                            )

                                        if (chat.photoUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = chat.photoUrl,
                                                contentDescription = null,
                                                modifier = avatarModifier,
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (chat.isOfficial) Color(0xFF1E88E5) else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = avatarModifier
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    val letter = (if (chat.type == "PRIVATE" && otherUser != null) otherUser.name else chat.name).take(1).uppercase()
                                                    Text(
                                                        text = letter.ifBlank { "P" },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp,
                                                        color = if (chat.isOfficial) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        // Online indicator dot
                                        if (isUserOnline) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(Color.White, CircleShape)
                                                    .padding(2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(0xFF4CAF50), CircleShape)
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.clickable { onNavigateToChat(chat.id) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                        }
                    }
                }
            }
        }
    }

    // Add Account Dialog (Google Account)
    if (showAddAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isAddingAccount) showAddAccountDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4285F4),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Добавить Google аккаунт", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Введите данные второго Google аккаунта (@gmail.com) для добавления в приложение. Вы сможете переключаться между ними в одно нажатие.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newAccountEmail,
                        onValueChange = { newAccountEmail = it },
                        label = { Text("Google Email") },
                        placeholder = { Text("user@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAccountName,
                        onValueChange = { newAccountName = it },
                        label = { Text("Имя пользователя") },
                        placeholder = { Text("Иван Иванов") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isAddingAccount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Добавление аккаунта...", fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAccountEmail.contains("@") && newAccountEmail.contains(".")) {
                            scope.launch {
                                isAddingAccount = true
                                try {
                                    val nameToUse = newAccountName.trim()
                                    repository.registerOrLoginGoogleUser(newAccountEmail.trim(), nameToUse)
                                    Toast.makeText(context, "Второй аккаунт ($newAccountEmail) успешно подключен!", Toast.LENGTH_SHORT).show()
                                    showAddAccountDialog = false
                                    newAccountEmail = ""
                                    newAccountName = ""
                                    drawerState.close()
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.localizedMessage ?: "Ошибка добавления аккаунта", Toast.LENGTH_LONG).show()
                                } finally {
                                    isAddingAccount = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Пожалуйста, введите корректный адрес Google email", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isAddingAccount
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddAccountDialog = false },
                    enabled = !isAddingAccount
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}
