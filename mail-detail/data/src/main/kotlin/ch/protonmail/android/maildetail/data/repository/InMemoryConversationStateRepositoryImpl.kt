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

package ch.protonmail.android.maildetail.data.repository

import java.util.concurrent.ConcurrentHashMap
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository
import ch.protonmail.android.maildetail.domain.repository.InMemoryConversationStateRepository.MessageState
import ch.protonmail.android.mailmessage.domain.model.MessageId
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@ViewModelScoped
class InMemoryConversationStateRepositoryImpl @Inject constructor() :
    InMemoryConversationStateRepository {

    private val conversationCache = ConcurrentHashMap<MessageId, MessageState>()
    private val conversationStateFlow = MutableSharedFlow<Map<MessageId, MessageState>>(1)

    init {
        conversationStateFlow.tryEmit(conversationCache)
    }

    override val conversationState: Flow<Map<MessageId, MessageState>> =
        conversationStateFlow

    override suspend fun expandMessage(messageId: MessageId, decryptedBody: DecryptedMessageBody) {
        conversationCache[messageId] = MessageState.Expanded(decryptedBody)
        conversationStateFlow.emit(conversationCache)
    }

    override suspend fun expandingMessage(messageId: MessageId) {
        conversationCache[messageId] = MessageState.Expanding
        conversationStateFlow.emit(conversationCache)
    }

    override suspend fun collapseMessage(messageId: MessageId) {
        conversationCache[messageId] = MessageState.Collapsed
        conversationStateFlow.emit(conversationCache)
    }
}
