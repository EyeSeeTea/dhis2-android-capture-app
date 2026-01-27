package org.dhis2.usescases.sms.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.sms.data.model.MessageTemplate
import org.dhis2.usescases.sms.domain.model.patient.Patient
import org.dhis2.usescases.sms.domain.model.preffered.PreferredLanguage
import org.dhis2.usescases.sms.domain.model.sms.Message
import org.dhis2.usescases.sms.domain.model.sms.SmsResult
import org.dhis2.usescases.sms.domain.model.types.Maybe
import org.dhis2.usescases.sms.domain.repository.audit.AuditRepository
import org.dhis2.usescases.sms.domain.repository.message.MessageTemplateRepository
import org.dhis2.usescases.sms.domain.repository.patient.PatientRepository
import org.dhis2.usescases.sms.domain.repository.preferred.PreferredLanguageRepository
import org.dhis2.usescases.sms.domain.repository.sms.SmsRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class SendSmsUseCaseTest {

    @Mock
    lateinit var patientRepository: PatientRepository

    @Mock
    lateinit var messageTemplateRepository: MessageTemplateRepository

    @Mock
    lateinit var smsRepository: SmsRepository

    @Mock
    lateinit var auditRepository: AuditRepository

    @Mock
    lateinit var preferredLanguageRepository: PreferredLanguageRepository

    private val defaultEnMessageTemplate = "Default message template"

    @Test
    fun `should send expected es message to expected number`() = runTest {
        val phone = "1234567890"

        val useCase = givenAPatientAndATemplate(
            phone = phone,
            number = "123-456",
            preferredLanguage = "es",
            messageTemplate = "Hola {{fullName}}, tu numero de paciente es {{patientNumber}}"
        )

        val result = useCase.invoke("PATIENT_UID")

        val expectedMessage = Message(
            text = "Hola John Doe, tu numero de paciente es 123-456",
            recipients = listOf(phone), language = "es"
        )

        verify(smsRepository).send(expectedMessage)
        assert(result is SmsResult.Success)
    }

    @Test
    fun `should use default en template if there is not template for specific language`() =
        runTest {
            val phone = "1234567890"

            val useCase = givenAPatientAndATemplate(
                phone = phone,
                preferredLanguage = "es",
                messageTemplate = null
            )

            val result = useCase.invoke("PATIENT_UID")

            val expectedMessage = Message(
                text = defaultEnMessageTemplate,
                recipients = listOf(phone),
                language = "en"
            )

            verify(smsRepository).send(expectedMessage)
            assert(result is SmsResult.SuccessUsingEn)
        }

    @Test
    fun `should return template failure if there are not any template`() = runTest {
        val useCase = givenAPatientAndATemplate(
            messageTemplate = null,
            createDefaultEnTemplate = false
        )

        val result = useCase.invoke("PATIENT_UID")

        assert(result is SmsResult.TemplateFailure)
    }

    @Test
    fun `should return send failure if send throw and error`() = runTest {
        val useCase = givenAPatientAndATemplate()

        givenAErrortoSendEmail()

        val result = useCase.invoke("PATIENT_UID")

        assert(result is SmsResult.SendFailure)
    }


    private suspend fun givenAPatientAndATemplate(
        phone: String = "1234567890",
        number: String = "123-456",
        preferredLanguage: String = "es",
        messageTemplate: String? = "Hola {{fullName}}, tu numero de paciente es {{patientNumber}}",
        createDefaultEnTemplate: Boolean = true
    ): SendSmsUseCase {

        whenever(preferredLanguageRepository.getByCode(any())).thenReturn(
            PreferredLanguage(
                "PATIENT_UID",
                "en",
                "English"
            )
        )

        whenever(
            patientRepository.getByUid(any())
        ).thenReturn(
            Patient(
                "PATIENT_UID",
                number,
                "John Doe",
                phone,
                preferredLanguage
            )
        )

        if (messageTemplate == null) {
            whenever(
                messageTemplateRepository.getByLanguage(preferredLanguage)
            ).thenReturn(Maybe.None)
        } else {
            whenever(
                messageTemplateRepository.getByLanguage(preferredLanguage)
            ).thenReturn(
                Maybe.Some(
                    MessageTemplate(
                        text = messageTemplate,
                        language = preferredLanguage
                    )
                )
            )
        }

        if (createDefaultEnTemplate) {
            whenever(
                messageTemplateRepository.getByLanguage("en")
            ).thenReturn(
                Maybe.Some(
                    MessageTemplate(
                        text = defaultEnMessageTemplate,
                        language = "en"
                    )
                )
            )
        } else {
            whenever(
                messageTemplateRepository.getByLanguage("en")
            ).thenReturn(Maybe.None)
        }

        return SendSmsUseCase(
            patientRepository = patientRepository,
            smsTemplateRepository = messageTemplateRepository,
            preferredLanguageRepository = preferredLanguageRepository,
            smsRepository = smsRepository,
            auditRepository = auditRepository
        )
    }

    private suspend fun givenAErrortoSendEmail() {
        whenever(smsRepository.send(any())).thenThrow(RuntimeException("Send error"))
    }

}