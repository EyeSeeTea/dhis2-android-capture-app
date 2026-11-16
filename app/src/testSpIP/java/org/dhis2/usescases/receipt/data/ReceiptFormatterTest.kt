package org.dhis2.usescases.receipt.data

import org.dhis2.usescases.receipt.domain.model.GeographicalLocation
import org.dhis2.usescases.receipt.domain.model.Receipt
import org.dhis2.usescases.receipt.domain.model.ReceiptItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat

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

    @Test
    fun `the formatted receipt shows the distribution point in the header`() {
        val receipt = Receipt(
            distributionPoint = "Gethy distribution point",
            items = listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")),
        )

        val formatted = ReceiptFormatter().format(receipt)

        assertEquals(
            listOf("Distribution point: Gethy distribution point", "", "Rice (kg) given: 5"),
            formatted,
        )
    }

    @Test
    fun `the formatted receipt shows the distribution date as year, month and day`() {
        val receipt = Receipt(
            distributionDate = SimpleDateFormat("yyyy-MM-dd").parse("2026-11-16"),
            items = listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")),
        )

        val formatted = ReceiptFormatter().format(receipt)

        assertEquals(
            listOf("Distribution date: 2026-11-16", "", "Rice (kg) given: 5"),
            formatted,
        )
    }

    @Test
    fun `the formatted receipt shows the date and time of the print`() {
        val receipt = Receipt(
            printedOn = SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2026-11-16 11:45"),
            items = listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")),
        )

        val formatted = ReceiptFormatter().format(receipt)

        assertEquals(
            listOf("Receipt printed on: 2026-11-16 11:45", "", "Rice (kg) given: 5"),
            formatted,
        )
    }

    @Test
    fun `the formatted receipt shows each geographical location with its level`() {
        val receipt = Receipt(
            geographicalLocations = listOf(
                GeographicalLocation(level = 1, name = "Gethy Region"),
                GeographicalLocation(level = 3, name = "Hill Village"),
            ),
            items = listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")),
        )

        val formatted = ReceiptFormatter().format(receipt)

        assertEquals(
            listOf(
                "Geographical Location 1: Gethy Region",
                "Geographical Location 3: Hill Village",
                "",
                "Rice (kg) given: 5",
            ),
            formatted,
        )
    }
}
