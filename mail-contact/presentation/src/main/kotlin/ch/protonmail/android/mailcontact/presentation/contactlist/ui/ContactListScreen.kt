package ch.protonmail.android.mailcontact.presentation.contactlist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListState
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListViewAction
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListViewModel
import ch.protonmail.android.mailcontact.presentation.utils.ContactFeatureFlags.ContactCreate
import ch.protonmail.android.mailcontact.presentation.upselling.ContactGroupsUpsellingBottomSheet
import ch.protonmail.android.mailupselling.presentation.model.BottomSheetVisibilityEffect
import ch.protonmail.android.mailupselling.presentation.ui.bottomsheet.UpsellingBottomSheet
import ch.protonmail.android.uicomponents.bottomsheet.bottomSheetHeightConstrainedContent
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.label.domain.entity.LabelId

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactListScreen(listActions: ContactListScreen.Actions, viewModel: ContactListViewModel = hiltViewModel()) {
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    val scope = rememberCoroutineScope()

    val state = viewModel.state.collectAsStateWithLifecycle().value

    val actions = listActions.copy(
        onNewGroupClick = { viewModel.submit(ContactListViewAction.OnNewContactGroupClick) }
    )

    if (bottomSheetState.currentValue != ModalBottomSheetValue.Hidden) {
        DisposableEffect(Unit) { onDispose { viewModel.submit(ContactListViewAction.OnDismissBottomSheet) } }
    }

    BackHandler(bottomSheetState.isVisible) {
        viewModel.submit(ContactListViewAction.OnDismissBottomSheet)
    }

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = bottomSheetHeightConstrainedContent {
            if (state is ContactListState.Loaded) {
                ConsumableLaunchedEffect(effect = state.bottomSheetVisibilityEffect) { bottomSheetEffect ->
                    when (bottomSheetEffect) {
                        BottomSheetVisibilityEffect.Hide -> scope.launch { bottomSheetState.hide() }
                        BottomSheetVisibilityEffect.Show -> scope.launch { bottomSheetState.show() }
                    }
                }

                when (state.bottomSheetType) {
                    ContactListState.BottomSheetType.Menu -> {
                        ContactBottomSheetContent(
                            isContactGroupsCrudEnabled = state.isContactGroupsCrudEnabled,
                            isContactGroupsUpsellingVisible = state.isContactGroupsUpsellingVisible,
                            actions = ContactBottomSheet.Actions(
                                onNewContactClick = {
                                    viewModel.submit(ContactListViewAction.OnNewContactClick)
                                },
                                onNewContactGroupClick = {
                                    viewModel.submit(ContactListViewAction.OnNewContactGroupClick)
                                },
                                onImportContactClick = {
                                    viewModel.submit(ContactListViewAction.OnImportContactClick)
                                }
                            )
                        )
                    }
                    ContactListState.BottomSheetType.Upselling -> {
                        ContactGroupsUpsellingBottomSheet(
                            actions = UpsellingBottomSheet.Actions.Empty.copy(
                                onDismiss = { viewModel.submit(ContactListViewAction.OnDismissBottomSheet) },
                                onUpgrade = { message -> actions.showNormSnackbar(message) },
                                onError = { message -> actions.showErrorSnackbar(message) }
                            )
                        )
                    }
                }
            }

        }
    ) {
        Scaffold(
            topBar = {
                ContactListTopBar(
                    actions = ContactListTopBar.Actions(
                        onBackClick = actions.onBackClick,
                        onAddClick = {
                            viewModel.submit(ContactListViewAction.OnOpenBottomSheet)
                        },
                        onSearchClick = {
                            viewModel.submit(ContactListViewAction.OnOpenContactSearch)
                        }
                    ),
                    isAddButtonVisible = state is ContactListState.Loaded.Data,
                    isContactGroupsCrudEnabled = state.isContactGroupsCrudEnabled,
                    isContactSearchEnabled = state.isContactSearchEnabled
                )
            },
            content = { paddingValues ->
                if (state is ContactListState.Loaded) {
                    ConsumableLaunchedEffect(effect = state.openContactForm) {
                        actions.onNavigateToNewContactForm()
                    }
                    ConsumableLaunchedEffect(effect = state.openContactGroupForm) {
                        actions.onNavigateToNewGroupForm()
                    }
                    ConsumableLaunchedEffect(effect = state.openImportContact) {
                        actions.openImportContact()
                    }
                    ConsumableLaunchedEffect(effect = state.openContactSearch) {
                        actions.onNavigateToContactSearch()
                    }
                }

                when (state) {
                    is ContactListState.Loaded.Data -> {
                        ContactTabLayout(
                            modifier = Modifier.padding(paddingValues),
                            scope = scope,
                            actions = actions,
                            state = state
                        )

                        ConsumableTextEffect(effect = state.subscriptionError) { message ->
                            actions.onSubscriptionUpgradeRequired(message)
                        }
                    }

                    is ContactListState.Loaded.Empty -> {
                        ContactEmptyDataScreen(
                            iconResId = R.drawable.ic_proton_users_plus,
                            title = stringResource(R.string.no_contacts),
                            description = stringResource(R.string.no_contacts_description),
                            buttonText = stringResource(R.string.add_contact),
                            showAddButton = ContactCreate.value,
                            onAddClick = { viewModel.submit(ContactListViewAction.OnOpenBottomSheet) }
                        )

                        ConsumableTextEffect(effect = state.subscriptionError) { message ->
                            actions.onSubscriptionUpgradeRequired(message)
                        }
                    }

                    is ContactListState.Loading -> {
                        ProtonCenteredProgress(modifier = Modifier.fillMaxSize())

                        ConsumableTextEffect(effect = state.errorLoading) { message ->
                            actions.exitWithErrorMessage(message)
                        }
                    }
                }
            }
        )
    }
}

object ContactListScreen {

    data class Actions(
        val onBackClick: () -> Unit,
        val onContactSelected: (ContactId) -> Unit,
        val onContactGroupSelected: (LabelId) -> Unit,
        val onNavigateToNewContactForm: () -> Unit,
        val onNavigateToNewGroupForm: () -> Unit,
        val onNavigateToContactSearch: () -> Unit,
        val onNewGroupClick: () -> Unit,
        val openImportContact: () -> Unit,
        val onSubscriptionUpgradeRequired: (String) -> Unit,
        val exitWithErrorMessage: (String) -> Unit,
        val showNormSnackbar: (String) -> Unit,
        val showErrorSnackbar: (String) -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onBackClick = {},
                onContactSelected = {},
                onContactGroupSelected = {},
                onNavigateToNewContactForm = {},
                onNavigateToNewGroupForm = {},
                onNavigateToContactSearch = {},
                openImportContact = {},
                onNewGroupClick = {},
                onSubscriptionUpgradeRequired = {},
                exitWithErrorMessage = {},
                showNormSnackbar = {},
                showErrorSnackbar = {}
            )

            fun fromContactSearchActions(
                onContactClick: (ContactId) -> Unit = {},
                onContactGroupClick: (LabelId) -> Unit = {}
            ) = Empty.copy(
                onContactSelected = onContactClick,
                onContactGroupSelected = onContactGroupClick
            )
        }
    }
}
