package org.dhis2.usescases.receipt.data

// Sends the lines of a receipt to a physical printer. Implementations hide how it is reached (Brother
// SDK, raw Bluetooth commands) so the receipt printer does not depend on that choice.
interface PrinterTransport {
    // Throws PrinterNotConnectedException when the printer at the address cannot be reached; any other
    // failure is thrown as it comes.
    suspend fun send(address: String, lines: List<String>)
}

class PrinterNotConnectedException : Exception("There is no printer connected")
