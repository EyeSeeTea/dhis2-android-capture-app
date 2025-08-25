package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository

private const val teamRequestStatusUId = "POi3jY1mjJ0"
private const val teamParentUId = "S5Hb8en5OJU"
private const val defaultCategoryOptionComboUid = "HllvX50cXC0"

//EyeSeeTea customization
internal class CreateChangeTeamRequest(
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val attrOptionComboUid: String,
    private val repository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(): Result<Unit> {

        val parentOrgUnit = repository.getParentOrgUnit(orgUnitUid)

        return repository.updateValue(
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            attrOptionComboUid = attrOptionComboUid,
            dataElementUid = teamParentUId,
            categoryOptionComboUid = defaultCategoryOptionComboUid,
            value = parentOrgUnit,
        ).flatMap {
            repository.updateValue(
                periodId = periodId,
                orgUnitUid = orgUnitUid,
                attrOptionComboUid = attrOptionComboUid,
                dataElementUid = teamRequestStatusUId,
                categoryOptionComboUid = defaultCategoryOptionComboUid,
                value = "REQUESTED",
            )
        }
    }
}

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    fold(
        onSuccess = { transform(it) },
        onFailure = { Result.failure(it) }
    )