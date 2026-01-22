package org.dhis2.form.ui.beneficiaryHub.processors

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.beneficiaryHub.ageFieldUid
import org.dhis2.form.ui.beneficiaryHub.ageInMonthsFieldUid
import org.dhis2.form.ui.beneficiaryHub.calculators.AgeCalculator
import org.dhis2.form.ui.beneficiaryHub.calculators.DateOfBirthCalculator
import org.dhis2.form.ui.beneficiaryHub.dateOfBirthFieldUid
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
     * Calculates estimated DOB from age and updates ageInMonths if age <= 5.
     *
     * @param fieldUiModel The age field that changed.
     * @param isDobKnown The value of the isDobKnown field (true, false, or null/undefined).
     * @return Result with the age value as string or an error.
     */
    fun process(fieldUiModel: FieldUiModel, isDobKnown: Boolean?): Result<String?> {
        if (fieldUiModel.uid != ageFieldUid || fieldUiModel.value.isNullOrBlank()) {
            return Result.success(fieldUiModel.value)
        }

        val ageParsed = fieldUiModel.value!!.toIntOrNull()
        if (ageParsed == null || ageParsed < 0) {
            return Result.failure(
                Throwable("Age must be a valid positive integer number")
            )
        }

        if (ageParsed > MAX_AGE) {
            return Result.failure(
                Throwable("Age cannot be greater than 125")
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

        return Result.success(ageParsed.toString())
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

