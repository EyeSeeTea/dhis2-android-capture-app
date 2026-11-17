package org.dhis2.usescases.receipt.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptItem
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.eyeseetea.functional.Either
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class PrintReceiptUseCaseTest {

    private val receiptRepository = FakeReceiptRepository()
    private val printer = FakePrinter()

    @Test
    fun `printing a receipt sends it to the printer and succeeds`() = runTest {
        val receipt = givenAReceiptForEvent("EVENT_UID")
        val printTime = Date(1_000L)
        val useCase = givenAPrintReceiptUseCase(now = { printTime })

        val result = useCase(eventUid = "EVENT_UID")

        assertEquals(Either.Right(Unit), result)
        assertEquals(listOf(receipt.copy(printedOn = printTime)), printer.printed)
    }

    @Test
    fun `each print stamps the receipt with the time of that print so a reprint shows its own time`() = runTest {
        givenAReceiptForEvent("EVENT_UID")
        val printTimes = ArrayDeque(listOf(Date(1_000L), Date(2_000L)))
        val useCase = givenAPrintReceiptUseCase(now = { printTimes.removeFirst() })

        useCase(eventUid = "EVENT_UID")
        useCase(eventUid = "EVENT_UID")

        assertEquals(listOf(Date(1_000L), Date(2_000L)), printer.printed.map { it.printedOn })
    }

    @Test
    fun `a successful print saves the receipt as printed`() = runTest {
        givenAReceiptForEvent("EVENT_UID")
        val useCase = givenAPrintReceiptUseCase()

        useCase(eventUid = "EVENT_UID")

        assertEquals(listOf("EVENT_UID"), receiptRepository.saved.filter { it.printed }.map { it.eventUid })
    }

    @Test
    fun `printing still succeeds when the receipt cannot be saved as printed`() = runTest {
        givenAReceiptForEvent("EVENT_UID")
        receiptRepository.failSavingWith(IllegalStateException("The event cannot be edited"))
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase(eventUid = "EVENT_UID")

        assertEquals(Either.Right(Unit), result)
    }

    @Test
    fun `a failed print saves nothing`() = runTest {
        givenAReceiptForEvent("EVENT_UID")
        givenAPrinterThatFailsToPrint()
        val useCase = givenAPrintReceiptUseCase()

        useCase(eventUid = "EVENT_UID")

        assertTrue(receiptRepository.saved.isEmpty())
    }

    @Test
    fun `printing returns a no-printer-connected error when no printer is connected`() = runTest {
        givenAReceiptForEvent("EVENT_UID")
        givenAPrinterWithoutConnection()
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase(eventUid = "EVENT_UID")

        assertEquals(Either.Left(PrintReceiptError.NoPrinterConnected), result)
    }

    @Test
    fun `printing returns a print-failed error when the connected printer fails to print`() = runTest {
        givenAReceiptForEvent("EVENT_UID")
        givenAPrinterThatFailsToPrint()
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase(eventUid = "EVENT_UID")

        assertEquals(Either.Left(PrintReceiptError.PrintFailed), result)
    }

    @Test
    fun `printing returns a no-receipt-available error and prints nothing when there is nothing to print`() = runTest {
        val useCase = givenAPrintReceiptUseCase()

        val result = useCase(eventUid = "EVENT_UID")

        assertEquals(Either.Left(PrintReceiptError.NoReceiptAvailable), result)
        assertTrue(printer.printed.isEmpty())
    }

    private fun givenAReceiptForEvent(eventUid: String): Receipt {
        val receipt = Receipt(
            eventUid = eventUid,
            distributionName = "TEST Distribution 1",
            items = listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")),
        )
        receiptRepository.add(receipt)
        return receipt
    }

    private fun givenAPrinterWithoutConnection() {
        printer.failWith(PrintReceiptError.NoPrinterConnected)
    }

    private fun givenAPrinterThatFailsToPrint() {
        printer.failWith(PrintReceiptError.PrintFailed)
    }

    private fun givenAPrintReceiptUseCase(now: () -> Date = { Date(0L) }) =
        PrintReceiptUseCase(receiptRepository, printer, now)

    private class FakeReceiptRepository : ReceiptRepository {
        private val receipts = mutableMapOf<String, Receipt>()
        val saved = mutableListOf<Receipt>()
        private var saveError: Throwable? = null

        fun failSavingWith(error: Throwable) {
            saveError = error
        }

        fun add(receipt: Receipt) {
            receipts[receipt.eventUid] = receipt
        }

        override suspend fun getReceipt(eventUid: String): Receipt? = receipts[eventUid]

        override suspend fun save(receipt: Receipt) {
            saveError?.let { throw it }
            saved.add(receipt)
        }
    }

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
