package org.dhis2.usescases.sms.domain.usecase

import org.dhis2.usescases.sms.data.model.MessageTemplate
import org.dhis2.usescases.sms.domain.model.audit.Audit
import org.dhis2.usescases.sms.domain.model.patient.Patient
import org.dhis2.usescases.sms.domain.model.sms.Message
import org.dhis2.usescases.sms.domain.model.sms.SmsResult
import org.dhis2.usescases.sms.domain.repository.audit.AuditRepository
import org.dhis2.usescases.sms.domain.repository.message.MessageTemplateRepository
import org.dhis2.usescases.sms.domain.repository.patient.PatientRepository
import org.dhis2.usescases.sms.domain.repository.preferred.PreferredLanguageRepository
import org.dhis2.usescases.sms.domain.repository.sms.SmsRepository
import java.util.Date


class SendSmsUseCase(
    private val patientRepository: PatientRepository,
    private val smsTemplateRepository: MessageTemplateRepository,
    private val preferredLanguageRepository: PreferredLanguageRepository,
    private val smsRepository: SmsRepository,
    private val auditRepository: AuditRepository
) {
    suspend fun invoke(uid: String): SmsResult {
        val patient = patientRepository.getByUid(uid)

        val message = createMessage(patient)
            ?: return SmsResult.TemplateFailure

        try {
            smsRepository.send(message)

            val audit = createAudit(patient, message)

            auditRepository.save(audit)

            return if (patient.preferredLanguage != "en" && message.language == "en") {
                val language = preferredLanguageRepository.getByCode(patient.preferredLanguage)

                SmsResult.SuccessUsingEn(language.name)
            } else {
                SmsResult.Success
            }
        } catch (e: Exception) {
            return SmsResult.SendFailure
        }

    }

    private suspend fun createMessage(patient: Patient, forcedLanguage: String? = null): Message? {
        val messageTemplate = getMessageTemplate(forcedLanguage ?: patient.preferredLanguage)
            ?: return null

        return Message(
            text = messageTemplate.text
                .replace("{{fullName}}", patient.name)
                .replace("{{patientNumber}}", patient.number),
            recipients = listOf(cleanupPhoneNumber(patient.phone)),
            language = messageTemplate.language
        )
    }

    private suspend fun getMessageTemplate(language: String): MessageTemplate? {
        val messageTemplate = smsTemplateRepository.getByLanguage(language)

        return if (messageTemplate.isSome()) {
            messageTemplate.getOrThrow()
        } else {
            val defaultMessageTemplate = smsTemplateRepository.getByLanguage("en")

            if (defaultMessageTemplate.isSome()) {
                defaultMessageTemplate.getOrThrow()
            } else {
                return null
            }
        }
    }

    private suspend fun createAudit(patient: Patient, message: Message): Audit {
        val translatedMessage =
            if (message.language != "en")
                createMessage(patient, "en")?.text
            else null

        return Audit( patient.uid, Date(), message.text, translatedMessage)
    }

    private fun cleanupPhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("\\D"), "")
    }
}