package com.example.ui.screens

import android.widget.Toast
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
import com.example.data.ActiveSession
import com.example.data.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sessions by AppSettings.activeSessions.collectAsState()

    var showQrScannerDialog by remember { mutableStateOf(false) }
    var showTerminateAllDialog by remember { mutableStateOf(false) }
    var selectedSessionForDetails by remember { mutableStateOf<ActiveSession?>(null) }

    val currentSession = sessions.find { it.isCurrent }
    val otherSessions = sessions.filter { !it.isCurrent }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Устройства и сеансы") },
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
            // 1. SCAN QR CODE / LINK DESKTOP BUTTON
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF8C52FF).copy(alpha = 0.15f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF8C52FF), modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Подключить устройство",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Сканируйте QR-код на экране PhantomGram Desktop или Web для мгновенного входа.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { showQrScannerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Подключить устройство (QR)")
                        }
                    }
                }
            }

            // 2. TERMINATE ALL OTHER SESSIONS BUTTON
            if (otherSessions.isNotEmpty()) {
                item {
                    Button(
                        onClick = { showTerminateAllDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Завершить все другие сеансы")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // 3. CURRENT DEVICE
            item {
                Text(
                    "ТЕКУЩЕЕ УСТРОЙСТВО",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (currentSession != null) {
                    ListItem(
                        headlineContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currentSession.deviceName, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "В сети",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        supportingContent = {
                            Column {
                                Text("${currentSession.platform} • ${currentSession.ipAddress}")
                                Text(currentSession.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 4. ACTIVE SESSIONS LIST
            item {
                Text(
                    "АКТИВНЫЕ СЕАНСЫ (${otherSessions.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (otherSessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Нет других активных сеансов. Ваш аккаунт используется только на этом устройстве.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(otherSessions) { session ->
                    val icon = when {
                        session.platform.contains("Desktop", ignoreCase = true) || session.platform.contains("Windows", ignoreCase = true) -> Icons.Default.Computer
                        session.platform.contains("Web", ignoreCase = true) || session.platform.contains("Chrome", ignoreCase = true) -> Icons.Default.Language
                        session.platform.contains("iPad", ignoreCase = true) || session.platform.contains("Tablet", ignoreCase = true) -> Icons.Default.Tablet
                        else -> Icons.Default.Smartphone
                    }

                    ListItem(
                        headlineContent = {
                            Text(session.deviceName, fontWeight = FontWeight.SemiBold)
                        },
                        supportingContent = {
                            Column {
                                Text("${session.platform} • ${session.ipAddress}")
                                Text("${session.location} • ${session.lastActive}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { selectedSessionForDetails = session }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                            }
                        },
                        modifier = Modifier.clickable { selectedSessionForDetails = session }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                }
            }
        }
    }

    // QR SCANNER MODAL DIALOG
    if (showQrScannerDialog) {
        AlertDialog(
            onDismissRequest = { showQrScannerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF8C52FF))
                    Spacer(Modifier.width(8.dp))
                    Text("Сканер QR-кода")
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Наведите камеру на QR-код на экране компьютера или планшета.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))

                    // Simulated Camera Viewfinder with animated frame
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF8C52FF).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF8C52FF)),
                            modifier = Modifier.size(150.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Наведите на QR", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newSession = ActiveSession(
                            deviceName = "PhantomGram Web / Edge",
                            platform = "Edge on Windows 11",
                            ipAddress = "85.140.2.19",
                            location = "Berlin, Germany",
                            lastActive = "Только что",
                            isCurrent = false
                        )
                        AppSettings.activeSessions.value = AppSettings.activeSessions.value + newSession
                        showQrScannerDialog = false
                        Toast.makeText(context, "Устройство успешно подключено!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                ) {
                    Text("Подтвердить сканирование")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrScannerDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // TERMINATE ALL SESSIONS CONFIRMATION
    if (showTerminateAllDialog) {
        AlertDialog(
            onDismissRequest = { showTerminateAllDialog = false },
            title = { Text("Завершить все другие сеансы?") },
            text = { Text("Вы выйдете со всех устройств, кроме текущего телефона. Все открытые сессии в Desktop и Web будут закрыты.") },
            confirmButton = {
                Button(
                    onClick = {
                        AppSettings.terminateAllOtherSessions()
                        showTerminateAllDialog = false
                        Toast.makeText(context, "Все другие сеансы успешно завершены!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Завершить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTerminateAllDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // SESSION DETAILS MODAL SHEET / DIALOG
    if (selectedSessionForDetails != null) {
        val session = selectedSessionForDetails!!
        AlertDialog(
            onDismissRequest = { selectedSessionForDetails = null },
            title = {
                Column {
                    Text(session.deviceName, fontWeight = FontWeight.Bold)
                    Text(session.platform, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("IP-адрес: ${session.ipAddress}", fontSize = 13.sp)
                    Text("Местоположение: ${session.location}", fontSize = 13.sp)
                    Text("Последняя активность: ${session.lastActive}", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppSettings.terminateSession(session.id)
                        selectedSessionForDetails = null
                        Toast.makeText(context, "Сеанс ${session.deviceName} завершен", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Завершить сеанс")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSessionForDetails = null }) {
                    Text("Закрыть")
                }
            }
        )
    }
}
