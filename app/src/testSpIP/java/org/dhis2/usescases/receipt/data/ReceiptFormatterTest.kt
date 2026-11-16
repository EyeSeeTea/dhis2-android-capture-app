package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptItem
import org.junit.Assert.assertEquals
import org.junit.Test

class ReceiptFormatterTest {

    @Test
    fun `the formatted receipt shows the header followed by each item with its quantity`() {
        val receipt = Receipt(
            distributionName = "TEST Distribution 1",
            spbhId = "SPBH-0042",
            beneficiaryName = "Ana Perez",
            items = listOf(
                ReceiptItem(label = "Rice (kg) given", quantity = "5"),
                ReceiptItem(label = "Dates", quantity = "3"),
            ),
        )

        val formatted = ReceiptFormatter().format(receipt)

        assertEquals(
            listOf(
                "Distribution: TEST Distribution 1",
                "SPBH ID: SPBH-0042",
                "Beneficiary: Ana Perez",
                "",
                "Rice (kg) given: 5",
                "Dates: 3",
            ),
            formatted,
        )
    }

    @Test
    fun `a header value the event does not have is left out of the formatted receipt`() {
        val receipt = Receipt(
            distributionName = "TEST Distribution 1",
            spbhId = null,
            beneficiaryName = null,
            items = listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")),
        )

        val formatted = ReceiptFormatter().format(receipt)

        assertEquals(
            listOf("Distribution: TEST Distribution 1", "", "Rice (kg) given: 5"),
            formatted,
        )
    }
}
