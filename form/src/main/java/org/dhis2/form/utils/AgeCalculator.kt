package org.dhis2.form.utils

import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object AgeCalculator {
    /**
     * Calculates the age based on a given date string.
     * Similar to the plugin web logic
     *
     * @param dateOfBirth The date of birth in string format (e.g., "YYYY-MM-DD").
     * @param clock Clock to get the current date (defaults to system clock).
     * @return The calculated age as a number, or null if the date is invalid.
     */
    fun calculateAgeInYears(
        dateOfBirth: String,
        clock: Clock = Clock.systemDefaultZone()
    ): Int? {
        return try {
            val birthDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_DATE)
            val today = LocalDate.now(clock)

            var age = today.year - birthDate.year
            val monthDiff = today.monthValue - birthDate.monthValue

            // Adjust age if the current month and day are before the birth month and day
            if (monthDiff < 0 || (monthDiff == 0 && today.dayOfMonth < birthDate.dayOfMonth)) {
                age--
            }

            age
        } catch (e: DateTimeParseException) {
            null
        }
    }
}

