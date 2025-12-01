package org.dhis2.form.ui.beneficiaryHub

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.hisp.dhis.android.core.common.ValueType
import java.time.Clock


private const val MAX_AGE = 125
private const val MAX_CALC_AGE_IN_MONTHS_YEARS = 5

class AgeProcessor(
    private val repository: FormRepository,
    private val handler: Handler,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    /**
     * Processes changes in the age field.
     * Only processes if isDobKnown == "false".
     * Calculates estimated DOB from age and updates ageInMonths if age <= 5.
     *
     * @param fieldUiModel The age field that changed.
     * @param isDobKnown The value of the isDobKnown field ("true", "false", or null/undefined).
     * @return RowAction with the processing result.
     */
    fun process(fieldUiModel: FieldUiModel, isDobKnown: Boolean?): RowAction {
        val action = RowAction(
            id = fieldUiModel.uid,
            value = fieldUiModel.value,
            valueType = fieldUiModel.valueType,
            type = ActionType.ON_SAVE,
        )

        if (fieldUiModel.uid != ageFieldUid || isDobKnown != false || fieldUiModel.value.isNullOrBlank()) {
            return action
        }

        val ageParsed = fieldUiModel.value!!.toIntOrNull()
        if (ageParsed == null || ageParsed < 0) {
            return action.copy(
                error = Throwable("Age must be a valid positive integer number")
            )
        }

        if (ageParsed > MAX_AGE) {
            return action.copy(
                error = Throwable("Age cannot be greater than 125")
            )
        }

        val estimatedDob = DateOfBirthCalculator.calculateFromAge(ageParsed, clock)

        val currentDob = repository.getField(dateOfBirthFieldUid)?.value
        val sameAgeAsDob = currentDob?.let { dob ->
            AgeCalculator.hasSameAge(dob, estimatedDob, clock)
        } ?: false

        val formattedEstimatedDob = if (sameAgeAsDob && currentDob != null) {
            currentDob
        } else {
            estimatedDob
        }

        updateDobAndAgeInMonths(formattedEstimatedDob, ageParsed)

        return action.copy(value = ageParsed.toString())
    }

    private fun updateDobAndAgeInMonths(formattedDob: String, age: Int) {
        handler.post {
            repository.save(dateOfBirthFieldUid, formattedDob, null)
            repository.updateValueOnList(dateOfBirthFieldUid, formattedDob, ValueType.DATE)

            val calculatedAgeInMonths = if (age <= MAX_CALC_AGE_IN_MONTHS_YEARS) {
                AgeCalculator.calculateAgeInMonths(formattedDob, clock)?.toString() ?: ""
            } else {
                ""
            }
            repository.save(ageInMonthsFieldUid, calculatedAgeInMonths, null)
            repository.updateValueOnList(ageInMonthsFieldUid, calculatedAgeInMonths, ValueType.INTEGER)
        }
    }
}

