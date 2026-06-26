package org.dhis2.form.data.lotnumber

// EyeSeeTea customization - Lot Number Search Field

import org.hisp.dhis.android.core.arch.api.HttpServiceClient

const val LOT_NUMBERS_DATASTORE_NAMESPACE = "openboxes-dhis2-sync"
const val LOT_NUMBERS_DATASTORE_KEY = "available-lot-numbers"

class LotNumbersApi(
    private val client: HttpServiceClient,
) {
    suspend fun getData(): LotNumbersApiResponse =
        client.get {
            url("dataStore/${LOT_NUMBERS_DATASTORE_NAMESPACE}/${LOT_NUMBERS_DATASTORE_KEY}")
        }
}
