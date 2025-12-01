package org.dhis2.form.beneficiaryHub

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.ui.beneficiaryHub.AgeInMonthsProcessor
import org.dhis2.form.ui.beneficiaryHub.ageFieldUid
import org.dhis2.form.ui.beneficiaryHub.ageInMonthsFieldUid
import org.dhis2.form.ui.beneficiaryHub.dateOfBirthFieldUid
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class AgeInMonthsProcessorTest {
    private val repository: FormRepository = mock()
    private val handler: Handler = mock()
    private val mockClock: Clock = Clock.fixed(
        Instant.parse("2025-06-24T00:00:00Z"),
        ZoneId.systemDefault()
    )

    @Test
    fun `should return action without changes if field is not ageInMonths field`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = "otherFieldUid", value = "25")
        val result = ageInMonthsProcessor.process(fieldUiModel, false)

        assertEquals("25", result.getOrNull())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return action without changes if isDobKnown is not false`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageInMonthsFieldUid, value = "25")

        val result1 = ageInMonthsProcessor.process(fieldUiModel, true)
        assertEquals("25", result1.getOrNull())
        assertTrue(result1.isSuccess)

        val result2 = ageInMonthsProcessor.process(fieldUiModel, null)
        assertEquals("25", result2.getOrNull())
        assertTrue(result2.isSuccess)
    }

    @Test
    fun `should return error for negative age in months`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageInMonthsFieldUid, value = "-1")
        val result = ageInMonthsProcessor.process(fieldUiModel, false)

        assertTrue(result.isFailure)
        assertEquals("Age in months must be a valid positive integer number", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return error for non-numeric age in months`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageInMonthsFieldUid, value = "abc")
        val result = ageInMonthsProcessor.process(fieldUiModel, false)

        assertTrue(result.isFailure)
        assertEquals("Age in months must be a valid positive integer number", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return action without changes if value is blank`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageInMonthsFieldUid, value = "")
        val result = ageInMonthsProcessor.process(fieldUiModel, false)

        assertEquals("", result.getOrNull())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should calculate DOB correctly from age in months`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageInMonthsFieldUid, value = "24")
        // Clock is fixed to 2025-06-24, so 24 months ago = 2023-06-01

        val result = ageInMonthsProcessor.process(fieldUiModel, false)

        assertEquals("24", result.getOrNull())
        assertTrue(result.isSuccess)
        verify(repository).save(dateOfBirthFieldUid, "2023-06-01", null)
        verify(repository).updateValueOnList(dateOfBirthFieldUid, "2023-06-01", ValueType.DATE)
    }

    @Test
    fun `should update age in years correctly from age in months`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageInMonthsFieldUid, value = "24")
        // Clock is fixed to 2025-06-24, so 24 months ago = 2023-06-01
        // Age in years from 2023-06-01 to 2025-06-24 = 2 years

        val result = ageInMonthsProcessor.process(fieldUiModel, false)

        assertEquals("24", result.getOrNull())
        assertTrue(result.isSuccess)
        verify(repository).save(dateOfBirthFieldUid, "2023-06-01", null)
        verify(repository).updateValueOnList(dateOfBirthFieldUid, "2023-06-01", ValueType.DATE)
        verify(repository).save(ageFieldUid, "2", null)
        verify(repository).updateValueOnList(ageFieldUid, "2", ValueType.INTEGER)
    }

    @Test
    fun `should calculate DOB correctly for age in months less than 12`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageInMonthsFieldUid, value = "6")
        // Clock is fixed to 2025-06-24, so 6 months ago = 2024-12-01

        val result = ageInMonthsProcessor.process(fieldUiModel, false)

        assertEquals("6", result.getOrNull())
        assertTrue(result.isSuccess)
        verify(repository).save(dateOfBirthFieldUid, "2024-12-01", null)
        verify(repository).updateValueOnList(dateOfBirthFieldUid, "2024-12-01", ValueType.DATE)
    }

    @Test
    fun `should calculate DOB correctly for age in months crossing year boundary`() {
        val ageInMonthsProcessor = givenAgeInMonthsProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageInMonthsFieldUid, value = "18")
        // Clock is fixed to 2025-06-24, so 18 months ago = 2023-12-01

        val result = ageInMonthsProcessor.process(fieldUiModel, false)

        assertEquals("18", result.getOrNull())
        assertTrue(result.isSuccess)
        verify(repository).save(dateOfBirthFieldUid, "2023-12-01", null)
        verify(repository).updateValueOnList(dateOfBirthFieldUid, "2023-12-01", ValueType.DATE)
    }

    private fun givenAgeInMonthsProcessor(): AgeInMonthsProcessor {
        doAnswer { invocation ->
            val runnable = invocation.getArgument<Runnable>(0)
            runnable.run()
            null
        }.whenever(handler).post(any())

        return AgeInMonthsProcessor(repository, handler, mockClock)
    }

    private fun givenAFieldUiModel(uid: String, value: String?): FieldUiModel {
        return FieldUiModelImpl(
            uid = uid,
            value = value,
            label = "Test Field",
            valueType = ValueType.INTEGER,
            programStageSection = null,
            uiEventFactory = null,
            optionSetConfiguration = null,
            autocompleteList = null,
        )
    }
}

