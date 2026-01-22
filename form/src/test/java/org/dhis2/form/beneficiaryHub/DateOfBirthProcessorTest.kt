package org.dhis2.form.beneficiaryHub

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.ui.beneficiaryHub.ageFieldUid
import org.dhis2.form.ui.beneficiaryHub.ageInMonthsFieldUid
import org.dhis2.form.ui.beneficiaryHub.dateOfBirthFieldUid
import org.dhis2.form.ui.beneficiaryHub.processors.DateOfBirthProcessor
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class DateOfBirthProcessorTest {
    private val repository: FormRepository = mock()
    private val handler: Handler = mock()
    private val mockClock: Clock = Clock.fixed(
        Instant.parse("2025-06-24T00:00:00Z"),
        ZoneId.systemDefault()
    )

    @Test
    fun `should return action without changes if isDobKnown is not true`() {
        val dateOfBirthProcessor = givenDateOfBirthProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = dateOfBirthFieldUid, value = "2020-06-24")

        val result1 = dateOfBirthProcessor.process(fieldUiModel, false)
        assertEquals("2020-06-24", result1.getOrNull())
        assertTrue(result1.isSuccess)

        val result2 = dateOfBirthProcessor.process(fieldUiModel, null)
        assertEquals("2020-06-24", result2.getOrNull())
        assertTrue(result2.isSuccess)
    }

    @Test
    fun `should calculate and update ageInMonths if age is less than or equal to 5`() {
        val dateOfBirthProcessor = givenDateOfBirthProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = dateOfBirthFieldUid, value = "2020-06-24")
        // Age from 2020-06-24 to 2025-06-24 = 5 years
        // Age in months = 5 * 12 = 60 months

        val result = dateOfBirthProcessor.process(fieldUiModel, true)

        assertTrue(result.isSuccess)
        assertEquals("2020-06-24", result.getOrNull())
        verify(repository).save(ageFieldUid, "5", null)
        verify(repository).updateValueOnList(ageFieldUid, "5", ValueType.INTEGER)
        verify(repository).save(ageInMonthsFieldUid, "60", null)
        verify(repository).updateValueOnList(ageInMonthsFieldUid, "60", ValueType.INTEGER)
    }

    @Test
    fun `should set ageInMonths to empty string if age is greater than 5`() {
        val dateOfBirthProcessor = givenDateOfBirthProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = dateOfBirthFieldUid, value = "2019-06-24")
        // Age from 2019-06-24 to 2025-06-24 = 6 years

        val result = dateOfBirthProcessor.process(fieldUiModel, true)

        assertTrue(result.isSuccess)
        assertEquals("2019-06-24", result.getOrNull())
        verify(repository).save(ageFieldUid, "6", null)
        verify(repository).updateValueOnList(ageFieldUid, "6", ValueType.INTEGER)
        // Should set ageInMonths to empty string when age > 5 (matching plugin web logic)
        verify(repository).save(ageInMonthsFieldUid, "", null)
        verify(repository).updateValueOnList(ageInMonthsFieldUid, "", ValueType.INTEGER)
    }

    @Test
    fun `should calculate ageInMonths correctly for age exactly 5`() {
        val dateOfBirthProcessor = givenDateOfBirthProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = dateOfBirthFieldUid, value = "2020-01-01")
        // Age from 2020-01-01 to 2025-06-24 = 5 years, 5 months, 23 days
        // Age in years = 5, so should calculate ageInMonths

        val result = dateOfBirthProcessor.process(fieldUiModel, true)

        assertTrue(result.isSuccess)
        assertEquals("2020-01-01", result.getOrNull())
        verify(repository).save(ageFieldUid, "5", null)
        verify(repository).updateValueOnList(ageFieldUid, "5", ValueType.INTEGER)
        verify(repository).save(ageInMonthsFieldUid, "65", null)
        verify(repository).updateValueOnList(ageInMonthsFieldUid, "65", ValueType.INTEGER)
    }

    @Test
    fun `should calculate ageInMonths correctly for age less than 1 year`() {
        val dateOfBirthProcessor = givenDateOfBirthProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = dateOfBirthFieldUid, value = "2024-12-01")
        // Age from 2024-12-01 to 2025-06-24 = 0 years, 6 months, 23 days
        // Age in years = 0, so should calculate ageInMonths

        val result = dateOfBirthProcessor.process(fieldUiModel, true)

        assertTrue(result.isSuccess)
        assertEquals("2024-12-01", result.getOrNull())
        verify(repository).save(ageFieldUid, "0", null)
        verify(repository).updateValueOnList(ageFieldUid, "0", ValueType.INTEGER)
        verify(repository).save(ageInMonthsFieldUid, "6", null)
        verify(repository).updateValueOnList(ageInMonthsFieldUid, "6", ValueType.INTEGER)
    }

    private fun givenDateOfBirthProcessor(): DateOfBirthProcessor {
        doAnswer { invocation ->
            val runnable = invocation.getArgument<Runnable>(0)
            runnable.run()
            null
        }.whenever(handler).post(any())

        return DateOfBirthProcessor(
            repository = repository,
            handler = handler,
            clock = mockClock,
        )
    }

    private fun givenAFieldUiModel(uid: String, value: String?): FieldUiModel {
        return FieldUiModelImpl(
            uid = uid,
            value = value,
            label = "Test Field",
            valueType = ValueType.DATE,
            programStageSection = null,
            uiEventFactory = null,
            optionSetConfiguration = null,
            autocompleteList = null,
        )
    }
}

