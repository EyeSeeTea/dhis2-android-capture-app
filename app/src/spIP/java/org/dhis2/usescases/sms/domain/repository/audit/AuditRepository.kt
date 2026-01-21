package org.dhis2.usescases.sms.domain.repository.audit

import org.dhis2.usescases.sms.domain.model.audit.Audit

interface AuditRepository {
  suspend fun save(audit: Audit)
}