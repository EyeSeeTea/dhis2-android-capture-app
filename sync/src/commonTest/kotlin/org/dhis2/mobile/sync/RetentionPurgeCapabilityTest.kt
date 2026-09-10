package org.dhis2.mobile.sync

import kotlin.test.Test
import kotlin.test.assertTrue

// EyeSeeTea customization - Synced Data Retention Purge
// This asserts the concrete value on this fork's branch, not a spec-level
// default: the "Per-fork enablement" requirement in spec.md still expects
// disabled-by-default as the general capability contract. OCA enables it
// explicitly here (see design.md "Decisions") until a second fork needs a
// different value, at which point the baseline default should move back
// to false with an explicit per-fork override point.
class RetentionPurgeCapabilityTest {
    @Test
    fun `should be enabled on this fork`() {
        assertTrue(RetentionPurgeCapability.IS_ENABLED)
    }
}
