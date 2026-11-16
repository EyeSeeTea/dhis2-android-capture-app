package org.dhis2.usescases.receipt.domain.model

sealed interface PrintReceiptError {
    data object NoReceiptAvailable : PrintReceiptError
    data object NoPrinterConnected : PrintReceiptError
    data object PrintFailed : PrintReceiptError
}
