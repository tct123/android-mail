/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mailmailbox.presentation.mailbox.reducer

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.domain.model.OpenMailboxItemRequest
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxEvent
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxListState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxViewAction
import me.proton.core.mailsettings.domain.entity.ViewMode
import javax.inject.Inject

class MailboxListReducer @Inject constructor() {

    internal fun newStateFrom(
        currentState: MailboxListState,
        operation: MailboxOperation.AffectingMailboxList
    ): MailboxListState {
        return when (operation) {
            is MailboxEvent.SelectedLabelChanged -> reduceSelectedLabelChanged(operation, currentState)
            is MailboxEvent.NewLabelSelected -> reduceNewLabelSelected(operation, currentState)
            is MailboxEvent.ItemDetailsOpenedInViewMode -> reduceItemDetailOpened(operation, currentState)
            is MailboxViewAction.OnOfflineWithData -> reduceOfflineWithData(currentState)
            is MailboxViewAction.OnErrorWithData -> reduceErrorWithData(currentState)
            is MailboxViewAction.Refresh -> reduceRefresh(currentState)
        }
    }

    private fun reduceSelectedLabelChanged(
        operation: MailboxEvent.SelectedLabelChanged,
        currentState: MailboxListState
    ): MailboxListState.Data {
        val currentMailLabel = operation.selectedLabel
        return when (currentState) {
            is MailboxListState.Loading -> MailboxListState.Data(
                currentMailLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                offlineEffect = Effect.empty(),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false
            )

            is MailboxListState.Data -> currentState.copy(
                currentMailLabel = currentMailLabel
            )
        }
    }

    private fun reduceNewLabelSelected(
        operation: MailboxEvent.NewLabelSelected,
        currentState: MailboxListState
    ): MailboxListState.Data {
        val currentMailLabel = operation.selectedLabel
        return when (currentState) {
            is MailboxListState.Loading -> MailboxListState.Data(
                currentMailLabel,
                openItemEffect = Effect.empty(),
                scrollToMailboxTop = Effect.empty(),
                offlineEffect = Effect.empty(),
                refreshErrorEffect = Effect.empty(),
                refreshRequested = false
            )

            is MailboxListState.Data -> currentState.copy(
                currentMailLabel = currentMailLabel,
                scrollToMailboxTop = Effect.of(currentMailLabel.id)
            )
        }
    }

    private fun reduceItemDetailOpened(
        operation: MailboxEvent.ItemDetailsOpenedInViewMode,
        currentState: MailboxListState
    ): MailboxListState {
        val request = when (operation.preferredViewMode) {
            ViewMode.ConversationGrouping -> {
                OpenMailboxItemRequest(
                    itemId = MailboxItemId(operation.item.conversationId.id),
                    itemType = MailboxItemType.Conversation
                )
            }

            ViewMode.NoConversationGrouping -> {
                OpenMailboxItemRequest(
                    itemId = MailboxItemId(operation.item.id),
                    itemType = operation.item.type
                )
            }
        }
        return when (currentState) {
            is MailboxListState.Loading -> currentState
            is MailboxListState.Data -> currentState.copy(openItemEffect = Effect.of(request))
        }
    }

    private fun reduceOfflineWithData(currentState: MailboxListState) = when (currentState) {
        is MailboxListState.Data -> {
            if (currentState.refreshRequested) {
                currentState.copy(offlineEffect = Effect.of(Unit), refreshRequested = false)
            } else {
                currentState
            }
        }

        is MailboxListState.Loading -> currentState
    }

    private fun reduceRefresh(currentState: MailboxListState) = when (currentState) {
        is MailboxListState.Data -> currentState.copy(refreshRequested = true)
        is MailboxListState.Loading -> currentState
    }

    private fun reduceErrorWithData(currentState: MailboxListState) = when (currentState) {
        is MailboxListState.Data -> {
            if (currentState.refreshRequested) {
                currentState.copy(refreshErrorEffect = Effect.of(Unit), refreshRequested = false)
            } else {
                currentState
            }
        }

        is MailboxListState.Loading -> currentState
    }
}
