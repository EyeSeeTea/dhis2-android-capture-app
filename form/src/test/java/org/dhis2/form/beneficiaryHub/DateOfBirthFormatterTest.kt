package org.dhis2.form.beneficiaryHub

import org.dhis2.form.ui.beneficiaryHub.DateOfBirthFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateOfBirthFormatterTest {

    @Test
    fun `formatDateYYYYMMDD should return null for an invalid date format`() {
        assertNull(DateOfBirthFormatter.formatAndValidate("invalid-date", DateOfBirthFormatter.DateFormat.YYYYMMDD))
        assertNull(DateOfBirthFormatter.formatAndValidate("", DateOfBirthFormatter.DateFormat.YYYYMMDD))
        assertNull(DateOfBirthFormatter.formatAndValidate("2023-01-1", DateOfBirthFormatter.DateFormat.YYYYMMDD))
    }

    @Test
    fun `formatDateYYYYMMDD should return the same date for a valid YYYY-MM-DD format`() {
        assertEquals("2025-05-12", DateOfBirthFormatter.formatAndValidate("2025-05-12", DateOfBirthFormatter.DateFormat.YYYYMMDD))
        assertEquals("2000-01-01", DateOfBirthFormatter.formatAndValidate("2000-01-01", DateOfBirthFormatter.DateFormat.YYYYMMDD))
    }

    @Test
    fun `formatDateYYYYMMDD should convert and return a valid date in YYYYMMDD format`() {
        assertEquals("2025-05-12", DateOfBirthFormatter.formatAndValidate("20250512", DateOfBirthFormatter.DateFormat.YYYYMMDD))
    }

    @Test
    fun `formatDateYYYYMMDD should return null for an invalid date in YYYYMMDD format`() {
        assertNull(DateOfBirthFormatter.formatAndValidate("20251301", DateOfBirthFormatter.DateFormat.YYYYMMDD))
        assertNull(DateOfBirthFormatter.formatAndValidate("20250229", DateOfBirthFormatter.DateFormat.YYYYMMDD))
        assertNull(DateOfBirthFormatter.formatAndValidate("20250431", DateOfBirthFormatter.DateFormat.YYYYMMDD))
    }

    @Test
    fun `formatDateYYYYMMDD should return null for an invalid date in YYYY-MM-DD format`() {
        assertNull(DateOfBirthFormatter.formatAndValidate("2025-13-01", DateOfBirthFormatter.DateFormat.YYYYMMDD))
        assertNull(DateOfBirthFormatter.formatAndValidate("2025-02-29", DateOfBirthFormatter.DateFormat.YYYYMMDD))
        assertNull(DateOfBirthFormatter.formatAndValidate("2025-04-31", DateOfBirthFormatter.DateFormat.YYYYMMDD))
    }

    @Test
    fun `formatDateMMDDYYYY should format MMDDYYYY to YYYY-MM-DD`() {
        assertEquals("2025-07-04", DateOfBirthFormatter.formatAndValidate("07042025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
    }

    @Test
    fun `formatDateMMDDYYYY should format MM-DD-YYYY to YYYY-MM-DD`() {
        assertEquals("2025-07-04", DateOfBirthFormatter.formatAndValidate("07-04-2025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
    }

    @Test
    fun `formatDateMMDDYYYY should return null for invalid MMDDYYYY format`() {
        assertNull(DateOfBirthFormatter.formatAndValidate("0704202", DateOfBirthFormatter.DateFormat.MMDDYYYY))
        assertNull(DateOfBirthFormatter.formatAndValidate("02292025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
        assertNull(DateOfBirthFormatter.formatAndValidate("04312025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
    }

    @Test
    fun `formatDateMMDDYYYY should return null for invalid MM-DD-YYYY format`() {
        assertNull(DateOfBirthFormatter.formatAndValidate("07-04-25", DateOfBirthFormatter.DateFormat.MMDDYYYY))
    }

    @Test
    fun `formatDateMMDDYYYY should return null for invalid date`() {
        assertNull(DateOfBirthFormatter.formatAndValidate("13-32-2025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
        assertNull(DateOfBirthFormatter.formatAndValidate("02-29-2025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
        assertNull(DateOfBirthFormatter.formatAndValidate("04-31-2025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
        assertNull(DateOfBirthFormatter.formatAndValidate("13322025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
        assertNull(DateOfBirthFormatter.formatAndValidate("00012025", DateOfBirthFormatter.DateFormat.MMDDYYYY))
    }

    @Test
    fun `formatDateMMDDYYYY should return the same YYYY-MM-DD if already valid`() {
        assertEquals("2025-07-04", DateOfBirthFormatter.formatAndValidate("2025-07-04", DateOfBirthFormatter.DateFormat.MMDDYYYY))
    }

    @Test
    fun `formatDateMMDDYYYY should return null for invalid YYYY-MM-DD format`() {
        assertNull(DateOfBirthFormatter.formatAndValidate("2025-13-04", DateOfBirthFormatter.DateFormat.MMDDYYYY))
        assertNull(DateOfBirthFormatter.formatAndValidate("2025-02-29", DateOfBirthFormatter.DateFormat.MMDDYYYY))
        assertNull(DateOfBirthFormatter.formatAndValidate("2025-04-31", DateOfBirthFormatter.DateFormat.MMDDYYYY))
    }

    @Test
    fun `isBeforeMinDate should return true for dates before 1900-01-01`() {
        org.junit.Assert.assertTrue(DateOfBirthFormatter.isBeforeMinDate("1899-12-31"))
        org.junit.Assert.assertTrue(DateOfBirthFormatter.isBeforeMinDate("1800-01-01"))
        org.junit.Assert.assertTrue(DateOfBirthFormatter.isBeforeMinDate("1899-01-01"))
    }

    @Test
    fun `isBeforeMinDate should return false for dates equal to or after 1900-01-01`() {
        org.junit.Assert.assertFalse(DateOfBirthFormatter.isBeforeMinDate("1900-01-01"))
        org.junit.Assert.assertFalse(DateOfBirthFormatter.isBeforeMinDate("1900-01-02"))
        org.junit.Assert.assertFalse(DateOfBirthFormatter.isBeforeMinDate("2000-01-01"))
        org.junit.Assert.assertFalse(DateOfBirthFormatter.isBeforeMinDate("2025-01-01"))
    }

    @Test
    fun `isBeforeMinDate should return false for invalid dates`() {
        org.junit.Assert.assertFalse(DateOfBirthFormatter.isBeforeMinDate("invalid-date"))
        org.junit.Assert.assertFalse(DateOfBirthFormatter.isBeforeMinDate(""))
        org.junit.Assert.assertFalse(DateOfBirthFormatter.isBeforeMinDate("1899-13-01"))
    }
}

