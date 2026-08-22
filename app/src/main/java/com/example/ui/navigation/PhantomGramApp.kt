package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.screens.*

@Composable
fun PhantomGramApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onSignInSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                onNavigateToSearch = { navController.navigate(SearchRoute) },
                onNavigateToCreateGroup = { navController.navigate(CreateGroupRoute) },
                onNavigateToChat = { chatId -> navController.navigate(ChatRoute(chatId)) }
            )
        }
        composable<ChatRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ChatRoute>()
            ChatScreen(
                chatId = route.chatId,
                onBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(UserProfileRoute(userId))
                }
            )
        }
        composable<UserProfileRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<UserProfileRoute>()
            UserProfileScreen(
                userId = route.userId,
                onBack = { navController.popBackStack() },
                onNavigateToChat = { chatId ->
                    navController.navigate(ChatRoute(chatId)) {
                        popUpTo(HomeRoute)
                    }
                }
            )
        }
        composable<SearchRoute> {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onNavigateToChat = { chatId -> navController.navigate(ChatRoute(chatId)) },
                onNavigateToUserProfile = { userId -> navController.navigate(UserProfileRoute(userId)) }
            )
        }
        composable<CreateGroupRoute> {
            CreateGroupScreen(
                onBack = { navController.popBackStack() },
                onGroupCreated = { chatId ->
                    navController.popBackStack()
                    navController.navigate(ChatRoute(chatId))
                }
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAccount = { navController.navigate(AccountSettingsRoute) },
                onNavigateToPrivacy = { navController.navigate(PrivacySettingsRoute) },
                onNavigateToChatAppearance = { navController.navigate(ChatAppearanceRoute) },
                onNavigateToActiveSessions = { navController.navigate(ActiveSessionsRoute) },
                onNavigateToDataAndStorage = { navController.navigate(DataAndStorageRoute) },
                onNavigateToNotifications = { navController.navigate(NotificationsSettingsRoute) },
                onNavigateToEditProfile = { navController.navigate(EditProfileRoute) },
                onNavigateToLanguage = { navController.navigate(LanguageSettingsRoute) },
                onNavigateToDeveloperPanel = { navController.navigate(DeveloperPanelRoute) },
                onLoggedOut = {
                    navController.navigate(LoginRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<LanguageSettingsRoute> {
            LanguageSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<DeveloperPanelRoute> {
            DeveloperPanelScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<EditProfileRoute> {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<AccountSettingsRoute> {
            AccountSettingsScreen(
                onBack = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(LoginRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<PrivacySettingsRoute> {
            PrivacySettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPrivacyDetail = { type -> navController.navigate(PrivacyDetailRoute(type)) },
                onNavigateTo2FA = { navController.navigate(TwoStepVerificationRoute) },
                onNavigateToActiveSessions = { navController.navigate(ActiveSessionsRoute) }
            )
        }
        composable<PrivacyDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PrivacyDetailRoute>()
            PrivacyDetailScreen(
                type = route.type,
                onBack = { navController.popBackStack() }
            )
        }
        composable<TwoStepVerificationRoute> {
            TwoStepVerificationScreen(onBack = { navController.popBackStack() })
        }
        composable<DataAndStorageRoute> {
            DataAndStorageScreen(
                onBack = { navController.popBackStack() },
                onNavigateToStorageUsage = { navController.navigate(StorageUsageRoute) }
            )
        }
        composable<StorageUsageRoute> {
            StorageUsageScreen(onBack = { navController.popBackStack() })
        }
        composable<ChatAppearanceRoute> {
            ChatAppearanceScreen(onBack = { navController.popBackStack() })
        }
        composable<ActiveSessionsRoute> {
            ActiveSessionsScreen(onBack = { navController.popBackStack() })
        }
        composable<NotificationsSettingsRoute> {
            NotificationsSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
