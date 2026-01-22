package org.dhis2.form.beneficiaryHub

import org.dhis2.form.ui.beneficiaryHub.calculators.AgeCalculator
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

    // Tests for calculateAgeInMonths
    @Test
    fun `should return the correct age in months for exact year anniversaries`() {
        val age1 = AgeCalculator.calculateAgeInMonths("2024-05-12", mockClock)
        assertEquals(12, age1)

        val age2 = AgeCalculator.calculateAgeInMonths("2023-05-12", mockClock)
        assertEquals(24, age2)

        val age3 = AgeCalculator.calculateAgeInMonths("2022-05-12", mockClock)
        assertEquals(36, age3)
    }

    @Test
    fun `should return 0 for dates in the same month or in the future`() {
        val age1 = AgeCalculator.calculateAgeInMonths("2025-05-01", mockClock)
        assertEquals(0, age1)

        val age2 = AgeCalculator.calculateAgeInMonths("2025-05-11", mockClock)
        assertEquals(0, age2)

        val age3 = AgeCalculator.calculateAgeInMonths("2025-05-13", mockClock)
        assertEquals(0, age3)

        val age4 = AgeCalculator.calculateAgeInMonths("2027-01-01", mockClock)
        assertEquals(0, age4)

        // 11 months ago + 1 day
        val age5 = AgeCalculator.calculateAgeInMonths("2024-05-13", mockClock)
        assertEquals(11, age5)
    }

    @Test
    fun `should return the correct age in months for different months`() {
        val age1 = AgeCalculator.calculateAgeInMonths("2025-04-12", mockClock)
        assertEquals(1, age1)

        val age2 = AgeCalculator.calculateAgeInMonths("2025-03-12", mockClock)
        assertEquals(2, age2)

        val age3 = AgeCalculator.calculateAgeInMonths("2025-01-12", mockClock)
        assertEquals(4, age3)

        val age4 = AgeCalculator.calculateAgeInMonths("2024-12-12", mockClock)
        assertEquals(5, age4)

        // ~1 month ago
        val age5 = AgeCalculator.calculateAgeInMonths("2025-04-01", mockClock)
        assertEquals(1, age5)

        // ~2 months ago
        val age6 = AgeCalculator.calculateAgeInMonths("2025-03-01", mockClock)
        assertEquals(2, age6)
    }

    @Test
    fun `should handle cross-year calculations correctly`() {
        // 1 year + 1 day
        val age1 = AgeCalculator.calculateAgeInMonths("2024-05-11", mockClock)
        assertEquals(12, age1)

        // 1 year + 4 months
        val age2 = AgeCalculator.calculateAgeInMonths("2024-01-12", mockClock)
        assertEquals(16, age2)

        // 1 year + 5 months
        val age3 = AgeCalculator.calculateAgeInMonths("2023-12-12", mockClock)
        assertEquals(17, age3)

        // 25 years = 300 months
        val age4 = AgeCalculator.calculateAgeInMonths("2000-05-12", mockClock)
        assertEquals(25 * 12, age4)

        // ~35 years + 4 months
        val age5 = AgeCalculator.calculateAgeInMonths("1990-01-01", mockClock)
        assertEquals(35 * 12 + 4, age5)
    }

    @Test
    fun `should handle leap years correctly for age in months`() {
        // From Feb 29, 2024 to May 12, 2025 = 14 months
        val age = AgeCalculator.calculateAgeInMonths("2024-02-29", mockClock)
        assertEquals(14, age)
    }

    @Test
    fun `should handle day adjustments correctly for age in months`() {
        // Same month, past day -> 1 month
        val age1 = AgeCalculator.calculateAgeInMonths("2025-04-10", mockClock)
        assertEquals(1, age1)

        // Future day in birth month -> 10 months
        val age2 = AgeCalculator.calculateAgeInMonths("2024-06-15", mockClock)
        assertEquals(10, age2)

        // Past day in birth month -> 11 months
        val age3 = AgeCalculator.calculateAgeInMonths("2024-06-10", mockClock)
        assertEquals(11, age3)
    }

    // Tests for hasSameAge
    @Test
    fun `should return true if both dates have the same age`() {
        // Both dates result in age 25 on 2025-05-12
        val result1 = AgeCalculator.hasSameAge("2000-01-01", "2000-05-12", mockClock)
        assertEquals(true, result1)

        // Both dates result in age 1 on 2025-05-12
        val result2 = AgeCalculator.hasSameAge("2024-05-12", "2024-01-01", mockClock)
        assertEquals(true, result2)

        // Same date should have same age
        val result3 = AgeCalculator.hasSameAge("2000-05-12", "2000-05-12", mockClock)
        assertEquals(true, result3)
    }

    @Test
    fun `should return false if dates have different ages`() {
        // 25 vs 24 years old
        val result1 = AgeCalculator.hasSameAge("2000-01-01", "2000-12-31", mockClock)
        assertEquals(false, result1)

        // 25 vs 1 year old
        val result2 = AgeCalculator.hasSameAge("2000-05-12", "2024-05-12", mockClock)
        assertEquals(false, result2)

        // 1 vs 0 years old
        val result3 = AgeCalculator.hasSameAge("2024-05-12", "2024-05-13", mockClock)
        assertEquals(false, result3)
    }

    @Test
    fun `should return false for invalid dates`() {
        // First date invalid
        val result1 = AgeCalculator.hasSameAge("invalid-date", "2000-05-12", mockClock)
        assertEquals(false, result1)

        // Second date invalid
        val result2 = AgeCalculator.hasSameAge("2000-05-12", "invalid-date", mockClock)
        assertEquals(false, result2)

        // Both dates invalid
        val result3 = AgeCalculator.hasSameAge("invalid-date1", "invalid-date2", mockClock)
        assertEquals(false, result3)

        // Empty strings
        val result4 = AgeCalculator.hasSameAge("", "2000-05-12", mockClock)
        assertEquals(false, result4)

        val result5 = AgeCalculator.hasSameAge("2000-05-12", "", mockClock)
        assertEquals(false, result5)
    }
}

