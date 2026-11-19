package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.test.runTest
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

    private fun givenABluetoothReceiptPrinter() = BluetoothReceiptPrinter(transport)

    private class FakePrinterTransport : PrinterTransport {
        val sent = mutableListOf<List<String>>()

        override suspend fun send(lines: List<String>) {
            sent += lines
        }
    }
}
