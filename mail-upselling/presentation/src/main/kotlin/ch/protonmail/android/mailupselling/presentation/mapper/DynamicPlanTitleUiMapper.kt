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

package ch.protonmail.android.mailupselling.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.dynamicplans.DynamicPlanTitleUiModel
import javax.inject.Inject

internal class DynamicPlanTitleUiMapper @Inject constructor() {

    @Suppress("MaxLineLength")
    fun toUiModel(upsellingEntryPoint: UpsellingEntryPoint.Feature): DynamicPlanTitleUiModel =
        when (upsellingEntryPoint) {
            UpsellingEntryPoint.Feature.ContactGroups -> DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_contact_groups_plus_title))
            UpsellingEntryPoint.Feature.Folders -> DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_folders_plus_title))
            UpsellingEntryPoint.Feature.Labels -> DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_labels_plus_title))
            UpsellingEntryPoint.Feature.MobileSignature -> DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_mobile_signature_plus_title))
            UpsellingEntryPoint.Feature.Mailbox,
            UpsellingEntryPoint.Feature.Navbar -> DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_mailbox_plus_title))
            UpsellingEntryPoint.Feature.AutoDelete -> DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_auto_delete_plus_title))
        }
}
