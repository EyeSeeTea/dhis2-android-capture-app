package org.dhis2.usescases.receipt.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.receipt.domain.model.PrinterError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptLine
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PrintReceiptUseCaseTest {

    private val receiptRepository: ReceiptRepository = mock()
    private val printer = FakePrinter()

    @Test
    fun `printing a receipt sends it to the printer and returns success`() = runTest {
        val receipt = givenAReceiptForEvent("EVENT_UID")
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase("EVENT_UID")

        assertTrue(result.isSuccess)
        assertEquals(listOf(receipt), printer.printed)
    }

    @Test
    fun `printing returns a no-printer-connected error when no printer is connected`() = runTest {
        givenAReceiptForEvent("EVENT_UID")
        givenAPrinterWithoutConnection()
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase("EVENT_UID")

        assertEquals(PrinterError.NoPrinterConnected, result.exceptionOrNull())
    }

    @Test
    fun `printing returns a print-failed error when the connected printer fails to print`() = runTest {
        givenAReceiptForEvent("EVENT_UID")
        givenAPrinterThatFailsToPrint()
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase("EVENT_UID")

        assertEquals(PrinterError.PrintFailed, result.exceptionOrNull())
    }

    private suspend fun givenAReceiptForEvent(eventUid: String): Receipt {
        val receipt = Receipt(
            lines = listOf(
                ReceiptLine(label = "Distribution point", value = "Point A"),
                ReceiptLine(label = "Household size", value = "5"),
            ),
        )
        whenever(receiptRepository.getReceipt(eventUid)).thenReturn(receipt)
        return receipt
    }

    private fun givenAPrinterWithoutConnection() {
        printer.failWith(PrinterError.NoPrinterConnected)
    }

    private fun givenAPrinterThatFailsToPrint() {
        printer.failWith(PrinterError.PrintFailed)
    }

    private fun givenAPrintReceiptUseCase() = PrintReceiptUseCase(receiptRepository, printer)

    private class FakePrinter : ReceiptPrinter {
        val printed = mutableListOf<Receipt>()
        private var error: PrinterError? = null

        fun failWith(error: PrinterError) {
            this.error = error
        }

        override suspend fun print(receipt: Receipt) {
            error?.let { throw it }
            printed.add(receipt)
        }
    }
}
