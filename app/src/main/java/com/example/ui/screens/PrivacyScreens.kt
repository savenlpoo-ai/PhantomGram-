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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onBack: () -> Unit,
    onNavigateToPrivacyDetail: (String) -> Unit,
    onNavigateTo2FA: () -> Unit,
    onNavigateToActiveSessions: () -> Unit
) {
    val phonePrivacy by AppSettings.phoneNumberPrivacy.collectAsState()
    val lastSeenPrivacy by AppSettings.lastSeenPrivacy.collectAsState()
    val photoPrivacy by AppSettings.profilePhotoPrivacy.collectAsState()
    val cloudPassword by AppSettings.cloudPassword.collectAsState()

    val phoneLabel = when (phonePrivacy.level) {
        PrivacyLevel.EVERYBODY -> "Все"
        PrivacyLevel.MY_CONTACTS -> "Мои контакты"
        PrivacyLevel.NOBODY -> "Никто"
    }
    val lastSeenLabel = when (lastSeenPrivacy.level) {
        PrivacyLevel.EVERYBODY -> "Все"
        PrivacyLevel.MY_CONTACTS -> "Мои контакты"
        PrivacyLevel.NOBODY -> "Никто"
    }
    val photoLabel = when (photoPrivacy.level) {
        PrivacyLevel.EVERYBODY -> "Все"
        PrivacyLevel.MY_CONTACTS -> "Мои контакты"
        PrivacyLevel.NOBODY -> "Никто"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Конфиденциальность") },
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
                    "Безопасность",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                ListItem(
                    headlineContent = { Text("Двухэтапная аутентификация") },
                    supportingContent = {
                        Text(if (!cloudPassword.isNullOrBlank()) "Включена (Облачный пароль)" else "Выключена")
                    },
                    leadingContent = { Icon(Icons.Default.Password, contentDescription = null) },
                    trailingContent = {
                        Text(
                            if (!cloudPassword.isNullOrBlank()) "Вкл" else "Выкл",
                            color = if (!cloudPassword.isNullOrBlank()) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { onNavigateTo2FA() }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Устройства и активные сеансы") },
                    supportingContent = { Text("Управление сессиями на Desktop, Web и смартфонах") },
                    leadingContent = { Icon(Icons.Default.Devices, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToActiveSessions() }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Автоудаление сообщений") },
                    leadingContent = { Icon(Icons.Default.Timer, contentDescription = null) },
                    trailingContent = { Text("Выкл") }
                )
                HorizontalDivider()

                Spacer(Modifier.height(16.dp))
                Text(
                    "Конфиденциальность",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                ListItem(
                    headlineContent = { Text("Номер телефона") },
                    supportingContent = { Text("Кто видит и кто может найти по номеру") },
                    leadingContent = { Icon(Icons.Default.Phone, contentDescription = null) },
                    trailingContent = { Text(phoneLabel, color = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigateToPrivacyDetail("PHONE") }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Был(а) в сети") },
                    supportingContent = { Text("Время последнего посещения и статус онлайн") },
                    leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    trailingContent = { Text(lastSeenLabel, color = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigateToPrivacyDetail("LAST_SEEN") }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Фотографии профиля") },
                    supportingContent = { Text("Кто видит аватарку вашего аккаунта") },
                    leadingContent = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                    trailingContent = { Text(photoLabel, color = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigateToPrivacyDetail("PROFILE_PHOTO") }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Пересылка сообщений") },
                    leadingContent = { Icon(Icons.Default.Forward, contentDescription = null) },
                    trailingContent = { Text("Все") }
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Группы и каналы") },
                    leadingContent = { Icon(Icons.Default.Group, contentDescription = null) },
                    trailingContent = { Text("Все") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDetailScreen(
    type: String, // "PHONE", "LAST_SEEN", "PROFILE_PHOTO"
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { PhantomRepository() }
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val currentUserId = PhantomRepository.currentUserId

    val title = when (type) {
        "PHONE" -> "Номер телефона"
        "LAST_SEEN" -> "Был(а) в сети"
        else -> "Фото профиля"
    }

    val stateFlow = when (type) {
        "PHONE" -> AppSettings.phoneNumberPrivacy
        "LAST_SEEN" -> AppSettings.lastSeenPrivacy
        else -> AppSettings.profilePhotoPrivacy
    }

    val currentSetting by stateFlow.collectAsState()

    var showAddUserDialog by remember { mutableStateOf<Boolean?>(null) } // true for always, false for never, null closed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            // 1. WHO CAN SEE SECTION
            item {
                Text(
                    "КТО ВИДИТ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                RadioRow(
                    title = "Все",
                    selected = currentSetting.level == PrivacyLevel.EVERYBODY,
                    onClick = { stateFlow.value = currentSetting.copy(level = PrivacyLevel.EVERYBODY) }
                )
                RadioRow(
                    title = "Мои контакты",
                    selected = currentSetting.level == PrivacyLevel.MY_CONTACTS,
                    onClick = { stateFlow.value = currentSetting.copy(level = PrivacyLevel.MY_CONTACTS) }
                )
                RadioRow(
                    title = "Никто",
                    selected = currentSetting.level == PrivacyLevel.NOBODY,
                    onClick = { stateFlow.value = currentSetting.copy(level = PrivacyLevel.NOBODY) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 2. WHO CAN FIND ME BY NUMBER (ONLY FOR PHONE)
            if (type == "PHONE") {
                item {
                    Text(
                        "КТО МОЖЕТ НАЙТИ МЕНЯ ПО НОМЕРУ",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    RadioRow(
                        title = "Все",
                        selected = currentSetting.findByNumberLevel == PhoneNumberFindLevel.EVERYBODY,
                        onClick = { stateFlow.value = currentSetting.copy(findByNumberLevel = PhoneNumberFindLevel.EVERYBODY) }
                    )
                    RadioRow(
                        title = "Мои контакты",
                        selected = currentSetting.findByNumberLevel == PhoneNumberFindLevel.MY_CONTACTS,
                        onClick = { stateFlow.value = currentSetting.copy(findByNumberLevel = PhoneNumberFindLevel.MY_CONTACTS) }
                    )

                    Text(
                        text = "Пользователи, у которых есть ваш номер в контактах, смогут найти вас в PhantomGram.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // 3. EXCEPTIONS SECTION (ДОБАВИТЬ ПОЛЬЗОВАТЕЛЕЙ)
            item {
                Text(
                    "ИСКЛЮЧЕНИЯ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                // Always share with...
                ListItem(
                    headlineContent = { Text("Всегда делиться с...") },
                    supportingContent = {
                        val count = currentSetting.alwaysShareWith.size
                        Text(if (count > 0) "$count пользователей добавлено" else "Добавить пользователей")
                    },
                    leadingContent = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    trailingContent = {
                        TextButton(onClick = { showAddUserDialog = true }) {
                            Text("Добавить")
                        }
                    },
                    modifier = Modifier.clickable { showAddUserDialog = true }
                )

                // Never show to...
                ListItem(
                    headlineContent = { Text("Никогда не показывать...") },
                    supportingContent = {
                        val count = currentSetting.neverShareWith.size
                        Text(if (count > 0) "$count пользователей заблокировано" else "Добавить пользователей")
                    },
                    leadingContent = { Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color(0xFFE53935)) },
                    trailingContent = {
                        TextButton(onClick = { showAddUserDialog = false }) {
                            Text("Добавить")
                        }
                    },
                    modifier = Modifier.clickable { showAddUserDialog = false }
                )

                Text(
                    text = "Эти настройки имеют приоритет над общими правилами выше.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    // DIALOG TO SELECT USERS FOR EXCEPTIONS
    if (showAddUserDialog != null) {
        val isAlwaysShare = showAddUserDialog == true
        val otherUsers = allUsers.filter { it.uid != currentUserId }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = null },
            title = {
                Text(if (isAlwaysShare) "Всегда делиться с..." else "Никогда не показывать...")
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(otherUsers) { user ->
                        val isSelected = if (isAlwaysShare) {
                            user.uid in currentSetting.alwaysShareWith
                        } else {
                            user.uid in currentSetting.neverShareWith
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isAlwaysShare) {
                                        val newList = if (isSelected) currentSetting.alwaysShareWith - user.uid
                                        else currentSetting.alwaysShareWith + user.uid
                                        stateFlow.value = currentSetting.copy(alwaysShareWith = newList)
                                    } else {
                                        val newList = if (isSelected) currentSetting.neverShareWith - user.uid
                                        else currentSetting.neverShareWith + user.uid
                                        stateFlow.value = currentSetting.copy(neverShareWith = newList)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(user.name, fontWeight = FontWeight.SemiBold)
                                Text("@${user.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Checkbox(checked = isSelected, onCheckedChange = { checked ->
                                if (isAlwaysShare) {
                                    val newList = if (checked) currentSetting.alwaysShareWith + user.uid
                                    else currentSetting.alwaysShareWith - user.uid
                                    stateFlow.value = currentSetting.copy(alwaysShareWith = newList)
                                } else {
                                    val newList = if (checked) currentSetting.neverShareWith + user.uid
                                    else currentSetting.neverShareWith - user.uid
                                    stateFlow.value = currentSetting.copy(neverShareWith = newList)
                                }
                            })
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showAddUserDialog = null
                    Toast.makeText(context, "Исключения обновлены", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Готово")
                }
            }
        )
    }
}

@Composable
fun RadioRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp)
        RadioButton(selected = selected, onClick = onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoStepVerificationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val savedPassword by AppSettings.cloudPassword.collectAsState()
    val savedHint by AppSettings.cloudPasswordHint.collectAsState()
    val savedEmail by AppSettings.cloudPasswordRecoveryEmail.collectAsState()

    var passwordInput by remember { mutableStateOf(savedPassword ?: "") }
    var hintInput by remember { mutableStateOf(savedHint) }
    var recoveryEmailInput by remember { mutableStateOf(savedEmail) }
    var passwordVisible by remember { mutableStateOf(false) }

    var showDisableConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Двухэтапная аутентификация") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (!savedPassword.isNullOrBlank()) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (!savedPassword.isNullOrBlank()) "Облачный пароль активен" else "Защитите свой аккаунт",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Этот пароль будет запрашиваться в дополнение к Google-авторизации при входе с нового устройства.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text("Настройка пароля", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Буквенно-цифровой пароль") },
                    placeholder = { Text("Введите надежный пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = hintInput,
                    onValueChange = { hintInput = it },
                    label = { Text("Подсказка для пароля") },
                    placeholder = { Text("Например: девичья фамилия кота") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Подсказка будет показана, если вы забудете пароль.") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = recoveryEmailInput,
                    onValueChange = { recoveryEmailInput = it },
                    label = { Text("Email для восстановления") },
                    placeholder = { Text("recovery@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("На эту почту придет код для сброса пароля при утере.") },
                    singleLine = true
                )
            }

            item {
                Button(
                    onClick = {
                        if (passwordInput.length < 4) {
                            Toast.makeText(context, "Пароль должен содержать минимум 4 символа", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        AppSettings.cloudPassword.value = passwordInput
                        AppSettings.cloudPasswordHint.value = hintInput
                        AppSettings.cloudPasswordRecoveryEmail.value = recoveryEmailInput
                        Toast.makeText(context, "Облачный пароль успешно сохранен!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (!savedPassword.isNullOrBlank()) "Сохранить изменения" else "Задать пароль")
                }
            }

            if (!savedPassword.isNullOrBlank()) {
                item {
                    Button(
                        onClick = { showDisableConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Отключить облачный пароль")
                    }
                }
            }
        }
    }

    if (showDisableConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDisableConfirmDialog = false },
            title = { Text("Отключить пароль?") },
            text = { Text("Вы уверены, что хотите удалить двухэтапную аутентификацию? Защита второго фактора будет отключена.") },
            confirmButton = {
                Button(
                    onClick = {
                        AppSettings.cloudPassword.value = null
                        AppSettings.cloudPasswordHint.value = ""
                        AppSettings.cloudPasswordRecoveryEmail.value = ""
                        passwordInput = ""
                        hintInput = ""
                        recoveryEmailInput = ""
                        showDisableConfirmDialog = false
                        Toast.makeText(context, "Двухэтапная аутентификация отключена", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Отключить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
