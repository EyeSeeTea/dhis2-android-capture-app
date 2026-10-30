package org.dhis2.usescases.receipt.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptLine
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.eyeseetea.functional.Either
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PrintReceiptUseCaseTest {

    private val receiptRepository: ReceiptRepository = mock()
    private val printer = FakePrinter()

    @Test
    fun `printing a receipt sends it to the printer and succeeds`() = runTest {
        val receipt = givenAReceiptForEnrollment("ENROLLMENT_UID")
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase("ENROLLMENT_UID")

        assertEquals(Either.Right(Unit), result)
        assertEquals(listOf(receipt), printer.printed)
    }

    @Test
    fun `printing returns a no-printer-connected error when no printer is connected`() = runTest {
        givenAReceiptForEnrollment("ENROLLMENT_UID")
        givenAPrinterWithoutConnection()
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase("ENROLLMENT_UID")

        assertEquals(Either.Left(PrintReceiptError.NoPrinterConnected), result)
    }

    @Test
    fun `printing returns a print-failed error when the connected printer fails to print`() = runTest {
        givenAReceiptForEnrollment("ENROLLMENT_UID")
        givenAPrinterThatFailsToPrint()
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase("ENROLLMENT_UID")

        assertEquals(Either.Left(PrintReceiptError.PrintFailed), result)
    }

    @Test
    fun `printing returns a no-receipt-available error and prints nothing when there is nothing to print`() = runTest {
        givenNoReceiptForEnrollment("ENROLLMENT_UID")
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase("ENROLLMENT_UID")

        assertEquals(Either.Left(PrintReceiptError.NoReceiptAvailable), result)
        assertTrue(printer.printed.isEmpty())
    }

    private suspend fun givenAReceiptForEnrollment(enrollmentUid: String): Receipt {
        val receipt = Receipt(
            lines = listOf(
                ReceiptLine(label = "Distribution point", value = "Point A"),
                ReceiptLine(label = "Household size", value = "5"),
            ),
        )
        whenever(receiptRepository.getReceipt(enrollmentUid)).thenReturn(receipt)
        return receipt
    }

    private suspend fun givenNoReceiptForEnrollment(enrollmentUid: String) {
        whenever(receiptRepository.getReceipt(enrollmentUid)).thenReturn(null)
    }

    private fun givenAPrinterWithoutConnection() {
        printer.failWith(PrintReceiptError.NoPrinterConnected)
    }

    private fun givenAPrinterThatFailsToPrint() {
        printer.failWith(PrintReceiptError.PrintFailed)
    }

    private fun givenAPrintReceiptUseCase() = PrintReceiptUseCase(receiptRepository, printer)

    private class FakePrinter : ReceiptPrinter {
        val printed = mutableListOf<Receipt>()
        private var error: PrintReceiptError? = null

        fun failWith(error: PrintReceiptError) {
            this.error = error
        }

        override suspend fun print(receipt: Receipt): Either<PrintReceiptError, Unit> {
            error?.let { return Either.Left(it) }
            printed.add(receipt)
            return Either.Right(Unit)
        }
    }
}
