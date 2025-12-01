package org.dhis2.form.ui.beneficiaryHub.processors

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.beneficiaryHub.DateOfBirthFormatter
import org.dhis2.form.ui.beneficiaryHub.ageFieldUid
import org.dhis2.form.ui.beneficiaryHub.calculators.AgeCalculator
import org.dhis2.form.ui.beneficiaryHub.dateOfBirthFieldUid
import org.hisp.dhis.android.core.common.ValueType

class DateOfBirthProcessor(
    private val repository: FormRepository,
    private val handler: Handler,
) {
    /**
     * Validate and format a date of birth field.
     *
     * @return Result with the formatted date or an error
     */
    fun process(fieldUIModel: FieldUiModel): Result<String?> {
        if (fieldUIModel.uid != dateOfBirthFieldUid ||
            fieldUIModel.value.isNullOrBlank()
        ) {
            return Result.success(fieldUIModel.value)
        }

        val formattedDate = DateOfBirthFormatter.formatAndValidate(fieldUIModel.value)

        if (formattedDate == null) {
            return Result.failure(
             Throwable("Invalid date. Please use format YYYY-MM-DD or YYYYMMDD and ensure the date exists and is valid")
            )
        }

        if (DateOfBirthFormatter.isFutureDate(formattedDate)) {
            return Result.failure(
                Throwable("Date of Birth cannot be in the future")
            )
        }

        if (DateOfBirthFormatter.isBeforeMinDate(formattedDate)) {
            return Result.failure(
                Throwable("Date of Birth cannot be previous to 1900-01-01")
            )
        }


        updateAgeFieldFromDateOfBirth(fieldUIModel.uid, formattedDate)

        return Result.success(formattedDate)
    }

    private fun updateAgeFieldFromDateOfBirth(dateOfBirthFieldUid: String, formattedDate: String) {
        val calculatedAge = AgeCalculator.calculateAgeInYears(formattedDate)
        calculatedAge?.let { age ->
            handler.post {
                repository.save(ageFieldUid, age.toString(), null)
                repository.updateValueOnList(ageFieldUid, age.toString(), ValueType.INTEGER)
            }
        }
    }
}

