package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.sync.data.SyncRepository
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

// EyeSeeTea customization - Synced Data Retention Purge
class RetentionPurgeTest {
    private val syncRepository: SyncRepository = mock()

    private val retentionPurge = RetentionPurge(syncRepository)

    @Test
    fun `Should return success when repository purge succeeds`() =
        runBlocking {
            whenever(syncRepository.purgeRetention()).thenReturn(Result.success(Unit))

            val result = retentionPurge.invoke()

            verify(syncRepository).purgeRetention()
            assert(result.isSuccess)
        }

    @Test
    fun `Should return failure when repository purge fails`() =
        runBlocking {
            val error = Exception("purge failed")
            whenever(syncRepository.purgeRetention()).thenReturn(Result.failure(error))

            val result = retentionPurge.invoke()

            verify(syncRepository).purgeRetention()
            assert(result.isFailure)
        }
}
