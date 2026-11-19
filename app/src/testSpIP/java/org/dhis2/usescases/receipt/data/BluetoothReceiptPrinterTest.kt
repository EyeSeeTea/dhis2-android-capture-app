package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptItem
import org.eyeseetea.functional.Either
import org.junit.Assert.assertEquals
import org.junit.Test

class BluetoothReceiptPrinterTest {

    private val transport = FakePrinterTransport()

    @Test
    fun `printing a receipt sends its formatted text to the transport and succeeds`() = runTest {
        val receipt = Receipt(
            eventUid = "EVENT_UID",
            distributionName = "TEST Distribution 1",
            items = listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")),
        )
        val printer = givenABluetoothReceiptPrinter()

        val result = printer.print(receipt)

        assertEquals(Either.Right(Unit), result)
        assertEquals(listOf(ReceiptFormatter().format(receipt)), transport.sent)
    }

    @Test
    fun `printing without a connected printer fails with no printer connected`() = runTest {
        transport.failWith(PrinterNotConnectedException())
        val printer = givenABluetoothReceiptPrinter()

        val result = printer.print(Receipt(eventUid = "EVENT_UID"))

        assertEquals(Either.Left(PrintReceiptError.NoPrinterConnected), result)
    }

    private fun givenABluetoothReceiptPrinter() = BluetoothReceiptPrinter(transport)

    private class FakePrinterTransport : PrinterTransport {
        val sent = mutableListOf<List<String>>()
        private var failure: Throwable? = null

        fun failWith(failure: Throwable) {
            this.failure = failure
        }

        override suspend fun send(lines: List<String>) {
            failure?.let { throw it }
            sent += lines
        }
    }
}
