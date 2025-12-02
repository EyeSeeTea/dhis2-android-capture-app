package org.dhis2.form.ui.beneficiaryHub.processors

import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.beneficiaryHub.ageFieldUid
import org.dhis2.form.ui.beneficiaryHub.ageInMonthsFieldUid
import org.dhis2.form.ui.beneficiaryHub.dateOfBirthFieldUid

class IsDobKnownProcessor(
    private val repository: FormRepository,
) {
    /**
     * Processes changes in the isDobKnown field.
     * Updates editable property of related fields:
     * - If isDobKnown == true: disables age and ageInMonths, enables dateOfBirth
     * - If isDobKnown == false: disables dateOfBirth, enables age and ageInMonths
     * - If isDobKnown == null/undefined: enables all fields
     *
     * @param fieldUiModel The isDobKnown field that changed.
     * @param isDobKnown The value of the isDobKnown field (true, false, or null/undefined).
     * @return Result with the isDobKnown value as string or an error.
     */
    fun process(fieldUiModel: FieldUiModel, isDobKnown: Boolean?): Result<String?> {
        // Update editable state synchronously to ensure it's applied before processCalculatedItems
        when (isDobKnown) {
            true -> {
                // Disable age and ageInMonths, enable dateOfBirth
                repository.updateEditableOnList(ageFieldUid, false)
                repository.updateEditableOnList(ageInMonthsFieldUid, false)
                repository.updateEditableOnList(dateOfBirthFieldUid, true)
            }
            false -> {
                // Disable dateOfBirth, enable age and ageInMonths
                repository.updateEditableOnList(dateOfBirthFieldUid, false)
                repository.updateEditableOnList(ageFieldUid, true)
                repository.updateEditableOnList(ageInMonthsFieldUid, true)
            }
            null -> {
                // Enable all fields when isDobKnown is undefined
                repository.updateEditableOnList(dateOfBirthFieldUid, true)
                repository.updateEditableOnList(ageFieldUid, true)
                repository.updateEditableOnList(ageInMonthsFieldUid, true)
            }
        }

        return Result.success(fieldUiModel.value)
    }
}

