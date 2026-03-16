package org.dhis2.mobile.commons.orgunit

import kotlinx.serialization.Serializable

@Serializable
sealed class OrgUnitSelectorScope(
    val uid: String?,
) {
    @Serializable
    data class UserSearchScope(
        val userUid: String? = null,
    ) : OrgUnitSelectorScope(userUid)

    @Serializable
    data class UserCaptureScope(
        val userUid: String? = null,
    ) : OrgUnitSelectorScope(userUid)

    @Serializable
    data class ProgramSearchScope(
        val programUid: String,
    ) : OrgUnitSelectorScope(programUid)

    @Serializable
    data class ProgramCaptureScope(
        val programUid: String,
    ) : OrgUnitSelectorScope(programUid)

    @Serializable
    data class DataSetSearchScope(
        val dataSetUid: String,
    ) : OrgUnitSelectorScope(dataSetUid)

    @Serializable
    data class DataSetCaptureScope(
        val dataSetUid: String,
    ) : OrgUnitSelectorScope(dataSetUid)

    // EyeSeeTea customization - multiple SDS org unit selection
    // SPOCC behavior: scope for team SDS org unit selector; filters org units by team/period.
    @Serializable
    data class SdsTeamScope(
        val currentOrgUnitUid: String,
        val period: String,
    ) : OrgUnitSelectorScope(currentOrgUnitUid)
}
