package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object HomeRoute

@Serializable
object SettingsRoute

@Serializable
object AccountSettingsRoute

@Serializable
object PrivacySettingsRoute

@Serializable
data class PrivacyDetailRoute(val type: String) // "PHONE", "LAST_SEEN", "PROFILE_PHOTO"

@Serializable
object TwoStepVerificationRoute

@Serializable
object DataAndStorageRoute

@Serializable
object StorageUsageRoute

@Serializable
object ChatAppearanceRoute

@Serializable
object ActiveSessionsRoute

@Serializable
object NotificationsSettingsRoute

@Serializable
data class ChatRoute(val chatId: String)

@Serializable
object SearchRoute

@Serializable
object EditProfileRoute

@Serializable
object CreateGroupRoute

@Serializable
object DeveloperPanelRoute

@Serializable
data class UserProfileRoute(val userId: String)

@Serializable
object LanguageSettingsRoute
