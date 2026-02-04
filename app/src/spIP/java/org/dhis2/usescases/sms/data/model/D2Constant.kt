package org.dhis2.usescases.sms.data.model

import kotlinx.serialization.Serializable

@Serializable
data class D2Constant(
  val id: String,
  val name: String,
  val description: String
)
