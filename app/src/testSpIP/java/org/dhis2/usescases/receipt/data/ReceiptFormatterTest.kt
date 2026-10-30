package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptFormatterTest {

    @Test
    fun `the formatted receipt has a line with the label and value of each data value`() {
        val receipt = givenAReceipt(
            "Distribution point" to "Point A",
            "Household size" to "5",
        )

        val formatted = ReceiptFormatter().format(receipt)

        assertEquals(2, formatted.size)
        assertTrue(formatted[0].contains("Distribution point") && formatted[0].contains("Point A"))
        assertTrue(formatted[1].contains("Household size") && formatted[1].contains("5"))
    }

    private fun givenAReceipt(vararg lines: Pair<String, String>) = Receipt(
        lines = lines.map { (label, value) -> ReceiptLine(label = label, value = value) },
    )
}
