package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.eyeseetea.functional.Either

class BluetoothReceiptPrinter(
    private val transport: PrinterTransport,
    private val formatter: ReceiptFormatter = ReceiptFormatter(),
) : ReceiptPrinter {
    override suspend fun print(receipt: Receipt): Either<PrintReceiptError, Unit> {
        transport.send(formatter.format(receipt))
        return Either.Right(Unit)
    }
}
