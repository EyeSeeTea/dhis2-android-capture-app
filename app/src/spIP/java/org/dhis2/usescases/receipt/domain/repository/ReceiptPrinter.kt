package org.dhis2.usescases.receipt.domain.repository

import org.dhis2.usescases.receipt.domain.model.Receipt

interface ReceiptPrinter {
    suspend fun print(receipt: Receipt)
}
