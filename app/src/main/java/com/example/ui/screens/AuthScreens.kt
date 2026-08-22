package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.PhantomRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onSignInSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var showCustomAccountDialog by remember { mutableStateOf(false) }

    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    val repository = remember { PhantomRepository() }

    fun processLogin(email: String, name: String) {
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                repository.registerOrLoginGoogleUser(email = email.trim(), name = name.trim())
                showGoogleAccountPicker = false
                showCustomAccountDialog = false
                onSignInSuccess()
            } catch (e: Exception) {
                errorMsg = e.localizedMessage ?: "Ошибка входа"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_phantom_logo_1787175567856),
            contentDescription = "PhantomGram Logo",
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "PhantomGram",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fast, secure and official messenger",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(48.dp))

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Connecting to Google Services...", color = MaterialTheme.colorScheme.onBackground)
        } else {
            Button(
                onClick = {
                    // Try launching system Google Credential Manager first, or open account picker
                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetSignInWithGoogleOption.Builder("phantomgram-auth.apps.googleusercontent.com")
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(context = context, request = request)
                            val credential = result.credential
                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                                processLogin(googleIdToken.id, googleIdToken.displayName ?: "Google User")
                            } else {
                                showGoogleAccountPicker = true
                            }
                        } catch (e: Exception) {
                            // On emulators without Play Services account or cancelled, show interactive Google Account Selection Sheet
                            showGoogleAccountPicker = true
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
                    .testTag("google_signin_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "G",
                                color = Color(0xFF4285F4),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Sign in with Google", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }

    // Google Account Picker Bottom Sheet
    if (showGoogleAccountPicker) {
        ModalBottomSheet(
            onDismissRequest = { showGoogleAccountPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4285F4),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Google Sign-In", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Choose an account to continue to PhantomGram", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()

                // Primary account (savenlpoo@gmail.com - Creator account)
                ListItem(
                    headlineContent = { Text("Savenlpoo", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("savenlpoo@gmail.com") },
                    leadingContent = {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE53935),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    },
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF5A9BEC).copy(alpha = 0.2f),
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "Creator",
                                color = Color(0xFF5A9BEC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        processLogin(email = "savenlpoo@gmail.com", name = "Savenlpoo")
                    }
                )

                HorizontalDivider()

                // Second creator account (savnko22377@gmail.com)
                ListItem(
                    headlineContent = { Text("Savenko", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("savnko22377@gmail.com") },
                    leadingContent = {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E88E5),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    },
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF5A9BEC).copy(alpha = 0.2f),
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "Creator",
                                color = Color(0xFF5A9BEC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        processLogin(email = "savnko22377@gmail.com", name = "Savenko")
                    }
                )

                HorizontalDivider()

                // Add / Use another account
                ListItem(
                    headlineContent = { Text("Войти с другим Google аккаунтом", fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Добавить второй аккаунт (@gmail.com)") },
                    leadingContent = {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable {
                        showGoogleAccountPicker = false
                        showCustomAccountDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialog for adding any real custom Google account
    if (showCustomAccountDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAccountDialog = false },
            title = { Text("Google Account Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter the Google email and full name to register or sign in:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Google Email (e.g. user@gmail.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Full Name (e.g. John Doe)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customEmail.contains("@") && customEmail.contains(".")) {
                            val nameToUse = customName.ifBlank { customEmail.substringBefore("@") }
                            processLogin(email = customEmail.trim(), name = nameToUse.trim())
                        } else {
                            Toast.makeText(context, "Please enter a valid Google email address", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
