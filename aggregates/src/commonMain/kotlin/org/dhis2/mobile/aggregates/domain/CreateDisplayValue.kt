package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.commons.extensions.userFriendlyValue

// EyeSeeTea customization - Multiple SDS org unit selection
internal suspend fun createDisplayValue(
    repository: DataSetInstanceRepository,
    dataElementUid: String,
    value: String?
): String? {
    if (dataElementUid != teamSDSUid) return value?.userFriendlyValue(
        dataElementUid
    )

    val orgUnitUIds = value?.split(",") ?: emptyList()
    val displayNames = orgUnitUIds.map { uid ->
        repository.getOrgUnitById(uid).toString()
    }
    return displayNames.joinToString(",")
}
