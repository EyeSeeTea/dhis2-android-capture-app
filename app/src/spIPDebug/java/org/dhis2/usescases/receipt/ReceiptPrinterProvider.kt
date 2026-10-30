package org.dhis2.usescases.receipt

import org.dhis2.usescases.receipt.data.FakeReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter

// Debug builds log the receipt instead of printing it, until the real printer is integrated.
internal fun provideReceiptPrinter(): ReceiptPrinter = FakeReceiptPrinter()
