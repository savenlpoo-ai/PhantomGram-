package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeletedAccountRecord
import com.example.data.PhantomRepository
import com.example.data.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPanelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { PhantomRepository() }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val deletedAccounts by repository.getDeletedAccounts().collectAsState(initial = emptyList())

    var selectedUserForMod by remember { mutableStateOf<User?>(null) }
    var showModDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer & Creator Panel", fontWeight = FontWeight.Bold) },
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Users (${allUsers.size})") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Deleted Accounts (${deletedAccounts.size})") },
                    icon = { Icon(Icons.Default.DeleteForever, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                // USERS LIST WITH MODERATION
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(allUsers) { user ->
                        val now = System.currentTimeMillis()
                        val isBanned = user.bannedUntil > now
                        val isSpamblocked = user.spamblockUntil == -1L || user.spamblockUntil > now
                        val isFrozen = user.isFrozen

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
                            supportingContent = {
                                Column {
                                    Text("@${user.username} • ${user.email}")
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isFrozen) {
                                            val hoursLeft = ((user.frozenTimestamp + 5 * 3600 * 1000L - now) / 3600000).coerceAtLeast(0)
                                            StatusBadge("❄ Frozen (~${hoursLeft}h left)", Color(0xFF00BCD4))
                                        }
                                        if (isBanned) {
                                            val hoursLeft = ((user.bannedUntil - now) / 3600000) + 1
                                            StatusBadge("🚫 Ban ${hoursLeft}h", Color(0xFFE53935))
                                        }
                                        if (isSpamblocked) {
                                            val tag = if (user.spamblockUntil == -1L) "🔇 Spamblock: Forever" else "🔇 Spamblock"
                                            StatusBadge(tag, Color(0xFFFF9800))
                                        }
                                    }
                                }
                            },
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isFrozen) Color(0xFF80DEEA) else MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = user.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    selectedUserForMod = user
                                    showModDialog = true
                                }) {
                                    Icon(Icons.Default.Gavel, contentDescription = "Moderate", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier.clickable {
                                selectedUserForMod = user
                                showModDialog = true
                            }
                        )
                        HorizontalDivider()
                    }
                }
            } else {
                // DELETED ACCOUNTS (RESTORATION TAB)
                if (deletedAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No deleted accounts. All purged accounts will appear here and can be restored.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(deletedAccounts) { record ->
                            val dateStr = remember(record.deletedTimestamp) {
                                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(record.deletedTimestamp))
                            }
                            ListItem(
                                headlineContent = {
                                    Text(record.name, fontWeight = FontWeight.Bold)
                                },
                                supportingContent = {
                                    Column {
                                        Text("Email: ${record.email}")
                                        Text("Deleted on: $dateStr by ${record.deletedBy}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                leadingContent = {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                },
                                trailingContent = {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.restoreDeletedAccount(record.uid)
                                                Toast.makeText(context, "Account ${record.email} restored successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Text("Restore")
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // MODERATION ACTION MODAL SHEET / DIALOG
    if (showModDialog && selectedUserForMod != null) {
        val user = selectedUserForMod!!
        UserModerationDialog(
            user = user,
            onDismiss = { showModDialog = false },
            onBan = { hours ->
                scope.launch {
                    repository.banUser(user.uid, hours)
                    Toast.makeText(context, "${user.name} banned for $hours hours", Toast.LENGTH_SHORT).show()
                    showModDialog = false
                }
            },
            onUnban = {
                scope.launch {
                    repository.unbanUser(user.uid)
                    Toast.makeText(context, "Ban lifted for ${user.name}", Toast.LENGTH_SHORT).show()
                    showModDialog = false
                }
            },
            onFreeze = {
                scope.launch {
                    repository.freezeUser(user.uid)
                    Toast.makeText(context, "Account frozen! Auto-deletion in 5 hours", Toast.LENGTH_SHORT).show()
                    showModDialog = false
                }
            },
            onUnfreeze = {
                scope.launch {
                    repository.unfreezeUser(user.uid)
                    Toast.makeText(context, "Account unfrozen", Toast.LENGTH_SHORT).show()
                    showModDialog = false
                }
            },
            onSpamblock = { hours, days, isIndefinite ->
                scope.launch {
                    repository.applySpamblock(user.uid, hours, isIndefinite, days)
                    val msg = if (isIndefinite) "Indefinite spamblock applied" else if (days > 0) "Spamblock for $days days applied" else "Spamblock for $hours hours applied"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    showModDialog = false
                }
            },
            onRemoveSpamblock = {
                scope.launch {
                    repository.removeSpamblock(user.uid)
                    Toast.makeText(context, "Spamblock removed", Toast.LENGTH_SHORT).show()
                    showModDialog = false
                }
            },
            onPurgeAccount = {
                scope.launch {
                    repository.purgeUserByCreator(user.uid)
                    Toast.makeText(context, "Profile deleted from app and blocked for login!", Toast.LENGTH_LONG).show()
                    showModDialog = false
                }
            }
        )
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun UserModerationDialog(
    user: User,
    onDismiss: () -> Unit,
    onBan: (hours: Int) -> Unit,
    onUnban: () -> Unit,
    onFreeze: () -> Unit,
    onUnfreeze: () -> Unit,
    onSpamblock: (hours: Int, days: Int, isIndefinite: Boolean) -> Unit,
    onRemoveSpamblock: () -> Unit,
    onPurgeAccount: () -> Unit
) {
    var banHours by remember { mutableFloatStateOf(1f) }
    var spamblockMode by remember { mutableIntStateOf(0) } // 0: 1-24h, 1: 1-40 days, 2: Indefinite
    var spamblockHours by remember { mutableFloatStateOf(1f) }
    var spamblockDays by remember { mutableFloatStateOf(1f) }

    var showPurgeConfirmation by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val isBanned = user.bannedUntil > now
    val isSpamblocked = user.spamblockUntil == -1L || user.spamblockUntil > now
    val isFrozen = user.isFrozen

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Moderate: ${user.name}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("@${user.username} (${user.email})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. BAN SECTION (1 to 24 HOURS)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFE53935))
                            Spacer(Modifier.width(8.dp))
                            Text("Ban User (1h to 24h)", fontWeight = FontWeight.Bold)
                        }
                        if (isBanned) {
                            val hoursLeft = ((user.bannedUntil - now) / 3600000) + 1
                            Text("Status: Currently Banned (~${hoursLeft}h remaining)", color = Color(0xFFE53935), fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = onUnban,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Unban User")
                            }
                        } else {
                            Text("Select ban duration: ${banHours.roundToInt()} hours", fontSize = 13.sp)
                            Slider(
                                value = banHours,
                                onValueChange = { banHours = it },
                                valueRange = 1f..24f,
                                steps = 22
                            )
                            Button(
                                onClick = { onBan(banHours.roundToInt()) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apply Ban for ${banHours.roundToInt()}h")
                            }
                        }
                    }
                }

                // 2. FREEZE SECTION (❄ 5h AUTO-DELETE)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF00BCD4))
                            Spacer(Modifier.width(8.dp))
                            Text("Freeze Account (❄)", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Renames user to \"❄frozen account\", removes message permissions everywhere except PhantomGroup. Auto-deletes in 5 hours.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        if (isFrozen) {
                            Button(
                                onClick = onUnfreeze,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Unfreeze Account")
                            }
                        } else {
                            Button(
                                onClick = onFreeze,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Freeze Account Now")
                            }
                        }
                    }
                }

                // 3. SPAMBLOCK SECTION (1h-24h, 1-40 days, Indefinite)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeOff, contentDescription = null, tint = Color(0xFFFF9800))
                            Spacer(Modifier.width(8.dp))
                            Text("Spamblock (Спамблок)", fontWeight = FontWeight.Bold)
                        }
                        if (isSpamblocked) {
                            val desc = if (user.spamblockUntil == -1L) "Status: Indefinite (Forever)" else "Status: Active Spamblock"
                            Text(desc, color = Color(0xFFFF9800), fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = onRemoveSpamblock,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Remove Spamblock")
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                FilterChip(
                                    selected = spamblockMode == 0,
                                    onClick = { spamblockMode = 0 },
                                    label = { Text("1-24h", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = spamblockMode == 1,
                                    onClick = { spamblockMode = 1 },
                                    label = { Text("1-40 Days", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = spamblockMode == 2,
                                    onClick = { spamblockMode = 2 },
                                    label = { Text("Бессрочно", fontSize = 11.sp) }
                                )
                            }
                            when (spamblockMode) {
                                0 -> {
                                    Text("Duration: ${spamblockHours.roundToInt()} hours", fontSize = 12.sp)
                                    Slider(
                                        value = spamblockHours,
                                        onValueChange = { spamblockHours = it },
                                        valueRange = 1f..24f,
                                        steps = 22
                                    )
                                    Button(
                                        onClick = { onSpamblock(spamblockHours.roundToInt(), 0, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Apply Spamblock (${spamblockHours.roundToInt()}h)")
                                    }
                                }
                                1 -> {
                                    Text("Duration: ${spamblockDays.roundToInt()} days", fontSize = 12.sp)
                                    Slider(
                                        value = spamblockDays,
                                        onValueChange = { spamblockDays = it },
                                        valueRange = 1f..40f,
                                        steps = 38
                                    )
                                    Button(
                                        onClick = { onSpamblock(0, spamblockDays.roundToInt(), false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Apply Spamblock (${spamblockDays.roundToInt()} Days)")
                                    }
                                }
                                2 -> {
                                    Text("Indefinite spamblock (cannot write to anyone except PhantomGroup forever).", fontSize = 12.sp)
                                    Button(
                                        onClick = { onSpamblock(0, 0, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Apply Indefinite Spamblock (Бессрочно)")
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. PURGE ACCOUNT / СНЕСТИ ПРОФИЛЬ
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.15f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFD32F2F))
                            Spacer(Modifier.width(8.dp))
                            Text("Снести профиль (Delete Profile)", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        }
                        Text(
                            "Terminates user session and blacklists their Google account. User cannot sign in again until Creator restores it from \"Deleted Accounts\".",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { showPurgeConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Снести профиль пользователя")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (showPurgeConfirmation) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirmation = false },
            title = { Text("Confirm Profile Purge") },
            text = {
                Text("Are you sure you want to completely purge and blacklist ${user.name} (${user.email})? They will immediately lose access to the app.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPurgeConfirmation = false
                        onPurgeAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Yes, Delete Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
