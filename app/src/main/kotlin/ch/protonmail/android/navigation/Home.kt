/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import ch.protonmail.android.feature.account.RemoveAccountDialog
import ch.protonmail.android.mailconversation.domain.ConversationId
import ch.protonmail.android.mailconversation.presentation.ConversationDetail
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.presentation.MailboxScreen
import ch.protonmail.android.mailsettings.presentation.accountsettings.AccountSettingScreen
import ch.protonmail.android.mailsettings.presentation.addConversationModeSettings
import ch.protonmail.android.mailsettings.presentation.addLanguageSettings
import ch.protonmail.android.mailsettings.presentation.addThemeSettings
import ch.protonmail.android.mailsettings.presentation.settings.MainSettingsScreen
import ch.protonmail.android.navigation.model.Destination
import ch.protonmail.android.navigation.model.Destination.Dialog.RemoveAccount
import ch.protonmail.android.sidebar.Sidebar
import me.proton.core.compose.navigation.require
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import timber.log.Timber

@Composable
fun Home(
    onSignIn: (UserId?) -> Unit,
    onSignOut: (UserId) -> Unit,
    onSwitch: (UserId) -> Unit,
    onSubscription: () -> Unit,
    onReportBug: () -> Unit,
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerShape = RectangleShape,
        drawerScrimColor = ProtonTheme.colors.blenderNorm,
        drawerContent = {
            Sidebar(
                onRemove = { navController.navigate(RemoveAccount(it)) },
                onSignOut = onSignOut,
                onSignIn = onSignIn,
                onSwitch = onSwitch,
                onMailLocation = { /* stack screens? */ },
                onFolder = { /*navController.navigate(...)*/ },
                onLabel = { /*navController.navigate(...)*/ },
                onSettings = { navController.navigate(Destination.Screen.Settings.route) },
                onSubscription = onSubscription,
                onReportBug = onReportBug,
                drawerState = scaffoldState.drawerState
            )
        }
    ) { contentPadding ->
        Box(
            Modifier.padding(contentPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Destination.Screen.Mailbox.route,
            ) {
                addMailbox(navController)
                addConversationDetail()
                addRemoveAccountDialog(navController)
                addSettings(navController)
                addAccountSettings(navController)
                addConversationModeSettings(
                    navController,
                    Destination.Screen.ConversationModeSettings.route
                )
                addThemeSettings(navController, Destination.Screen.ThemeSettings.route)
                addLanguageSettings(navController, Destination.Screen.LanguageSettings.route)
            }
        }
    }
}

private fun NavGraphBuilder.addMailbox(
    navController: NavHostController,
) = composable(
    route = Destination.Screen.Mailbox.route
) {
    MailboxScreen(
        navigateToMailboxItem = { item: MailboxItem ->
            navController.navigate(
                when (item.type) {
                    MailboxItemType.Message ->
                        Destination.Screen.Conversation(ConversationId(item.id))
                    MailboxItemType.Conversation ->
                        Destination.Screen.Conversation(ConversationId(item.id))
                }
            )
        }
    )
}

private fun NavGraphBuilder.addConversationDetail() = composable(
    route = Destination.Screen.Conversation.route,
) {
    ConversationDetail(Destination.Screen.Conversation.getConversationId(it.require(Destination.key)))
}

private fun NavGraphBuilder.addRemoveAccountDialog(navController: NavHostController) = dialog(
    route = Destination.Dialog.RemoveAccount.route,
) {
    RemoveAccountDialog(
        userId = Destination.Dialog.RemoveAccount.getUserId(it.require(Destination.key)),
        onRemoved = { navController.popBackStack() },
        onCancelled = { navController.popBackStack() }
    )
}

fun NavGraphBuilder.addSettings(navController: NavHostController) = composable(
    route = Destination.Screen.Settings.route
) {
    MainSettingsScreen(
        onAccountClicked = {
            Timber.d("Navigating to account settings")
            navController.navigate(Destination.Screen.AccountSettings.route)
        },
        onThemeClick = {
            Timber.d("Navigating to theme settings")
            navController.navigate(Destination.Screen.ThemeSettings.route)
        },
        onPushNotificationsClick = {
            Timber.i("Push Notifications setting clicked")
        },
        onAutoLockClick = {
            Timber.i("Auto Lock setting clicked")
        },
        onAlternativeRoutingClick = {
            Timber.i("Alternative routing setting clicked")
        },
        onAppLanguageClick = {
            Timber.d("Navigating to language settings")
            navController.navigate(Destination.Screen.LanguageSettings.route)
        },
        onCombinedContactsClick = {
            Timber.i("Combined contacts setting clicked")
        },
        onSwipeActionsClick = {
            Timber.i("Swipe actions setting clicked")
        },
        onBackClick = {
            navController.popBackStack()
        }
    )
}

fun NavGraphBuilder.addAccountSettings(navController: NavHostController) = composable(
    route = Destination.Screen.AccountSettings.route
) {
    AccountSettingScreen(
        onBackClick = { navController.popBackStack() },
        onPasswordManagementClick = {
            Timber.i("Password management setting clicked")
        },
        onRecoveryEmailClick = {
            Timber.i("Recovery email setting clicked")
        },
        onConversationModeClick = {
            Timber.d("Navigating to conversation mode settings")
            navController.navigate(Destination.Screen.ConversationModeSettings.route)
        },
        onDefaultEmailAddressClick = {
            Timber.i("Default email address setting clicked")
        },
        onDisplayNameClick = {
            Timber.i("Display name setting clicked")
        },
        onPrivacyClick = {
            Timber.i("Privacy setting clicked")
        },
        onSearchMessageContentClick = {
            Timber.i("Search message content setting clicked")
        },
        onLabelsFoldersClick = {
            Timber.i("Labels folders setting clicked")
        },
        onLocalStorageClick = {
            Timber.i("Local storage setting clicked")
        },
        onSnoozeNotificationsClick = {
            Timber.i("Snooze notification setting clicked")
        }
    )
}
