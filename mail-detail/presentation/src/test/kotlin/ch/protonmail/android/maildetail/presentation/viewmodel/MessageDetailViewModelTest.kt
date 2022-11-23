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

package ch.protonmail.android.maildetail.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Event
import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailcommon.presentation.model.BottomBarState
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.reducer.BottomBarReducer
import ch.protonmail.android.mailcontact.domain.usecase.GetContacts
import ch.protonmail.android.maildetail.domain.model.MessageWithLabels
import ch.protonmail.android.maildetail.domain.usecase.MarkUnread
import ch.protonmail.android.maildetail.domain.usecase.ObserveMessageDetailActions
import ch.protonmail.android.maildetail.domain.usecase.ObserveMessageWithLabels
import ch.protonmail.android.maildetail.domain.usecase.StarMessage
import ch.protonmail.android.maildetail.domain.usecase.UnStarMessage
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.maildetail.presentation.mapper.MessageDetailUiModelMapper
import ch.protonmail.android.maildetail.presentation.model.MessageDetailMetadataState
import ch.protonmail.android.maildetail.presentation.model.MessageDetailMetadataUiModel
import ch.protonmail.android.maildetail.presentation.model.MessageDetailState
import ch.protonmail.android.maildetail.presentation.model.MessageViewAction
import ch.protonmail.android.maildetail.presentation.reducer.MessageDetailMetadataReducer
import ch.protonmail.android.maildetail.presentation.reducer.MessageDetailReducer
import ch.protonmail.android.maildetail.presentation.ui.MessageDetailScreen
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.domain.entity.MessageId
import ch.protonmail.android.testdata.action.ActionUiModelTestData
import ch.protonmail.android.testdata.contact.ContactTestData
import ch.protonmail.android.testdata.maildetail.MessageDetailHeaderUiModelTestData
import ch.protonmail.android.testdata.message.MessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData.userId
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MessageDetailViewModelTest {

    private val rawMessageId = "detailMessageId"
    private val actionUiModelMapper = ActionUiModelMapper()
    private val messageDetailReducer = MessageDetailReducer(
        MessageDetailMetadataReducer(),
        BottomBarReducer()
    )

    private val messageUiModelMapper = mockk<MessageDetailUiModelMapper> {
        every { toUiModel(any(), any()) } returns MessageDetailMetadataUiModel(
            messageId = MessageTestData.message.messageId,
            subject = MessageTestData.message.subject,
            isStarred = false,
            messageDetailHeader = MessageDetailHeaderUiModelTestData.messageDetailHeaderUiModel
        )
    }
    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk.invoke() } returns flowOf(userId)
    }

    private val observeMessageWithLabels = mockk<ObserveMessageWithLabels> {
        every { this@mockk.invoke(userId, any()) } returns flowOf(
            MessageWithLabels(
                MessageTestData.message,
                emptyList()
            ).right()
        )
    }
    private val savedStateHandle = mockk<SavedStateHandle> {
        every { this@mockk.get<String>(MessageDetailScreen.MESSAGE_ID_KEY) } returns rawMessageId
    }
    private val observeDetailActions = mockk<ObserveMessageDetailActions> {
        every { this@mockk.invoke(userId, MessageId(rawMessageId)) } returns flowOf(
            nonEmptyListOf(Action.Reply, Action.Archive, Action.MarkUnread).right()
        )
    }
    private val markUnread = mockk<MarkUnread> {
        every { this@mockk.invoke(userId, MessageId(rawMessageId)) } returns flowOf(Unit.right())
    }
    private val getContacts = mockk<GetContacts> {
        coEvery { this@mockk.invoke(userId) } returns ContactTestData.contacts.right()
    }
    private val starMessage = mockk<StarMessage> {
        every { this@mockk.invoke(userId, MessageId(rawMessageId)) } returns flowOf(Unit.right())
    }
    private val unStarMessage = mockk<UnStarMessage> {
        every { this@mockk.invoke(userId, MessageId(rawMessageId)) } returns flowOf(Unit.right())
    }

    private val viewModel by lazy {
        MessageDetailViewModel(
            observePrimaryUserId = observePrimaryUserId,
            messageDetailReducer = messageDetailReducer,
            observeMessageWithLabels = observeMessageWithLabels,
            uiModelMapper = messageUiModelMapper,
            actionUiModelMapper = actionUiModelMapper,
            observeDetailActions = observeDetailActions,
            markUnread = markUnread,
            getContacts = getContacts,
            starMessage = starMessage,
            unStarMessage = unStarMessage,
            savedStateHandle = savedStateHandle
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun `initial state is loading`() = runTest {
        // When
        viewModel.state.test {
            // Then
            assertEquals(MessageDetailState.Loading, awaitItem())
        }
    }

    @Test
    fun `no message state is emitted when there is no primary user`() = runTest {
        // Given
        givenNoLoggedInUser()

        // When
        viewModel.state.test {
            initialStateEmitted()
        }
    }

    @Test
    fun `throws exception when message id parameter was not provided as input`() = runTest {
        // Given
        every { savedStateHandle.get<String>(MessageDetailScreen.MESSAGE_ID_KEY) } returns null

        // Then
        val thrown = assertThrows(IllegalStateException::class.java) { viewModel.state }
        // Then
        assertEquals("No Message id given", thrown.message)
    }

    @Test
    fun `message state is data when use case returns message metadata`() = runTest {
        // Given
        val messageId = MessageId(rawMessageId)
        val subject = "message subject"
        val isStarred = true
        val cachedMessage = MessageTestData.buildMessage(
            userId = userId,
            id = messageId.id,
            subject = subject,
            labelIds = listOf(SystemLabelId.Starred.labelId.id)
        )
        val messageWithLabels = MessageWithLabels(cachedMessage, emptyList())
        every { observeMessageWithLabels.invoke(userId, messageId) } returns flowOf(messageWithLabels.right())
        every { messageUiModelMapper.toUiModel(any(), any()) } returns MessageDetailMetadataUiModel(
            messageId = messageId,
            subject = subject,
            isStarred = isStarred,
            messageDetailHeader = MessageDetailHeaderUiModelTestData.messageDetailHeaderUiModel
        )

        // When
        viewModel.state.test {
            initialStateEmitted()
            // Then
            val expected = MessageDetailMetadataState.Data(
                MessageDetailMetadataUiModel(
                    messageId,
                    subject,
                    isStarred,
                    MessageDetailHeaderUiModelTestData.messageDetailHeaderUiModel
                )
            )
            assertEquals(expected, awaitItem().messageState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `bottomBar state is data when use case returns actions`() = runTest {
        // Given
        val messageId = MessageId(rawMessageId)
        val cachedMessage = MessageTestData.buildMessage(
            userId = userId,
            id = messageId.id,
            subject = "message subject",
            labelIds = listOf(SystemLabelId.Starred.labelId.id)
        )
        val messageWithLabels = MessageWithLabels(cachedMessage, emptyList())
        every { observeMessageWithLabels.invoke(userId, messageId) } returns flowOf(messageWithLabels.right())
        every { observeDetailActions.invoke(userId, MessageId(rawMessageId)) } returns flowOf(
            nonEmptyListOf(Action.Reply, Action.Archive).right()
        )

        // When
        viewModel.state.test {
            initialStateEmitted()
            messageStateEmitted()
            // Then
            val actionUiModels = listOf(ActionUiModelTestData.reply, ActionUiModelTestData.archive)
            val expected = BottomBarState.Data(actionUiModels)
            assertEquals(expected, awaitItem().bottomBarState)
        }
    }

    @Test
    fun `bottomBar state is failed loading actions when use case returns no actions`() = runTest {
        // Given
        val messageId = MessageId(rawMessageId)
        val cachedMessage = MessageTestData.buildMessage(
            userId = userId,
            id = messageId.id,
            subject = "message subject",
            labelIds = listOf(SystemLabelId.Starred.labelId.id)
        )
        val messageWithLabels = MessageWithLabels(cachedMessage, emptyList())
        every { observeMessageWithLabels.invoke(userId, messageId) } returns flowOf(messageWithLabels.right())

        every { observeDetailActions.invoke(userId, MessageId(rawMessageId)) } returns
            flowOf(DataError.Local.NoDataCached.left())

        // When
        viewModel.state.test {
            initialStateEmitted()
            messageStateEmitted()
            // Then
            val expected = BottomBarState.Error.FailedLoadingActions
            assertEquals(expected, awaitItem().bottomBarState)
        }
    }

    @Test
    fun `message detail state is dismiss message screen when mark unread is successful`() = runTest {
        // Given
        every { markUnread.invoke(userId, MessageId(rawMessageId)) } returns flowOf(Unit.right())

        viewModel.state.test {
            initialStateEmitted()
            // When
            viewModel.submit(MessageViewAction.MarkUnread)
            advanceUntilIdle()
            // Then
            val events = cancelAndConsumeRemainingEvents()
            val lastState = (events.last() as Event.Item).value
            assertEquals(Unit, lastState.dismiss.consume())
        }
    }

    @Test
    fun `message detail state is error marking unread when mark unread fails`() = runTest {
        // Given
        every { markUnread.invoke(userId, MessageId(rawMessageId)) } returns flowOf(DataError.Local.NoDataCached.left())

        viewModel.state.test {
            // When
            viewModel.submit(MessageViewAction.MarkUnread)
            advanceUntilIdle()
            // Then
            val events = cancelAndConsumeRemainingEvents()
            val lastState = (events.last() as Event.Item).value
            assertEquals(TextUiModel(R.string.error_mark_unread_failed), lastState.error.consume())
        }
    }

    @Test
    fun `starred message metadata is emitted when star action is successful`() = runTest {
        // Given
        every { starMessage.invoke(userId, MessageId(rawMessageId)) } returns flowOf(Unit.right())

        viewModel.state.test {
            initialStateEmitted()
            // When
            viewModel.submit(MessageViewAction.Star)
            advanceUntilIdle()
            // Then
            val events = cancelAndConsumeRemainingEvents()
            val lastState = (events.last() as Event.Item).value
            val actual = assertIs<MessageDetailMetadataState.Data>(lastState.messageState)
            assertTrue(actual.messageUiModel.isStarred)
        }
    }

    @Test
    fun `error starring message is emitted when star action fails`() = runTest {
        // Given
        val messageId = MessageId(rawMessageId)
        every { starMessage.invoke(userId, messageId) } returns flowOf(DataError.Local.NoDataCached.left())

        viewModel.state.test {
            initialStateEmitted()
            // When
            viewModel.submit(MessageViewAction.Star)
            advanceUntilIdle()
            // Then
            val events = cancelAndConsumeRemainingEvents()
            val lastState = (events.last() as Event.Item).value
            assertEquals(TextUiModel(R.string.error_star_operation_failed), lastState.error.consume())
        }
    }

    @Test
    fun `unStarred message metadata is emitted when unStar action is successful`() = runTest {
        // Given
        val messageId = MessageId(rawMessageId)
        val messageWithLabels = MessageWithLabels(MessageTestData.starredMessage, emptyList())
        every { observeMessageWithLabels.invoke(userId, messageId) } returns flowOf(messageWithLabels.right())
        every { unStarMessage.invoke(userId, messageId) } returns flowOf(Unit.right())

        viewModel.state.test {
            initialStateEmitted()
            // When
            viewModel.submit(MessageViewAction.UnStar)
            advanceUntilIdle()
            // Then
            val events = cancelAndConsumeRemainingEvents()
            val lastState = (events.last() as Event.Item).value
            val actual = assertIs<MessageDetailMetadataState.Data>(lastState.messageState)
            assertFalse(actual.messageUiModel.isStarred)
        }
    }

    @Test
    fun `error unStarring message is emitted when unstar action fails`() = runTest {
        // Given
        val messageId = MessageId(rawMessageId)
        every { unStarMessage.invoke(userId, messageId) } returns flowOf(DataError.Local.NoDataCached.left())

        viewModel.state.test {
            initialStateEmitted()
            // When
            viewModel.submit(MessageViewAction.UnStar)
            advanceUntilIdle()
            // Then
            val events = cancelAndConsumeRemainingEvents()
            val lastState = (events.last() as Event.Item).value
            assertEquals(TextUiModel(R.string.error_unstar_operation_failed), lastState.error.consume())
        }
    }

    private suspend fun FlowTurbine<MessageDetailState>.messageStateEmitted() {
        assertIs<MessageDetailMetadataState.Data>(awaitItem().messageState)
    }

    private suspend fun FlowTurbine<MessageDetailState>.initialStateEmitted() {
        assertEquals(MessageDetailState.Loading, awaitItem())
    }

    private fun givenNoLoggedInUser() {
        every { observePrimaryUserId.invoke() } returns flowOf(null)
    }

}
