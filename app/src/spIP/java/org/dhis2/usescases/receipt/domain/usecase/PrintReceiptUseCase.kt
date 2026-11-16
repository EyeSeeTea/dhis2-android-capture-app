package org.dhis2.usescases.receipt.domain.usecase

import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.eyeseetea.functional.Either
import java.util.Date

// Returns an Either instead of implementing UseCase<R, T>: the expected outcomes are typed values, not exceptions.
class PrintReceiptUseCase(
    private val receiptRepository: ReceiptRepository,
    private val receiptPrinter: ReceiptPrinter,
    private val now: () -> Date = { Date() },
) {
    suspend operator fun invoke(eventUid: String): Either<PrintReceiptError, Unit> {
        val receipt = receiptRepository.getReceipt(eventUid)
            ?: return Either.Left(PrintReceiptError.NoReceiptAvailable)
        // The time is stamped on every print, so a reprint shows its own time.
        return receiptPrinter.print(receipt.copy(printedOn = now()))
    }
}
