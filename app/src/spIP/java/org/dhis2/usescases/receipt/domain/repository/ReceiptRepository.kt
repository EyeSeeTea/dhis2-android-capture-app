package org.dhis2.usescases.receipt.domain.repository

import org.dhis2.usescases.receipt.domain.model.Receipt

interface ReceiptRepository {
    suspend fun getReceipt(eventUid: String): Receipt
}
