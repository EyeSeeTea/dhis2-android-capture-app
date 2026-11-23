package org.dhis2.usescases.receipt.domain.repository

import org.dhis2.usescases.receipt.domain.model.Receipt

interface ReceiptRepository {
    suspend fun getReceipt(eventUid: String): Receipt

    // Persists only the print status of the receipt: the rest of its content is derived from the event and the
    // tracked entity, so changing it here has no effect.
    suspend fun save(receipt: Receipt)
}
