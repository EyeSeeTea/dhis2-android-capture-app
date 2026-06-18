package org.dhis2.form.model.lotnumber

// EyeSeeTea customization - Lot Number Search Field

interface LotNumberRepository {
    suspend fun getLotNumbers(eventUid: String): LotNumbersResult

    suspend fun refreshCache()
}
