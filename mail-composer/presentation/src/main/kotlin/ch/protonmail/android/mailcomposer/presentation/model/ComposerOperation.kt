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

package ch.protonmail.android.mailcomposer.presentation.model

import android.net.Uri
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.MessagePassword
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.MessageAttachment
import ch.protonmail.android.mailmessage.domain.model.MessageId

sealed interface ComposerOperation

internal sealed interface ComposerAction : ComposerOperation {
    data class AttachmentsAdded(val uriList: List<Uri>) : ComposerAction
    data class SenderChanged(val sender: SenderUiModel) : ComposerAction
    data class RecipientsToChanged(val recipients: List<RecipientUiModel>) : ComposerAction
    data class RecipientsCcChanged(val recipients: List<RecipientUiModel>) : ComposerAction
    data class RecipientsBccChanged(val recipients: List<RecipientUiModel>) : ComposerAction
    data class ContactSuggestionTermChanged(
        val searchTerm: String,
        val suggestionsField: ContactSuggestionsField
    ) : ComposerAction
    data class ContactSuggestionsDismissed(val suggestionsField: ContactSuggestionsField) : ComposerAction

    data class SubjectChanged(val subject: Subject) : ComposerAction
    data class DraftBodyChanged(val draftBody: DraftBody) : ComposerAction
    data class RemoveAttachment(val attachmentId: AttachmentId) : ComposerAction

    object ChangeSenderRequested : ComposerAction
    object OnBottomSheetOptionSelected : ComposerAction
    object OnAddAttachments : ComposerAction
    object OnCloseComposer : ComposerAction
    object OnSendMessage : ComposerAction
    object ConfirmSendingWithoutSubject : ComposerAction
    object RejectSendingWithoutSubject : ComposerAction
}

sealed interface ComposerEvent : ComposerOperation {
    data class DefaultSenderReceived(val sender: SenderUiModel) : ComposerEvent
    data class SenderAddressesReceived(val senders: List<SenderUiModel>) : ComposerEvent
    data class OpenExistingDraft(val draftId: MessageId) : ComposerEvent
    data class OpenWithMessageAction(val parentId: MessageId, val draftAction: DraftAction) : ComposerEvent
    data class PrefillDraftDataReceived(
        val draftUiModel: DraftUiModel,
        val isDataRefreshed: Boolean,
        val isBlockedSendingFromPmAddress: Boolean,
        val isBlockedSendingFromDisabledAddress: Boolean
    ) : ComposerEvent
    data class PrefillDataReceivedViaShare(val draftUiModel: DraftUiModel) : ComposerEvent
    data class ReplaceDraftBody(val draftBody: DraftBody) : ComposerEvent
    data class OnAttachmentsUpdated(val attachments: List<MessageAttachment>) : ComposerEvent
    data class OnSendingError(val sendingError: TextUiModel) : ComposerEvent
    data class OnMessagePasswordUpdated(val messagePassword: MessagePassword?) : ComposerEvent
    data class UpdateContactSuggestions(
        val contactSuggestions: List<ContactSuggestionUiModel>,
        val suggestionsField: ContactSuggestionsField
    ) : ComposerEvent

    object ErrorLoadingDefaultSenderAddress : ComposerEvent
    object ErrorFreeUserCannotChangeSender : ComposerEvent
    object ErrorVerifyingPermissionsToChangeSender : ComposerEvent
    object ErrorStoringDraftSenderAddress : ComposerEvent
    object ErrorStoringDraftBody : ComposerEvent
    object ErrorStoringDraftRecipients : ComposerEvent
    object ErrorStoringDraftSubject : ComposerEvent
    object OnCloseWithDraftSaved : ComposerEvent
    object OnSendMessageOffline : ComposerEvent
    object ErrorLoadingDraftData : ComposerEvent
    object ErrorLoadingParentMessageData : ComposerEvent
    object ErrorAttachmentsExceedSizeLimit : ComposerEvent
    object ErrorAttachmentsReEncryption : ComposerEvent
    object ConfirmEmptySubject : ComposerEvent
}
