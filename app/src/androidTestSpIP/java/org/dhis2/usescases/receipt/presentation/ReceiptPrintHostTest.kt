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
import org.dhis2.usescases.receipt.domain.model.PrintReceiptError
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptItem
import org.dhis2.usescases.receipt.domain.repository.ReceiptPrinter
import org.dhis2.usescases.receipt.domain.repository.ReceiptRepository
import org.dhis2.usescases.receipt.domain.usecase.PrintReceiptUseCase
import org.eyeseetea.functional.Either
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class ReceiptPrintHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val receipt = Receipt(eventUid = EVENT_UID, items = listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")))
    private val receiptPrinter = GatedReceiptPrinter()

    @Test
    fun printingShowsTheDialogWhileThePrinterWorksAndThenThePrintedConfirmation() {
        givenAReceiptPrintHost()

        composeRule.onNodeWithText(PRINT_BUTTON).performClick()

        composeRule.onNodeWithText(context.getString(R.string.receipt_printing)).assertIsDisplayed()

        receiptPrinter.finishPrinting()

        composeRule.waitUntilDoesNotExist(hasText(context.getString(R.string.receipt_printing)))
        composeRule.onNodeWithText(context.getString(R.string.receipt_printed)).assertIsDisplayed()
        assertEquals(listOf(receipt), receiptPrinter.printedWithoutPrintTime())
    }

    @Test
    fun aNoPrinterConnectedErrorStaysOnScreenUntilItIsAcceptedAndNothingIsPrinted() {
        val noPrinterConnected = context.getString(R.string.receipt_no_printer_connected)
        receiptPrinter.failWith(PrintReceiptError.NoPrinterConnected)
        givenAReceiptPrintHost()

        composeRule.onNodeWithText(PRINT_BUTTON).performClick()

        composeRule.waitUntilExactlyOneExists(hasText(noPrinterConnected))
        composeRule.mainClock.advanceTimeBy(LONGER_THAN_A_SNACKBAR_MILLIS)
        composeRule.onNodeWithText(noPrinterConnected).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.receipt_error_accept)).performClick()

        composeRule.onNodeWithText(noPrinterConnected).assertDoesNotExist()
        assertTrue(receiptPrinter.printed.isEmpty())
    }

    @Test
    fun aPrintFailedErrorIsShownInsteadOfTheNoPrinterConnectedOneWhenThePrinterFails() {
        val printFailed = context.getString(R.string.receipt_print_failed)
        receiptPrinter.failWith(PrintReceiptError.PrintFailed(IllegalStateException("The printer ran out of paper")))
        givenAReceiptPrintHost()

        composeRule.onNodeWithText(PRINT_BUTTON).performClick()

        composeRule.waitUntilExactlyOneExists(hasText(printFailed))
        composeRule.onNodeWithText(context.getString(R.string.receipt_no_printer_connected)).assertDoesNotExist()

        composeRule.onNodeWithText(context.getString(R.string.receipt_error_accept)).performClick()

        composeRule.onNodeWithText(printFailed).assertDoesNotExist()
        assertTrue(receiptPrinter.printed.isEmpty())
    }

    @Test
    fun pressingPrintTwiceWhilePrintingPrintsOnlyOneReceipt() {
        givenAReceiptPrintHost()

        composeRule.onNodeWithText(PRINT_BUTTON).performClick()
        composeRule.onNodeWithText(PRINT_BUTTON).performClick()
        receiptPrinter.finishPrinting()

        composeRule.waitUntilExactlyOneExists(hasText(context.getString(R.string.receipt_printed)))
        assertEquals(listOf(receipt), receiptPrinter.printedWithoutPrintTime())
    }

    @Test
    fun printingWithoutThePermissionAsksForItAndPrintsOnceItIsGranted() {
        receiptPrinter.requirePermission()
        receiptPrinter.finishPrinting()
        val permissionRequester = FakePermissionRequester(grants = true) { receiptPrinter.grantPermission() }
        givenAReceiptPrintHostAskingForPermissionWith(permissionRequester)

        composeRule.onNodeWithText(PRINT_BUTTON).performClick()

        composeRule.waitUntil { permissionRequester.requests == 1 }
        composeRule.waitUntilExactlyOneExists(hasText(context.getString(R.string.receipt_printed)))
        assertEquals(listOf(receipt), receiptPrinter.printedWithoutPrintTime())
    }

    private fun givenAReceiptPrintHost() =givenAReceiptPrintHostAskingForPermissionWith(FakePermissionRequester(grants = true))

    private fun givenAReceiptPrintHostAskingForPermissionWith(permissionRequester: PermissionRequester) {
        val receiptPrintViewModel = ReceiptPrintViewModel(
            PrintReceiptUseCase(FakeReceiptRepository(receipt), receiptPrinter),
        )
        composeRule.setContent {
            ReceiptPrintHost(receiptPrintViewModel, permissionRequester)
            Button(onClick = { receiptPrintViewModel.print(EVENT_UID) }) { Text(PRINT_BUTTON) }
        }
    }

    private class FakePermissionRequester(
        private val grants: Boolean,
        private val onGranted: () -> Unit = {},
    ) : PermissionRequester {
        var requests = 0
            private set

        override fun request(onResult: (Boolean) -> Unit) {
            requests++
            if (grants) onGranted()
            onResult(grants)
        }
    }

    private class FakeReceiptRepository(private val receipt: Receipt) : ReceiptRepository {
        override suspend fun getReceipt(eventUid: String): Receipt = receipt

        override suspend fun save(receipt: Receipt) = Unit
    }

    private class GatedReceiptPrinter : ReceiptPrinter {
        private val gate = CompletableDeferred<Unit>()
        private var error: PrintReceiptError? = null
        private var permissionGranted = true
        val printed = mutableListOf<Receipt>()

        fun requirePermission() {
            permissionGranted = false
        }

        fun grantPermission() {
            permissionGranted = true
        }

        // The use case stamps the print time, which the host tests do not control.
        fun printedWithoutPrintTime(): List<Receipt> = printed.map { it.copy(printedOn = null) }

        fun finishPrinting() {
            gate.complete(Unit)
        }

        fun failWith(error: PrintReceiptError) {
            this.error = error
        }

        override suspend fun print(receipt: Receipt): Either<PrintReceiptError, Unit> {
            if (!permissionGranted) return Either.Left(PrintReceiptError.PermissionRequired)
            error?.let { return Either.Left(it) }
            gate.await()
            printed.add(receipt)
            return Either.Right(Unit)
        }
    }

    private companion object {
        const val EVENT_UID = "EVENT_UID"
        const val PRINT_BUTTON = "Print receipt"
        const val LONGER_THAN_A_SNACKBAR_MILLIS = 10_000L
    }
}
