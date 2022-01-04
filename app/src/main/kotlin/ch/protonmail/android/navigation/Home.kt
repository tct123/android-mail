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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.protonmail.android.feature.account.RemoveAccountDialog
import ch.protonmail.android.mailconversation.domain.ConversationId
import ch.protonmail.android.mailconversation.presentation.ConversationDetail
import ch.protonmail.android.mailmailbox.presentation.MailboxScreen
import ch.protonmail.android.mailmailbox.presentation.MailboxState
import ch.protonmail.android.mailmailbox.presentation.MailboxViewModel
import ch.protonmail.android.mailmessage.domain.model.MailLocation
import ch.protonmail.android.navigation.model.Destination
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.navigation.require
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId

@Composable
fun Home(
    onSignIn: (UserId?) -> Unit,
    onSignOut: (UserId) -> Unit,
    onSwitch: (UserId) -> Unit,
    mailboxViewModel: MailboxViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val mailboxState by rememberAsState(mailboxViewModel.state, MailboxState())
    val sidebarState = rememberSidebarState(
        drawerState = scaffoldState.drawerState,
        mailboxState = mailboxState
    )

    Scaffold(
        scaffoldState = scaffoldState,
        drawerShape = RectangleShape,
        drawerScrimColor = ProtonTheme.colors.blenderNorm,
        drawerContent = {
            Sidebar(
                onRemove = { navController.navigate(Destination.Dialog.RemoveAccount(it)) },
                onSignOut = onSignOut,
                onSignIn = onSignIn,
                onSwitch = onSwitch,
                onMailLocation = { navController.navigate(Destination.Screen.Mailbox(it)) },
                onFolder = { /*navController.navigate(...)*/ },
                onLabel = { /*navController.navigate(...)*/ },
                onSettings = { /*navController.navigate(Destination.Screen.Settings.route)*/ },
                onReportBug = { /*navController.navigate(Destination.Screen.ReportBug.route)*/ },
                sidebarState = sidebarState,
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
                addMailbox(navController, mailboxViewModel)
                addConversationDetail()
                addRemoveAccountDialog(navController)
            }
        }
    }
}

private fun NavGraphBuilder.addMailbox(
    navController: NavHostController,
    mailboxViewModel: MailboxViewModel,
) = composable(
    route = Destination.Screen.Mailbox.route,
    arguments = listOf(navArgument(Destination.key) { defaultValue = MailLocation.Inbox.name })
) {
    MailboxScreen(
        location = Destination.Screen.Mailbox.getLocation(it.require(Destination.key)),
        navigateToConversation = { conversationId: ConversationId ->
            navController.navigate(Destination.Screen.Conversation(conversationId))
        },
        viewModel = mailboxViewModel,
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
