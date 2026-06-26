package org.dhis2.form.data.lotnumber

import kotlinx.serialization.Serializable

// EyeSeeTea customization - Lot Number Search Field

typealias LotNumbersApiResponse = Map<String, Map<String, LotNumbersEntry>>

@Serializable
data class LotNumbersEntry(
    val lotNumbers: List<String> = emptyList(),
)
