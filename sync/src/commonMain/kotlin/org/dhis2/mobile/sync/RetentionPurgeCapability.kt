package org.dhis2.mobile.sync

// EyeSeeTea customization - Synced Data Retention Purge
// This is OCA's fork value. See design.md ("Decisions") for why this is
// a single shared constant instead of a per-flavor override mechanism.
object RetentionPurgeCapability {
    const val IS_ENABLED: Boolean = true
}
