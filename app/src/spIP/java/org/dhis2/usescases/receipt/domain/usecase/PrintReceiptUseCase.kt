package org.dhis2.usescases.receipt.domain.usecase

import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.eyeseetea.functional.Either
import timber.log.Timber
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

// Returns an Either instead of implementing UseCase<R, T>: the expected outcomes are typed values, not exceptions.
class PrintReceiptUseCase(
    private val receiptRepository: ReceiptRepository,
    private val receiptPrinter: ReceiptPrinter,
    private val now: () -> Date = { Date() },
) {
    suspend operator fun invoke(eventUid: String): Either<PrintReceiptError, Unit> {
        val receipt = receiptRepository.getReceipt(eventUid)
        // The time is stamped on every print, so a reprint shows its own time.
        val stamped = receipt.copy(printedOn = now())
        val result = receiptPrinter.print(stamped)
        if (result is Either.Right) saveAsPrinted(stamped)
        return result
    }

    // Best effort: the receipt is already on paper, so a failure to save it as printed is logged and never
    // turns the success into an error. Cancellation is not swallowed.
    private suspend fun saveAsPrinted(receipt: Receipt) {
        try {
            receiptRepository.save(receipt.markAsPrinted())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Timber.w(error, "The receipt of event %s was printed but could not be saved as printed", receipt.eventUid)
        }
    }
}
