package org.dhis2.form.beneficiaryHub

import android.os.Handler
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.ui.beneficiaryHub.processors.AgeProcessor
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

class AgeProcessorTest {
    private val repository: FormRepository = mock()
    private val handler: Handler = mock()
    private val mockClock: Clock = Clock.fixed(
        Instant.parse("2025-06-24T00:00:00Z"),
        ZoneId.systemDefault()
    )

    @Test
    fun `should return action without changes if field is not age field`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = "otherFieldUid", value = "25")
        val result = ageProcessor.process(fieldUiModel, false)

        assertEquals("25", result.getOrNull())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return action without changes if isDobKnown is not false`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "25")

        val result1 = ageProcessor.process(fieldUiModel, true)
        assertEquals("25", result1.getOrNull())
        assertTrue(result1.isSuccess)

        val result2 = ageProcessor.process(fieldUiModel, null)
        assertEquals("25", result2.getOrNull())
        assertTrue(result2.isSuccess)
    }

    @Test
    fun `should return error for negative age`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "-1")
        val result = ageProcessor.process(fieldUiModel, false)

        assertTrue(result.isFailure)
        assertEquals("Age must be a valid positive integer number", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return error for age greater than 125`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "126")
        val result = ageProcessor.process(fieldUiModel, false)

        assertTrue(result.isFailure)
        assertEquals("Age cannot be greater than 125", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return error for non-numeric age`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "abc")
        val result = ageProcessor.process(fieldUiModel, false)

        assertTrue(result.isFailure)
        assertEquals("Age must be a valid positive integer number", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should calculate DOB correctly from age`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "22")
        whenever(repository.getField(dateOfBirthFieldUid)) doReturn null

        val result = ageProcessor.process(fieldUiModel, false)

        assertEquals("22", result.getOrNull())
        assertTrue(result.isSuccess)
        verify(repository).save(dateOfBirthFieldUid, "2003-01-01", null)
        verify(repository).updateValueOnList(dateOfBirthFieldUid, "2003-01-01", ValueType.DATE)
    }

    @Test
    fun `should update ageInMonths if age is less than or equal to 5`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "5")
        whenever(repository.getField(dateOfBirthFieldUid)) doReturn null

        val result = ageProcessor.process(fieldUiModel, false)

        assertEquals("5", result.getOrNull())
        assertTrue(result.isSuccess)

        verify(repository).save(dateOfBirthFieldUid, "2020-01-01", null)
        verify(repository).updateValueOnList(dateOfBirthFieldUid, "2020-01-01", ValueType.DATE)

        verify(repository).save(ageInMonthsFieldUid, "65", null)
        verify(repository).updateValueOnList(ageInMonthsFieldUid, "65", ValueType.INTEGER)
    }

    @Test
    fun `should set ageInMonths to empty string if age is greater than 5`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "6")
        whenever(repository.getField(dateOfBirthFieldUid)) doReturn null

        val result = ageProcessor.process(fieldUiModel, false)

        assertEquals("6", result.getOrNull())
        assertTrue(result.isSuccess)

        verify(repository).save(dateOfBirthFieldUid, "2019-01-01", null)
        verify(repository).updateValueOnList(dateOfBirthFieldUid, "2019-01-01", ValueType.DATE)

        verify(repository).save(ageInMonthsFieldUid, "", null)
        verify(repository).updateValueOnList(ageInMonthsFieldUid, "", ValueType.INTEGER)
    }

    @Test
    fun `should use existing DOB if it has the same age`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "22")
        val existingDobField = givenAFieldUiModel(uid = dateOfBirthFieldUid, value = "2003-01-01")
        whenever(repository.getField(dateOfBirthFieldUid)) doReturn existingDobField

        val result = ageProcessor.process(fieldUiModel, false)

        assertEquals("22", result.getOrNull())
        assertTrue(result.isSuccess)

        verify(repository).save(dateOfBirthFieldUid, "2003-01-01", null)
        verify(repository).updateValueOnList(dateOfBirthFieldUid, "2003-01-01", ValueType.DATE)
    }

    @Test
    fun `should return action without changes if value is blank`() {
        val ageProcessor = givenAgeProcessor()
        val fieldUiModel = givenAFieldUiModel(uid = ageFieldUid, value = "")
        val result = ageProcessor.process(fieldUiModel, false)

        assertEquals("", result.getOrNull())
        assertTrue(result.isSuccess)
    }

    private fun givenAgeProcessor() : AgeProcessor {
        doAnswer { invocation ->
            val runnable = invocation.getArgument<Runnable>(0)
            runnable.run()
            null
        }.whenever(handler).post(any())

        return AgeProcessor(
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
            valueType = ValueType.INTEGER,
            programStageSection = null,
            uiEventFactory = null,
            optionSetConfiguration = null,
            autocompleteList = null,
        )
    }
}

