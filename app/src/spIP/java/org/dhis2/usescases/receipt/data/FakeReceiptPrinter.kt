package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.delay
import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.eyeseetea.functional.Either
import timber.log.Timber

// Development printer used until the real Bluetooth printer is integrated: it logs the receipt after
// a delay, so the printing dialog can be seen.
class FakeReceiptPrinter(
    private val formatter: ReceiptFormatter = ReceiptFormatter(),
    private val isConnected: Boolean = true,
    private val printingTimeMillis: Long = PRINTING_TIME_MILLIS,
) : ReceiptPrinter {
    override suspend fun print(receipt: Receipt): Either<PrintReceiptError, Unit> {
        if (!isConnected) return Either.Left(PrintReceiptError.NoPrinterConnected)
        delay(printingTimeMillis)
        Timber.i("Receipt printed:\n%s", formatter.format(receipt).joinToString("\n"))
        return Either.Right(Unit)
    }

    private companion object {
        const val PRINTING_TIME_MILLIS = 5_000L
    }
}
