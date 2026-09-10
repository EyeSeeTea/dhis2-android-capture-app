package org.dhis2.usescases.settings.models

data class RetentionPurgeSettingsViewModel(
    val purgePeriod: Int,
    val lastPurge: String,
    val purgeHasErrors: Boolean,
    val purgeInProgress: Boolean,
)
