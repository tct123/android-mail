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

package ch.protonmail.android.mailcommon.data.worker

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import javax.inject.Inject

class Enqueuer @Inject constructor(private val workManager: WorkManager) {

    inline fun <reified T : ListenableWorker> enqueue(params: Map<String, Any>) {
        enqueue(T::class.java, params)
    }

    inline fun <reified T : ListenableWorker> enqueueUniqueWork(
        workerId: String,
        params: Map<String, Any>,
        constraints: Constraints? = buildDefaultConstraints(),
        existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
        enqueueUniqueWork(workerId, T::class.java, params, constraints, existingWorkPolicy)
    }

    inline fun <reified T : ListenableWorker, reified K : ListenableWorker> enqueueInChain(
        uniqueWorkId: String,
        params1: Map<String, Any>,
        params2: Map<String, Any>,
        constraints: Constraints = buildDefaultConstraints(),
        existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
        enqueueInChain(
            uniqueWorkId,
            T::class.java,
            params1,
            K::class.java,
            params2,
            constraints,
            existingWorkPolicy
        )
    }

    inline fun <
        reified T : ListenableWorker,
        reified K : ListenableWorker,
        reified R : ListenableWorker
        > enqueueInChain(
        uniqueWorkId: String,
        params1: Map<String, Any>,
        params2: Map<String, Any>,
        params3: Map<String, Any>,
        constraints: Constraints = buildDefaultConstraints(),
        existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
        enqueueInChain(
            uniqueWorkId,
            T::class.java,
            params1,
            K::class.java,
            params2,
            R::class.java,
            params3,
            constraints,
            existingWorkPolicy
        )
    }

    fun enqueue(worker: Class<out ListenableWorker>, params: Map<String, Any>) {
        workManager.enqueue(createRequest(worker, params, buildDefaultConstraints()))
    }

    @Suppress("LongParameterList")
    fun enqueueInChain(
        uniqueWorkId: String,
        worker1: Class<out ListenableWorker>,
        params1: Map<String, Any>,
        worker2: Class<out ListenableWorker>,
        params2: Map<String, Any>,
        constraints: Constraints,
        existingWorkPolicy: ExistingWorkPolicy
    ) {
        workManager.beginUniqueWork(
            uniqueWorkId,
            existingWorkPolicy,
            createRequest(worker1, params1, constraints)
        )
            .then(createRequest(worker2, params2, constraints))
            .enqueue()
    }

    @Suppress("LongParameterList")
    fun enqueueInChain(
        uniqueWorkId: String,
        worker1: Class<out ListenableWorker>,
        params1: Map<String, Any>,
        worker2: Class<out ListenableWorker>,
        params2: Map<String, Any>,
        worker3: Class<out ListenableWorker>,
        params3: Map<String, Any>,
        constraints: Constraints,
        existingWorkPolicy: ExistingWorkPolicy
    ) {
        workManager.beginUniqueWork(
            uniqueWorkId,
            existingWorkPolicy,
            createRequest(worker1, params1, constraints)
        )
            .then(createRequest(worker2, params2, constraints))
            .then(createRequest(worker3, params3, constraints))
            .enqueue()
    }

    fun enqueueUniqueWork(
        workerId: String,
        worker: Class<out ListenableWorker>,
        params: Map<String, Any>,
        constraints: Constraints?,
        existingWorkPolicy: ExistingWorkPolicy
    ) {
        workManager.enqueueUniqueWork(workerId, existingWorkPolicy, createRequest(worker, params, constraints))
    }

    private fun createRequest(
        worker: Class<out ListenableWorker>,
        params: Map<String, Any>,
        constraints: Constraints?
    ): OneTimeWorkRequest {

        val data = workDataOf(*params.map { Pair(it.key, it.value) }.toTypedArray())

        return OneTimeWorkRequest.Builder(worker).run {
            setInputData(data)
            if (constraints != null) setConstraints(constraints)
            build()
        }
    }

    fun buildDefaultConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
