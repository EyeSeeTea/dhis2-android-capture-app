package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.eyeseetea.functional.Either
import kotlin.coroutines.cancellation.CancellationException

class BluetoothReceiptPrinter(
    private val transport: PrinterTransport,
    private val permission: BluetoothPermission,
    private val pairedDevicesReader: PairedDevicesReader,
    private val formatter: ReceiptFormatter = ReceiptFormatter(),
) : ReceiptPrinter {
    override suspend fun print(receipt: Receipt): Either<PrintReceiptError, Unit> {
        val lines = formatter.format(receipt)
        if (!permission.isGranted()) return Either.Left(PrintReceiptError.PermissionRequired)
        val printer = pairedDevicesReader.read().firstOrNull()
            ?: return Either.Left(PrintReceiptError.NoPrinterConnected)
        return try {
            transport.send(printer.address, lines)
            Either.Right(Unit)
        } catch (e: PrinterNotConnectedException) {
            Either.Left(PrintReceiptError.NoPrinterConnected)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Either.Left(PrintReceiptError.PrintFailed(e))
        }
    }
}
