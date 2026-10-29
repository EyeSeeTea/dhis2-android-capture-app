package org.dhis2.usescases.receipt.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository

class PrintReceiptUseCase(
    private val receiptRepository: ReceiptRepository,
    private val receiptPrinter: ReceiptPrinter,
) : UseCase<String, Unit> {
    override suspend fun invoke(input: String): Result<Unit> {
        receiptPrinter.print(receiptRepository.getReceipt(input))
        return Result.success(Unit)
    }
}
