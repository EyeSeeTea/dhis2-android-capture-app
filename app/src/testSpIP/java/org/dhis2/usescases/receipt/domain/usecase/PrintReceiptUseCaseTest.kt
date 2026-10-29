package org.dhis2.usescases.receipt.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptLine
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrintReceiptUseCaseTest {

    @Test
    fun `printing a receipt sends it to the printer and returns success`() = runTest {
        val receipt = Receipt(
            lines = listOf(
                ReceiptLine(label = "Distribution point", value = "Point A"),
                ReceiptLine(label = "Household size", value = "5"),
            ),
        )
        val printer = FakePrinter()
        val useCase = PrintReceiptUseCase(
            receiptRepository = FakeReceiptRepository(eventUid = "EVENT_UID", receipt = receipt),
            receiptPrinter = printer,
        )

        val result = useCase("EVENT_UID")

        assertTrue(result.isSuccess)
        assertEquals(listOf(receipt), printer.printed)
    }

    private class FakePrinter : ReceiptPrinter {
        val printed = mutableListOf<Receipt>()

        override suspend fun print(receipt: Receipt) {
            printed.add(receipt)
        }
    }

    private class FakeReceiptRepository(
        private val eventUid: String,
        private val receipt: Receipt,
    ) : ReceiptRepository {
        override suspend fun getReceipt(eventUid: String): Receipt {
            check(eventUid == this.eventUid) { "Unexpected event $eventUid" }
            return receipt
        }
    }
}
