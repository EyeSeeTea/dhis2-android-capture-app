package org.dhis2.form.ui.beneficiaryHub

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateOfBirthFormatter {
    enum class DateFormat {
        YYYYMMDD,
        MMDDYYYY
    }

    private val defaultFormat = DateFormat.YYYYMMDD
    private val MIN_DOB = "1900-01-01"

    private val YYYY_MM_DD_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    private val MM_DD_YYYY_REGEX = Regex("^\\d{2}-\\d{2}-\\d{4}$")

    fun formatAndValidate(
        input: String?,
        format: DateFormat = defaultFormat
    ): String? {
        if (input.isNullOrBlank()) return null

        return when (format) {
            DateFormat.YYYYMMDD -> formatDateYYYYMMDD(input)
            DateFormat.MMDDYYYY -> formatDateMMDDYYYY(input)
        }
    }

    /**
     * Validates and formats a date string.
     *
     * - If the input is in YYYYMMDD format, it converts it to YYYY-MM-DD.
     * - If the input is in YYYY-MM-DD format, it validates and returns it as is.
     * - Returns null if the date is invalid
     *
     * @param dateString - The input date string to validate and format.
     * @returns The formatted date string in YYYY-MM-DD format, or null if invalid.
     */
    private fun formatDateYYYYMMDD(dateString: String): String? {
        val normalized = if (dateString.length == 8 && dateString.all { it.isDigit() }) {
            "${dateString.substring(0, 4)}-${dateString.substring(4, 6)}-${dateString.substring(6, 8)}"
        } else if (YYYY_MM_DD_REGEX.matches(dateString)) {
            dateString
        } else {
            return null
        }

        return if (isValidDate(normalized)) normalized else null
    }

    /**
     * Validates and formats a date string.
     *
     * - If the input is in MMDDYYYY or MM-DD-YYYY format, it converts it to YYYY-MM-DD.
     * - If the input is in YYYY-MM-DD format, it validates and returns it as is.
     * - Returns null if the date is invalid
     *
     * @param dateString - The input date string to validate and format.
     * @returns The formatted date string in YYYY-MM-DD format, or null if invalid.
     */
    private fun formatDateMMDDYYYY(dateString: String): String? {
        val normalized = when {
            dateString.length == 8 && dateString.all { it.isDigit() } -> {
                val month = dateString.substring(0, 2)
                val day = dateString.substring(2, 4)
                val year = dateString.substring(4, 8)
                "$year-$month-$day"
            }

            MM_DD_YYYY_REGEX.matches(dateString) -> {
                val (month, day, year) = dateString.split("-")
                "$year-$month-$day"
            }

            YYYY_MM_DD_REGEX.matches(dateString) -> dateString
            else -> return null
        }

        return if (isValidDate(normalized)) normalized else null
    }

    /**
     * Checks if a date is valid (returns false for invalid dates like 2025-02-29 or 2025-04-31)
     * @param dateString - date in YYYY-MM-DD format
     */
    private fun isValidDate(dateString: String): Boolean {
        return try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
            date.format(DateTimeFormatter.ISO_DATE) == dateString
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isFutureDate(dateString: String): Boolean {
        return try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
            date.isAfter(LocalDate.now())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a date is before the minimum allowed date of birth (1900-01-01).
     * @param dateString - date in YYYY-MM-DD format
     * @return true if the date is before 1900-01-01, false otherwise
     */
    fun isBeforeMinDate(dateString: String): Boolean {
        return try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
            val minDate = LocalDate.parse(MIN_DOB, DateTimeFormatter.ISO_DATE)
            date.isBefore(minDate)
        } catch (e: Exception) {
            false
        }
    }
}

