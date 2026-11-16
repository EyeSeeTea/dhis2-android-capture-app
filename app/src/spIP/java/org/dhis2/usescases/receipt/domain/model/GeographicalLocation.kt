package org.dhis2.usescases.receipt.domain.model

// One level of the geographic location of the beneficiary (Geographical Location 1, 2 or 3).
data class GeographicalLocation(val level: Int, val name: String)
