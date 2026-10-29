package org.dhis2.usescases.receipt.presentation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CompletableDeferred
import org.dhis2.R
import org.dhis2.usescases.receipt.domain.model.PrinterError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptLine
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.dhis2.usescases.receipt.domain.usecase.PrintReceiptUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class ReceiptPrintHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val receipt = Receipt(lines = listOf(ReceiptLine(label = "Household size", value = "5")))
    private val receiptPrinter = GatedReceiptPrinter()

    @Test
    fun printingShowsTheDialogWhileThePrinterWorksAndThenThePrintedConfirmation() {
        givenAReceiptPrintHost()

        composeRule.onNodeWithText(PRINT_BUTTON).performClick()

        composeRule.onNodeWithText(context.getString(R.string.receipt_printing)).assertIsDisplayed()

        receiptPrinter.finishPrinting()

        composeRule.waitUntilDoesNotExist(hasText(context.getString(R.string.receipt_printing)))
        composeRule.onNodeWithText(context.getString(R.string.receipt_printed)).assertIsDisplayed()
        assertEquals(listOf(receipt), receiptPrinter.printed)
    }

    @Test
    fun aNoPrinterConnectedErrorStaysOnScreenUntilItIsAcceptedAndNothingIsPrinted() {
        val noPrinterConnected = context.getString(R.string.receipt_no_printer_connected)
        receiptPrinter.failWith(PrinterError.NoPrinterConnected)
        givenAReceiptPrintHost()

        composeRule.onNodeWithText(PRINT_BUTTON).performClick()

        composeRule.waitUntilExactlyOneExists(hasText(noPrinterConnected))
        composeRule.mainClock.advanceTimeBy(LONGER_THAN_A_SNACKBAR_MILLIS)
        composeRule.onNodeWithText(noPrinterConnected).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.receipt_error_accept)).performClick()

        composeRule.onNodeWithText(noPrinterConnected).assertDoesNotExist()
        assertTrue(receiptPrinter.printed.isEmpty())
    }

    private fun givenAReceiptPrintHost() {
        val receiptPrintViewModel = ReceiptPrintViewModel(
            PrintReceiptUseCase(FakeReceiptRepository(receipt), receiptPrinter),
        )
        composeRule.setContent {
            ReceiptPrintHost(receiptPrintViewModel)
            Button(onClick = { receiptPrintViewModel.print(EVENT_UID) }) { Text(PRINT_BUTTON) }
        }
    }

    private class FakeReceiptRepository(private val receipt: Receipt) : ReceiptRepository {
        override suspend fun getReceipt(eventUid: String): Receipt = receipt
    }

    private class GatedReceiptPrinter : ReceiptPrinter {
        private val gate = CompletableDeferred<Unit>()
        private var error: PrinterError? = null
        val printed = mutableListOf<Receipt>()

        fun finishPrinting() {
            gate.complete(Unit)
        }

        fun failWith(error: PrinterError) {
            this.error = error
        }

        override suspend fun print(receipt: Receipt) {
            error?.let { throw it }
            gate.await()
            printed.add(receipt)
        }
    }

    private companion object {
        const val EVENT_UID = "EVENT_UID"
        const val PRINT_BUTTON = "Print receipt"
        const val LONGER_THAN_A_SNACKBAR_MILLIS = 10_000L
    }
}
