package org.dhis2.usescases.receipt.presentation

// Distribution stage of SP Beneficiary Hub. UID of the test server; confirm the production one.
private const val DISTRIBUTION_STAGE_UID = "tCC0YnvF9SJ"

// EyeSeeTea customization - Print Receipt
fun isPrintReceiptAvailable(programStageUid: String): Boolean = programStageUid == DISTRIBUTION_STAGE_UID
