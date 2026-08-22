package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val username: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val isDeveloper: Boolean = false,
    val isOfficial: Boolean = false,
    val isOnline: Boolean = true,
    // Punishment fields
    val isFrozen: Boolean = false,
    val frozenTimestamp: Long = 0L, // Auto-delete after 5 hours = 5 * 3600 * 1000
    val bannedUntil: Long = 0L, // 0 = not banned, timestamp in ms
    val spamblockUntil: Long = 0L, // 0 = not spamblocked, -1 = indefinite (бессрочно), or timestamp in ms
    val originalName: String = "" // To restore name if unfrozen
)

@Serializable
data class DeletedAccountRecord(
    val uid: String,
    val email: String,
    val name: String,
    val username: String,
    val deletedTimestamp: Long = System.currentTimeMillis(),
    val deletedBy: String = "Creator"
)

@Serializable
data class Chat(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val type: String = "PRIVATE", // PRIVATE, GROUP, CHANNEL
    val isOfficial: Boolean = false,
    val ownerId: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = 0L
)

@Serializable
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
