package org.dhis2.mobile.sync

import kotlin.test.Test
import kotlin.test.assertFalse

class RetentionPurgeCapabilityTest {
    @Test
    fun `should be disabled by default`() {
        assertFalse(RetentionPurgeCapability.IS_ENABLED)
    }
}
