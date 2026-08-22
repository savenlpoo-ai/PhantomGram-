package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppearanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val selectedThemeIdx by AppSettings.selectedThemeIndex.collectAsState()
    val fontSize by AppSettings.messageFontSize.collectAsState()
    val cornerRadius by AppSettings.messageCornerRadius.collectAsState()
    val bubbleColor by AppSettings.customBubbleAccentColor.collectAsState()
    val appIcon by AppSettings.selectedAppIcon.collectAsState()
    val bgPattern by AppSettings.chatBackgroundPattern.collectAsState()

    val currentTheme = AppSettings.chatThemes[selectedThemeIdx.coerceIn(0, AppSettings.chatThemes.size - 1)]

    val accentColors = listOf(
        Color(0xFF6200EE),
        Color(0xFF00897B),
        Color(0xFF0288D1),
        Color(0xFFD81B60),
        Color(0xFFE65100),
        Color(0xFF7B1FA2),
        Color(0xFF43A047),
        Color(0xFF37474F)
    )

    val appIconsList = listOf(
        "Classic Phantom" to Color(0xFF8C52FF),
        "Dark Phantom" to Color(0xFF1E1E1E),
        "Neon Glow" to Color(0xFFFF4081),
        "Royal Gold" to Color(0xFFFFD700),
        "Cyberpunk Mint" to Color(0xFF00E676)
    )

    val backgroundOptions = listOf("Subtle Doodles", "Plain Dark", "Blurred Gradient", "Cyber Grid")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Темы и внешний вид") },
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
            // 1. LIVE CHAT PREVIEW BOX
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.backgroundColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Предпросмотр чата",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.primaryColor
                        )
                        Spacer(Modifier.height(12.dp))

                        // Incoming bubble
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = cornerRadius.dp,
                                topEnd = cornerRadius.dp,
                                bottomEnd = cornerRadius.dp,
                                bottomStart = 4.dp
                            ),
                            color = Color(0xFF2C2C2C),
                            modifier = Modifier
                                .widthIn(max = 260.dp)
                                .align(Alignment.Start)
                        ) {
                            Text(
                                text = "Привет! Как тебе новая тема оформления PhantomGram?",
                                fontSize = fontSize.sp,
                                color = Color.White,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Outgoing bubble with accent color
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = cornerRadius.dp,
                                topEnd = cornerRadius.dp,
                                bottomStart = cornerRadius.dp,
                                bottomEnd = 4.dp
                            ),
                            color = bubbleColor,
                            modifier = Modifier
                                .widthIn(max = 260.dp)
                                .align(Alignment.End)
                        ) {
                            Text(
                                text = "Выглядит потрясающе! Шрифты и скругления настроены идеально 🔥",
                                fontSize = fontSize.sp,
                                color = Color.White,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // 2. THEME PRESETS CAROUSEL (ЦВЕТОВАЯ ПАЛИТРА)
            item {
                Text(
                    "ЦВЕТОВАЯ ПАЛИТРА ТЕМ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(AppSettings.chatThemes) { idx, theme ->
                        val isSelected = selectedThemeIdx == idx
                        Card(
                            modifier = Modifier
                                .width(110.dp)
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF8C52FF) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    AppSettings.selectedThemeIndex.value = idx
                                    AppSettings.customBubbleAccentColor.value = theme.bubbleColor
                                },
                            colors = CardDefaults.cardColors(containerColor = theme.backgroundColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(45.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.horizontalGradient(theme.previewGradient))
                                )
                                Text(
                                    text = theme.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF8C52FF) else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 3. COLOR ACCENTS (КРУЖОЧКИ ЦВЕТОВЫХ АКЦЕНТОВ)
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "ЦВЕТОВЫЕ АКЦЕНТЫ ОБЛАЧКОВ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accentColors) { color ->
                        val isSelected = bubbleColor == color
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { AppSettings.customBubbleAccentColor.value = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            // 4. CHAT BACKGROUND / WALLPAPERS
            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Фон для чатов") },
                    supportingContent = { Text("Текущий узор: $bgPattern") },
                    leadingContent = { Icon(Icons.Default.Wallpaper, contentDescription = null) },
                    trailingContent = {
                        TextButton(onClick = {
                            val nextIdx = (backgroundOptions.indexOf(bgPattern) + 1) % backgroundOptions.size
                            AppSettings.chatBackgroundPattern.value = backgroundOptions[nextIdx]
                            Toast.makeText(context, "Фон изменен на: ${backgroundOptions[nextIdx]}", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Изменить")
                        }
                    }
                )
                HorizontalDivider()
            }

            // 5. MESSAGE FONT SIZE SLIDER (12pt to 30pt)
            item {
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Размер текста сообщений",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("${fontSize.roundToInt()} pt", fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = fontSize,
                        onValueChange = { AppSettings.messageFontSize.value = it },
                        valueRange = 12f..30f,
                        steps = 17
                    )
                }
            }

            // 6. MESSAGE CORNER RADIUS SLIDER (0dp to 24dp)
            item {
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Уголки сообщений",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("${cornerRadius.roundToInt()} dp", fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = cornerRadius,
                        onValueChange = { AppSettings.messageCornerRadius.value = it },
                        valueRange = 0f..24f,
                        steps = 23
                    )
                }
            }

            // 7. APP LAUNCHER ICON SELECTION
            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Text(
                    "ИКОНКА ПРИЛОЖЕНИЯ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    appIconsList.forEach { (name, iconColor) ->
                        val isSelected = appIcon == name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    AppSettings.selectedAppIcon.value = name
                                    Toast.makeText(context, "Иконка приложения установлена: $name", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = iconColor,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("P", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(name, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    AppSettings.selectedAppIcon.value = name
                                    Toast.makeText(context, "Иконка приложения: $name", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
