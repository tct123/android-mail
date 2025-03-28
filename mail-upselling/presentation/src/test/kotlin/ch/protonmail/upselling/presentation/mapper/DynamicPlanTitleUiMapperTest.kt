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

package ch.protonmail.upselling.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.mapper.DynamicPlanTitleUiMapper
import ch.protonmail.android.mailupselling.presentation.model.dynamicplans.DynamicPlanTitleUiModel
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("MaxLineLength")
internal class DynamicPlanTitleUiMapperTest {

    private val mapper = DynamicPlanTitleUiMapper()

    @Test
    fun `should map to the corresponding ui models`() {
        // Given
        val expected = mapOf(
            UpsellingEntryPoint.Feature.ContactGroups to DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_contact_groups_plus_title)),
            UpsellingEntryPoint.Feature.Folders to DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_folders_plus_title)),
            UpsellingEntryPoint.Feature.Labels to DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_labels_plus_title)),
            UpsellingEntryPoint.Feature.MobileSignature to DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_mobile_signature_plus_title)),
            UpsellingEntryPoint.Feature.Mailbox to DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_mailbox_plus_title)),
            UpsellingEntryPoint.Feature.AutoDelete to DynamicPlanTitleUiModel(TextUiModel(R.string.upselling_auto_delete_plus_title))
        )

        // When
        val actual = mapOf(
            UpsellingEntryPoint.Feature.ContactGroups to mapper.toUiModel(UpsellingEntryPoint.Feature.ContactGroups),
            UpsellingEntryPoint.Feature.Folders to mapper.toUiModel(UpsellingEntryPoint.Feature.Folders),
            UpsellingEntryPoint.Feature.Labels to mapper.toUiModel(UpsellingEntryPoint.Feature.Labels),
            UpsellingEntryPoint.Feature.MobileSignature to mapper.toUiModel(UpsellingEntryPoint.Feature.MobileSignature),
            UpsellingEntryPoint.Feature.Mailbox to mapper.toUiModel(UpsellingEntryPoint.Feature.Mailbox),
            UpsellingEntryPoint.Feature.AutoDelete to mapper.toUiModel(UpsellingEntryPoint.Feature.AutoDelete)
        )

        // Then
        for (pair in expected) {
            assertEquals(pair.value, actual[pair.key])
        }
    }
}
