package org.dhis2.usescases.sms.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OutboundResponse(
  val httpStatus: String,
  val httpStatusCode: Int,
  val status: String,
  val message: String
)

@Serializable
data class OutboundRequest(
  val message: String,
  val recipients: List<String>
)