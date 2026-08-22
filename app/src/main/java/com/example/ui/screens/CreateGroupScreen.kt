package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.PhantomRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(onBack: () -> Unit, onGroupCreated: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isChannel by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val repository = remember { PhantomRepository() }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Group or Channel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(24.dp))
                    } else {
                        TextButton(
                            onClick = {
                                if (name.isNotBlank()) {
                                    scope.launch {
                                        isSaving = true
                                        try {
                                            val id = repository.createGroupOrChannel(name, description, isChannel, photoUri)
                                            onGroupCreated(id)
                                        } catch (e: Exception) {
                                            // Handle error
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                }
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Add, contentDescription = "Add Photo", modifier = Modifier.padding(32.dp))
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Is this a Channel?")
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = isChannel, onCheckedChange = { isChannel = it })
            }
        }
    }
}
