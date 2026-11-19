package org.dhis2.usescases.receipt.data

// Sends the lines of a receipt to a physical printer. Implementations hide how it is reached (Brother
// SDK, raw Bluetooth commands) so the receipt printer does not depend on that choice.
interface PrinterTransport {
    suspend fun send(lines: List<String>)
}
