package org.dhis2.usescases.sms.data.model

import kotlinx.serialization.Serializable

//TODO: Technical debt
// fix bug to release in urgent mode
// Create D2ConstantDTO with serializable and
// map from D2ConstantDTO to D2Constant in repository
@Serializable
data class D2Constant(
  val id: String,
  val name: String,
  val description: String
)
