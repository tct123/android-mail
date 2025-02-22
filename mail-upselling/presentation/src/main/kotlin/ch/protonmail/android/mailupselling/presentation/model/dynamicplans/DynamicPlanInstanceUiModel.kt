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

package ch.protonmail.android.mailupselling.presentation.model.dynamicplans

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.domain.model.telemetry.UpsellingTelemetryTargetPlanPayload
import ch.protonmail.android.mailupselling.presentation.model.UserIdUiModel
import me.proton.core.plan.domain.entity.DynamicPlan

data class DynamicPlanInstanceUiModel(
    val name: String,
    val userId: UserIdUiModel,
    val price: TextUiModel,
    val fullPrice: TextUiModel,
    val currency: String,
    val discount: Int?,
    val cycle: Int,
    val highlighted: Boolean,
    val viewId: Int,
    val dynamicPlan: DynamicPlan
)

internal fun DynamicPlanInstanceUiModel.isYearly() = this.cycle == YEARLY_CYCLE

internal fun DynamicPlanInstanceUiModel.toTelemetryPayload() =
    UpsellingTelemetryTargetPlanPayload(dynamicPlan.name ?: "", cycle)

private const val YEARLY_CYCLE = 12
