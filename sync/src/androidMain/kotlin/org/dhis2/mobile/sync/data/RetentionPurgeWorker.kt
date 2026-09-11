package org.dhis2.mobile.sync.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.notifications.NotificationManager
import org.dhis2.mobile.commons.notifications.WorkerNotificationInfo
import org.dhis2.mobile.sync.R
import org.dhis2.mobile.sync.domain.RetentionPurge
import org.dhis2.mobile.sync.resources.Res
import org.dhis2.mobile.sync.resources.app_name
import org.dhis2.mobile.sync.resources.purging_retention
import org.jetbrains.compose.resources.getString

// EyeSeeTea customization - Synced Data Retention Purge
class RetentionPurgeWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val retentionPurge: RetentionPurge,
    private val notificationManager: NotificationManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val isPeriodic = inputData.getBoolean(IS_PERIODIC, false)

        if (!isPeriodic) {
            setForeground(getForegroundInfo())
        }

        notificationManager.displayRetentionPurgeNotification(
            smallIcon = R.drawable.ic_sync,
            contentTitle = getString(Res.string.app_name),
            contentText = getString(Res.string.purging_retention),
        )

        val result = retentionPurge()

        if (!isPeriodic) {
            notificationManager.cancelRetentionPurgeNotification()
        }

        return when {
            result.isSuccess -> Result.success()
            else -> Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationModel =
            notificationManager.getRetentionPurgeNotification(
                smallIcon = R.drawable.ic_sync,
                contentTitle = getString(Res.string.app_name),
                contentText = getString(Res.string.purging_retention),
            )
        val notificationInfo =
            notificationModel as? WorkerNotificationInfo
                ?: throw IllegalStateException(
                    "Expected WorkerNotificationInfo but got ${notificationModel::class.qualifiedName}",
                )
        return notificationInfo.foregroundInfo
    }
}
