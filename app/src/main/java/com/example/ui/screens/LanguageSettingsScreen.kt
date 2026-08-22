package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalizationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    onBack: () -> Unit
) {
    val currentLang by LocalizationManager.currentLanguage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            LocalizationManager.supportedLanguages
        } else {
            val query = searchQuery.trim().lowercase()
            LocalizationManager.supportedLanguages.filter {
                it.englishName.lowercase().contains(query) ||
                it.nativeName.lowercase().contains(query) ||
                it.code.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LocalizationManager.tr("language", "Language")) },
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(LocalizationManager.tr("search_language", "Search language...")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Current Language banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(currentLang.flagEmoji, fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Current: ${currentLang.nativeName} (${currentLang.englishName})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Default on new login: English",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "All Languages (${LocalizationManager.supportedLanguages.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLanguages, key = { it.code }) { lang ->
                    val isSelected = lang.code == currentLang.code
                    ListItem(
                        leadingContent = {
                            Text(lang.flagEmoji, fontSize = 24.sp)
                        },
                        headlineContent = {
                            Text(
                                text = lang.nativeName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "${lang.englishName} • ${lang.code.uppercase()}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.clickable {
                            LocalizationManager.setLanguage(lang.code)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }
        }
    }
}
