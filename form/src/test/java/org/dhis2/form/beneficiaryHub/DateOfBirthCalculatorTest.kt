package org.dhis2.form.beneficiaryHub

import org.dhis2.form.ui.beneficiaryHub.DateOfBirthCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class DateOfBirthCalculatorTest {
    // Using MOCK_DATE_NOW = "2025-06-24T00:00:00Z"
    private val MOCK_DATE_NOW = "2025-06-24T00:00:00Z"
    private val mockClock: Clock = Clock.fixed(
        Instant.parse(MOCK_DATE_NOW),
        ZoneId.systemDefault()
    )

    @Test
    fun `should return January 1st of the correct birth year for a given age`() {
        val dob1 = DateOfBirthCalculator.calculateFromAge(22, mockClock)
        assertEquals("2003-01-01", dob1)

        val dob2 = DateOfBirthCalculator.calculateFromAge(0, mockClock)
        assertEquals("2025-01-01", dob2)

        val dob3 = DateOfBirthCalculator.calculateFromAge(1, mockClock)
        assertEquals("2024-01-01", dob3)

        val dob4 = DateOfBirthCalculator.calculateFromAge(100, mockClock)
        assertEquals("1925-01-01", dob4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an error for negative ages`() {
        DateOfBirthCalculator.calculateFromAge(-1, mockClock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an error for negative ages -5`() {
        DateOfBirthCalculator.calculateFromAge(-5, mockClock)
    }

    // Tests for calculateFromAgeInMonths
    @Test
    fun `should return the correct birth date for a given age in months`() {
        // June 1, 2025
        val dob1 = DateOfBirthCalculator.calculateFromAgeInMonths(0, mockClock)
        assertEquals("2025-06-01", dob1)

        // June 1, 2024
        val dob2 = DateOfBirthCalculator.calculateFromAgeInMonths(12, mockClock)
        assertEquals("2024-06-01", dob2)

        // May 1, 2025
        val dob3 = DateOfBirthCalculator.calculateFromAgeInMonths(1, mockClock)
        assertEquals("2025-05-01", dob3)
    }

    @Test
    fun `should handle ages greater than 12 months correctly`() {
        // May 1, 2024 (13 months ago)
        val dob1 = DateOfBirthCalculator.calculateFromAgeInMonths(13, mockClock)
        assertEquals("2024-05-01", dob1)

        // Apr 1, 2024 (14 months ago)
        val dob2 = DateOfBirthCalculator.calculateFromAgeInMonths(14, mockClock)
        assertEquals("2024-04-01", dob2)

        // June 1, 2023 (24 months ago)
        val dob3 = DateOfBirthCalculator.calculateFromAgeInMonths(24, mockClock)
        assertEquals("2023-06-01", dob3)

        // May 1, 2023 (25 months ago)
        val dob4 = DateOfBirthCalculator.calculateFromAgeInMonths(25, mockClock)
        assertEquals("2023-05-01", dob4)

        // June 1, 2022 (36 months ago)
        val dob5 = DateOfBirthCalculator.calculateFromAgeInMonths(36, mockClock)
        assertEquals("2022-06-01", dob5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an error for negative ages in months`() {
        DateOfBirthCalculator.calculateFromAgeInMonths(-1, mockClock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an error for negative ages in months -5`() {
        DateOfBirthCalculator.calculateFromAgeInMonths(-5, mockClock)
    }
}

