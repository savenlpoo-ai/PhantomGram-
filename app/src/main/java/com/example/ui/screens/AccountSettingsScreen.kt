package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhantomRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(onBack: () -> Unit, onAccountDeleted: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { PhantomRepository() }
    val scope = rememberCoroutineScope()
    val currentUser = repository.getCurrentUser()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val isFrozen = currentUser?.isFrozen == true
    val frozenHoursLeft = if (isFrozen && currentUser != null) {
        val msLeft = (currentUser.frozenTimestamp + 5 * 3600 * 1000L) - now
        ((msLeft / 3600000).coerceAtLeast(0))
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FROZEN ACCOUNT WARNING BANNER
            if (isFrozen) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF006064)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF80DEEA))
                            Spacer(Modifier.width(8.dp))
                            Text("Account Frozen (❄)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Your account is frozen. Your name is set to \"❄frozen account\" and message sending is restricted except in PhantomGroup.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "⏳ This account will be automatically deleted in ~$frozenHoursLeft hours, or you can delete it immediately below.",
                            color = Color(0xFF80DEEA),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Delete Account Immediately")
                        }
                    }
                }
            }

            Text("Google Account Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Connected Email: ${currentUser?.email ?: "Not connected"}", fontWeight = FontWeight.Medium)
                    Text("Display Name: ${currentUser?.name ?: ""}")
                    Text("Username: @${currentUser?.username ?: ""}")
                    if (currentUser?.isDeveloper == true) {
                        Text("Role: Creator / Developer (SuperAdmin)", color = Color(0xFF5A9BEC), fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Danger Zone", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)

            Button(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete My Account")
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account?") },
            text = {
                Text("Are you sure you want to delete your account? Your messages, profile and sessions will be removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            showDeleteConfirmDialog = false
                            repository.deleteMyOwnAccount()
                            Toast.makeText(context, "Your account has been deleted", Toast.LENGTH_SHORT).show()
                            onAccountDeleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
