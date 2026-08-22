package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
fun EditProfileScreen(onBack: () -> Unit) {
    val repository = remember { PhantomRepository() }
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    LaunchedEffect(Unit) {
        val uid = PhantomRepository.currentUserId
        if (uid != null) {
            val user = repository.getUser(uid)
            if (user != null) {
                name = user.name
                username = user.username
                bio = user.bio
                existingPhotoUrl = user.photoUrl
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(24.dp))
                    } else {
                        TextButton(onClick = {
                            scope.launch {
                                isSaving = true
                                try {
                                    repository.updateUserProfile(name, username, bio, photoUri)
                                    onBack()
                                } catch (e: Exception) {
                                    // Handle error
                                } finally {
                                    isSaving = false
                                }
                            }
                        }) {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
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
                    } else if (existingPhotoUrl.isNotEmpty()) {
                        AsyncImage(model = existingPhotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Add Photo", modifier = Modifier.size(64.dp))
                    }
                }
                Text("Tap to change profile photo", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("@Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "You can choose a username on PhantomGram. If you do, other people will be able to find you by this username and contact you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }
    }
}
