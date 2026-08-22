package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalizationManager
import com.example.data.PhantomRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToChatAppearance: () -> Unit,
    onNavigateToActiveSessions: () -> Unit,
    onNavigateToDataAndStorage: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToDeveloperPanel: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val repository = remember { PhantomRepository() }
    val currentUser = repository.getCurrentUser()
    val isDeveloper = currentUser?.isDeveloper == true
    val currentLang by LocalizationManager.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LocalizationManager.tr("settings", "Settings")) },
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
            item {
                ListItem(
                    headlineContent = { Text(LocalizationManager.tr("edit_profile", "Edit Profile")) },
                    supportingContent = { Text("@${currentUser?.username ?: ""}") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToEditProfile() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(LocalizationManager.tr("account", "Account")) },
                    supportingContent = { Text(currentUser?.email ?: "") },
                    leadingContent = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToAccount() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(LocalizationManager.tr("privacy_security", "Privacy and Security")) },
                    supportingContent = { Text(LocalizationManager.tr("privacy_subtitle", "Phone number, online status, 2FA passcode")) },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToPrivacy() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(LocalizationManager.tr("chat_appearance", "Chat Settings & Appearance")) },
                    supportingContent = { Text("Themes, accents, font size, wallpapers") },
                    leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color(0xFF8C52FF)) },
                    modifier = Modifier.clickable { onNavigateToChatAppearance() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(LocalizationManager.tr("devices", "Devices")) },
                    supportingContent = { Text("Active sessions, QR login") },
                    leadingContent = { Icon(Icons.Default.Devices, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToActiveSessions() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(LocalizationManager.tr("language", "Language")) },
                    supportingContent = { Text("${currentLang.flagEmoji} ${currentLang.nativeName} (${currentLang.englishName})") },
                    leadingContent = { Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF00B0FF)) },
                    modifier = Modifier.clickable { onNavigateToLanguage() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(LocalizationManager.tr("data_storage", "Data and Storage")) },
                    supportingContent = { Text("Storage usage, cache cleanup, media retention") },
                    leadingContent = { Icon(Icons.Default.PieChart, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToDataAndStorage() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(LocalizationManager.tr("notifications_sounds", "Notifications and Sounds")) },
                    supportingContent = { Text("Private chats, groups, channels") },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToNotifications() }
                )

                if (isDeveloper) {
                    HorizontalDivider()
                    Surface(color = Color(0xFF1E88E5).copy(alpha = 0.08f)) {
                        ListItem(
                            headlineContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Инструменты разработчика (Creator Panel)", fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
                                }
                            },
                            supportingContent = {
                                Text("Бан пользователей 1-24ч, снос профилей, заморозка, спамблок и восстановление аккаунтов")
                            },
                            leadingContent = {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF1E88E5))
                            },
                            modifier = Modifier.clickable { onNavigateToDeveloperPanel() }
                        )
                    }
                }

                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Выйти", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        repository.logout()
                        onLoggedOut()
                    }
                )
            }
        }
    }
}

