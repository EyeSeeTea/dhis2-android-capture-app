package org.dhis2.usescases.receipt.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptMenuAvailabilityTest {

    @Test
    fun `the print receipt option is available for an event of the distribution stage`() {
        assertTrue(isPrintReceiptAvailable(programStageUid = DISTRIBUTION_STAGE_UID))
    }

    @Test
    fun `the print receipt option is not available for an event of any other stage`() {
        assertFalse(isPrintReceiptAvailable(programStageUid = "OTHER_STAGE_UID"))
    }

    private companion object {
        const val DISTRIBUTION_STAGE_UID = "tCC0YnvF9SJ"
    }
}
