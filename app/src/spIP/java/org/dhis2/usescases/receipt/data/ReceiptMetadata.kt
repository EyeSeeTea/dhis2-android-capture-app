package org.dhis2.usescases.receipt.data

// UIDs of the metadata the receipt reads. Kept in one place because the production UIDs are not confirmed yet.
data class ReceiptMetadata(
    val distributionDataElementUid: String,
    val rationCardIdAttributeUid: String,
    val beneficiaryNameAttributeUid: String,
) {
    companion object {
        // SP Beneficiary Hub on the test server (8380). Confirm the production UIDs before release.
        val SP_BENEFICIARY_HUB = ReceiptMetadata(
            distributionDataElementUid = "OQEWu6dpWYl",
            rationCardIdAttributeUid = "ynR25f3ZTig",
            beneficiaryNameAttributeUid = "OehKwDX8asz",
        )
    }
}
