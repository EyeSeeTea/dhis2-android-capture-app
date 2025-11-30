package org.dhis2.form.ui.beneficiaryHub

import org.dhis2.form.data.FormRepository

object DateOfBirthFieldMapper {
    private val dateOfBirthToAgeMapping = mapOf(
        "J1b2qRpPC6f" to "VewXVFJFYvM"
    )

    fun isDateOfBirthField(fieldUid: String): Boolean {
        return dateOfBirthToAgeMapping.containsKey(fieldUid)
    }

    fun getAgeField(
        dateOfBirthFieldUid: String,
        repository: FormRepository
    ): String? {
        return getAgeFieldUid(dateOfBirthFieldUid)?.let { ageFieldUid ->
            repository.getField(ageFieldUid)?.uid
        }
    }

    fun getAgeFieldUid(dateOfBirthFieldUid: String): String? {
        return dateOfBirthToAgeMapping[dateOfBirthFieldUid]
    }

}

