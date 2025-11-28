package org.dhis2.form.beneficiaryHub

import org.dhis2.form.ui.beneficiaryHub.AgeCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class AgeCalculatorTest {
    private val MOCK_DATE_NOW = "2025-05-12T00:00:00Z"
    private val mockClock: Clock = Clock.fixed(
        Instant.parse(MOCK_DATE_NOW),
        ZoneId.systemDefault()
    )

    @Test
    fun `should return the correct age for a past date`() {
        // These tests match the plugin exactly
        val age1 = AgeCalculator.calculateAgeInYears("2000-05-12", mockClock)
        assertEquals(25, age1)

        val age2 = AgeCalculator.calculateAgeInYears("2024-05-12", mockClock)
        assertEquals(1, age2)
    }

    @Test
    fun `should return the correct age for a date later in the year`() {
        // Not yet 25 on 2025-05-12
        val dateString = "2000-12-31"
        val age = AgeCalculator.calculateAgeInYears(dateString, mockClock)
        assertEquals(24, age)

        val age2 = AgeCalculator.calculateAgeInYears("2024-05-13", mockClock)
        assertEquals(0, age2)
    }

    @Test
    fun `should return the correct age for a date earlier in the year`() {
        // Already 25 on 2025-05-12
        val dateString = "2000-01-01"
        val age = AgeCalculator.calculateAgeInYears(dateString, mockClock)
        assertEquals(25, age)

        val age2 = AgeCalculator.calculateAgeInYears("1989-04-13", mockClock)
        assertEquals(36, age2)
    }

    @Test
    fun `should handle leap years correctly`() {
        // Leap year birthday - 2025-05-12
        val dateString = "2004-02-29"
        val age = AgeCalculator.calculateAgeInYears(dateString, mockClock)
        assertEquals(21, age) // 2025-05-12
    }
}

