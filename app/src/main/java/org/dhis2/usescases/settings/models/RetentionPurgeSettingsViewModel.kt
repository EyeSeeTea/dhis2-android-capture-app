package org.dhis2.usescases.settings.models

// EyeSeeTea customization - Synced Data Retention Purge
data class RetentionPurgeSettingsViewModel(
    val purgePeriod: Int,
    val lastPurge: String,
    val purgeHasErrors: Boolean,
    val purgeInProgress: Boolean,
)
