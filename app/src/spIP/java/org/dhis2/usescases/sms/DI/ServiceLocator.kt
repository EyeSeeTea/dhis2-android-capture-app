package org.dhis2.usescases.sms.DI

import org.dhis2.usescases.sms.data.api.ConstantApi
import org.dhis2.usescases.sms.data.api.ConstantApiImpl
import org.dhis2.usescases.sms.data.api.OutboundApi
import org.dhis2.usescases.sms.data.api.OutboundApiImpl
import org.dhis2.usescases.sms.data.repository.audit.AuditD2Repository
import org.dhis2.usescases.sms.data.repository.message.MessageTemplateD2Repository
import org.dhis2.usescases.sms.data.repository.patient.PatientD2Repository
import org.dhis2.usescases.sms.data.repository.preferred.PreferredLanguageD2Repository
import org.dhis2.usescases.sms.data.repository.sms.SmsApiRepository
import org.dhis2.usescases.sms.domain.usecase.SendSmsUseCase
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.arch.api.HttpServiceClient

object SPIPServiceLocator {

    fun provideSendSmsUseCase(): SendSmsUseCase {
        val outboundApi: OutboundApi = OutboundApiImpl(provideHttpClient())
        val constantApi: ConstantApi = ConstantApiImpl(provideHttpClient())

        val patientRepository = PatientD2Repository(provideD2())
        val auditRepository = AuditD2Repository(provideD2())
        val messageTemplate = MessageTemplateD2Repository(constantApi, provideD2())
        val preferredLanguageRepository = PreferredLanguageD2Repository(provideD2())
        val smsRepository = SmsApiRepository(outboundApi)

        return SendSmsUseCase(
            patientRepository,
            messageTemplate,
            preferredLanguageRepository,
            smsRepository,
            auditRepository
        )
    }

    private fun provideD2(): D2 {
        return D2Manager.getD2()
    }

    private fun provideHttpClient(): HttpServiceClient {
        return D2Manager.getD2().httpServiceClient()
    }

}