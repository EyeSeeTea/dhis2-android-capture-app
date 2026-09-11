package org.dhis2.mobile.sync.data

import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.sync.model.SyncJobStatus

interface SyncBackgroundJobAction {
    fun launchMetadataSync(syncingPeriod: Long)

    fun launchDataSync(syncingPeriod: Long)

    fun launchSyncSettings()

    // EyeSeeTea customization - Synced Data Retention Purge
    fun launchRetentionPurge(purgingPeriod: Long)

    fun observeMetadataJob(): Flow<List<SyncJobStatus>>

    fun observeDataJob(): Flow<List<SyncJobStatus>>

    // EyeSeeTea customization - Synced Data Retention Purge
    fun observeRetentionPurgeJob(): Flow<List<SyncJobStatus>>

    suspend fun cancelSyncSettings()

    suspend fun cancelMetadataSync()

    suspend fun cancelDataSync()

    // EyeSeeTea customization - Synced Data Retention Purge
    suspend fun cancelRetentionPurge()

    suspend fun cancelAll()

    fun getNextMetadataSync(): Long?

    fun getNextDataSync(): Long?

    fun getNextSettingsSync(): Long?
}
