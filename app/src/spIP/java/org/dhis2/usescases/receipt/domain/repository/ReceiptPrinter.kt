package org.dhis2.usescases.receipt.domain.repository

import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.eyeseetea.functional.Either

interface ReceiptPrinter {
    suspend fun print(receipt: Receipt): Either<PrintReceiptError, Unit>
}
