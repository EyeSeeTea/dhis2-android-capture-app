package org.dhis2.usescases.receipt.data

// UIDs of the metadata the receipt reads, kept in one place. Production uses the same UIDs as the test server.
data class ReceiptMetadata(
    val distributionDataElementUid: String,
    val spbhIdAttributeUid: String,
    val beneficiaryNameAttributeUid: String,
) {
    companion object {
        // SP Beneficiary Hub (same UIDs on the test server 8380 and in production).
        val SP_BENEFICIARY_HUB = ReceiptMetadata(
            distributionDataElementUid = "OQEWu6dpWYl",
            spbhIdAttributeUid = "DjPrtej5I1x",
            beneficiaryNameAttributeUid = "OehKwDX8asz",
        )
    }
}
