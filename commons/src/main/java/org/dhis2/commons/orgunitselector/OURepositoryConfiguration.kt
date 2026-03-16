package org.dhis2.commons.orgunitselector

import org.dhis2.commons.team.ValidationData
import org.dhis2.commons.team.nonActiveOrgUnits
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository

class OURepositoryConfiguration(
    private val d2: D2,
    private val orgUnitSelectorScope: OrgUnitSelectorScope,
    private val validationData: ValidationData?,
) {
    fun orgUnitRepository(name: String?): List<OrganisationUnit> {
        var orgUnitRepository =
            d2
                .organisationUnitModule()
                .organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)

        orgUnitRepository =
            when {
                !name.isNullOrEmpty() -> orgUnitRepository.byDisplayName().like(name)
                else -> orgUnitRepository
            }

        orgUnitRepository =
            when (orgUnitSelectorScope) {
                is OrgUnitSelectorScope.DataSetCaptureScope,
                is OrgUnitSelectorScope.ProgramCaptureScope,
                is OrgUnitSelectorScope.UserCaptureScope,
                    ->
                    applyCaptureFilter(orgUnitRepository)

                is OrgUnitSelectorScope.ProgramSearchScope,
                is OrgUnitSelectorScope.DataSetSearchScope,
                is OrgUnitSelectorScope.UserSearchScope,
                is OrgUnitSelectorScope.SdsTeamScope,
                    ->
                    applySearchFilter(orgUnitRepository)
            }

        val orgUnits =
            when (orgUnitSelectorScope) {
                is OrgUnitSelectorScope.DataSetCaptureScope,
                is OrgUnitSelectorScope.DataSetSearchScope,
                    ->
                    orgUnitRepository.byDataSetUids(listOf(orgUnitSelectorScope.uid!!))
                        .blockingGet()

                is OrgUnitSelectorScope.ProgramCaptureScope,
                is OrgUnitSelectorScope.ProgramSearchScope,
                    ->
                    orgUnitRepository.byProgramUids(listOf(orgUnitSelectorScope.uid!!))
                        .blockingGet()

                is OrgUnitSelectorScope.UserCaptureScope,
                is OrgUnitSelectorScope.UserSearchScope,
                    ->
                    orgUnitRepository.blockingGet()

                is OrgUnitSelectorScope.SdsTeamScope ->
                    getSdsOrgUnits(orgUnitSelectorScope)
            }

        // EyeSeeTea customization - Validate or hide orgunit by Teamprofile
        // Base behavior: return all org units allowed by capture/search scope.
        val nonActiveOrgUnits =
            if (validationData == null) {
                emptyList()
            } else {
                nonActiveOrgUnits(d2, validationData)
            }

        return orgUnits
            .filter { organisationUnit ->
                organisationUnit.uid() !in nonActiveOrgUnits
            }
    }

    fun countChildren(
        parentOrgUnitUid: String,
        selectedOrgUnits: List<String>,
    ): Int =
        d2
            .organisationUnitModule()
            .organisationUnits()
            .byPath()
            .like("%$parentOrgUnitUid%")
            .byUid()
            .`in`(selectedOrgUnits)
            .blockingCount()

    fun orgUnit(uid: String): OrganisationUnit? =
        d2
            .organisationUnitModule()
            .organisationUnits()
            .uid(uid)
            .blockingGet()

    private fun applyCaptureFilter(orgUnitRepository: OrganisationUnitCollectionRepository) =
        orgUnitRepository.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)

    private fun applySearchFilter(orgUnitRepository: OrganisationUnitCollectionRepository) = orgUnitRepository

    // EyeSeeTea customization - Multiple SDS org unit selection
    private fun getSdsOrgUnits(
        scope: OrgUnitSelectorScope.SdsTeamScope,
    ): List<OrganisationUnit> {
        val sdsOrgUnitLevel = 5
        val countryLevel = 4
        val sdsOrgUnitGroupId = "yA9VnZi6g7f"

        val orgUnit = d2.organisationUnitModule().organisationUnits()
            .uid(scope.currentOrgUnitUid).blockingGet()

        val countryParent = orgUnit?.path()?.split("/")[countryLevel] ?: ""

        val level5OrgUnits = d2.organisationUnitModule().organisationUnits()
            .byLevel().eq(sdsOrgUnitLevel).withOrganisationUnitGroups().blockingGet()
            .filter { organisationUnit ->
                organisationUnit.path()?.contains(countryParent) ?: false
            }

        val orgUnitsInSDSGroup = level5OrgUnits.filter { organisationUnit ->
            val groups = organisationUnit.organisationUnitGroups() ?: listOf()

            groups.any { organisationUnitGroup ->
                organisationUnitGroup.uid() == sdsOrgUnitGroupId
            }
        }

        return orgUnitsInSDSGroup
    }
}



