package org.dhis2.form.beneficiaryHub

import org.dhis2.form.ui.beneficiaryHub.DateOfBirthCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class DateOfBirthCalculatorTest {
    // Using MOCK_DATE_NOW = "2025-06-24T00:00:00Z" as in plugin web
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
}

