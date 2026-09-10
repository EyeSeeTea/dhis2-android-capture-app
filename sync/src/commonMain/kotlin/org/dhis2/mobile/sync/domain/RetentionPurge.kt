package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncRepository

class RetentionPurge(
    private val repository: SyncRepository,
) : UseCase<Unit, Unit> {
    override suspend fun invoke(input: Unit): Result<Unit> = repository.purgeRetention()
}
