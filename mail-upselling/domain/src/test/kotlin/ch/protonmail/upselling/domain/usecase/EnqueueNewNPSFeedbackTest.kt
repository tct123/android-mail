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

package ch.protonmail.upselling.domain.usecase

import java.time.Instant
import java.time.ZoneId
import java.util.TimeZone
import ch.protonmail.android.mailcommon.domain.sample.UserSample
import ch.protonmail.android.mailupselling.domain.repository.GetInstalledProtonApps
import ch.protonmail.android.mailupselling.domain.repository.InstalledProtonApps
import ch.protonmail.android.mailupselling.domain.repository.NPSFeedbackRepository
import ch.protonmail.android.mailupselling.domain.usecase.EnqueueNewNPSFeedback
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.usecase.GetPrimaryUser
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class EnqueueNewNPSFeedbackTest {

    private val getPrimaryUser = mockk<GetPrimaryUser>()
    private val dispatcherProvider = TestDispatcherProvider(UnconfinedTestDispatcher())
    private val scopeProvider = TestCoroutineScopeProvider(dispatcherProvider)
    private val getInstalledProtonApps = mockk<GetInstalledProtonApps>()
    private val repo = mockk<NPSFeedbackRepository>()

    private val timezone = TimeZone.getTimeZone(ZoneId.of("GMT"))

    private val sut: EnqueueNewNPSFeedback
        get() = EnqueueNewNPSFeedback(
            getPrimaryUser = getPrimaryUser,
            scopeProvider = scopeProvider,
            repo = repo,
            getInstalledProtonApps = getInstalledProtonApps
        )

    private val user = UserSample.Primary

    @BeforeTest
    fun setup() {
        mockTimes()
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should not track events when subscription user is not found`() {
        // Given
        coEvery { getPrimaryUser() } returns null

        // When
        sut.skip()

        // Then
        verify(exactly = 0) {
            repo.enqueue(
                userId = any(),
                ratingValue = any(),
                comment = any(),
                skipped = any(),
                installedProtonApps = any()
            )
        }
    }

    @Test
    fun `should track skipped event`() = runTest {
        // Given
        coEvery { getPrimaryUser() } returns user
        every { getInstalledProtonApps() } returns InstalledProtonApps(emptyList())

        // When
        sut.skip()

        // Then

        verify(exactly = 1) {
            repo.enqueue(
                userId = user.userId,
                ratingValue = null,
                comment = null,
                skipped = true,
                installedProtonApps = InstalledProtonApps(emptyList())
            )
        }
    }

    @Test
    fun `should track submit tap event without comment`() = runTest {
        // Given
        val installedApps = InstalledProtonApps(listOf(InstalledProtonApps.AppInfo("pn1", "v1")))
        coEvery { getPrimaryUser() } returns user
        every { getInstalledProtonApps() } returns installedApps

        val ratingValue = 4

        // When
        sut.submit(ratingValue, null)

        // Then
        verify(exactly = 1) {
            repo.enqueue(
                userId = user.userId,
                ratingValue = ratingValue,
                comment = null,
                skipped = false,
                installedProtonApps = installedApps
            )
        }
    }

    @Test
    fun `should track submit tap event with comment`() = runTest {
        // Given
        val installedApps = InstalledProtonApps(
            listOf(
                InstalledProtonApps.AppInfo("pn1", "v1"),
                InstalledProtonApps.AppInfo("pn12", "v2")
            )
        )
        coEvery { getPrimaryUser() } returns user
        every { getInstalledProtonApps() } returns installedApps

        val ratingValue = 10
        val comment = "Great app!"

        // When
        sut.submit(ratingValue, comment)

        // Then
        verify(exactly = 1) {
            repo.enqueue(
                userId = user.userId,
                ratingValue = ratingValue,
                comment = comment,
                skipped = false,
                installedProtonApps = installedApps
            )
        }
    }

    private fun mockTimes() {
        mockkStatic(Instant::class)
        mockkStatic(TimeZone::class)
        every { Instant.now() } returns mockk { every { epochSecond } returns 0 }
        every { TimeZone.getDefault() } returns timezone
    }
}
