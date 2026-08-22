package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.data.ChatCacheInfo
import com.example.data.KeepMediaDuration
import com.example.data.MaxCacheSize
import kotlin.math.roundToInt

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageUsageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val keepMedia by AppSettings.keepMediaDuration.collectAsState()
    val maxCache by AppSettings.maxCacheSize.collectAsState()
    val chatCaches by AppSettings.chatCaches.collectAsState()

    var showClearCacheDialog by remember { mutableStateOf(false) }
    var selectedChatForClear by remember { mutableStateOf<ChatCacheInfo?>(null) }

    val totalAppCacheBytes = chatCaches.sumOf { it.totalBytes }
    val otherAppsBytes = 18_400_000_000L // 18.4 GB simulated
    val freeSpaceBytes = 38_600_000_000L // 38.6 GB simulated
    val totalDeviceStorage = totalAppCacheBytes + otherAppsBytes + freeSpaceBytes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Использование памяти") },
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
            // 1. VISUAL DIAGRAM / BAR
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Память устройства",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        // Visual multi-color bar chart
                        val appFraction = (totalAppCacheBytes.toFloat() / totalDeviceStorage).coerceIn(0.02f, 1f)
                        val otherFraction = (otherAppsBytes.toFloat() / totalDeviceStorage).coerceIn(0.1f, 1f)
                        val freeFraction = (freeSpaceBytes.toFloat() / totalDeviceStorage).coerceIn(0.1f, 1f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(appFraction)
                                    .fillMaxHeight()
                                    .background(Color(0xFF8C52FF))
                            )
                            Spacer(Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .weight(otherFraction)
                                    .fillMaxHeight()
                                    .background(Color(0xFF546E7A))
                            )
                            Spacer(Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .weight(freeFraction)
                                    .fillMaxHeight()
                                    .background(Color(0xFF2E7D32))
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LegendItem(color = Color(0xFF8C52FF), title = "PhantomGram", value = formatBytes(totalAppCacheBytes))
                            LegendItem(color = Color(0xFF546E7A), title = "Другие", value = formatBytes(otherAppsBytes))
                            LegendItem(color = Color(0xFF2E7D32), title = "Свободно", value = formatBytes(freeSpaceBytes))
                        }
                    }
                }
            }

            // 2. CLEAR CACHE ACTION BUTTON
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Button(
                        onClick = {
                            selectedChatForClear = null
                            showClearCacheDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF)),
                        enabled = totalAppCacheBytes > 0
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Очистить кэш (${formatBytes(totalAppCacheBytes)})", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. MAX CACHE SIZE SLIDER
            item {
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Максимальный размер кэша",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Если кэш превысит лимит, старые файлы автоматически удалятся.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    val maxOptions = MaxCacheSize.values()
                    val currentIndex = maxOptions.indexOf(maxCache).coerceAtLeast(0)

                    Slider(
                        value = currentIndex.toFloat(),
                        onValueChange = {
                            val newIdx = it.roundToInt().coerceIn(0, maxOptions.size - 1)
                            AppSettings.maxCacheSize.value = maxOptions[newIdx]
                        },
                        valueRange = 0f..(maxOptions.size - 1).toFloat(),
                        steps = maxOptions.size - 2
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        maxOptions.forEach { opt ->
                            Text(
                                text = opt.label,
                                fontSize = 11.sp,
                                fontWeight = if (opt == maxCache) FontWeight.Bold else FontWeight.Normal,
                                color = if (opt == maxCache) Color(0xFF8C52FF) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. KEEP MEDIA DURATION SLIDER
            item {
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Хранить медиа",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Фото и видео удаляются из памяти устройства (в облаке PhantomGram они остаются).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    val keepOptions = KeepMediaDuration.values()
                    val currentIndex = keepOptions.indexOf(keepMedia).coerceAtLeast(0)

                    Slider(
                        value = currentIndex.toFloat(),
                        onValueChange = {
                            val newIdx = it.roundToInt().coerceIn(0, keepOptions.size - 1)
                            AppSettings.keepMediaDuration.value = keepOptions[newIdx]
                        },
                        valueRange = 0f..(keepOptions.size - 1).toFloat(),
                        steps = keepOptions.size - 2
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        keepOptions.forEach { opt ->
                            Text(
                                text = opt.label,
                                fontSize = 11.sp,
                                fontWeight = if (opt == keepMedia) FontWeight.Bold else FontWeight.Normal,
                                color = if (opt == keepMedia) Color(0xFF8C52FF) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 5. CHAT SPECIFIC CACHE LIST
            item {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Text(
                    text = "Использование памяти по чатам",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            items(chatCaches) { chatCache ->
                ListItem(
                    headlineContent = {
                        Text(chatCache.chatName, fontWeight = FontWeight.SemiBold)
                    },
                    supportingContent = {
                        Text(
                            text = "Видео: ${formatBytes(chatCache.videoBytes)} • Фото: ${formatBytes(chatCache.photoBytes)} • Файлы: ${formatBytes(chatCache.filesBytes)}",
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    leadingContent = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = chatCache.chatName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Text(
                            text = formatBytes(chatCache.totalBytes),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        selectedChatForClear = chatCache
                        showClearCacheDialog = true
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
            }
        }
    }

    // MODAL DIALOG TO CLEAR CACHE WITH CATEGORY CHECKBOXES
    if (showClearCacheDialog) {
        val targetChat = selectedChatForClear
        var clearVideo by remember { mutableStateOf(true) }
        var clearPhoto by remember { mutableStateOf(true) }
        var clearFiles by remember { mutableStateOf(true) }
        var clearVoice by remember { mutableStateOf(true) }
        var clearMusic by remember { mutableStateOf(true) }

        val videoSum = if (targetChat != null) targetChat.videoBytes else chatCaches.sumOf { it.videoBytes }
        val photoSum = if (targetChat != null) targetChat.photoBytes else chatCaches.sumOf { it.photoBytes }
        val filesSum = if (targetChat != null) targetChat.filesBytes else chatCaches.sumOf { it.filesBytes }
        val voiceSum = if (targetChat != null) targetChat.voiceBytes else chatCaches.sumOf { it.voiceBytes }
        val musicSum = if (targetChat != null) targetChat.musicBytes else chatCaches.sumOf { it.musicBytes }

        val selectedToClearBytes = (if (clearVideo) videoSum else 0L) +
                (if (clearPhoto) photoSum else 0L) +
                (if (clearFiles) filesSum else 0L) +
                (if (clearVoice) voiceSum else 0L) +
                (if (clearMusic) musicSum else 0L)

        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = {
                Text(
                    if (targetChat != null) "Очистить кэш: ${targetChat.chatName}"
                    else "Очистить кэш PhantomGram"
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Выберите типы файлов для удаления с устройства:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))

                    CategoryCheckboxRow(label = "Видео", bytes = videoSum, checked = clearVideo, onCheckedChange = { clearVideo = it })
                    CategoryCheckboxRow(label = "Фотографии", bytes = photoSum, checked = clearPhoto, onCheckedChange = { clearPhoto = it })
                    CategoryCheckboxRow(label = "Файлы и документы", bytes = filesSum, checked = clearFiles, onCheckedChange = { clearFiles = it })
                    CategoryCheckboxRow(label = "Голосовые сообщения", bytes = voiceSum, checked = clearVoice, onCheckedChange = { clearVoice = it })
                    CategoryCheckboxRow(label = "Музыка и аудио", bytes = musicSum, checked = clearMusic, onCheckedChange = { clearMusic = it })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val categories = mutableSetOf<String>()
                        if (clearVideo) categories.add("VIDEO")
                        if (clearPhoto) categories.add("PHOTO")
                        if (clearFiles) categories.add("FILES")
                        if (clearVoice) categories.add("VOICE")
                        if (clearMusic) categories.add("MUSIC")

                        AppSettings.clearCache(categories, targetChat?.chatId)
                        Toast.makeText(context, "Очищено ${formatBytes(selectedToClearBytes)}", Toast.LENGTH_SHORT).show()
                        showClearCacheDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    enabled = selectedToClearBytes > 0
                ) {
                    Text("Очистить (${formatBytes(selectedToClearBytes)})")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun CategoryCheckboxRow(
    label: String,
    bytes: Long,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp)
        }
        Text(formatBytes(bytes), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
fun LegendItem(color: Color, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Column {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
