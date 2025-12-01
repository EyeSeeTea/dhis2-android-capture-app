package org.dhis2.form.ui.beneficiaryHub

import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateOfBirthCalculator {
    /**
     * Calculates the date of birth given an age.
     * Returns January 1st of the year that makes the age valid.
     * Similar to the plugin web logic
     * E.g., if age is 22 and current year is 2025, returns 2003-01-01.
     *
     * @param age The age in years.
     * @param clock Clock to get the current date (defaults to system clock).
     * @return The calculated date of birth as a string in "YYYY-MM-DD" format.
     * @throws IllegalArgumentException if age is negative.
     */
    fun calculateFromAge(
        age: Int,
        clock: Clock = Clock.systemDefaultZone()
    ): String {
        if (age < 0) {
            throw IllegalArgumentException("Age cannot be negative")
        }
        val today = LocalDate.now(clock)
        val birthYear = today.year - age
        val birthDate = LocalDate.of(birthYear, 1, 1) // January 1st of the birth year
        return birthDate.format(DateTimeFormatter.ISO_DATE)
    }
}

