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

package ch.protonmail.android.mailsettings.presentation.accountsettings.swipeactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailsettings.domain.usecase.ObserveSwipeActionsPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import javax.inject.Inject

@HiltViewModel
class SwipeActionsPreferenceViewModel @Inject constructor(
    private val observeSwipeActionsPreference: ObserveSwipeActionsPreference,
    private val swipeActionPreferenceUiModelMapper: SwipeActionPreferenceUiModelMapper
) : ViewModel() {

    val state: StateFlow<SwipeActionsPreferenceState> =
        observeState().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = SwipeActionsPreferenceState.Loading
        )

    private fun observeState(): Flow<SwipeActionsPreferenceState.Data> =
        observeSwipeActionsPreference()
            .map { SwipeActionsPreferenceState.Data(swipeActionPreferenceUiModelMapper.toUiModel(it)) }
}
