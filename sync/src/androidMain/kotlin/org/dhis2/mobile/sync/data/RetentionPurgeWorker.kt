package org.dhis2.mobile.sync.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.sync.domain.RetentionPurge

// EyeSeeTea customization - Synced Data Retention Purge
class RetentionPurgeWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val retentionPurge: RetentionPurge,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val result = retentionPurge()

        return when {
            result.isSuccess -> Result.success()
            else -> Result.failure()
        }
    }
}
