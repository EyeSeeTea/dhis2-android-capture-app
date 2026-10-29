package org.dhis2.usescases.receipt.domain.model

sealed class PrinterError : Exception() {
    data object NoPrinterConnected : PrinterError()
    data object PrintFailed : PrinterError()
}
