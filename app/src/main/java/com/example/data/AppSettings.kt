package com.example.data

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

enum class PrivacyLevel {
    EVERYBODY,
    MY_CONTACTS,
    NOBODY
}

enum class PhoneNumberFindLevel {
    EVERYBODY,
    MY_CONTACTS
}

enum class KeepMediaDuration(val label: String, val days: Int) {
    THREE_DAYS("3 days", 3),
    ONE_WEEK("1 week", 7),
    ONE_MONTH("1 month", 30),
    FOREVER("Forever", -1)
}

enum class MaxCacheSize(val label: String, val gb: Int) {
    FIVE_GB("5 GB", 5),
    TEN_GB("10 GB", 10),
    TWENTY_GB("20 GB", 20),
    NO_LIMIT("No Limit", -1)
}

data class PrivacySettingItem(
    val level: PrivacyLevel = PrivacyLevel.EVERYBODY,
    val findByNumberLevel: PhoneNumberFindLevel = PhoneNumberFindLevel.EVERYBODY,
    val alwaysShareWith: List<String> = emptyList(), // user uids
    val neverShareWith: List<String> = emptyList()   // user uids
)

data class ActiveSession(
    val id: String = UUID.randomUUID().toString(),
    val deviceName: String,
    val platform: String, // "Telegram Desktop / Windows 11", "Telegram Web / Chrome", "Telegram iOS / iPad Pro"
    val ipAddress: String,
    val location: String,
    val lastActive: String,
    val isCurrent: Boolean = false
)

data class ChatCacheInfo(
    val chatId: String,
    val chatName: String,
    val videoBytes: Long,
    val photoBytes: Long,
    val filesBytes: Long,
    val voiceBytes: Long,
    val musicBytes: Long
) {
    val totalBytes: Long get() = videoBytes + photoBytes + filesBytes + voiceBytes + musicBytes
}

data class ChatThemePreset(
    val name: String,
    val primaryColor: Color,
    val bubbleColor: Color,
    val backgroundColor: Color,
    val previewGradient: List<Color>
)

object AppSettings {
    // 1. Data & Storage
    val keepMediaDuration = MutableStateFlow(KeepMediaDuration.FOREVER)
    val maxCacheSize = MutableStateFlow(MaxCacheSize.TWENTY_GB)
    val chatCaches = MutableStateFlow<List<ChatCacheInfo>>(
        listOf(
            ChatCacheInfo("phantom_group_official", "PhantomGroup Official", 185_000_000L, 42_000_000L, 15_000_000L, 8_000_000L, 24_000_000L),
            ChatCacheInfo("bot_phantom", "PhantomBot", 0L, 1_500_000L, 500_000L, 0L, 0L)
        )
    )

    // Category sizes calculated
    fun clearCache(categories: Set<String>, specificChatId: String? = null) {
        val current = chatCaches.value.map { chat ->
            if (specificChatId == null || chat.chatId == specificChatId) {
                chat.copy(
                    videoBytes = if ("VIDEO" in categories) 0L else chat.videoBytes,
                    photoBytes = if ("PHOTO" in categories) 0L else chat.photoBytes,
                    filesBytes = if ("FILES" in categories) 0L else chat.filesBytes,
                    voiceBytes = if ("VOICE" in categories) 0L else chat.voiceBytes,
                    musicBytes = if ("MUSIC" in categories) 0L else chat.musicBytes
                )
            } else chat
        }
        chatCaches.value = current
    }

    // 2. Privacy Settings
    val phoneNumberPrivacy = MutableStateFlow(
        PrivacySettingItem(level = PrivacyLevel.NOBODY, findByNumberLevel = PhoneNumberFindLevel.MY_CONTACTS)
    )
    val lastSeenPrivacy = MutableStateFlow(
        PrivacySettingItem(level = PrivacyLevel.EVERYBODY)
    )
    val profilePhotoPrivacy = MutableStateFlow(
        PrivacySettingItem(level = PrivacyLevel.EVERYBODY)
    )
    val forwardedPrivacy = MutableStateFlow(PrivacySettingItem(level = PrivacyLevel.EVERYBODY))
    val callsPrivacy = MutableStateFlow(PrivacySettingItem(level = PrivacyLevel.EVERYBODY))

    // 3. Two-Step Verification (Cloud Password)
    val cloudPassword = MutableStateFlow<String?>(null)
    val cloudPasswordHint = MutableStateFlow("")
    val cloudPasswordRecoveryEmail = MutableStateFlow("")
    val is2FAEnabled: Boolean get() = !cloudPassword.value.isNullOrBlank()

    // 4. Chat Appearance & Customization
    val chatThemes = listOf(
        ChatThemePreset("Classic Dark", Color(0xFFBB86FC), Color(0xFF6200EE), Color(0xFF121212), listOf(Color(0xFF1E1E1E), Color(0xFF2A1B4E))),
        ChatThemePreset("Day / Light", Color(0xFF1976D2), Color(0xFF2196F3), Color(0xFFE3F2FD), listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))),
        ChatThemePreset("Night Sky", Color(0xFF81D4FA), Color(0xFF0288D1), Color(0xFF0A192F), listOf(Color(0xFF0A192F), Color(0xFF172A45))),
        ChatThemePreset("Arctic Mint", Color(0xFF00E676), Color(0xFF00897B), Color(0xFF002422), listOf(Color(0xFF002422), Color(0xFF004D40))),
        ChatThemePreset("Phantom Neon", Color(0xFFFF4081), Color(0xFFD81B60), Color(0xFF180A22), listOf(Color(0xFF180A22), Color(0xFF38006B))),
        ChatThemePreset("Monochrome", Color(0xFFE0E0E0), Color(0xFF424242), Color(0xFF181818), listOf(Color(0xFF181818), Color(0xFF2E2E2E)))
    )

    val selectedThemeIndex = MutableStateFlow(0)
    val messageFontSize = MutableStateFlow(15f) // 12pt to 30pt
    val messageCornerRadius = MutableStateFlow(16f) // 0dp to 24dp
    val customBubbleAccentColor = MutableStateFlow(Color(0xFF6200EE))
    val selectedAppIcon = MutableStateFlow("Classic Phantom") // Classic Phantom, Dark Phantom, Neon Glow, Royal Gold, Cyberpunk
    val chatBackgroundPattern = MutableStateFlow("Subtle Doodles") // Plain, Subtle Doodles, Blurred Gradient, Cyber Grid

    // 5. Active Sessions (Devices)
    val activeSessions = MutableStateFlow<List<ActiveSession>>(
        listOf(
            ActiveSession(
                id = "current_device_1",
                deviceName = "Pixel 8 Pro",
                platform = "PhantomGram Android 14",
                ipAddress = "192.168.1.104",
                location = "Frankfurt, Germany",
                lastActive = "Online now",
                isCurrent = true
            ),
            ActiveSession(
                id = "session_desktop_win",
                deviceName = "PhantomGram Desktop",
                platform = "Windows 11 x64",
                ipAddress = "178.62.204.18",
                location = "Amsterdam, Netherlands",
                lastActive = "Yesterday at 21:40",
                isCurrent = false
            ),
            ActiveSession(
                id = "session_web_chrome",
                deviceName = "PhantomGram Web",
                platform = "Chrome on macOS Sonoma",
                ipAddress = "94.23.144.89",
                location = "Paris, France",
                lastActive = "August 18 at 14:12",
                isCurrent = false
            ),
            ActiveSession(
                id = "session_ipad_pro",
                deviceName = "iPad Pro 12.9\"",
                platform = "iPadOS 17.5",
                ipAddress = "212.58.244.70",
                location = "London, United Kingdom",
                lastActive = "August 12 at 09:30",
                isCurrent = false
            )
        )
    )

    fun terminateSession(sessionId: String) {
        activeSessions.value = activeSessions.value.filter { it.id != sessionId || it.isCurrent }
    }

    fun terminateAllOtherSessions() {
        activeSessions.value = activeSessions.value.filter { it.isCurrent }
    }

    // 6. Muted Users
    val mutedUserIds = MutableStateFlow<Set<String>>(emptySet())

    fun isUserMuted(userId: String): Boolean {
        return mutedUserIds.value.contains(userId)
    }

    fun toggleMuteUser(userId: String): Boolean {
        val current = mutedUserIds.value
        val isNowMuted = !current.contains(userId)
        mutedUserIds.value = if (isNowMuted) current + userId else current - userId
        return isNowMuted
    }
}
