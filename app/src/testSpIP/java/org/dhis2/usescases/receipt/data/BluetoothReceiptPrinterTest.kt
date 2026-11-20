package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptItem
import org.eyeseetea.functional.Either
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.coroutines.cancellation.CancellationException

class BluetoothReceiptPrinterTest {

    private companion object {
        val PRINTER = PairedDevice(name = "RJ-2055WB-1234", address = "00:11:22:33:44:55")
    }

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
        assertEquals(listOf(Sent(PRINTER.address, ReceiptFormatter().format(receipt))), transport.sent)
    }

    @Test
    fun `printing without a connected printer fails with no printer connected`() = runTest {
        transport.failWith(PrinterNotConnectedException())
        val printer = givenABluetoothReceiptPrinter()

        val result = printer.print(Receipt(eventUid = "EVENT_UID"))

        assertEquals(Either.Left(PrintReceiptError.NoPrinterConnected), result)
    }

    @Test
    fun `any other failure of the transport fails with print failed carrying its cause`() = runTest {
        val cause = IllegalStateException("The printer ran out of paper")
        transport.failWith(cause)
        val printer = givenABluetoothReceiptPrinter()

        val result = printer.print(Receipt(eventUid = "EVENT_UID"))

        assertEquals(Either.Left(PrintReceiptError.PrintFailed(cause)), result)
    }

    @Test
    fun `a cancellation while printing is not reported as a print failure`() = runTest {
        transport.failWith(CancellationException("The screen was closed"))
        val printer = givenABluetoothReceiptPrinter()

        val error = runCatching { printer.print(Receipt(eventUid = "EVENT_UID")) }.exceptionOrNull()

        assertTrue(error is CancellationException)
    }

    @Test
    fun `printing without the Bluetooth permission fails with permission required and sends nothing`() = runTest {
        val printer = givenABluetoothReceiptPrinterWithoutThePermission()

        val result = printer.print(Receipt(eventUid = "EVENT_UID"))

        assertEquals(Either.Left(PrintReceiptError.PermissionRequired), result)
        assertTrue(transport.sent.isEmpty())
    }

    private fun givenABluetoothReceiptPrinter() =
        BluetoothReceiptPrinter(transport, bluetoothPermission(granted = true), pairedDevicesReader(PRINTER))

    private fun givenABluetoothReceiptPrinterWithoutThePermission() =
        BluetoothReceiptPrinter(transport, bluetoothPermission(granted = false), pairedDevicesReader(PRINTER))

    private fun bluetoothPermission(granted: Boolean) =
        mock<BluetoothPermission> { on { isGranted() } doReturn granted }

    private fun pairedDevicesReader(vararg devices: PairedDevice) = PairedDevicesReader { devices.toList() }

    private data class Sent(val address: String, val lines: List<String>)

    private class FakePrinterTransport : PrinterTransport {
        val sent = mutableListOf<Sent>()
        private var failure: Throwable? = null

        fun failWith(failure: Throwable) {
            this.failure = failure
        }

        override suspend fun send(address: String, lines: List<String>) {
            failure?.let { throw it }
            sent += Sent(address, lines)
        }
    }
}
