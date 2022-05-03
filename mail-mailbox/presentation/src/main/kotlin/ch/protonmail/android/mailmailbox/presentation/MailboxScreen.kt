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

package ch.protonmail.android.mailmailbox.presentation

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import ch.protonmail.android.mailconversation.domain.entity.Recipient
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailpagination.presentation.paging.rememberLazyListState
import ch.protonmail.android.mailpagination.presentation.paging.verticalScrollbar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId

const val TEST_TAG_MAILBOX_SCREEN = "MailboxScreenTestTag"

@Composable
fun MailboxScreen(
    navigateToMailboxItem: (MailboxItem) -> Unit,
    openDrawerMenu: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MailboxViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val mailboxState by rememberAsState(viewModel.state, MailboxState.Loading)
    val mailboxListItems = viewModel.items.collectAsLazyPagingItems()
    val mailboxListState = mailboxListItems.rememberLazyListState()

    Scaffold(
        modifier = modifier.testTag(TEST_TAG_MAILBOX_SCREEN),
        topBar = {
            MailboxTopAppBar(
                state = mailboxState.topAppBar,
                onOpenMenu = openDrawerMenu,
                onCloseSelectionMode = { viewModel.submit(MailboxViewModel.Action.CloseSelectionMode) },
                onCloseSearchMode = {},
                onTitleClick = { scope.launch { mailboxListState.animateScrollToItem(0) } },
                onOpenSearchMode = {},
                onSearch = {},
                onOpenCompose = {}
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
                .background(ProtonTheme.colors.backgroundNorm)
                .fillMaxSize()
        ) {
            MailboxHeader(
                mailboxState = mailboxState,
            )
            MailboxList(
                navigateToMailboxItem = navigateToMailboxItem,
                onRefresh = { viewModel.submit(MailboxViewModel.Action.Refresh) },
                onOpenSelectionMode = { viewModel.submit(MailboxViewModel.Action.OpenSelectionMode) },
                modifier = modifier,
                items = mailboxListItems,
                listState = mailboxListState
            )
        }
    }
}

@Composable
private fun MailboxHeader(
    mailboxState: MailboxState,
) {
    val locationName = mailboxState.selectedLocation?.javaClass?.simpleName
    Text(
        text = "Location: $locationName (unread: ${mailboxState.unread})",
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MailboxList(
    navigateToMailboxItem: (MailboxItem) -> Unit,
    onRefresh: () -> Unit,
    onOpenSelectionMode: () -> Unit,
    modifier: Modifier = Modifier,
    items: LazyPagingItems<MailboxItem>,
    listState: LazyListState
) {

    val isRefreshing = when {
        items.loadState.refresh is LoadState.Loading -> true
        items.loadState.append is LoadState.Loading -> true
        items.loadState.prepend is LoadState.Loading -> true
        else -> false
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { onRefresh(); items.refresh() },
        modifier = modifier,
    ) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .let { if (BuildConfig.DEBUG) it.verticalScrollbar(listState) else it }
        ) {
            items(
                items = items,
                key = { it.id }
            ) { item ->
                item?.let {
                    MailboxItem(
                        item = item,
                        modifier = Modifier.animateItemPlacement(),
                        onItemClicked = navigateToMailboxItem,
                        onOpenSelectionMode = onOpenSelectionMode
                    )
                }
            }
            item {
                when (items.loadState.append) {
                    is LoadState.NotLoading -> Unit
                    is LoadState.Loading -> ProtonCenteredProgress(Modifier.fillMaxWidth())
                    is LoadState.Error -> Button(
                        onClick = { items.retry() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Retry")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MailboxItem(
    item: MailboxItem,
    modifier: Modifier = Modifier,
    onItemClicked: (MailboxItem) -> Unit,
    onOpenSelectionMode: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = { onItemClicked(item) }, onLongClick = onOpenSelectionMode)
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
                val fontWeight = if (item.read) FontWeight.Normal else FontWeight.Bold
                Text(
                    text = "UserId: ${item.userId}",
                    fontWeight = fontWeight,
                    maxLines = 1,
                    softWrap = false,
                )
                Text(
                    text = "Senders: ${item.senders}",
                    fontWeight = fontWeight,
                    maxLines = 1
                )
                Text(
                    text = "Subject: ${item.subject}",
                    fontWeight = fontWeight,
                    maxLines = 1
                )
                Text(
                    text = when (item.type) {
                        MailboxItemType.Message -> "Message "
                        MailboxItemType.Conversation -> "Conversation "
                    } + "Labels: ${item.labels.map { it.name }}",
                    fontWeight = fontWeight,
                    maxLines = 1
                )
                Text(text = "Time: ${item.time}", fontWeight = fontWeight)
            }
        }
    }
}

@Preview(
    name = "Sidebar in light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Sidebar in dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
fun PreviewMailbox() {
    val items = flowOf(
        PagingData.from(
            listOf(
                MailboxItem(
                    type = MailboxItemType.Message,
                    id = "1",
                    userId = UserId("0"),
                    senders = listOf(Recipient("address", "name")),
                    recipients = emptyList(),
                    subject = "First message",
                    time = 0,
                    size = 0,
                    order = 0,
                    read = false,

                ),
                MailboxItem(
                    type = MailboxItemType.Message,
                    id = "2",
                    userId = UserId("0"),
                    senders = listOf(Recipient("address", "name")),
                    recipients = emptyList(),
                    subject = "Second message",
                    time = 0,
                    size = 0,
                    order = 0,
                    read = true,

                ),
            )
        )
    ).collectAsLazyPagingItems()

    ProtonTheme {
        MailboxList(
            navigateToMailboxItem = {},
            onRefresh = {},
            onOpenSelectionMode = {},
            items = items,
            listState = items.rememberLazyListState()
        )
    }
}
