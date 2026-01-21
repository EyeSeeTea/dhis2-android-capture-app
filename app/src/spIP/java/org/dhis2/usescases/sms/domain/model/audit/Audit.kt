package org.dhis2.usescases.sms.domain.model.audit

import java.util.Date

data class Audit(
    val patientUid: String,
    val date: Date,
    val message: String,
    val translatedMessage: String?
)