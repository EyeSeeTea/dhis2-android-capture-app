package org.dhis2.form.data.lotnumber

// EyeSeeTea customization - Lot Number Search Field

/** DataElement that holds the selected product; each product renders its own lot DataElement. */
const val PRODUCT_DE_UID = "BzKc72LZLxw"

/**
 * DataElements that render the lot-number search field. One per product family, so when the
 * product changes the previously entered lot may live on a DataElement that is no longer
 * composed. The set lets the form clear every lot value of the event on product change.
 */
val LOT_NUMBER_DE_UIDS =
    setOf(
        "mh54C5HEI7C", // NTO - Batch/Lot Number (nutrition)
        "mSiFpMaYeua", // Batch/Lot Number (Paracetamol / Amoxicillin / Ampicillin)
        "V86WBAXY0Sq", // NTO - Vitamin A Batch/Lot
        "rXAG58ZVgd7", // Batch/Lot Number
    )
