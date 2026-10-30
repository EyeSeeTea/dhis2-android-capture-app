package org.dhis2.usescases.receipt.domain.usecase

import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.eyeseetea.functional.Either

// Returns an Either instead of implementing UseCase<R, T>: the expected outcomes are typed values, not exceptions.
class PrintReceiptUseCase(
    private val receiptRepository: ReceiptRepository,
    private val receiptPrinter: ReceiptPrinter,
) {
    suspend operator fun invoke(enrollmentUid: String): Either<PrintReceiptError, Unit> {
        val receipt = receiptRepository.getReceipt(enrollmentUid)
            ?: return Either.Left(PrintReceiptError.NoReceiptAvailable)
        return receiptPrinter.print(receipt)
    }
}
