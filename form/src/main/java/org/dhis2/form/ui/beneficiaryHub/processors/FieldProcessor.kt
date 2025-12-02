package org.dhis2.form.ui.beneficiaryHub.processors

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.beneficiaryHub.ageFieldUid
import org.dhis2.form.ui.beneficiaryHub.ageInMonthsFieldUid
import org.dhis2.form.ui.beneficiaryHub.dateOfBirthFieldUid
import org.dhis2.form.ui.beneficiaryHub.isDateOfBirthKnownFieldUid
import java.time.Clock

class FieldProcessor(
    repository: FormRepository,
    handler: Handler,
    clock: Clock = Clock.systemDefaultZone(),
) {
    private val dateOfBirthProcessor = DateOfBirthProcessor(repository, handler, clock)
    private val ageProcessor = AgeProcessor(repository, handler, clock)
    private val ageInMonthsProcessor = AgeInMonthsProcessor(repository, handler, clock)
    private val isDobKnownProcessor = IsDobKnownProcessor(repository)

    fun process(fieldUIModel: FieldUiModel, isDobKnown: Boolean?): Result<String?> {
        return when (fieldUIModel.uid) {
            isDateOfBirthKnownFieldUid -> {
                isDobKnownProcessor.process(fieldUIModel, isDobKnown)
            }

            dateOfBirthFieldUid -> {
                dateOfBirthProcessor.process(fieldUIModel, isDobKnown)
            }

            ageFieldUid -> {
                ageProcessor.process(fieldUIModel, isDobKnown)
            }

            ageInMonthsFieldUid -> {
                ageInMonthsProcessor.process(fieldUIModel, isDobKnown)
            }

            else -> Result.success(fieldUIModel.value)
        }
    }
}