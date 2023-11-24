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

package ch.protonmail.android.maillabel.presentation.folderform

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.compose.dismissKeyboard
import ch.protonmail.android.mailcommon.presentation.ui.CommonTestTags
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.getColorFromHexString
import ch.protonmail.android.maillabel.presentation.previewdata.FolderFormPreviewData.createFolderFormState
import ch.protonmail.android.maillabel.presentation.ui.ColorPicker
import ch.protonmail.android.maillabel.presentation.ui.FormInputField
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.theme.defaultStrongNorm

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FolderFormScreen(actions: FolderFormScreen.Actions, viewModel: FolderFormViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostErrorState = ProtonSnackbarHostState(defaultType = ProtonSnackbarType.ERROR)
    val state = rememberAsState(flow = viewModel.state, initial = FolderFormState.Loading(Effect.empty())).value

    val customActions = actions.copy(
        onFolderNameChanged = {
            viewModel.submit(FolderFormViewAction.FolderNameChanged(it))
        },
        onFolderColorChanged = {
            viewModel.submit(FolderFormViewAction.FolderColorChanged(it))
        },
        onFolderNotificationsChanged = {
            viewModel.submit(FolderFormViewAction.FolderNotificationsChanged(it))
        },
        onSaveClick = {
            dismissKeyboard(context, view, keyboardController)
            viewModel.submit(FolderFormViewAction.OnSaveClick)
        }
    )

    Scaffold(
        topBar = {
            FolderFormTopBar(
                state = state,
                onCloseFolderFormClick = {
                    viewModel.submit(FolderFormViewAction.OnCloseFolderFormClick)
                },
                onSaveFolderClick = {
                    viewModel.submit(FolderFormViewAction.OnSaveClick)
                }
            )
        },
        content = { paddingValues ->
            when (state) {
                is FolderFormState.Data -> {
                    FolderFormContent(
                        state = state,
                        actions = customActions,
                        modifier = Modifier.padding(paddingValues)
                    )

                    ConsumableLaunchedEffect(effect = state.closeWithSave) {
                        customActions.onBackClick()
                        actions.showFolderSavedSnackbar()
                    }
                    val folderAlreadyExistsMessage = stringResource(id = R.string.label_already_exists)
                    ConsumableLaunchedEffect(effect = state.showFolderAlreadyExistsSnackbar) {
                        snackbarHostErrorState.showSnackbar(
                            message = folderAlreadyExistsMessage,
                            type = ProtonSnackbarType.ERROR
                        )
                    }
                    val folderLimitReachedMessage = stringResource(id = R.string.folder_limit_reached_error)
                    ConsumableLaunchedEffect(effect = state.showFolderLimitReachedSnackbar) {
                        snackbarHostErrorState.showSnackbar(
                            message = folderLimitReachedMessage,
                            type = ProtonSnackbarType.ERROR
                        )
                    }
                    val saveFolderErrorMessage = stringResource(id = R.string.save_folder_error)
                    ConsumableLaunchedEffect(effect = state.showSaveFolderErrorSnackbar) {
                        snackbarHostErrorState.showSnackbar(
                            message = saveFolderErrorMessage,
                            type = ProtonSnackbarType.ERROR
                        )
                    }
                }
                is FolderFormState.Loading -> {
                    ProtonCenteredProgress(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )
                }
            }
        },
        snackbarHost = {
            ProtonSnackbarHost(
                modifier = Modifier.testTag(CommonTestTags.SnackbarHostError),
                hostState = snackbarHostErrorState
            )
        }
    )

    ConsumableLaunchedEffect(effect = state.close) {
        dismissKeyboard(context, view, keyboardController)
        customActions.onBackClick()
    }
}

@Composable
fun FolderFormContent(
    state: FolderFormState.Data,
    actions: FolderFormScreen.Actions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        FormInputField(
            initialValue = state.name,
            title = stringResource(R.string.label_name_title),
            hint = stringResource(R.string.add_a_folder_name_hint),
            onTextChange = {
                actions.onFolderNameChanged(it)
            }
        )
        Divider()
        FolderFormParentFolderField(state, actions)
        ProtonSettingsToggleItem(
            name = stringResource(id = R.string.folder_form_notifications),
            hint = stringResource(id = R.string.folder_form_no_parent),
            value = state.notifications,
            onToggle = {
                actions.onFolderNotificationsChanged(it)
            }
        )
        Divider()
        ColorPicker(
            colors = state.colorList,
            selectedColor = state.color.getColorFromHexString(),
            iconResId = R.drawable.ic_proton_folder_filled,
            onColorClicked = {
                actions.onFolderColorChanged(it)
            }
        )
    }
}

@Composable
fun FolderFormParentFolderField(state: FolderFormState.Data, actions: FolderFormScreen.Actions) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(R.string.folder_form_parent),
                role = Role.Button,
                onClick = actions.onFolderParentClick
            )
    ) {
        Text(
            text = stringResource(id = R.string.folder_form_parent),
            modifier = Modifier.padding(
                top = ProtonDimens.DefaultSpacing,
                start = ProtonDimens.DefaultSpacing
            ),
            style = ProtonTheme.typography.defaultNorm
        )
        Text(
            modifier = Modifier.padding(
                start = ProtonDimens.DefaultSpacing,
                bottom = ProtonDimens.DefaultSpacing
            ),
            text = state.parent?.name ?: stringResource(id = R.string.folder_form_no_parent),
            color = ProtonTheme.colors.textHint,
            style = ProtonTheme.typography.defaultSmallWeak
        )
    }
    Divider()
}

@Composable
fun FolderFormTopBar(
    state: FolderFormState,
    onCloseFolderFormClick: () -> Unit,
    onSaveFolderClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            val title = when (state) {
                is FolderFormState.Data.Create -> stringResource(id = R.string.folder_form_create_folder)
                is FolderFormState.Loading -> ""
            }
            Text(text = title)
        },
        navigationIcon = {
            IconButton(onClick = onCloseFolderFormClick) {
                Icon(
                    tint = ProtonTheme.colors.iconNorm,
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.presentation_close)
                )
            }
        },
        actions = {
            ProtonTextButton(
                onClick = onSaveFolderClick,
                enabled = state.isSaveEnabled
            ) {
                val textColor =
                    if (state.isSaveEnabled) ProtonTheme.colors.textAccent
                    else ProtonTheme.colors.interactionDisabled
                Text(
                    text = stringResource(id = R.string.label_form_save),
                    color = textColor,
                    style = ProtonTheme.typography.defaultStrongNorm
                )
            }
        }
    )
}

object FolderFormScreen {

    data class Actions(
        val onBackClick: () -> Unit,
        val showFolderSavedSnackbar: () -> Unit,
        val onFolderNameChanged: (String) -> Unit,
        val onFolderColorChanged: (Color) -> Unit,
        val onFolderNotificationsChanged: (Boolean) -> Unit,
        val onFolderParentClick: () -> Unit,
        val onSaveClick: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onBackClick = {},
                showFolderSavedSnackbar = {},
                onFolderNameChanged = {},
                onFolderColorChanged = {},
                onFolderNotificationsChanged = {},
                onFolderParentClick = {},
                onSaveClick = {}
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun CreateFolderFormScreenPreview() {
    FolderFormContent(
        state = createFolderFormState,
        actions = FolderFormScreen.Actions.Empty
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun CreateFolderFormTopBarPreview() {
    FolderFormTopBar(
        state = createFolderFormState,
        onCloseFolderFormClick = {},
        onSaveFolderClick = {}
    )
}
