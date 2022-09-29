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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import androidx.viewbinding.BuildConfig
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailmailbox.domain.model.OpenMailboxItemRequest
import ch.protonmail.android.mailmailbox.presentation.UnreadItemsFilter
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxListState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.UnreadFilterState
import ch.protonmail.android.mailmailbox.presentation.mailbox.previewdata.MailboxPreview
import ch.protonmail.android.mailmailbox.presentation.mailbox.previewdata.MailboxPreviewProvider
import ch.protonmail.android.mailpagination.presentation.paging.rememberLazyListState
import ch.protonmail.android.mailpagination.presentation.paging.verticalScrollbar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import timber.log.Timber
import ch.protonmail.android.mailcommon.presentation.R.string as commonString

@Composable
fun MailboxScreen(
    modifier: Modifier = Modifier,
    navigateToMailboxItem: (OpenMailboxItemRequest) -> Unit,
    openDrawerMenu: () -> Unit,
    viewModel: MailboxViewModel = hiltViewModel()
) {
    val mailboxState = rememberAsState(viewModel.state, MailboxViewModel.initialState).value
    val mailboxListItems = viewModel.items.collectAsLazyPagingItems()

    val actions = MailboxScreen.Actions(
        navigateToMailboxItem = navigateToMailboxItem,
        onDisableUnreadFilter = { viewModel.submit(MailboxViewModel.Action.DisableUnreadFilter) },
        onEnableUnreadFilter = { viewModel.submit(MailboxViewModel.Action.EnableUnreadFilter) },
        onExitSelectionMode = { viewModel.submit(MailboxViewModel.Action.ExitSelectionMode) },
        onNavigateToMailboxItem = { item -> viewModel.submit(MailboxViewModel.Action.OpenItemDetails(item)) },
        onOpenSelectionMode = { viewModel.submit(MailboxViewModel.Action.EnterSelectionMode) },
        onRefreshList = { viewModel.submit(MailboxViewModel.Action.Refresh) },
        openDrawerMenu = openDrawerMenu
    )

    MailboxScreen(
        mailboxState = mailboxState,
        mailboxListItems = mailboxListItems,
        actions = actions,
        modifier = modifier
    )

}

@Composable
fun MailboxScreen(
    mailboxState: MailboxState,
    mailboxListItems: LazyPagingItems<MailboxItemUiModel>,
    actions: MailboxScreen.Actions,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val lazyListState = mailboxListItems.rememberLazyListState()

    Scaffold(
        modifier = modifier.testTag(MailboxScreen.TestTag),
        topBar = {
            Column {
                MailboxTopAppBar(
                    state = mailboxState.topAppBarState,
                    actions = MailboxTopAppBar.Actions(
                        onOpenMenu = actions.openDrawerMenu,
                        onExitSelectionMode = actions.onExitSelectionMode,
                        onExitSearchMode = {},
                        onTitleClick = { scope.launch { lazyListState.animateScrollToItem(0) } },
                        onEnterSearchMode = {},
                        onSearch = {},
                        onOpenCompose = {}
                    )
                )

                MailboxStickyHeader(
                    modifier = Modifier,
                    state = mailboxState.unreadFilterState,
                    onFilterEnabled = actions.onEnableUnreadFilter,
                    onFilterDisabled = actions.onDisableUnreadFilter
                )
            }
        }
    ) { paddingValues ->
        when (val mailboxListState = mailboxState.mailboxListState) {
            is MailboxListState.Data -> {

                ConsumableLaunchedEffect(mailboxListState.scrollToMailboxTop) {
                    lazyListState.animateScrollToItem(0)
                }

                ConsumableLaunchedEffect(mailboxListState.openItemEffect) { itemId ->
                    actions.navigateToMailboxItem(itemId)
                }

                MailboxSwipeRefresh(
                    modifier = Modifier.padding(paddingValues),
                    items = mailboxListItems,
                    listState = lazyListState,
                    actions = actions
                )
            }
            MailboxListState.Loading -> ProtonCenteredProgress(
                modifier = Modifier.testTag(MailboxScreen.ListProgressTestTag).padding(paddingValues)
            )
        }
    }
}

@Composable
private fun MailboxStickyHeader(
    modifier: Modifier = Modifier,
    state: UnreadFilterState,
    onFilterEnabled: () -> Unit,
    onFilterDisabled: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.DefaultSpacing),
        horizontalArrangement = Arrangement.End
    ) {
        UnreadItemsFilter(
            modifier = Modifier,
            state = state,
            onFilterEnabled = onFilterEnabled,
            onFilterDisabled = onFilterDisabled
        )
    }
}

@Composable
private fun MailboxSwipeRefresh(
    items: LazyPagingItems<MailboxItemUiModel>,
    listState: LazyListState,
    actions: MailboxScreen.Actions,
    modifier: Modifier = Modifier
) {

    val isLoading = when {
        items.loadState.refresh is LoadState.Loading -> true
        items.loadState.append is LoadState.Loading -> true
        items.loadState.prepend is LoadState.Loading -> true
        else -> false
    }

    Timber.v("Is loading: $isLoading, items count: ${items.itemCount}")

    SwipeRefresh(
        modifier = modifier,
        state = rememberSwipeRefreshState(isLoading),
        onRefresh = {
            actions.onRefreshList()
            items.refresh()
        }
    ) {

        if (isLoading.not() && items.itemCount == 0) {
            MailboxEmpty(
                modifier = Modifier.scrollable(
                    rememberScrollableState(consumeScrollDelta = { 0f }),
                    orientation = Orientation.Vertical
                )
            )
        } else {
            MailboxItemsList(listState, items, actions)
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MailboxItemsList(
    listState: LazyListState,
    items: LazyPagingItems<MailboxItemUiModel>,
    actions: MailboxScreen.Actions
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
                    modifier = Modifier.animateItemPlacement(),
                    item = item,
                    onItemClicked = actions.onNavigateToMailboxItem,
                    onOpenSelectionMode = actions.onOpenSelectionMode
                )
            }
            Divider(color = ProtonTheme.colors.separatorNorm, thickness = MailDimens.ListSeparatorHeight)
        }
        item {
            when (items.loadState.append) {
                is LoadState.NotLoading -> Unit
                is LoadState.Loading -> ProtonCenteredProgress(Modifier.fillMaxWidth())
                is LoadState.Error -> Button(
                    onClick = { items.retry() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = commonString.retry))
                }
            }
        }
    }
}

@Composable
private fun MailboxEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .testTag(MailboxScreen.MailboxEmptyTestTag)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Empty mailbox")
    }
}

object MailboxScreen {

    const val ListProgressTestTag = "MailboxListProgress"
    const val MailboxEmptyTestTag = "MailboxEmpty"
    const val TestTag = "MailboxScreen"

    data class Actions(
        val navigateToMailboxItem: (OpenMailboxItemRequest) -> Unit,
        val onDisableUnreadFilter: () -> Unit,
        val onEnableUnreadFilter: () -> Unit,
        val onExitSelectionMode: () -> Unit,
        val onNavigateToMailboxItem: (MailboxItemUiModel) -> Unit,
        val onOpenSelectionMode: () -> Unit,
        val onRefreshList: () -> Unit,
        val openDrawerMenu: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                navigateToMailboxItem = {},
                onDisableUnreadFilter = {},
                onEnableUnreadFilter = {},
                onExitSelectionMode = {},
                onNavigateToMailboxItem = {},
                onOpenSelectionMode = {},
                onRefreshList = {},
                openDrawerMenu = {}
            )
        }
    }
}

/**
 * Note: The preview won't show mailbox items because this: https://issuetracker.google.com/issues/194544557
 *  Start preview in Interactive Mode to correctly see the mailbox items
 */
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun MailboxScreenPreview(
    @PreviewParameter(MailboxPreviewProvider::class) mailboxPreview: MailboxPreview
) {
    ProtonTheme {
        MailboxScreen(
            mailboxListItems = mailboxPreview.items.collectAsLazyPagingItems(),
            mailboxState = mailboxPreview.state,
            actions = MailboxScreen.Actions.Empty
        )
    }
}
