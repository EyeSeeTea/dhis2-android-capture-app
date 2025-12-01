package org.dhis2.form.ui.beneficiaryHub

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.hisp.dhis.android.core.common.ValueType

class DateOfBirthProcessor(
    private val repository: FormRepository,
    private val handler: Handler,
) {
    /**
     * Valida y formatea un campo de fecha de nacimiento.
     * Sigue el patrón de checkFieldError: retorna Pair<RowAction?, Throwable?>
     *
     * @return Pair donde:
     *   - First: RowAction procesado con fecha formateada, o null si hay error
     *   - Second: Throwable si hay error, o null si está bien
     */
    fun process(fieldUIModel: FieldUiModel): RowAction {

        val action = RowAction(
            id = fieldUIModel.uid,
            value = fieldUIModel.value,
            valueType = fieldUIModel.valueType,
            type = ActionType.ON_SAVE,
        )

        if (fieldUIModel.uid != dateOfBirthFieldUid ||
            fieldUIModel.value.isNullOrBlank()
        ) {
            return action
        }

        val formattedDate = DateOfBirthFormatter.formatAndValidate(action.value)

        if (formattedDate == null) {
            return action.copy(
                error = Throwable("Invalid date. Please use format YYYY-MM-DD or YYYYMMDD and ensure the date exists and is valid")
            )
        }

        if (DateOfBirthFormatter.isFutureDate(formattedDate)) {
            return action.copy(
                error =
                    Throwable("Date of Birth cannot be in the future")
            )
        }

        if (DateOfBirthFormatter.isBeforeMinDate(formattedDate)) {
            return action.copy(
                error = Throwable("Date of Birth cannot be previous to 1900-01-01")
            )
        }

        val processedAction = action.copy(value = formattedDate)

        updateAgeFieldFromDateOfBirth(action.id, formattedDate)

        return processedAction
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

