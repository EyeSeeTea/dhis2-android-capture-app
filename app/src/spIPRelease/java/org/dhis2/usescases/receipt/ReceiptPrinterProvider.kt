package org.dhis2.usescases.receipt

import org.dhis2.usescases.receipt.domain.model.PrinterError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter

// There is no real printer yet: release builds must never report a receipt as printed.
internal fun provideReceiptPrinter(): ReceiptPrinter = NoPrinterAvailable

private object NoPrinterAvailable : ReceiptPrinter {
    override suspend fun print(receipt: Receipt) = throw PrinterError.NoPrinterConnected
}
