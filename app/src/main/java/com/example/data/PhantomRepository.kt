package com.example.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class PhantomRepository {

    companion object {
        val users = MutableStateFlow<List<User>>(emptyList())
        val deletedAccounts = MutableStateFlow<List<DeletedAccountRecord>>(emptyList())
        val chats = MutableStateFlow<List<Chat>>(emptyList())
        val messages = MutableStateFlow<List<Message>>(emptyList())

        val currentUserIdState = MutableStateFlow<String?>(null)
        val savedAccounts = MutableStateFlow<List<User>>(emptyList())

        var currentUserId: String?
            get() = currentUserIdState.value
            set(value) {
                currentUserIdState.value = value
            }

        private var isInitialized = false

        fun isCreatorEmail(email: String): Boolean {
            return email.equals("savenlpoo@gmail.com", ignoreCase = true) ||
                   email.equals("savnko22377@gmail.com", ignoreCase = true)
        }

        fun generateUniqueUsername(): String {
            var candidate: String
            val existing = users.value.map { it.username.lowercase() }.toSet()
            do {
                val rand = (1000..9999).random()
                candidate = "user_$rand"
            } while (candidate.lowercase() in existing)
            return candidate
        }

        fun initMockData() {
            if (isInitialized) return
            isInitialized = true

            val bot = User(
                uid = "bot_phantom",
                email = "bot@phantomgram.app",
                name = "PhantomBot",
                username = "phantombot",
                bio = "Official PhantomGram automated support and moderation bot.",
                isDeveloper = true,
                isOfficial = true,
                isOnline = true
            )

            val creator = User(
                uid = "user_creator_savenlpoo",
                email = "savenlpoo@gmail.com",
                name = "Savenlpoo",
                username = "creator",
                bio = "PhantomGram Creator & Lead Developer",
                isDeveloper = true,
                isOfficial = true,
                isOnline = true
            )

            val creator2 = User(
                uid = "user_creator_savnko",
                email = "savnko22377@gmail.com",
                name = "Savenko",
                username = "savnko22377",
                bio = "PhantomGram Creator & Lead Developer",
                isDeveloper = true,
                isOfficial = true,
                isOnline = true
            )

            users.value = listOf(bot, creator, creator2)

            val officialGroup = Chat(
                id = "phantom_group_official",
                name = "PhantomGroup",
                description = "This is the official group of the Phantomgram. Here you can write, complain about bugs, submit appeals, and ask to lift restrictions.",
                type = "GROUP",
                isOfficial = true,
                ownerId = "bot_phantom",
                participantIds = listOf("bot_phantom", "user_creator_savenlpoo", "user_creator_savnko"),
                lastMessageText = "In the future, we are going to add media file support. Feel free to report issues or appeal penalties here.",
                lastMessageTimestamp = System.currentTimeMillis() - 3600000
            )

            chats.value = listOf(officialGroup)

            val welcomeMsg = Message(
                id = UUID.randomUUID().toString(),
                chatId = "phantom_group_official",
                senderId = "bot_phantom",
                text = "Welcome to PhantomGroup! This is the official moderation and support group. All users, including those with restrictions (spamblock/freeze/ban), can write here to request help or penalty review.",
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = true
            )

            messages.value = listOf(welcomeMsg)
        }
    }

    init {
        initMockData()
        checkAutoDeleteFrozenAccounts()
    }

    private fun checkAutoDeleteFrozenAccounts() {
        val now = System.currentTimeMillis()
        val fiveHoursMs = 5 * 3600 * 1000L
        val currentUsers = users.value
        val expiredFrozen = currentUsers.filter { it.isFrozen && (now - it.frozenTimestamp >= fiveHoursMs) }

        if (expiredFrozen.isNotEmpty()) {
            val expiredUids = expiredFrozen.map { it.uid }.toSet()
            val newDeleted = expiredFrozen.map {
                DeletedAccountRecord(
                    uid = it.uid,
                    email = it.email,
                    name = it.name,
                    username = it.username,
                    deletedTimestamp = now,
                    deletedBy = "Auto-Deletion (5 hours expired)"
                )
            }
            deletedAccounts.value = deletedAccounts.value + newDeleted
            users.value = currentUsers.filterNot { it.uid in expiredUids }
            if (currentUserId in expiredUids) {
                currentUserId = null
            }
        }
    }

    suspend fun registerOrLoginGoogleUser(email: String, name: String, photoUrl: String = ""): User {
        checkAutoDeleteFrozenAccounts()

        // Check if email is in deleted accounts blacklist
        val deleted = deletedAccounts.value.find { it.email.equals(email, ignoreCase = true) }
        if (deleted != null) {
            throw Exception("Этот Google аккаунт ($email) был удален создателем и заблокирован для входа. Обратитесь к Создателю для восстановления.")
        }

        val existingUser = users.value.find { it.email.equals(email, ignoreCase = true) }
        val userToLogin: User
        val isDev = isCreatorEmail(email)

        if (existingUser != null) {
            userToLogin = if (isDev && (!existingUser.isDeveloper || !existingUser.isOfficial)) {
                val updated = existingUser.copy(isDeveloper = true, isOfficial = true)
                users.value = users.value.map { if (it.uid == existingUser.uid) updated else it }
                updated
            } else {
                existingUser
            }
        } else {
            // Create new user - Do NOT use email login for username!
            val uid = "user_" + UUID.randomUUID().toString().replace("-", "").take(10)
            val generatedUsername = if (isDev) {
                if (email.contains("savnko", ignoreCase = true)) "savnko22377" else "creator"
            } else {
                generateUniqueUsername()
            }

            val defaultDisplayName = if (isDev) {
                if (email.contains("savnko", ignoreCase = true)) "Savenko" else "Savenlpoo"
            } else {
                "User_${generatedUsername.takeLast(4)}"
            }

            val newUser = User(
                uid = uid,
                email = email,
                name = name.ifBlank { defaultDisplayName },
                username = generatedUsername,
                bio = if (isDev) "PhantomGram Creator & Lead Developer" else "Hey there! I am using PhantomGram.",
                photoUrl = photoUrl,
                isDeveloper = isDev,
                isOfficial = isDev,
                isOnline = true
            )

            users.value = users.value + newUser
            userToLogin = newUser
        }

        currentUserId = userToLogin.uid
        if (savedAccounts.value.none { it.uid == userToLogin.uid }) {
            savedAccounts.value = savedAccounts.value + userToLogin
        } else {
            savedAccounts.value = savedAccounts.value.map { if (it.uid == userToLogin.uid) userToLogin else it }
        }
        joinOfficialGroup(userToLogin.uid)
        return userToLogin
    }

    fun switchAccount(uid: String) {
        val target = users.value.find { it.uid == uid }
        if (target != null) {
            currentUserId = target.uid
            if (savedAccounts.value.none { it.uid == target.uid }) {
                savedAccounts.value = savedAccounts.value + target
            }
        }
    }

    fun removeAccount(uid: String) {
        savedAccounts.value = savedAccounts.value.filterNot { it.uid == uid }
        if (currentUserId == uid) {
            currentUserId = savedAccounts.value.firstOrNull()?.uid
        }
    }

    fun getSavedAccounts(): Flow<List<User>> = savedAccounts

    fun getCurrentUserIdFlow(): Flow<String?> = currentUserIdState

    private fun joinOfficialGroup(uid: String) {
        val group = chats.value.find { it.id == "phantom_group_official" }
        if (group != null && !group.participantIds.contains(uid)) {
            val updatedGroup = group.copy(participantIds = group.participantIds + uid)
            chats.value = chats.value.map { if (it.id == group.id) updatedGroup else it }
        }
    }

    fun getCurrentUser(): User? {
        val uid = currentUserId ?: return null
        return users.value.find { it.uid == uid }
    }

    fun getAllUsers(): Flow<List<User>> = users

    fun getDeletedAccounts(): Flow<List<DeletedAccountRecord>> = deletedAccounts

    fun getChatsForUser(uid: String): Flow<List<Chat>> = chats.map { allChats ->
        allChats.filter { it.participantIds.contains(uid) }
            .sortedByDescending { it.lastMessageTimestamp }
    }

    fun getMessagesForChat(chatId: String): Flow<List<Message>> = messages.map { allMsgs ->
        allMsgs.filter { it.chatId == chatId }.sortedBy { it.timestamp }
    }

    suspend fun markMessagesAsRead(chatId: String, currentUid: String) {
        messages.value = messages.value.map { msg ->
            if (msg.chatId == chatId && msg.senderId != currentUid && !msg.isRead) {
                msg.copy(isRead = true)
            } else {
                msg
            }
        }
    }

    suspend fun sendMessage(chatId: String, text: String) {
        val uid = currentUserId ?: throw Exception("Не авторизован")
        val sender = users.value.find { it.uid == uid } ?: throw Exception("Пользователь не найден")

        val now = System.currentTimeMillis()
        val isOfficialGroup = (chatId == "phantom_group_official")

        // Validation for Punishments:
        // If user is banned, frozen, or spamblocked, they can ONLY send messages to official group
        if (!isOfficialGroup) {
            if (sender.bannedUntil > now) {
                val remainingHours = ((sender.bannedUntil - now) / 3600000) + 1
                throw Exception("Ваш аккаунт забанен еще на $remainingHours ч.! Вы можете писать ТОЛЬКО в официальную группу PhantomGroup.")
            }
            if (sender.isFrozen) {
                throw Exception("Ваш аккаунт заморожен (❄frozen account). Вы можете писать ТОЛЬКО в официальную группу PhantomGroup.")
            }
            if (sender.spamblockUntil == -1L) {
                throw Exception("На ваш аккаунт наложен БЕССРОЧНЫЙ спамблок! Вы можете писать ТОЛЬКО в официальную группу PhantomGroup.")
            } else if (sender.spamblockUntil > now) {
                val remainingHours = ((sender.spamblockUntil - now) / 3600000) + 1
                throw Exception("На ваш аккаунт наложен спамблок еще на $remainingHours ч.! Вы можете писать ТОЛЬКО в официальную группу PhantomGroup.")
            }
        }

        val msgId = UUID.randomUUID().toString()
        val msg = Message(
            id = msgId,
            chatId = chatId,
            senderId = uid,
            text = text,
            timestamp = now,
            isRead = false
        )
        messages.value = messages.value + msg

        // Update chat snippet
        chats.value = chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(
                    lastMessageText = text,
                    lastMessageTimestamp = now
                )
            } else chat
        }
    }

    suspend fun getOrCreatePrivateChat(targetUserId: String): String {
        val uid = currentUserId ?: throw Exception("Не авторизован")
        val targetUser = users.value.find { it.uid == targetUserId } ?: throw Exception("Собеседник не найден")

        // Search existing private chat between these 2 users
        val existing = chats.value.find { chat ->
            chat.type == "PRIVATE" &&
                    chat.participantIds.contains(uid) &&
                    chat.participantIds.contains(targetUserId)
        }

        if (existing != null) {
            return existing.id
        }

        val newChatId = "dm_" + UUID.randomUUID().toString().replace("-", "").take(12)
        val newChat = Chat(
            id = newChatId,
            name = targetUser.name,
            description = "@${targetUser.username}",
            photoUrl = targetUser.photoUrl,
            type = "PRIVATE",
            isOfficial = targetUser.isOfficial,
            ownerId = uid,
            participantIds = listOf(uid, targetUserId),
            lastMessageText = "",
            lastMessageTimestamp = System.currentTimeMillis()
        )

        chats.value = chats.value + newChat
        return newChatId
    }

    suspend fun createGroupOrChannel(name: String, description: String, isChannel: Boolean, photoUri: Uri?): String {
        val uid = currentUserId ?: throw Exception("Не авторизован")
        val groupId = "chat_" + UUID.randomUUID().toString().replace("-", "").take(10)

        val chat = Chat(
            id = groupId,
            name = name,
            description = description,
            photoUrl = photoUri?.toString() ?: "",
            type = if (isChannel) "CHANNEL" else "GROUP",
            ownerId = uid,
            participantIds = listOf(uid),
            lastMessageText = if (isChannel) "Канал создан" else "Группа создана",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        chats.value = chats.value + chat
        return groupId
    }

    suspend fun updateUserProfile(name: String, username: String, bio: String, photoUri: Uri?) {
        val uid = currentUserId ?: throw Exception("Не авторизован")
        users.value = users.value.map {
            if (it.uid == uid) {
                it.copy(
                    name = name,
                    username = username,
                    bio = bio,
                    photoUrl = photoUri?.toString() ?: it.photoUrl
                )
            } else it
        }
    }

    suspend fun searchUsers(query: String): List<User> {
        if (query.isBlank()) return emptyList()
        return users.value.filter {
            it.username.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true)
        }
    }

    suspend fun searchChats(query: String): List<Chat> {
        if (query.isBlank()) return emptyList()
        return chats.value.filter {
            (it.type == "GROUP" || it.type == "CHANNEL") &&
                    (it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true))
        }
    }

    suspend fun getUser(uid: String): User? {
        return users.value.find { it.uid == uid }
    }

    suspend fun getChat(chatId: String): Chat? {
        return chats.value.find { it.id == chatId }
    }

    // ==================== CREATOR / DEVELOPER ACTIONS ====================

    suspend fun banUser(uid: String, hours: Int) {
        val now = System.currentTimeMillis()
        val banExpiry = now + (hours.coerceIn(1, 24) * 3600 * 1000L)
        users.value = users.value.map {
            if (it.uid == uid) it.copy(bannedUntil = banExpiry) else it
        }
    }

    suspend fun unbanUser(uid: String) {
        users.value = users.value.map {
            if (it.uid == uid) it.copy(bannedUntil = 0L) else it
        }
    }

    suspend fun freezeUser(uid: String) {
        val now = System.currentTimeMillis()
        users.value = users.value.map {
            if (it.uid == uid) {
                it.copy(
                    isFrozen = true,
                    frozenTimestamp = now,
                    originalName = if (it.originalName.isEmpty()) it.name else it.originalName,
                    name = "❄frozen account"
                )
            } else it
        }
    }

    suspend fun unfreezeUser(uid: String) {
        users.value = users.value.map {
            if (it.uid == uid) {
                it.copy(
                    isFrozen = false,
                    frozenTimestamp = 0L,
                    name = if (it.originalName.isNotEmpty()) it.originalName else "User"
                )
            } else it
        }
    }

    suspend fun applySpamblock(uid: String, durationHours: Int, isIndefinite: Boolean = false, days: Int = 0) {
        val now = System.currentTimeMillis()
        val spamblockExpiry = when {
            isIndefinite -> -1L
            days > 0 -> now + (days.toLong() * 24 * 3600 * 1000L)
            else -> now + (durationHours.toLong() * 3600 * 1000L)
        }
        users.value = users.value.map {
            if (it.uid == uid) it.copy(spamblockUntil = spamblockExpiry) else it
        }
    }

    suspend fun removeSpamblock(uid: String) {
        users.value = users.value.map {
            if (it.uid == uid) it.copy(spamblockUntil = 0L) else it
        }
    }

    suspend fun purgeUserByCreator(uid: String) {
        val target = users.value.find { it.uid == uid } ?: return
        val record = DeletedAccountRecord(
            uid = target.uid,
            email = target.email,
            name = target.name,
            username = target.username,
            deletedTimestamp = System.currentTimeMillis(),
            deletedBy = "Creator"
        )
        deletedAccounts.value = deletedAccounts.value + record
        users.value = users.value.filterNot { it.uid == uid }

        if (currentUserId == uid) {
            currentUserId = null
        }
    }

    suspend fun restoreDeletedAccount(uid: String) {
        val record = deletedAccounts.value.find { it.uid == uid } ?: return
        val isDev = record.email.equals("savenlpoo@gmail.com", ignoreCase = true)
        val restoredUser = User(
            uid = record.uid,
            email = record.email,
            name = record.name,
            username = record.username,
            isDeveloper = isDev,
            isOfficial = isDev,
            isOnline = true
        )
        deletedAccounts.value = deletedAccounts.value.filterNot { it.uid == uid }
        users.value = users.value + restoredUser
        joinOfficialGroup(restoredUser.uid)
    }

    suspend fun deleteMyOwnAccount() {
        val uid = currentUserId ?: return
        purgeUserByCreator(uid)
        currentUserId = null
    }

    fun logout() {
        currentUserId = null
    }
}
