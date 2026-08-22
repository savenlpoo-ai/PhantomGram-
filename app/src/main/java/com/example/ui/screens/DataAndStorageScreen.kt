package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataAndStorageScreen(
    onBack: () -> Unit,
    onNavigateToStorageUsage: () -> Unit
) {
    val chatCaches by AppSettings.chatCaches.collectAsState()
    val totalBytes = chatCaches.sumOf { it.totalBytes }

    var autoDownloadMobile by remember { mutableStateOf(true) }
    var autoDownloadWifi by remember { mutableStateOf(true) }
    var autoDownloadRoaming by remember { mutableStateOf(false) }

    var streamVideo by remember { mutableStateOf(true) }
    var streamAudio by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Данные и память") },
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
                Text(
                    "ИСПОЛЬЗОВАНИЕ ДИСКА И СЕТИ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Использование памяти") },
                    supportingContent = { Text("Очистка кэша, лимиты памяти и хранение медиа") },
                    leadingContent = { Icon(Icons.Default.PieChart, contentDescription = null, tint = Color(0xFF8C52FF)) },
                    trailingContent = { Text(formatBytes(totalBytes), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.clickable { onNavigateToStorageUsage() }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Использование трафика") },
                    supportingContent = { Text("Статистика мобильной сети, Wi-Fi и роуминга") },
                    leadingContent = { Icon(Icons.Default.DataUsage, contentDescription = null) },
                    trailingContent = { Text("64.2 MB") }
                )
                HorizontalDivider()

                Spacer(Modifier.height(16.dp))
                Text(
                    "АВТОЗАГРУЗКА МЕДИА",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Через мобильную сеть") },
                    supportingContent = { Text(if (autoDownloadMobile) "Фото и видео до 10 МБ" else "Отключено") },
                    trailingContent = { Switch(checked = autoDownloadMobile, onCheckedChange = { autoDownloadMobile = it }) }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Через Wi-Fi") },
                    supportingContent = { Text(if (autoDownloadWifi) "Все медиафайлы" else "Отключено") },
                    trailingContent = { Switch(checked = autoDownloadWifi, onCheckedChange = { autoDownloadWifi = it }) }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("В роуминге") },
                    supportingContent = { Text(if (autoDownloadRoaming) "Включено" else "Отключено") },
                    trailingContent = { Switch(checked = autoDownloadRoaming, onCheckedChange = { autoDownloadRoaming = it }) }
                )
                HorizontalDivider()

                Spacer(Modifier.height(16.dp))
                Text(
                    "СТРИМИНГ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Стриминг видео и аудио") },
                    trailingContent = { Switch(checked = streamVideo, onCheckedChange = { streamVideo = it }) }
                )
            }
        }
    }
}
