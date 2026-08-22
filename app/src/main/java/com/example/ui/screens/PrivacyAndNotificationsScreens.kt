package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy") },
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
                Text("Security", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                ListItem(headlineContent = { Text("Passcode Lock") })
                ListItem(headlineContent = { Text("Cloud Password") })
                ListItem(headlineContent = { Text("Auto-Delete Messages") })
                ListItem(headlineContent = { Text("Blocklist") })

                HorizontalDivider()

                Text("Privacy", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                ListItem(headlineContent = { Text("Phone Number") }, trailingContent = { Text("Nobody") })
                ListItem(headlineContent = { Text("Last Seen") }, trailingContent = { Text("Everybody") })
                ListItem(headlineContent = { Text("Profile Photos") }, trailingContent = { Text("Everybody") })
                ListItem(headlineContent = { Text("Forwarded Messages") }, trailingContent = { Text("Everybody") })
                ListItem(headlineContent = { Text("Calls") }, trailingContent = { Text("Everybody") })
                ListItem(headlineContent = { Text("Groups & Channels") }, trailingContent = { Text("Everybody") })

                HorizontalDivider()
                
                Text("Delete my account", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                ListItem(headlineContent = { Text("If away for") }, trailingContent = { Text("6 months") })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
                Text("Message Notifications", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                ListItem(
                    headlineContent = { Text("Private Chats") },
                    trailingContent = { Switch(checked = true, onCheckedChange = {}) }
                )
                ListItem(
                    headlineContent = { Text("Groups") },
                    trailingContent = { Switch(checked = true, onCheckedChange = {}) }
                )
                ListItem(
                    headlineContent = { Text("Channels") },
                    trailingContent = { Switch(checked = true, onCheckedChange = {}) }
                )

                HorizontalDivider()

                Text("Calls", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                ListItem(
                    headlineContent = { Text("Vibrate") },
                    trailingContent = { Text("Default") }
                )
            }
        }
    }
}
