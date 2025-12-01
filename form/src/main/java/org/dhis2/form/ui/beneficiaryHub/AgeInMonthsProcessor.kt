package org.dhis2.form.ui.beneficiaryHub

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.common.ValueType
import java.time.Clock

class AgeInMonthsProcessor(
    private val repository: FormRepository,
    private val handler: Handler,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    /**
     * Processes changes in the age in months field.
     * Only processes if isDobKnown == false.
     * Calculates estimated DOB from age in months and updates age in years.
     *
     * @param fieldUiModel The age in months field that changed.
     * @param isDobKnown The value of the isDobKnown field (true, false, or null/undefined).
     * @return Result with the age in months value as string or an error.
     */
    fun process(fieldUiModel: FieldUiModel, isDobKnown: Boolean?): Result<String?> {
        if (fieldUiModel.uid != ageInMonthsFieldUid || isDobKnown != false || fieldUiModel.value.isNullOrBlank()) {
            return Result.success(fieldUiModel.value)
        }

        val ageInMonthsParsed = fieldUiModel.value!!.toIntOrNull()
        if (ageInMonthsParsed == null || ageInMonthsParsed < 0) {
            return Result.failure(
                Throwable("Age in months must be a valid positive integer number")
            )
        }

        val estimatedDob = DateOfBirthCalculator.calculateFromAgeInMonths(ageInMonthsParsed, clock)

        updateDobAndAge(estimatedDob, ageInMonthsParsed)

        return Result.success(ageInMonthsParsed.toString())
    }

    private fun updateDobAndAge(formattedDob: String, ageInMonths: Int) {
        handler.post {
            repository.save(dateOfBirthFieldUid, formattedDob, null)
            repository.updateValueOnList(dateOfBirthFieldUid, formattedDob, ValueType.DATE)

            val calculatedAge = AgeCalculator.calculateAgeInYears(formattedDob, clock)
            calculatedAge?.let { age ->
                repository.save(ageFieldUid, age.toString(), null)
                repository.updateValueOnList(ageFieldUid, age.toString(), ValueType.INTEGER)
            }
        }
    }
}

