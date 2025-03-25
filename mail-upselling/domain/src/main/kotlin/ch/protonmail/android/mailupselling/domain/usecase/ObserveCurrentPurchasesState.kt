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

package ch.protonmail.android.mailupselling.domain.usecase

import ch.protonmail.android.mailupselling.domain.model.CurrentPurchaseStatus.FlowStatus
import ch.protonmail.android.mailupselling.domain.repository.CurrentPurchaseStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import javax.inject.Inject

internal class ObserveCurrentPurchasesState @Inject constructor(
    private val currentPurchaseStatusRepository: CurrentPurchaseStatusRepository,
    private val purchaseManager: PurchaseManager
) {

    operator fun invoke(sessionId: SessionId?): Flow<CurrentPurchasesState> {
        val purchaseEvents = purchaseManager.observePurchases().map { it.reduceToEvent(sessionId) }
            .distinctUntilChanged()
        val flowStatuses = currentPurchaseStatusRepository.observe()
            .mapNotNull { it.getOrNull()?.flowStatus }
            .distinctUntilChanged()

        return combine(purchaseEvents, flowStatuses) { purchaseEvent, flowStatus ->
            when (purchaseEvent) {
                CurrentPurchasesState.Pending,
                CurrentPurchasesState.Acknowledged,
                CurrentPurchasesState.Deleted -> purchaseEvent
                CurrentPurchasesState.NotApplicable -> if (flowStatus == FlowStatus.GiapSuccess) {
                    CurrentPurchasesState.Pending
                } else purchaseEvent
            }
        }
    }

    private fun List<Purchase>.reduceToEvent(sessionId: SessionId?): CurrentPurchasesState {
        val userIdPurchases = filter { it.sessionId == sessionId }
        if (userIdPurchases.isEmpty()) return CurrentPurchasesState.NotApplicable
        val hasPendingPurchases = userIdPurchases.any {
            it.purchaseState in listOf(
                PurchaseState.Pending,
                PurchaseState.Purchased,
                PurchaseState.Subscribed
            )
        }
        // Deleted with no failure.
        val hasDeletedPurchases = userIdPurchases.any {
            it.purchaseState == PurchaseState.Deleted && it.purchaseFailure == null
        }
        val hasAcknowledgedPurchases = userIdPurchases.any {
            it.purchaseState == PurchaseState.Acknowledged && it.planName == MAIL_PLUS_PLAN_NAME
        }
        return when {
            hasPendingPurchases -> CurrentPurchasesState.Pending
            hasAcknowledgedPurchases -> CurrentPurchasesState.Acknowledged
            hasDeletedPurchases -> CurrentPurchasesState.Deleted
            else -> CurrentPurchasesState.NotApplicable
        }
    }
}

private const val MAIL_PLUS_PLAN_NAME = "mail2022"

internal enum class CurrentPurchasesState { Pending, Acknowledged, Deleted, NotApplicable }
