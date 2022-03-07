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

package ch.protonmail.android.mailsettings.domain.testdata

import me.proton.core.user.domain.entity.Delinquent.None
import me.proton.core.user.domain.entity.Role.NoOrganization
import me.proton.core.user.domain.entity.User

object UserTestData {

    const val USER_EMAIL_RAW = "userEmail"
    const val USER_DISPLAY_NAME_RAW = "userDisplayName"

    val user = User(
        userId = UserIdTestData.userId,
        email = USER_EMAIL_RAW,
        displayName = USER_DISPLAY_NAME_RAW,
        name = null,
        currency = "CHF",
        credit = 1,
        usedSpace = 1,
        maxSpace = 1,
        maxUpload = 1,
        role = NoOrganization,
        private = true,
        services = 1,
        subscribed = 1,
        delinquent = None,
        keys = emptyList()
    )
}
